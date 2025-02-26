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
            boolean isNodeBreakerValid = nodeBreakerValidation.isConsideredNodeBreaker(getContainersMapping().getBusesSet(voltageLevelId));

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
        Optional<PsseSubstation> psseSubstation = nodeBreakerValidation.getTheOnlySubstation(buses);
        if (psseSubstation.isPresent()) {
            int lastNode = psseSubstation.get().getNodes().stream().map(PsseSubstationNode::getNi).max(Comparator.naturalOrder()).orElseThrow();
            for (int bus : buses) {
                lastNode = addNodeBreakerConnectivity(voltageLevelId, voltageLevel, psseSubstation.get(), bus, lastNode, nodeBreakerImport);
            }
        }
    }

    private static int addNodeBreakerConnectivity(String voltageLevelId, VoltageLevel voltageLevel, PsseSubstation psseSubstation, int bus, int lastNodeInternalConnections, NodeBreakerImport nodeBreakerImport) {
        Set<Integer> nodesSet = psseSubstation.getNodes().stream().filter(psseNode -> psseNode.getI() == bus).map(PsseSubstationNode::getNi).collect(Collectors.toSet());

        List<PsseSubstationSwitchingDevice> switchingDeviceList = psseSubstation.getSwitchingDevices().stream()
                .filter(sd -> nodesSet.contains(sd.getNi()) && nodesSet.contains(sd.getNj()))
                .sorted(Comparator.comparing(VoltageLevelConverter::switchingDeviceString))
                .toList();

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

        // add data for control, if only the bus is specified the control will be associated with the default node
        int defaultNode = nodesSet.stream().sorted().findFirst().orElseThrow();
        nodeBreakerImport.addBusControl(bus, voltageLevelId, defaultNode);

        return lastNode;
    }

    private static String switchingDeviceString(PsseSubstationSwitchingDevice switchingDevice) {
        return switchingDevice.getNi() + "-" + switchingDevice.getNj() + "-" + switchingDevice.getCkt();
    }

    private static SwitchKind findSwitchingKind(int type) {
        return type == 2 ? SwitchKind.BREAKER : SwitchKind.DISCONNECTOR;
    }

    private static int findSlackNode(NodeBreakerValidation nodeBreakerValidation, int bus) {
        PsseSubstation psseSubstation = nodeBreakerValidation.getTheOnlySubstation(bus).orElseThrow();
        return psseSubstation.getNodes().stream().filter(n -> n.getI() == bus)
                .map(PsseSubstationNode::getNi).min(Comparator.comparingInt((Integer node) -> connectedGenerators(psseSubstation, node))
                        .reversed().thenComparing(Comparator.naturalOrder())).orElseThrow();
    }

    private static int connectedGenerators(PsseSubstation psseSubstation, int node) {
        return (int) psseSubstation.getEquipmentTerminals().stream().filter(eqt -> eqt.getNi() == node && eqt.getType().equals(PSSE_GENERATOR.getTextCode())).count();
    }

    static ContextExport createContextExport(Network network, PssePowerFlowModel psseModel, boolean isFullExport) {
        ContextExport contextExport = new ContextExport(isFullExport);
        if (!isFullExport) {
            mapVoltageLevelsAndPsseSubstation(network, psseModel, contextExport);
        }
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

    static void createSubstations(Network network, PssePowerFlowModel psseModel, ContextExport contextExport) {
        getSortedVoltageLevels(network).forEach(voltageLevel -> {
            if (exportVoltageLevelAsNodeBreaker(voltageLevel)) {
                psseModel.addSubstations(Collections.singletonList(createPsseSubstation(voltageLevel, contextExport)));
            }
        });
    }

    private static PsseSubstation createPsseSubstation(VoltageLevel voltageLevel, ContextExport contextExport) {
        return new PsseSubstation(createPsseSubstationSubstationRecord(voltageLevel, contextExport),
                createPsseSubstationNodes(voltageLevel, contextExport),
                createPsseSubstationSwitchingDevices(voltageLevel, contextExport),
                createPsseSubstationEquipmentTerminals(voltageLevel, contextExport));
    }

    private static PsseSubstation.PsseSubstationRecord createPsseSubstationSubstationRecord(VoltageLevel voltageLevel, ContextExport contextExport) {
        PsseSubstation.PsseSubstationRecord substationRecord = new PsseSubstation.PsseSubstationRecord();
        substationRecord.setIs(contextExport.getFullExport().getNewPsseSubstationIs());
        substationRecord.setName(voltageLevel.getId());
        substationRecord.setLati(0.0);
        substationRecord.setLong(0.0);
        substationRecord.setSrg(0.1);
        return substationRecord;
    }

    private static List<PsseSubstation.PsseSubstationNode> createPsseSubstationNodes(VoltageLevel voltageLevel, ContextExport contextExport) {
        Set<Integer> nodesToBeExported = new HashSet<>();
        for (int node : voltageLevel.getNodeBreakerView().getNodes()) {
            nodesToBeExported.add(convertToPsseNode(contextExport.getFullExport().getRepresentativeNode(voltageLevel, node)));
        }

        List<PsseSubstation.PsseSubstationNode> nodes = new ArrayList<>();
        nodesToBeExported.stream().sorted(Comparator.naturalOrder()).forEach(ni -> {
            PsseSubstation.PsseSubstationNode psseNode = new PsseSubstationNode();
            int busI = contextExport.getLinkExport().getBusI(voltageLevel, ni).orElseThrow();
            Bus bus = contextExport.getLinkExport().getBusView(voltageLevel, ni).orElse(null);

            psseNode.setNi(ni);
            psseNode.setName(getNodeId(voltageLevel, ni));
            psseNode.setI(busI);
            psseNode.setStatus(1);
            psseNode.setVm(getVm(bus));
            psseNode.setVa(getVa(bus));

            nodes.add(psseNode);
        });
        return nodes;
    }

    // ckt must be unique inside the voltageLevel
    private static List<PsseSubstation.PsseSubstationSwitchingDevice> createPsseSubstationSwitchingDevices(VoltageLevel voltageLevel, ContextExport contextExport) {
        List<PsseSubstation.PsseSubstationSwitchingDevice> switchingDevices = new ArrayList<>();
        voltageLevel.getSwitches().forEach(sw -> {
            int ni = convertToPsseNode(contextExport.getFullExport().getRepresentativeNode(voltageLevel, sw.getVoltageLevel().getNodeBreakerView().getNode1(sw.getId())));
            int nj = convertToPsseNode(contextExport.getFullExport().getRepresentativeNode(voltageLevel, sw.getVoltageLevel().getNodeBreakerView().getNode2(sw.getId())));
            PsseSubstation.PsseSubstationSwitchingDevice switchingDevice = new PsseSubstationSwitchingDevice();
            switchingDevice.setNi(ni);
            switchingDevice.setNj(nj);
            switchingDevice.setCkt(contextExport.getFullExport().getEquipmentCkt(voltageLevel, sw.getId(), IdentifiableType.SWITCH, ni, nj));
            switchingDevice.setName(sw.getNameOrId());
            switchingDevice.setType(getSwitchingDeviceType(sw));
            switchingDevice.setStatus(sw.isOpen() ? 0 : 1);
            switchingDevice.setNstat(1);
            switchingDevice.setX(0.0001);
            switchingDevice.setRate1(0.0);
            switchingDevice.setRate2(0.0);
            switchingDevice.setRate3(0.0);

            switchingDevices.add(switchingDevice);
        });
        return switchingDevices;
    }

    private static int getSwitchingDeviceType(Switch sw) {
        return switch (sw.getKind()) {
            case BREAKER, LOAD_BREAK_SWITCH -> 2;
            case DISCONNECTOR -> 3;
        };
    }

    private static List<PsseSubstation.PsseSubstationEquipmentTerminal> createPsseSubstationEquipmentTerminals(VoltageLevel voltageLevel, ContextExport contextExport) {
        List<PsseSubstation.PsseSubstationEquipmentTerminal> equipmentTerminals = new ArrayList<>();

        getEquipmentListToBeExported(voltageLevel).forEach(equipmentId -> {
            List<NodeBusR> nodeBusRList = getEquipmentNodesInsideVoltageLevelPreservingOrder(voltageLevel, equipmentId, contextExport);
            Identifiable<?> identifiable = getIdentifiable(voltageLevel, equipmentId);
            String type = getPsseEquipmentType(identifiable);
            getNodesInsideVoltageLevelPreservingOrder(nodeBusRList, voltageLevel).forEach(nodeBusR -> {
                List<Integer> otherBuses = getTwoOtherBusesPreservingOrder(voltageLevel, equipmentId, nodeBusR, contextExport);

                PsseSubstation.PsseSubstationEquipmentTerminal equipmentTerminal = new PsseSubstationEquipmentTerminal();
                equipmentTerminal.setNi(nodeBusR.node());
                equipmentTerminal.setType(type);
                equipmentTerminal.setId(contextExport.getFullExport().getEquipmentCkt(equipmentId, identifiable.getType(), nodeBusR.busI(), otherBuses.get(0), otherBuses.get(1)));
                equipmentTerminal.setI(nodeBusR.busI());
                equipmentTerminal.setJ(otherBuses.get(0));
                equipmentTerminal.setK(otherBuses.get(1));

                equipmentTerminals.add(equipmentTerminal);
            });
        });
        return equipmentTerminals;
    }

    private static List<NodeBusR> getEquipmentNodesInsideVoltageLevelPreservingOrder(VoltageLevel voltageLevel, String equipmentId, ContextExport contextExport) {
        return getEquipmentTerminals(voltageLevel, equipmentId).stream()
                .filter(terminal -> terminal.getVoltageLevel().equals(voltageLevel))
                .map(terminal -> findNodeBusR(terminal, contextExport)).toList();
    }

    private static NodeBusR findNodeBusR(Terminal terminal, ContextExport contextExport) {
        int representativeNode = convertToPsseNode(contextExport.getFullExport().getRepresentativeNode(terminal.getVoltageLevel(), getTerminalNode(terminal)));
        int busI = getTerminalBusI(terminal, contextExport);
        return new NodeBusR(terminal.getVoltageLevel(), representativeNode, busI);
    }

    private record NodeBusR(VoltageLevel voltageLevel, int node, int busI) {
        boolean equals(NodeBusR other) {
            return voltageLevel().equals(other.voltageLevel()) && node() == other.node();
        }
    }

    private static List<NodeBusR> getNodesInsideVoltageLevelPreservingOrder(List<NodeBusR> nodeBusRList, VoltageLevel voltageLevel) {
        return nodeBusRList.stream().filter(nodeBusR -> nodeBusR.voltageLevel().equals(voltageLevel)).toList();
    }

    private static List<Integer> getTwoOtherBusesPreservingOrder(VoltageLevel voltageLevel, String equipmentId, NodeBusR nodeBusR, ContextExport contextExport) {
        List<Integer> buses = new ArrayList<>();
        getEquipmentTerminals(voltageLevel, equipmentId).forEach(terminal -> {
            if (exportVoltageLevelAsNodeBreaker(terminal.getVoltageLevel())) {
                NodeBusR otherNodeBusR = findNodeBusR(terminal, contextExport);
                if (!nodeBusR.equals(otherNodeBusR)) {
                    buses.add(otherNodeBusR.busI());
                }
                // busJ associated with boundary side of the dangling lines
                Identifiable<?> identifiable = getIdentifiable(voltageLevel, equipmentId);
                if (identifiable.getType().equals(IdentifiableType.DANGLING_LINE)) {
                    buses.add(contextExport.getLinkExport().getBusI((DanglingLine) identifiable).orElseThrow());
                }
            } else {
                Bus busView = getTerminalBusView(terminal);
                buses.add(contextExport.getLinkExport().getBusI(busView).orElseThrow());
            }
        });
        // Complete with zeros until two buses
        if (buses.isEmpty()) {
            buses.add(0);
            buses.add(0);
        } else if (buses.size() == 1) {
            buses.add(0);
        } else if (buses.size() > 2) {
            throw new PsseException("Unexpected number of buses for equipmentId: " + equipmentId);
        }
        return buses;
    }

    private static Identifiable<?> getIdentifiable(VoltageLevel voltageLevel, String identifiableId) {
        Identifiable<?> identifiable = voltageLevel.getNetwork().getIdentifiable(identifiableId);
        if (identifiable != null) {
            return identifiable;
        } else {
            throw new PsseException("Unexpected identifiableId: " + identifiableId);
        }
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
        psseNodeSet.forEach(psseSubstationNode -> {
            Optional<Bus> busView = contextExport.getLinkExport().getBusView(voltageLevel, psseSubstationNode.getNi());
            if (busView.isPresent()) {
                psseSubstationNode.setVm(getVm(busView.get()));
                psseSubstationNode.setVa(getVa(busView.get()));
            } else {
                psseSubstationNode.setVm(1.0);
                psseSubstationNode.setVa(0.0);
            }
        });

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
