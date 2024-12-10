/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.*;
import java.util.stream.Collectors;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.converter.PsseImporter.PerUnitContext;
import com.powsybl.psse.converter.NodeBreakerValidation.NodeBreakerControl;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.pf.PsseBus;
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import com.powsybl.psse.model.pf.PsseSubstation;
import com.powsybl.psse.model.pf.PsseSubstation.PsseSubstationNode;
import com.powsybl.psse.model.pf.PsseSubstation.PsseSubstationSwitchingDevice;
import com.powsybl.psse.model.pf.PsseSubstation.PsseSubstationEquipmentTerminal;

import static com.powsybl.psse.converter.AbstractConverter.PsseEquipmentType.PSSE_GENERATOR;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class VoltageLevelConverter extends AbstractConverter {

    VoltageLevelConverter(PsseBus psseBus, ContainersMapping containerMapping, PerUnitContext perUnitContext, Network network, NodeBreakerValidation nodeBreakerValidation, NodeBreakerImport nodeBreakerImport) {
        super(containerMapping, network);
        this.psseBus = Objects.requireNonNull(psseBus);
        this.perUnitContext = Objects.requireNonNull(perUnitContext);
        this.nodeBreakerValidation = Objects.requireNonNull(nodeBreakerValidation);
        this.nodeBreakerImport = Objects.requireNonNull(nodeBreakerImport);
    }

    VoltageLevel create(Substation substation) {
        String voltageLevelId = getContainersMapping().getVoltageLevelId(psseBus.getI());
        VoltageLevel voltageLevel = getNetwork().getVoltageLevel(voltageLevelId);

        if (voltageLevel == null) {
            double nominalV = getNominalV(psseBus, perUnitContext.ignoreBaseVoltage());
            boolean isNodeBreakerValid = nodeBreakerValidation.isNodeBreakerSubstationDataCoherent(getContainersMapping().getBusesSet(voltageLevelId));

            TopologyKind topologyKind = isNodeBreakerValid ? TopologyKind.NODE_BREAKER : TopologyKind.BUS_BREAKER;
            voltageLevel = substation.newVoltageLevel()
                .setId(voltageLevelId)
                .setNominalV(nominalV)
                .setTopologyKind(topologyKind)
                .add();

            if (isNodeBreakerValid) {
                addNodeBreakerConnectivity(voltageLevelId, voltageLevel, nodeBreakerValidation, nodeBreakerImport);
            }
        }

        // Add slack control data
        if (voltageLevel.getTopologyKind().equals(TopologyKind.NODE_BREAKER) && psseBus.getIde() == 3) {
            nodeBreakerImport.addSlackControl(psseBus.getI(), voltageLevelId, findSlackNode(nodeBreakerValidation, psseBus.getI()));
        }
        return voltageLevel;
    }

    static double getNominalV(PsseBus psseBus, boolean isIgnoreBaseVoltage) {
        return isIgnoreBaseVoltage || psseBus.getBaskv() == 0 ? 1 : psseBus.getBaskv();
    }

    private void addNodeBreakerConnectivity(String voltageLevelId, VoltageLevel voltageLevel, NodeBreakerValidation nodeBreakerValidation, NodeBreakerImport nodeBreakerImport) {
        Set<Integer> buses = getContainersMapping().getBusesSet(voltageLevelId);
        nodeBreakerImport.addTopologicalBuses(buses);
        Optional<PsseSubstation> psseSubstation = nodeBreakerValidation.getSubstationIfOnlyOneExists(buses);
        if (psseSubstation.isPresent()) {
            int lastNode = psseSubstation.get().getNodes().stream().map(PsseSubstationNode::getNi).max(Comparator.naturalOrder()).orElseThrow();
            for (int bus : buses) {
                lastNode = addNodeBreakerConnectivity(voltageLevelId, voltageLevel, psseSubstation.get(), bus, lastNode, nodeBreakerValidation, nodeBreakerImport);
            }
        }
    }

    private static int addNodeBreakerConnectivity(String voltageLevelId, VoltageLevel voltageLevel, PsseSubstation psseSubstation, int bus, int lastNodeInternalConnections, NodeBreakerValidation nodeBreakerValidation, NodeBreakerImport nodeBreakerImport) {
        Set<Integer> nodesSet = psseSubstation.getNodes().stream().filter(psseNode -> psseNode.getStatus() == 1 && psseNode.getI() == bus).map(PsseSubstationNode::getNi).collect(Collectors.toSet());

        List<PsseSubstationSwitchingDevice> switchingDeviceList = psseSubstation.getSwitchingDevices().stream()
                .filter(sd -> nodesSet.contains(sd.getNi()) && nodesSet.contains(sd.getNj())).toList();

        for (PsseSubstationSwitchingDevice switchingDevice : switchingDeviceList) {
            voltageLevel.getNodeBreakerView().newSwitch()
                    .setId(getSwitchId(voltageLevelId, switchingDevice))
                    .setName(switchingDevice.getName())
                    .setNode1(switchingDevice.getNi())
                    .setNode2(switchingDevice.getNj())
                    .setKind(findSwitchingKind(switchingDevice.getType()))
                    .setOpen(switchingDevice.getStatus() != 1)
                    .add();
        }

        // Define where equipment must be connected
        Set<Integer> nodesWithEquipment = new HashSet<>();

        List<PsseSubstationEquipmentTerminal> equipmentTerminalList = psseSubstation.getEquipmentTerminals().stream().filter(eqt -> nodesSet.contains(eqt.getNi())).toList();

        int lastNode = lastNodeInternalConnections;
        for (PsseSubstationEquipmentTerminal equipmentTerminal : equipmentTerminalList) {
            String equipmentId = getNodeBreakerEquipmentId(equipmentTerminal.getType(), equipmentTerminal.getI(), equipmentTerminal.getJ(), equipmentTerminal.getK(), equipmentTerminal.getId());
            String equipmentIdBus = getNodeBreakerEquipmentIdBus(equipmentId, bus);
            // IIDM only allows one piece of equipment by node
            if (nodesWithEquipment.contains(equipmentTerminal.getNi())) {
                lastNode++;
                voltageLevel.getNodeBreakerView().newInternalConnection().setNode1(equipmentTerminal.getNi()).setNode2(lastNode).add();
                nodeBreakerImport.addEquipment(equipmentIdBus, lastNode);
            } else {
                nodeBreakerImport.addEquipment(equipmentIdBus, equipmentTerminal.getNi());
                nodesWithEquipment.add(equipmentTerminal.getNi());
            }
        }

        // Nodes not isolated and without equipment are defined as busbar sections to improve the regulating terminal selection
        psseSubstation.getNodes().stream()
                .filter(psseNode -> nodesSet.contains(psseNode.getNi()) && !nodesWithEquipment.contains(psseNode.getNi()))
                .forEach(psseNode -> voltageLevel.getNodeBreakerView()
                        .newBusbarSection()
                        .setId(busbarSectionId(voltageLevel.getId(), psseNode.getNi()))
                        .setName(psseNode.getName())
                        .setNode(psseNode.getNi())
                        .add());

        // Define where controls must be connected. Psse nodes and iidm nodes are identical.
        List<NodeBreakerControl> controls = nodeBreakerValidation.getControls(bus);
        controls.forEach(control -> nodeBreakerImport.addControl(getNodeBreakerEquipmentIdBus(control.getEquipmentId(), bus), voltageLevelId, control.getNode()));

        // update voltages
        psseSubstation.getNodes().stream()
                .filter(psseNode -> nodesSet.contains(psseNode.getNi()))
                .forEach(psseNode -> {
                    Bus busView = findBusViewNode(voltageLevel, psseNode.getNi());
                    if (busView != null) {
                        busView.setV(psseNode.getVm() * voltageLevel.getNominalV());
                        busView.setAngle(psseNode.getVa());
                    }
                });

        return lastNode;
    }

    private static SwitchKind findSwitchingKind(int type) {
        return type == 2 ? SwitchKind.BREAKER : SwitchKind.DISCONNECTOR;
    }

    private static int findSlackNode(NodeBreakerValidation nodeBreakerValidation, int bus) {
        PsseSubstation psseSubstation = nodeBreakerValidation.getSubstationIfOnlyOneExists(bus).orElseThrow();
        return psseSubstation.getNodes().stream().filter(n -> n.getI() == bus)
                .map(PsseSubstationNode::getNi).min(Comparator.comparingInt((Integer node) -> connectedGenerators(psseSubstation, node))
                        .reversed().thenComparing(Comparator.naturalOrder())).orElseThrow();
    }

    private static int connectedGenerators(PsseSubstation psseSubstation, int node) {
        return (int) psseSubstation.getEquipmentTerminals().stream().filter(eqt -> eqt.getNi() == node && eqt.getType().equals(PSSE_GENERATOR.getTextCode())).count();
    }

    static ContextExport createContextExport(Network network, PssePowerFlowModel psseModel) {
        ContextExport contextExport = new ContextExport();
        mapVoltageLevelsAndPsseSubstation(network, psseModel, contextExport);
        getSortedVoltageLevels(network).forEach(voltageLevel -> {
            if (exportVoltageLevelAsNodeBreaker(voltageLevel)) {
                createNodeBreakerContextExport(voltageLevel, contextExport);
            } else {
                createBusBranchContextExport(voltageLevel, contextExport);
            }
        });
        return contextExport;
    }

    private static List<VoltageLevel> getSortedVoltageLevels(Network network) {
        return network.getVoltageLevelStream().sorted(Comparator.comparingInt(VoltageLevelConverter::minBus)
                .thenComparing(Identifiable::getId)).toList();
    }

    private static int minBus(VoltageLevel voltageLevel) {
        List<Integer> buses = extractBusesFromVoltageLevelId(voltageLevel.getId());
        return buses.isEmpty() ? Integer.MAX_VALUE : buses.get(0);
    }

    private static void mapVoltageLevelsAndPsseSubstation(Network network, PssePowerFlowModel psseModel, ContextExport contextExport) {
        Map<Integer, PsseSubstation> busPsseSubstation = new HashMap<>();
        psseModel.getSubstations().forEach(psseSubstation -> {
            Set<Integer> buses = psseSubstation.getNodes().stream().map(PsseSubstationNode::getI).collect(Collectors.toSet());
            buses.forEach(bus -> busPsseSubstation.put(bus, psseSubstation));
        });

        network.getVoltageLevels().forEach(voltageLevel -> getVoltageLevelPsseSubstation(voltageLevel, busPsseSubstation)
                .ifPresent(psseSubstation -> contextExport.getUpdateExport().addVoltageLevelPsseSubstation(voltageLevel, psseSubstation)));
    }

    private static Optional<PsseSubstation> getVoltageLevelPsseSubstation(VoltageLevel voltageLevel, Map<Integer, PsseSubstation> busPsseSubstation) {
        List<Integer> buses = extractBusesFromVoltageLevelId(voltageLevel.getId());
        Set<PsseSubstation> psseSubstationSet = new HashSet<>();
        buses.forEach(bus -> {
            if (busPsseSubstation.containsKey(bus)) {
                psseSubstationSet.add(busPsseSubstation.get(bus));
            }
        });
        if (psseSubstationSet.size() >= 2) {
            throw new PsseException("Only one PsseSubstation by voltageLevel. VoltageLevelId: " + voltageLevel.getId());
        }
        return psseSubstationSet.isEmpty() ? Optional.empty() : Optional.of(psseSubstationSet.iterator().next());
    }

    private static void createBusBranchContextExport(VoltageLevel voltageLevel, ContextExport contextExport) {
        Map<String, List<String>> busViewBusBusBreakerViewBuses = new HashMap<>();

        // psse isolated buses do not have busView
        voltageLevel.getBusBreakerView().getBuses().forEach(busBreakerViewBus -> {
            Bus mergedBus = voltageLevel.getBusView().getMergedBus(busBreakerViewBus.getId());
            if (mergedBus != null) {
                busViewBusBusBreakerViewBuses.computeIfAbsent(mergedBus.getId(), k -> new ArrayList<>()).add(busBreakerViewBus.getId());
            }
        });

        voltageLevel.getBusView().getBuses().forEach(bus -> {
            String busBreakerViewId = findBusBreakerViewBusId(bus.getId(), busViewBusBusBreakerViewBuses);
            extractBusNumber(busBreakerViewId).ifPresent(busI -> contextExport.getLinkExport().addBusViewBusILink(bus, busI));
        });
    }

    private static String findBusBreakerViewBusId(String busViewBusId, Map<String, List<String>> busViewBusBusBreakerViewBuses) {
        if (busViewBusBusBreakerViewBuses.containsKey(busViewBusId)) {
            List<String> busBreakerViewBuses = busViewBusBusBreakerViewBuses.get(busViewBusId);
            if (busBreakerViewBuses.isEmpty()) {
                throw new PsseException("BusView without busBreakerView: " + busViewBusId);
            } else {
                return busBreakerViewBuses.stream().sorted().toList().get(0);
            }
        } else {
            throw new PsseException("BusView bus does not found: " + busViewBusId);
        }
    }

    // All the nodes are always associated with the same busI, so the busViewId will be ok only when we do not have bus-sections
    private static void createNodeBreakerContextExport(VoltageLevel voltageLevel, ContextExport contextExport) {
        Map<Integer, List<Bus>> busIBusViews = new HashMap<>();
        PsseSubstation psseSubstation = contextExport.getUpdateExport().getPsseSubstation(voltageLevel).orElseThrow();
        for (int node : voltageLevel.getNodeBreakerView().getNodes()) {
            Bus bus = findBusViewNode(voltageLevel, node);
            if (bus != null) {
                int busI = findBusI(psseSubstation, node).orElseThrow();
                contextExport.getLinkExport().addNodeBusILink(voltageLevel, node, busI);
                busIBusViews.computeIfAbsent(busI, k -> new ArrayList<>()).add(bus);
            }
        }
        // we try to assign a busView inside mainConnectedComponent with the strong psse bus type and, we preserve the original bus type
        busIBusViews.forEach((busI, busList) -> {
            Bus selectedBus = busList.stream().min(Comparator.comparingInt(busView -> findPriorityType(voltageLevel, busView))).orElseThrow();
            contextExport.getLinkExport().addBusViewBusILink(selectedBus, busI);
        });
    }

    private static int findPriorityType(VoltageLevel voltageLevel, Bus busView) {
        int type = findBusViewBusType(voltageLevel, busView);
        return switch (type) {
            case 3 -> 0;
            case 2 -> 1;
            case 1 -> 2;
            case 4 -> 3;
            default -> throw new PsseException("Unexpected psse bus type: " + type);
        };
    }

    private static Optional<Integer> findBusI(PsseSubstation psseSubstation, int node) {
        return psseSubstation.getNodes().stream().filter(psseNode -> psseNode.getNi() == node).map(PsseSubstationNode::getI).findFirst();
    }

    static void updateSubstations(Network network, ContextExport contextExport) {
        network.getVoltageLevels().forEach(voltageLevel -> {
            if (voltageLevel.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
                Set<Integer> buses = new HashSet<>(extractBusesFromVoltageLevelId(voltageLevel.getId()));
                contextExport.getUpdateExport().getPsseSubstation(voltageLevel).ifPresent(psseSubstation -> updateSubstation(voltageLevel, psseSubstation, buses, contextExport));
            }
        });
    }

    private static void updateSubstation(VoltageLevel voltageLevel, PsseSubstation psseSubstation, Set<Integer> busesSet, ContextExport contextExport) {
        Set<PsseSubstationNode> psseNodeSet = psseSubstation.getNodes().stream().filter(psseNode -> busesSet.contains(psseNode.getI())).collect(Collectors.toSet());
        psseNodeSet.forEach(psseSubstationNode -> contextExport.getLinkExport().getBusView(voltageLevel, psseSubstationNode.getNi()).ifPresent(busView -> {
            psseSubstationNode.setVm(getVm(busView));
            psseSubstationNode.setVa(getVa(busView));
        }));

        Set<Integer> nodesSet = psseNodeSet.stream().map(PsseSubstationNode::getNi).collect(Collectors.toSet());
        Set<PsseSubstationSwitchingDevice> switchingDeviceSet = psseSubstation.getSwitchingDevices().stream()
                .filter(sd -> nodesSet.contains(sd.getNi()) && nodesSet.contains(sd.getNj())).collect(Collectors.toSet());

        switchingDeviceSet.forEach(switchingDevice -> {
            String switchId = getSwitchId(voltageLevel.getId(), switchingDevice);
            Switch sw = voltageLevel.getNodeBreakerView().getSwitch(switchId);
            if (sw == null) {
                throw new PsseException("Unexpected null breaker: " + switchId);
            }
            switchingDevice.setStatus(sw.isOpen() ? 0 : 1);
        });
    }

    private final PsseBus psseBus;
    private final PerUnitContext perUnitContext;
    private final NodeBreakerValidation nodeBreakerValidation;
    private final NodeBreakerImport nodeBreakerImport;
}
