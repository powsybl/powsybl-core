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
import com.powsybl.iidm.network.extensions.SlackTerminal;
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
            boolean isNodeBreakerValid = nodeBreakerValidation.isNodeBreakerSubstationCoherent(getContainersMapping().getBusesSet(voltageLevelId));

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
            nodeBreakerImport.addSlackControl(psseBus.getI(), voltageLevelId, obtainSlackNode(nodeBreakerValidation, psseBus.getI()));
        }
        return voltageLevel;
    }

    static double getNominalV(PsseBus psseBus, boolean isIgnoreBaseVoltage) {
        return isIgnoreBaseVoltage || psseBus.getBaskv() == 0 ? 1 : psseBus.getBaskv();
    }

    private void addNodeBreakerConnectivity(String voltageLevelId, VoltageLevel voltageLevel, NodeBreakerValidation nodeBreakerValidation, NodeBreakerImport nodeBreakerImport) {
        Set<Integer> buses = getContainersMapping().getBusesSet(voltageLevelId);
        PsseSubstation substation = buses.stream().map(nodeBreakerValidation::getSubstationIfOnlyOneExists)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst().orElseThrow();

        nodeBreakerImport.addTopologicalBuses(buses);

        for (PsseSubstationSwitchingDevice switchingDevice : substation.getSwitchingDevices()) {
            voltageLevel.getNodeBreakerView().newSwitch()
                    .setId(getSwitchId(voltageLevelId, switchingDevice))
                    .setName(switchingDevice.getName())
                    .setNode1(switchingDevice.getNi())
                    .setNode2(switchingDevice.getNj())
                    .setKind(obtainSwitchingKind(switchingDevice.getType()))
                    .setOpen(switchingDevice.getStatus() != 1)
                    .add();
        }

        // Define where equipment must be connected
        int lastNode = substation.getNodes().stream().map(PsseSubstationNode::getNi).max(Comparator.naturalOrder()).orElseThrow();
        Set<Integer> nodesWithEquipment = new HashSet<>();
        for (PsseSubstationEquipmentTerminal equipmentTerminal : substation.getEquipmentTerminals()) {
            String equipmentId = getNodeBreakerEquipmentId(equipmentTerminal.getType(), equipmentTerminal.getI(), equipmentTerminal.getJ(), equipmentTerminal.getK(), equipmentTerminal.getId());
            int bus = obtainBus(equipmentTerminal, buses);
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

        // Nodes without equipment are defined as busbar sections to improve the regulating terminal selection
        substation.getNodes().stream()
                .map(PsseSubstationNode::getNi)
                .filter(node -> !nodesWithEquipment.contains(node))
                .collect(Collectors.toSet()).forEach(node -> voltageLevel.getNodeBreakerView()
                        .newBusbarSection()
                        .setId(busbarSectionId(voltageLevel.getId(), node))
                        .setNode(node)
                        .add());

        // Define where controls must be connected. Psse nodes and iidm nodes are identical.
        buses.forEach(bus -> {
            List<NodeBreakerControl> controls = nodeBreakerValidation.getControls(bus);
            controls.forEach(control -> nodeBreakerImport.addControl(getNodeBreakerEquipmentIdBus(control.getEquipmentId(), bus), voltageLevelId, control.getNode()));
        });
    }

    private static SwitchKind obtainSwitchingKind(int type) {
        return type == 2 ? SwitchKind.BREAKER : SwitchKind.DISCONNECTOR;
    }

    private static int obtainBus(PsseSubstationEquipmentTerminal equipmentTerminal, Set<Integer> buses) {
        return buses.stream().filter(bus -> bus == equipmentTerminal.getI() || bus == equipmentTerminal.getJ() || bus == equipmentTerminal.getK()).min(Comparator.naturalOrder()).orElseThrow();
    }

    private static String getSwitchId(String voltageLevelId, PsseSubstationSwitchingDevice switchingDevice) {
        return voltageLevelId + "-Sw-" + switchingDevice.getNi() + "-" + switchingDevice.getNj() + "-" + switchingDevice.getCkt();
    }

    private static String busbarSectionId(String voltageLevelId, int node) {
        return String.format("%s-Busbar-%d", voltageLevelId, node);
    }

    private static int obtainSlackNode(NodeBreakerValidation nodeBreakerValidation, int bus) {
        PsseSubstation psseSubstation = nodeBreakerValidation.getSubstationIfOnlyOneExists(bus).orElseThrow();
        return psseSubstation.getNodes().stream().filter(n -> n.getI() == bus)
                .map(PsseSubstationNode::getNi).min(Comparator.comparingInt((Integer node) -> connectedGenerators(psseSubstation, node))
                        .reversed().thenComparing(Comparator.naturalOrder())).orElseThrow();
    }

    private static int connectedGenerators(PsseSubstation psseSubstation, int node) {
        return (int) psseSubstation.getEquipmentTerminals().stream().filter(eqt -> eqt.getNi() == node && eqt.getType().equals(PSSE_GENERATOR.getTextCode())).count();
    }

    static NodeBreakerExport mapSubstations(Network network, PssePowerFlowModel psseModel) {
        int maxPsseBus = psseModel.getBuses().stream().map(PsseBus::getI).max(Comparator.naturalOrder()).orElseThrow();
        NodeBreakerExport nodeBreakerExport = new NodeBreakerExport(maxPsseBus);

        psseModel.getSubstations().forEach(psseSubstation -> mapSubstation(network, psseSubstation, nodeBreakerExport));

        return nodeBreakerExport;
    }

    private static void mapSubstation(Network network, PsseSubstation psseSubstation, NodeBreakerExport nodeBreakerExport) {
        VoltageLevel voltageLevel = obtainVoltageLevel(network, psseSubstation);
        if (voltageLevel != null && isNodeBreaker(voltageLevel)) {
            mapBusBreakerBusesToPsseBuses(voltageLevel, psseSubstation, nodeBreakerExport);
            mapIsolatedPsseNodesToPsseBuses(voltageLevel, psseSubstation, nodeBreakerExport);
            mapEquipmentIdBustoBus(voltageLevel, psseSubstation, nodeBreakerExport);
        }
    }

    private static VoltageLevel obtainVoltageLevel(Network network, PsseSubstation psseSubstation) {
        Set<Integer> busesSet = psseSubstation.getNodes().stream().map(PsseSubstationNode::getI).collect(Collectors.toSet());
        String voltageLevelId = getVoltageLevelId(busesSet);
        return network.getVoltageLevel(voltageLevelId);
    }

    private static boolean isNodeBreaker(VoltageLevel voltageLevel) {
        Objects.requireNonNull(voltageLevel);
        return voltageLevel.getTopologyKind().equals(TopologyKind.NODE_BREAKER);
    }

    private static void mapBusBreakerBusesToPsseBuses(VoltageLevel voltageLevel, PsseSubstation psseSubstation, NodeBreakerExport nodeBreakerExport) {
        Set<Integer> psseNodesSet = psseSubstation.getNodes().stream().map(PsseSubstationNode::getNi).collect(Collectors.toSet());
        Map<String, List<Integer>> busBreakerBusIdNodeList = obtainBusBreakerBusIdNodeList(voltageLevel);

        // First, buses with more psse nodes
        Comparator<String> comp = Comparator.comparingInt((String key) -> onlyPsseNodes(busBreakerBusIdNodeList.get(key), psseNodesSet).size())
                .reversed()
                .thenComparing(Comparator.naturalOrder());

        busBreakerBusIdNodeList.keySet().stream().sorted(comp).forEach(key -> {

            Optional<Integer> bus = mapPsseBus(psseSubstation, nodeBreakerExport);
            int busType = obtainBusBreakerBusType(voltageLevel, key);
            if (bus.isPresent()) {
                nodeBreakerExport.addBusMapping(bus.get(), key, busType);
                nodeBreakerExport.addNodesBusMapping(voltageLevel.getId(), onlyPsseNodes(busBreakerBusIdNodeList.get(key), psseNodesSet), bus.get(), true);
            } else {
                int newBus = nodeBreakerExport.getNewPsseBus();
                nodeBreakerExport.addNewMappedBus(newBus, selectCopyBus(psseSubstation), key, busType);
                nodeBreakerExport.addNodesBusMapping(voltageLevel.getId(), onlyPsseNodes(busBreakerBusIdNodeList.get(key), psseNodesSet), newBus, true);
            }
        });
    }

    // discard nodes associated with internal connections
    private static List<Integer> onlyPsseNodes(List<Integer> iidmNodes, Set<Integer> psseNodesSet) {
        return iidmNodes.stream().filter(psseNodesSet::contains).toList();
    }

    private static Map<String, List<Integer>> obtainBusBreakerBusIdNodeList(VoltageLevel voltageLevel) {
        Map<String, List<Integer>> busBreakerBusIdNodeList = new HashMap<>();

        for (int node : voltageLevel.getNodeBreakerView().getNodes()) {
            Terminal terminal = obtainTerminalNode(voltageLevel, node);
            if (terminal != null) {
                Bus bus = terminal.getBusBreakerView().getBus();
                if (bus != null) {
                    busBreakerBusIdNodeList.computeIfAbsent(bus.getId(), k -> new ArrayList<>()).add(node);
                }
            }
        }
        return busBreakerBusIdNodeList;
    }

    // Map to the free psse bus with more matching nodes
    private static Optional<Integer> mapPsseBus(PsseSubstation psseSubstation, NodeBreakerExport nodeBreakerExport) {

        Comparator<Integer> comp = Comparator.comparingInt((Integer b) -> nodesAssociatedWithBus(psseSubstation, b).size())
                .reversed()
                .thenComparing(Comparator.naturalOrder());

        return psseSubstation.getNodes().stream()
                .map(PsseSubstationNode::getI).filter(nodeBreakerExport::isFreeBus)
                .collect(Collectors.toSet())
                .stream().min(comp);
    }

    private static int selectCopyBus(PsseSubstation psseSubstation) {
        return psseSubstation.getNodes().stream().map(PsseSubstationNode::getI).min(Comparator.naturalOrder()).orElseThrow();
    }

    private static List<Integer> nodesAssociatedWithBus(PsseSubstation psseSubstation, int bus) {
        return psseSubstation.getNodes().stream().filter(n -> n.getI() == bus).map(PsseSubstationNode::getNi).toList();
    }

    private static int obtainBusBreakerBusType(VoltageLevel voltageLevel, String busBreakerBusId) {
        Bus bus = voltageLevel.getBusBreakerView().getBus(busBreakerBusId);
        if (bus == null) { // unexpected
            return 4;
        }
        SlackTerminal slackTerminal = voltageLevel.getExtension(SlackTerminal.class);
        if (slackTerminal != null && bus.equals(slackTerminal.getTerminal().getBusBreakerView().getBus())) {
            return 3;
        }
        return bus.getGeneratorStream().anyMatch(VoltageLevelConverter::withLocalRegulatingControl) ? 2 : 1;
    }

    private static boolean withLocalRegulatingControl(Generator generator) {
        return generator.isVoltageRegulatorOn()
                && generator.getTerminal().getBusBreakerView().getBus().equals(generator.getRegulatingTerminal().getBusBreakerView().getBus());
    }

    private static void mapIsolatedPsseNodesToPsseBuses(VoltageLevel voltageLevel, PsseSubstation psseSubstation, NodeBreakerExport nodeBreakerExport) {
        mapIsolatedPsseNodesToTheAssociatedFreeBus(voltageLevel, psseSubstation, nodeBreakerExport);
        mapIsolatedPsseNodesToFreeBus(voltageLevel, psseSubstation, nodeBreakerExport);
        mapIsolatedPsseNodesToNewBus(voltageLevel, psseSubstation, nodeBreakerExport);
    }

    private static void mapIsolatedPsseNodesToTheAssociatedFreeBus(VoltageLevel voltageLevel, PsseSubstation psseSubstation, NodeBreakerExport nodeBreakerExport) {
        List<Integer> isolatedNodes = psseSubstation.getNodes().stream().map(PsseSubstationNode::getNi).filter(node -> nodeBreakerExport.isFreeNode(voltageLevel.getId(), node)).toList();
        isolatedNodes.forEach(node -> {
            Optional<Integer> bus = psseSubstation.getNodes().stream()
                    .filter(n -> n.getNi() == node && nodeBreakerExport.isFreeBus(n.getI()))
                    .map(PsseSubstationNode::getI).min(Comparator.naturalOrder());
            if (bus.isPresent()) {
                nodeBreakerExport.addNodeBusMapping(voltageLevel.getId(), node, bus.get(), false);
                nodeBreakerExport.addIsolatedBus(bus.get());
            }
        });
    }

    private static void mapIsolatedPsseNodesToFreeBus(VoltageLevel voltageLevel, PsseSubstation psseSubstation, NodeBreakerExport nodeBreakerExport) {
        List<Integer> isolatedNodes = psseSubstation.getNodes().stream().map(PsseSubstationNode::getNi).filter(node -> nodeBreakerExport.isFreeNode(voltageLevel.getId(), node)).toList();
        isolatedNodes.forEach(node -> {
            Optional<Integer> bus = psseSubstation.getNodes().stream()
                    .map(PsseSubstationNode::getI)
                    .filter(nodeBreakerExport::isFreeBus)
                    .min(Comparator.naturalOrder());
            if (bus.isPresent()) {
                nodeBreakerExport.addNodeBusMapping(voltageLevel.getId(), node, bus.get(), false);
                nodeBreakerExport.addIsolatedBus(bus.get());
            }
        });
    }

    private static void mapIsolatedPsseNodesToNewBus(VoltageLevel voltageLevel, PsseSubstation psseSubstation, NodeBreakerExport nodeBreakerExport) {
        List<Integer> isolatedNodes = psseSubstation.getNodes().stream().map(PsseSubstationNode::getNi).filter(node -> nodeBreakerExport.isFreeNode(voltageLevel.getId(), node)).toList();
        isolatedNodes.forEach(node -> {
            int newBus = nodeBreakerExport.getNewPsseBus();
            nodeBreakerExport.addNewNotMappedBus(newBus, selectCopyBus(psseSubstation));
            nodeBreakerExport.addNodeBusMapping(voltageLevel.getId(), node, newBus, false);
            nodeBreakerExport.addIsolatedBus(newBus);
        });
    }

    private static void mapEquipmentIdBustoBus(VoltageLevel voltageLevel, PsseSubstation psseSubstation, NodeBreakerExport nodeBreakerExport) {
        psseSubstation.getEquipmentTerminals().forEach(equipmentTerminal -> {
            String equipmentId = getNodeBreakerEquipmentId(equipmentTerminal.getType(), equipmentTerminal.getI(), equipmentTerminal.getJ(), equipmentTerminal.getK(), equipmentTerminal.getId());
            nodeBreakerExport.getNodeBus(voltageLevel.getId(), equipmentTerminal.getNi()).ifPresent(bus -> {
                if (bus != equipmentTerminal.getI()) {
                    nodeBreakerExport.addEquipmentIdBus(getNodeBreakerEquipmentIdBus(equipmentId, equipmentTerminal.getI()), bus);
                }
            });
        });
    }

    static void updateSubstations(Network network, PssePowerFlowModel psseModel, NodeBreakerExport nodeBreakerExport) {
        psseModel.getSubstations().forEach(psseSubstation -> {
            VoltageLevel voltageLevel = obtainVoltageLevel(network, psseSubstation);
            if (voltageLevel != null && isNodeBreaker(voltageLevel)) {
                updateSubstation(voltageLevel, psseSubstation, nodeBreakerExport);
            }
        });
    }

    private static void updateSubstation(VoltageLevel voltageLevel, PsseSubstation psseSubstation, NodeBreakerExport nodeBreakerExport) {

        psseSubstation.getNodes().forEach(psseSubstationNode -> {
            int bus = nodeBreakerExport.getNodeBus(voltageLevel.getId(), psseSubstationNode.getNi()).orElseThrow();
            boolean inService = nodeBreakerExport.isNodeInService(voltageLevel.getId(), psseSubstationNode.getNi()).orElseThrow();
            psseSubstationNode.setI(bus);
            psseSubstationNode.setStatus(inService ? 1 : 0);
        });

        psseSubstation.getSwitchingDevices().forEach(switchingDevice -> {
            String switchId = getSwitchId(voltageLevel.getId(), switchingDevice);
            Switch sw = voltageLevel.getNodeBreakerView().getSwitch(switchId);
            if (sw == null) {
                throw new PsseException("Unexpected null breaker: " + switchId);
            }
            switchingDevice.setStatus(sw.isOpen() ? 0 : 1);
        });

        psseSubstation.getEquipmentTerminals().forEach(equipmentTerminal -> {
            String equipmentId = getNodeBreakerEquipmentId(equipmentTerminal.getType(), equipmentTerminal.getI(), equipmentTerminal.getJ(), equipmentTerminal.getK(), equipmentTerminal.getId());
            nodeBreakerExport.getEquipmentIdBusBus(getNodeBreakerEquipmentIdBus(equipmentId, equipmentTerminal.getI())).ifPresent(equipmentTerminal::setI);
            nodeBreakerExport.getEquipmentIdBusBus(getNodeBreakerEquipmentIdBus(equipmentId, equipmentTerminal.getJ())).ifPresent(equipmentTerminal::setJ);
            nodeBreakerExport.getEquipmentIdBusBus(getNodeBreakerEquipmentIdBus(equipmentId, equipmentTerminal.getK())).ifPresent(equipmentTerminal::setK);
        });
    }

    private final PsseBus psseBus;
    private final PerUnitContext perUnitContext;
    private final NodeBreakerValidation nodeBreakerValidation;
    private final NodeBreakerImport nodeBreakerImport;
}
