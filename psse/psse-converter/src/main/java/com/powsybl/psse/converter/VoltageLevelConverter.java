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
                    Bus busView = findBusViewFromNode(voltageLevel, psseNode.getNi());
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
            int busI = findBusI(busBreakerViewId, contextExport);
            contextExport.getLinkExport().addBusViewBusIDoubleLink(bus, busI);
        });
    }

    private static int findBusI(String busBreakerViewId, ContextExport contextExport) {
        if (contextExport.isFullExport()) {
            return contextExport.getFullExport().getNewPsseBusI();
        } else {
            return extractBusNumber(busBreakerViewId).orElseThrow();
        }
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

    private static void createNodeBreakerContextExport(VoltageLevel voltageLevel, ContextExport contextExport) {
        List<Set<Integer>> connectedSetsByInternalConnections = connectedSetsByInternalConnections(voltageLevel);
        findAndSetRepresentativeNodes(voltageLevel, connectedSetsByInternalConnections, contextExport);
        List<Set<Integer>> connectedSetsBySwitches = connectedSetsBySwitches(voltageLevel, connectedSetsByInternalConnections, contextExport);

        PsseSubstation psseSubstation = findPsseSubstation(voltageLevel, contextExport);
        connectedSetsBySwitches.forEach(connectedSetBySwitches -> {
            int busI = findBusI(connectedSetBySwitches, psseSubstation, contextExport);
            Set<Bus> busViewBuses = findBusViewBuses(voltageLevel, connectedSetBySwitches);
            connectedSetBySwitches.forEach(node -> contextExport.getLinkExport().addNodeBusILink(voltageLevel, convertToPsseNode(node), busI));

            if (!busViewBuses.isEmpty()) {
                Bus selectedBus = busViewBuses.stream().min(Comparator.comparingInt(busView -> findPriorityType(voltageLevel, busView))).orElseThrow();
                contextExport.getLinkExport().addBusViewBusIDoubleLink(selectedBus, busI);
            }
        });

        voltageLevel.getDanglingLineStream().filter(danglingLine -> !danglingLine.isPaired()).forEach(danglingLine -> contextExport.getLinkExport().addDanglingLineBusILink(danglingLine, contextExport.getFullExport().getNewPsseBusI()));
    }

    private static List<Set<Integer>> connectedSetsByInternalConnections(VoltageLevel voltageLevel) {
        Graph<Integer, Pair<Integer, Integer>> icGraph = new Pseudograph<>(null, null, false);

        voltageLevel.getNodeBreakerView().getInternalConnections().forEach(internalConnection -> {
            int node1 = internalConnection.getNode1();
            int node2 = internalConnection.getNode2();
            icGraph.addVertex(node1);
            icGraph.addVertex(node2);
            icGraph.addEdge(node1, node2, Pair.of(node1, node2));
        });

        return new ConnectivityInspector<>(icGraph).connectedSets();
    }

    private static void findAndSetRepresentativeNodes(VoltageLevel voltageLevel, List<Set<Integer>> connectedSetsByInternalConnections, ContextExport contextExport) {
        Set<Integer> nodesOfSwitches = new HashSet<>();
        voltageLevel.getNodeBreakerView().getSwitches().forEach(sw -> {
            nodesOfSwitches.add(voltageLevel.getNodeBreakerView().getNode1(sw.getId()));
            nodesOfSwitches.add(voltageLevel.getNodeBreakerView().getNode2(sw.getId()));
        });

        connectedSetsByInternalConnections.forEach(connectedSetByInternalConnections -> {
            int representativeNode = findRepresentativeNode(connectedSetByInternalConnections, nodesOfSwitches);
            contextExport.getFullExport().addInternalConnectionNodeRepresentativeNode(voltageLevel, connectedSetByInternalConnections, representativeNode);
        });
    }

    private static int findRepresentativeNode(Set<Integer> connectedSetByInternalConnections, Set<Integer> nodesOfSwitches) {
        List<Integer> representativeNodes = connectedSetByInternalConnections.stream().filter(nodesOfSwitches::contains).toList();
        return representativeNodes.isEmpty() ? connectedSetByInternalConnections.stream().min(Comparator.naturalOrder()).orElseThrow() : representativeNodes.stream().min(Comparator.naturalOrder()).orElseThrow();
    }

    private static List<Set<Integer>> connectedSetsBySwitches(VoltageLevel voltageLevel, List<Set<Integer>> connectedSetsByInternalConnections, ContextExport contextExport) {
        List<Integer> nodesConnectedByInternalConnections = connectedSetsByInternalConnections.stream().flatMap(Collection::stream).toList();

        Graph<Integer, Pair<Integer, Integer>> swGraph = new Pseudograph<>(null, null, false);

        // Add buses
        connectedSetsByInternalConnections.forEach(connectedSetByInternalConnections -> {
            int representativeNode = contextExport.getFullExport().getRepresentativeNode(voltageLevel, connectedSetByInternalConnections.stream().findFirst().orElseThrow());
            swGraph.addVertex(representativeNode);
        });
        for (int node : voltageLevel.getNodeBreakerView().getNodes()) {
            if (!nodesConnectedByInternalConnections.contains(node)) {
                swGraph.addVertex(node);
            }
        }

        // Add switches
        voltageLevel.getNodeBreakerView().getSwitches().forEach(sw -> {
            int node1 = contextExport.getFullExport().getRepresentativeNode(voltageLevel, voltageLevel.getNodeBreakerView().getNode1(sw.getId()));
            int node2 = contextExport.getFullExport().getRepresentativeNode(voltageLevel, voltageLevel.getNodeBreakerView().getNode2(sw.getId()));
            swGraph.addEdge(node1, node2, Pair.of(node1, node2));
        });

        return new ConnectivityInspector<>(swGraph).connectedSets();
    }

    private static PsseSubstation findPsseSubstation(VoltageLevel voltageLevel, ContextExport contextExport) {
        return contextExport.isFullExport() ? null : contextExport.getUpdateExport().getPsseSubstation(voltageLevel).orElseThrow();
    }

    private static int findBusI(Set<Integer> connectedSetBySwitches, PsseSubstation psseSubstation, ContextExport contextExport) {
        if (psseSubstation != null) {
            Set<Integer> busesI = psseSubstation.getNodes().stream()
                    .filter(psseNode -> connectedSetBySwitches.contains(psseNode.getNi()))
                    .map(PsseSubstationNode::getI)
                    .collect(Collectors.toSet());
            if (busesI.size() == 1) {
                return busesI.iterator().next();
            } else {
                throw new PsseException("More than one busI assigned to one connectivitySet in psseSubstation " + psseSubstation.getName());
            }
        } else {
            return contextExport.getFullExport().getNewPsseBusI();
        }
    }

    private static Set<Bus> findBusViewBuses(VoltageLevel voltageLevel, Set<Integer> connectedSetBySwitches) {
        return connectedSetBySwitches.stream()
                .map(node -> findBusViewFromNode(voltageLevel, node)).collect(Collectors.toSet())
                .stream().filter(Objects::nonNull).collect(Collectors.toSet());
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

    static void updateSubstations(Network network, ContextExport contextExport) {
        network.getVoltageLevels().forEach(voltageLevel -> {
            if (exportVoltageLevelAsNodeBreaker(voltageLevel)) {
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
