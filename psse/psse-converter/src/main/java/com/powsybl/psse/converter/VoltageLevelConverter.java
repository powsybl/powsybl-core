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
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.Pseudograph;

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
        if (voltageLevel.getTopologyKind() == TopologyKind.NODE_BREAKER && psseBus.getIde() == 3) {
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

    private static int addNodeBreakerConnectivity(String voltageLevelId, VoltageLevel voltageLevel, PsseSubstation psseSubstation, int bus, int lastNodeUsedForInternalConnections, NodeBreakerImport nodeBreakerImport) {
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

        int lastNode = lastNodeUsedForInternalConnections;
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
                .forEach(psseNode -> findBusViewNode(voltageLevel, psseNode.getNi())
                        .ifPresent(busView -> {
                            busView.setV(psseNode.getVm() * voltageLevel.getNominalV());
                            busView.setAngle(psseNode.getVa());
                        }));

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
                boolean isCreated = createNodeBreakerContextExport(voltageLevel, contextExport);
                if (!isCreated) {
                    createBusBranchContextExport(voltageLevel, contextExport);
                }
            } else {
                createBusBranchContextExport(voltageLevel, contextExport);
            }
        });
        return contextExport;
    }

    private static List<VoltageLevel> getSortedVoltageLevels(Network network) {
        return network.getVoltageLevelStream().sorted(Comparator.comparingInt(VoltageLevelConverter::minBus)
                        .thenComparing(vl -> vl.getSubstation().map(Identifiable::getId).orElse(vl.getId()))
                        .thenComparing(Comparator.comparingDouble(VoltageLevel::getNominalV).reversed()))
                .toList();
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

        Set<PsseSubstation> psseSubstationSet = buses.stream()
                .filter(busPsseSubstation::containsKey)
                .map(busPsseSubstation::get)
                .collect(Collectors.toSet());

        if (psseSubstationSet.size() > 1) {
            throw new PsseException("Only one PsseSubstation is allowed per VoltageLevel. VoltageLevelId: " + voltageLevel.getId());
        }

        return psseSubstationSet.stream().findFirst();
    }

    private static void createBusBranchContextExport(VoltageLevel voltageLevel, ContextExport contextExport) {
        if (contextExport.isFullExport()) {
            createBusBranchContextExportForFullExport(voltageLevel, contextExport);
        } else {
            createBusBranchContextExportForUpdating(voltageLevel, contextExport);
        }
    }

    private static void createBusBranchContextExportForFullExport(VoltageLevel voltageLevel, ContextExport contextExport) {
        voltageLevel.getBusView().getBuses().forEach(bus -> contextExport.getFullExport().addBusIBusView(contextExport.getFullExport().getNewPsseBusI(), bus));
    }

    private static void createBusBranchContextExportForUpdating(VoltageLevel voltageLevel, ContextExport contextExport) {
        voltageLevel.getBusBreakerView().getBuses().forEach(busBreakerViewBus ->
                Optional.ofNullable(voltageLevel.getBusView().getMergedBus(busBreakerViewBus.getId()))
                        .ifPresent(mergedBus -> extractBusNumber(busBreakerViewBus.getId())
                                .ifPresent(busI -> contextExport.getUpdateExport().addBusIBusView(busI, mergedBus))
                        )
        );
    }

    // All the nodes are always associated with the same busI, so the busViewId will be ok only when we do not have bus-sections
    private static boolean createNodeBreakerContextExport(VoltageLevel voltageLevel, ContextExport contextExport) {
        if (contextExport.isFullExport()) {
            return createNodeBreakerContextExportForFullExport(voltageLevel, contextExport);
        } else {
            createNodeBreakerContextExportForUpdating(voltageLevel, contextExport);
            return true;
        }
    }

    private static void createNodeBreakerContextExportForUpdating(VoltageLevel voltageLevel, ContextExport contextExport) {
        Map<Integer, List<Bus>> busIBusViews = new HashMap<>();
        PsseSubstation psseSubstation = contextExport.getUpdateExport().getPsseSubstation(voltageLevel).orElseThrow();
        for (int node : voltageLevel.getNodeBreakerView().getNodes()) {
            findBusViewNode(voltageLevel, node).ifPresent(bus -> {
                contextExport.getUpdateExport().addNodeBusView(voltageLevel, node, bus);
                findBusI(psseSubstation, node).ifPresent(busI -> busIBusViews.computeIfAbsent(busI, k -> new ArrayList<>()).add(bus));
            });
        }
        // we try to assign a busView inside mainConnectedComponent with the strong psse bus type and, we preserve the original bus type
        busIBusViews.forEach((busI, busList) -> {
            Bus selectedBus = busList.stream().min(Comparator.comparingInt(VoltageLevelConverter::findPriorityType)).orElseThrow();
            contextExport.getUpdateExport().addBusIBusView(busI, selectedBus);
        });
    }

    private static boolean createNodeBreakerContextExportForFullExport(VoltageLevel voltageLevel, ContextExport contextExport) {
        // a new psseSubstation is created for each iidm substation (false) or for each iidm voltageLevel (true)
        boolean psseSubstationByVoltageLevel = false;
        String psseSubstationId = findPsseSubstationId(voltageLevel, psseSubstationByVoltageLevel);

        Map<Integer, Integer> representativeForInternalConnectionsNodes = findRepresentativeNodes(voltageLevel);
        List<Set<Integer>> connectedSetsBySwitchesAndInternalConnections = connectedSetsBySwitchesAndInternalConnections(voltageLevel);

        // only getMaxPsseNodeBySubstation psse nodes are allowed inside a substation
        if (contextExport.getFullExport().getLastPsseNode(psseSubstationId) +
                newPsseNodes(connectedSetsBySwitchesAndInternalConnections, representativeForInternalConnectionsNodes)
                > getMaxPsseNodeBySubstation()) {
            return false;
        }

        if (!connectedSetsBySwitchesAndInternalConnections.isEmpty()) {
            contextExport.getFullExport().addPsseSubstationIdVoltageLevel(psseSubstationId, voltageLevel);
        }

        connectedSetsBySwitchesAndInternalConnections.forEach(connectedSet -> {
            int busI = contextExport.getFullExport().getNewPsseBusI();
            Set<Bus> busViewBusesForBusI = new HashSet<>();

            connectedSet.stream()
                    .filter(node -> !isRepresented(representativeForInternalConnectionsNodes, node))
                    .forEach(nonRepresentedNode -> contextForNonRepresentedNode(voltageLevel, nonRepresentedNode, busI, busViewBusesForBusI, psseSubstationId, contextExport));

            connectedSet.stream()
                    .filter(node -> isRepresented(representativeForInternalConnectionsNodes, node))
                    .forEach(representedNode -> {
                        int representativeNode = representativeForInternalConnectionsNodes.get(representedNode);
                        contextForRepresentedNode(voltageLevel, representedNode, representativeNode, contextExport);
                    });

            Bus selectedBus = busViewBusesForBusI.stream().min(Comparator.comparingInt(VoltageLevelConverter::findPriorityType)).orElse(null);
            contextExport.getFullExport().addBusIBusView(busI, selectedBus);
        });

        voltageLevel.getDanglingLineStream().filter(danglingLine -> !danglingLine.isPaired())
                .forEach(danglingLine -> contextExport.getFullExport().addDanglingLineBusI(danglingLine, contextExport.getFullExport().getNewPsseBusI()));

        return true;
    }

    private static int newPsseNodes(List<Set<Integer>> connectedSetsBySwitchesAndInternalConnections, Map<Integer, Integer> representativeForInternalConnectionsNodes) {
        return connectedSetsBySwitchesAndInternalConnections.stream()
                .mapToInt(connectedSet -> (int) connectedSet.stream()
                        .filter(node -> !isRepresented(representativeForInternalConnectionsNodes, node))
                        .count())
                .sum();
    }

    private static String findPsseSubstationId(VoltageLevel voltageLevel, boolean psseSubstationByVoltageLevel) {
        return psseSubstationByVoltageLevel ? voltageLevel.getId() : voltageLevel.getSubstation().map(Identifiable::getId).orElse(voltageLevel.getId());
    }

    private static Map<Integer, Integer> findRepresentativeNodes(VoltageLevel voltageLevel) {
        List<Set<Integer>> connectedSetsByInternalConnections = connectedSetsByInternalConnections(voltageLevel);

        Set<Integer> nodesOfSwitches = new HashSet<>();
        voltageLevel.getNodeBreakerView().getSwitches().forEach(sw -> {
            nodesOfSwitches.add(voltageLevel.getNodeBreakerView().getNode1(sw.getId()));
            nodesOfSwitches.add(voltageLevel.getNodeBreakerView().getNode2(sw.getId()));
        });

        // the best candidate is a node with switches
        Map<Integer, Integer> representativeForInternalConnectionsNodes = new HashMap<>();
        connectedSetsByInternalConnections.forEach(connectedSetByInternalConnections -> {
            int representativeNode = findRepresentativeNode(connectedSetByInternalConnections, nodesOfSwitches);
            connectedSetByInternalConnections.forEach(node -> representativeForInternalConnectionsNodes.put(node, representativeNode));
        });

        return representativeForInternalConnectionsNodes;
    }

    private static List<Set<Integer>> connectedSetsByInternalConnections(VoltageLevel voltageLevel) {
        Graph<Integer, Pair<Integer, Integer>> icGraph = new Pseudograph<>(null, null, false);
        addInternalConnections(voltageLevel, icGraph);
        return new ConnectivityInspector<>(icGraph).connectedSets();
    }

    private static void addInternalConnections(VoltageLevel voltageLevel, Graph<Integer, Pair<Integer, Integer>> icGraph) {
        voltageLevel.getNodeBreakerView().getInternalConnections().forEach(internalConnection -> {
            int node1 = internalConnection.getNode1();
            int node2 = internalConnection.getNode2();
            icGraph.addVertex(node1);
            icGraph.addVertex(node2);
            icGraph.addEdge(node1, node2, Pair.of(node1, node2));
        });
    }

    private static int findRepresentativeNode(Set<Integer> connectedSetByInternalConnections, Set<Integer> nodesOfSwitches) {
        return connectedSetByInternalConnections.stream()
                .filter(nodesOfSwitches::contains)
                .min(Comparator.naturalOrder())
                .orElse(connectedSetByInternalConnections.stream().min(Comparator.naturalOrder()).orElseThrow());
    }

    private static List<Set<Integer>> connectedSetsBySwitchesAndInternalConnections(VoltageLevel voltageLevel) {
        Graph<Integer, Pair<Integer, Integer>> swIcGraph = new Pseudograph<>(null, null, false);

        addInternalConnections(voltageLevel, swIcGraph);

        voltageLevel.getNodeBreakerView().getSwitches().forEach(sw -> {
            int node1 = voltageLevel.getNodeBreakerView().getNode1(sw.getId());
            int node2 = voltageLevel.getNodeBreakerView().getNode2(sw.getId());
            swIcGraph.addVertex(node1);
            swIcGraph.addVertex(node2);
            swIcGraph.addEdge(node1, node2, Pair.of(node1, node2));
        });

        return new ConnectivityInspector<>(swIcGraph).connectedSets();
    }

    private static boolean isRepresented(Map<Integer, Integer> representativeForInternalConnectionsNodes, int node) {
        return Optional.ofNullable(representativeForInternalConnectionsNodes.get(node)).map(representativeNode -> representativeNode != node).orElse(false);
    }

    private static void contextForNonRepresentedNode(VoltageLevel voltageLevel, int node, int busI, Set<Bus> busViewBusesForBusI, String psseSubstationId, ContextExport contextExport) {
        int psseNode = contextExport.getFullExport().getNewPsseNode(psseSubstationId);
        findBusViewNode(voltageLevel, node).ifPresentOrElse(busView -> {
            contextExport.getFullExport().addNodeData(voltageLevel, node, busI, psseNode, busView);
            busViewBusesForBusI.add(busView);
        }, () -> contextExport.getFullExport().addNodeData(voltageLevel, node, busI, psseNode, null));
    }

    private static void contextForRepresentedNode(VoltageLevel voltageLevel, int node, int representativeNode, ContextExport contextExport) {
        int busI = contextExport.getFullExport().getBusI(voltageLevel, representativeNode).orElseThrow();
        int psseNode = contextExport.getFullExport().getPsseNode(voltageLevel, representativeNode).orElseThrow();
        Bus busView = contextExport.getFullExport().getVoltageBus(voltageLevel, representativeNode).orElse(null);

        contextExport.getFullExport().addNodeData(voltageLevel, node, busI, psseNode, busView);
    }

    private static int findPriorityType(Bus busView) {
        int type = findBusViewBusType(busView);
        return switch (type) {
            case 3 -> 0;
            case 2 -> 1;
            case 1 -> 2;
            case 4 -> 3;
            default -> throw new PsseException("Unexpected psse bus type: " + type);
        };
    }

    static void createSubstations(PssePowerFlowModel psseModel, ContextExport contextExport) {
        List<PsseSubstation> psseSubstations = new ArrayList<>();

        contextExport.getFullExport().getSortedPsseSubstationIds().forEach(psseSubstationId -> {
            List<PsseSubstation.PsseSubstationNode> nodes = new ArrayList<>();
            List<PsseSubstation.PsseSubstationSwitchingDevice> switchingDevices = new ArrayList<>();
            List<PsseSubstation.PsseSubstationEquipmentTerminal> equipmentTerminals = new ArrayList<>();

            contextExport.getFullExport().getVoltageLevelSet(psseSubstationId).forEach(voltageLevel -> {
                nodes.addAll(createPsseSubstationNodes(voltageLevel, contextExport));
                switchingDevices.addAll(createPsseSubstationSwitchingDevices(voltageLevel, contextExport));
                equipmentTerminals.addAll(createPsseSubstationEquipmentTerminals(voltageLevel, contextExport));
            });

            PsseSubstation psseSubstation = new PsseSubstation(createPsseSubstationSubstationRecord(psseSubstationId, contextExport),
                    nodes.stream().sorted(Comparator.comparingInt(PsseSubstation.PsseSubstationNode::getNi)).toList(),
                    switchingDevices.stream().sorted(Comparator.comparingInt(PsseSubstation.PsseSubstationSwitchingDevice::getNi)
                            .thenComparingInt(PsseSubstation.PsseSubstationSwitchingDevice::getNj)
                            .thenComparing(PsseSubstation.PsseSubstationSwitchingDevice::getCkt)).toList(),
                    equipmentTerminals.stream().sorted(Comparator.comparingInt(PsseSubstation.PsseSubstationEquipmentTerminal::getI)
                                    .thenComparingInt(PsseSubstation.PsseSubstationEquipmentTerminal::getNi)
                                    .thenComparingInt(PsseSubstation.PsseSubstationEquipmentTerminal::getJ)
                                    .thenComparingInt(PsseSubstation.PsseSubstationEquipmentTerminal::getK)
                                    .thenComparing(PsseSubstation.PsseSubstationEquipmentTerminal::getId)
                                    .thenComparing(PsseSubstation.PsseSubstationEquipmentTerminal::getType)).toList());
            psseSubstations.add(psseSubstation);
        });

        psseModel.addSubstations(psseSubstations);
    }

    private static PsseSubstation.PsseSubstationRecord createPsseSubstationSubstationRecord(String psseSubstationId, ContextExport contextExport) {
        PsseSubstation.PsseSubstationRecord substationRecord = new PsseSubstation.PsseSubstationRecord();
        substationRecord.setIs(contextExport.getFullExport().getNewPsseSubstationIs());
        substationRecord.setName(psseSubstationId);
        substationRecord.setLati(0.0);
        substationRecord.setLong(0.0);
        substationRecord.setSrg(0.1);
        return substationRecord;
    }

    private static List<PsseSubstation.PsseSubstationNode> createPsseSubstationNodes(VoltageLevel voltageLevel, ContextExport contextExport) {
        List<PsseSubstation.PsseSubstationNode> nodes = new ArrayList<>();

        for (int node : voltageLevel.getNodeBreakerView().getNodes()) {
            contextExport.getFullExport().getBusI(voltageLevel, node).ifPresent(busI -> {
                int ni = contextExport.getFullExport().getPsseNode(voltageLevel, node).orElseThrow();
                Bus voltageBusView = contextExport.getFullExport().getVoltageBus(voltageLevel, node).orElse(null);
                boolean isDeEnergized = contextExport.getFullExport().isDeEnergized(voltageLevel, node);

                PsseSubstation.PsseSubstationNode psseNode = new PsseSubstationNode();
                psseNode.setNi(ni);
                psseNode.setName(getNodeId(voltageLevel, node));
                psseNode.setI(busI);
                psseNode.setStatus(isDeEnergized ? 0 : 1);
                psseNode.setVm(getVm(voltageBusView));
                psseNode.setVa(getVa(voltageBusView));

                nodes.add(psseNode);
            });
        }

        return nodes;
    }

    // ckt must be unique inside the voltageLevel
    private static List<PsseSubstation.PsseSubstationSwitchingDevice> createPsseSubstationSwitchingDevices(VoltageLevel voltageLevel, ContextExport contextExport) {
        List<PsseSubstation.PsseSubstationSwitchingDevice> switchingDevices = new ArrayList<>();
        voltageLevel.getSwitches().forEach(sw -> {
            int ni = contextExport.getFullExport().getPsseNode(voltageLevel, sw.getVoltageLevel().getNodeBreakerView().getNode1(sw.getId())).orElseThrow();
            int nj = contextExport.getFullExport().getPsseNode(voltageLevel, sw.getVoltageLevel().getNodeBreakerView().getNode2(sw.getId())).orElseThrow();
            PsseSubstation.PsseSubstationSwitchingDevice switchingDevice = new PsseSubstationSwitchingDevice();
            switchingDevice.setNi(ni);
            switchingDevice.setNj(nj);
            switchingDevice.setCkt(contextExport.getFullExport().getEquipmentCkt(voltageLevel, sw.getId(), ni, nj));
            switchingDevice.setName(sw.getId());
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
            Identifiable<?> identifiable = getIdentifiable(voltageLevel, equipmentId);
            String type = getPsseEquipmentType(identifiable);
            List<Terminal> terminals = getEquipmentTerminals(voltageLevel, equipmentId);

            getNodesInsideVoltageLevelPreservingOrder(voltageLevel, terminals, contextExport).forEach(nodeBusR -> {
                List<Integer> otherBuses = getTwoOtherBusesPreservingOrder(identifiable, terminals, nodeBusR, contextExport);
                String ckt = contextExport.getFullExport().getEquipmentCkt(equipmentId, identifiable.getType(), nodeBusR.busI(), otherBuses.get(0), otherBuses.get(1));

                PsseSubstation.PsseSubstationEquipmentTerminal equipmentTerminal = new PsseSubstationEquipmentTerminal();
                equipmentTerminal.setNi(nodeBusR.psseNode);
                equipmentTerminal.setType(type);
                equipmentTerminal.setId(getEquipmentTerminalId(type, identifiable, ckt));
                equipmentTerminal.setI(nodeBusR.busI);
                equipmentTerminal.setJ(otherBuses.get(0));
                equipmentTerminal.setK(otherBuses.get(1));

                equipmentTerminals.add(equipmentTerminal);
            });
        });
        return equipmentTerminals;
    }

    private static String getEquipmentTerminalId(String type, Identifiable<?> identifiable, String ckt) {
        return switch (type) {
            case "A" -> extractFactsDeviceName(identifiable.getId());
            case "D" -> extractTwoTerminalDcName(identifiable.getId());
            case "V" -> extractVscDcTransmissionLineName(identifiable.getId());
            default -> ckt;
        };
    }

    private static List<NodeBusR> getNodesInsideVoltageLevelPreservingOrder(VoltageLevel voltageLevel, List<Terminal> terminals, ContextExport contextExport) {
        return terminals.stream()
                .filter(terminal -> terminal.getVoltageLevel().equals(voltageLevel))
                .map(terminal -> findNodeBusR(terminal, contextExport)).toList();
    }

    private static NodeBusR findNodeBusR(Terminal terminal, ContextExport contextExport) {
        int node = terminal.getNodeBreakerView().getNode();
        int busI = contextExport.getFullExport().getBusI(terminal.getVoltageLevel(), node).orElseThrow();
        int psseNode = contextExport.getFullExport().getPsseNode(terminal.getVoltageLevel(), node).orElseThrow();
        return new NodeBusR(terminal.getVoltageLevel(), node, psseNode, busI);
    }

    private record NodeBusR(VoltageLevel voltageLevel, int node, int psseNode, int busI) {
        boolean equals(NodeBusR other) {
            return voltageLevel().equals(other.voltageLevel()) && node() == other.node();
        }
    }

    private static List<Integer> getTwoOtherBusesPreservingOrder(Identifiable<?> identifiable, List<Terminal> terminals, NodeBusR nodeBusR, ContextExport contextExport) {
        List<Integer> buses = new ArrayList<>();
        if (identifiable.getType() == IdentifiableType.DANGLING_LINE) {
            // busJ associated with boundary side of the dangling lines
            buses.add(contextExport.getFullExport().getBusI((DanglingLine) identifiable).orElseThrow());
        } else {
            terminals.forEach(terminal -> {
                if (contextExport.getFullExport().isExportedAsNodeBreaker(terminal.getVoltageLevel())) {
                    NodeBusR otherNodeBusR = findNodeBusR(terminal, contextExport);
                    if (!nodeBusR.equals(otherNodeBusR)) {
                        buses.add(otherNodeBusR.busI);
                    }
                } else {
                    Bus busView = getTerminalBusView(terminal);
                    buses.add(contextExport.getFullExport().getBusI(busView).orElseThrow());
                }
            });
        }
        completeWithZerosUntilTwoBuses(identifiable.getId(), buses);
        return buses;
    }

    private static void completeWithZerosUntilTwoBuses(String equipmentId, List<Integer> buses) {
        if (buses.isEmpty()) {
            buses.add(0);
            buses.add(0);
        } else if (buses.size() == 1) {
            buses.add(0);
        } else if (buses.size() > 2) {
            throw new PsseException("Unexpected number of buses for equipmentId: " + equipmentId);
        }
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
            if (voltageLevel.getTopologyKind() == TopologyKind.NODE_BREAKER) {
                Set<Integer> buses = new HashSet<>(extractBusesFromVoltageLevelId(voltageLevel.getId()));
                contextExport.getUpdateExport().getPsseSubstation(voltageLevel).ifPresent(psseSubstation -> updateSubstation(voltageLevel, psseSubstation, buses, contextExport));
            }
        });
    }

    private static void updateSubstation(VoltageLevel voltageLevel, PsseSubstation psseSubstation, Set<Integer> busesSet, ContextExport contextExport) {
        Set<PsseSubstationNode> psseNodeSet = psseSubstation.getNodes().stream().filter(psseNode -> busesSet.contains(psseNode.getI())).collect(Collectors.toSet());
        psseNodeSet.forEach(psseSubstationNode -> {
            Optional<Bus> busView = contextExport.getUpdateExport().getBusView(voltageLevel, psseSubstationNode.getNi());
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
