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
            nodeBreakerImport.addSlackControl(psseBus.getI(), voltageLevelId, findSlackNode(nodeBreakerValidation, psseBus.getI()));
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
                    .setKind(findSwitchingKind(switchingDevice.getType()))
                    .setOpen(switchingDevice.getStatus() != 1)
                    .add();
        }

        // Define where equipment must be connected
        int lastNode = substation.getNodes().stream().map(PsseSubstationNode::getNi).max(Comparator.naturalOrder()).orElseThrow();
        Set<Integer> nodesWithEquipment = new HashSet<>();
        for (PsseSubstationEquipmentTerminal equipmentTerminal : substation.getEquipmentTerminals()) {
            String equipmentId = getNodeBreakerEquipmentId(equipmentTerminal.getType(), equipmentTerminal.getI(), equipmentTerminal.getJ(), equipmentTerminal.getK(), equipmentTerminal.getId());
            int bus = findBus(equipmentTerminal, buses);
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

    private static SwitchKind findSwitchingKind(int type) {
        return type == 2 ? SwitchKind.BREAKER : SwitchKind.DISCONNECTOR;
    }

    private static int findBus(PsseSubstationEquipmentTerminal equipmentTerminal, Set<Integer> buses) {
        return buses.stream().filter(bus -> bus == equipmentTerminal.getI() || bus == equipmentTerminal.getJ() || bus == equipmentTerminal.getK()).min(Comparator.naturalOrder()).orElseThrow();
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
        int maxPsseBus = psseModel.getBuses().stream().map(PsseBus::getI).max(Comparator.naturalOrder()).orElse(0);
        int maxPsseSubstationIs = psseModel.getSubstations().stream().map(PsseSubstation::getIs).max(Comparator.naturalOrder()).orElse(0);
        ContextExport contextExport = new ContextExport(maxPsseBus, maxPsseSubstationIs);

        // First busBreaker voltageLevels
        List<VoltageLevel> busBreakerVoltageLevels = network.getVoltageLevelStream().filter(vl -> vl.getTopologyKind().equals(TopologyKind.BUS_BREAKER)).toList();
        busBreakerVoltageLevels.forEach(voltageLevel -> createBusBreakerExport(voltageLevel, contextExport));

        // Always after busBreaker voltageLevels
        findNodeBreakerVoltageLevelsAndMapPsseSubstation(network, psseModel, contextExport);
        contextExport.getNodeBreakerExport().getVoltageLevels().forEach(voltageLevel -> {
            Optional<PsseSubstation> psseSubstation = contextExport.getNodeBreakerExport().getPsseSubstationMappedToVoltageLevel(voltageLevel);
            if (psseSubstation.isPresent()) {
                createNodeBreakerExport(voltageLevel, psseSubstation.get(), contextExport);
            } else {
                createNodeBreakerExport(voltageLevel, contextExport);
            }
        });

        return contextExport;
    }

    private static void findNodeBreakerVoltageLevelsAndMapPsseSubstation(Network network, PssePowerFlowModel psseModel, ContextExport contextExport) {
        psseModel.getSubstations().forEach(psseSubstation -> {
            VoltageLevel voltageLevel = findVoltageLevel(network, psseSubstation);
            if (voltageLevel != null && voltageLevel.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
                contextExport.getNodeBreakerExport().addVoltageLevel(voltageLevel, psseSubstation);
            }
        });
        List<VoltageLevel> voltageLevelList = contextExport.getNodeBreakerExport().getVoltageLevels();
        List<VoltageLevel> nodeBreakerVoltageLevelsNotMapped = network.getVoltageLevelStream().filter(voltageLevel -> voltageLevel.getTopologyKind().equals(TopologyKind.NODE_BREAKER) && !voltageLevelList.contains(voltageLevel)).toList();
        contextExport.getNodeBreakerExport().addVoltageLevels(nodeBreakerVoltageLevelsNotMapped);
    }

    private static void createNodeBreakerExport(VoltageLevel voltageLevel, PsseSubstation psseSubstation, ContextExport contextExport) {
        List<Integer> nodesAfterExcludingInternalConnections = findNodesAfterExcludingInternalConnections(voltageLevel, contextExport);
        Map<String, List<Integer>> busViewBusIdNodeList = findBusViewBusIdNodeList(voltageLevel, nodesAfterExcludingInternalConnections);

        // First, buses with more nodes
        Comparator<String> comp = Comparator.comparingInt((String key) -> busViewBusIdNodeList.get(key).size())
                .reversed()
                .thenComparing(Comparator.naturalOrder());

        busViewBusIdNodeList.keySet().stream().sorted(comp).forEach(busViewBusId -> {
            Optional<Integer> bus = mapPsseBus(psseSubstation, busViewBusIdNodeList.get(busViewBusId), contextExport);
            int busType = findBusViewBusType(voltageLevel, busViewBusId);
            if (bus.isPresent()) {
                contextExport.getNodeBreakerExport().addNodes(busViewBusId, voltageLevel, busViewBusIdNodeList.get(busViewBusId), bus.get(), busType);
            } else {
                int newBus = contextExport.getNewPsseBusI();
                contextExport.getNodeBreakerExport().addNodes(busViewBusId, voltageLevel, busViewBusIdNodeList.get(busViewBusId), newBus, selectCopyBus(psseSubstation), busType);
            }
        });

        List<Integer> isolatedNodes = findIsolatedNodes(nodesAfterExcludingInternalConnections, busViewBusIdNodeList);
        isolatedNodes.forEach(node -> {
            Optional<Integer> bus = mapPsseBus(psseSubstation, Collections.singletonList(node), contextExport);
            if (bus.isPresent()) {
                contextExport.getNodeBreakerExport().addIsolatedNode(voltageLevel, node, bus.get());
            } else {
                int newBus = contextExport.getNewPsseBusI();
                contextExport.getNodeBreakerExport().addIsolatedNode(voltageLevel, node, newBus, selectCopyBus(psseSubstation));
            }
        });

        getEquipmentListToBeExported(voltageLevel).forEach(equipmentId ->
                getEquipmentTerminals(voltageLevel, equipmentId).forEach(terminal -> {
                    int node = contextExport.getNodeBreakerExport().getSelectedNode(voltageLevel, terminal.getNodeBreakerView().getNode()).orElseThrow();
                    int busI = contextExport.getNodeBreakerExport().getNodeBusI(voltageLevel, node).orElseThrow();
                    int end = getSideEnd(getTerminalSide(terminal));
                    contextExport.addEquipmentEnd(equipmentId, voltageLevel, node, busI, end);
                }));
    }

    private static void createNodeBreakerExport(VoltageLevel voltageLevel, ContextExport contextExport) {
        createNodeBreakerExport(voltageLevel, null, contextExport);
    }

    private static void createBusBreakerExport(VoltageLevel voltageLevel, ContextExport contextExport) {
        Map<String, List<String>> busViewBusBusBreakerViewBuses = new HashMap<>();
        voltageLevel.getBusBreakerView().getBuses().forEach(busBreakerViewBus -> busViewBusBusBreakerViewBuses.computeIfAbsent(voltageLevel.getBusView().getMergedBus(busBreakerViewBus.getId()).getId(), k -> new ArrayList<>()).add(busBreakerViewBus.getId()));

        voltageLevel.getBusView().getBuses().forEach(bus -> {
            int busType = findBusViewBusType(voltageLevel, bus.getId());
            String busBreakerViewId = findBusBreakerViewBusId(bus.getId(), busViewBusBusBreakerViewBuses);
            OptionalInt busI = extractBusNumber(busBreakerViewId);
            if (busI.isEmpty()) {
                contextExport.getBusBreakerExport().addBus(bus.getId(), contextExport.getNewPsseBusI(), busType);
            } else {
                contextExport.getBusBreakerExport().addBus(bus.getId(), busI.getAsInt(), busType);
            }
        });

        getEquipmentListToBeExported(voltageLevel).forEach(equipmentId ->
                getEquipmentTerminals(voltageLevel, equipmentId).forEach(terminal -> {
                    Bus bus = getTerminalBus(terminal);
                    int busI = contextExport.getBusBreakerExport().getBusBusI(bus.getId()).orElseThrow();
                    int end = getSideEnd(terminal.getSide());
                    contextExport.addEquipmentEnd(equipmentId, voltageLevel, busI, end);
                }));
    }

    private static List<Integer> findNodesAfterExcludingInternalConnections(VoltageLevel voltageLevel, ContextExport contextExport) {
        Graph<Integer, Pair<Integer, Integer>> inGraph = new Pseudograph<>(null, null, false);

        for (int node : voltageLevel.getNodeBreakerView().getNodes()) {
            inGraph.addVertex(node);
        }
        voltageLevel.getNodeBreakerView().getInternalConnections().forEach(internalConnection -> inGraph.addEdge(internalConnection.getNode1(), internalConnection.getNode2(), Pair.of(internalConnection.getNode1(), internalConnection.getNode2())));

        List<Set<Integer>> componentsSetList = new ConnectivityInspector<>(inGraph).connectedSets();

        componentsSetList.forEach(componetSet -> contextExport.getNodeBreakerExport().addSelectedNode(voltageLevel, componetSet, componetSet.stream().min(Comparator.naturalOrder()).orElseThrow()));
        return contextExport.getNodeBreakerExport().getSelectedNodes(voltageLevel);
    }

    private static Map<String, List<Integer>> findBusViewBusIdNodeList(VoltageLevel voltageLevel, List<Integer> nodesAfterExcludingInternalConnections) {
        Map<String, List<Integer>> busViewBusIdNodeList = new HashMap<>();

        nodesAfterExcludingInternalConnections.forEach(node -> {
            Terminal terminal = findTerminalNode(voltageLevel, node);
            if (terminal != null) {
                Bus bus = terminal.getBusView().getBus();
                if (bus != null) {
                    busViewBusIdNodeList.computeIfAbsent(bus.getId(), k -> new ArrayList<>()).add(node);
                }
            }
        });
        return busViewBusIdNodeList;
    }

    private static List<Integer> findIsolatedNodes(List<Integer> nodesAfterExcludingInternalConnections, Map<String, List<Integer>> busViewBusIdNodeList) {
        List<Integer> psseBusesIAssociatedWithBus = busViewBusIdNodeList.values().stream().flatMap(List::stream).toList();
        return nodesAfterExcludingInternalConnections.stream().filter(node -> !psseBusesIAssociatedWithBus.contains(node)).toList();
    }

    private static Optional<Integer> mapPsseBus(PsseSubstation psseSubstation, List<Integer> nodes, ContextExport contextExport) {
        if (psseSubstation == null) {
            return Optional.empty();
        }

        Comparator<Integer> comp = Comparator.comparingInt((Integer busI) -> nodesAssociatedWithBus(psseSubstation, nodes, busI).size())
                .reversed()
                .thenComparing(Comparator.naturalOrder());

        return psseSubstation.getNodes().stream()
                .map(PsseSubstationNode::getI).filter(contextExport::isFreePsseBusI)
                .collect(Collectors.toSet())
                .stream().min(comp);
    }

    private static int findBusViewBusType(VoltageLevel voltageLevel, String busViewBusId) {
        Bus bus = voltageLevel.getBusView().getBus(busViewBusId);
        if (bus == null) { // unexpected
            return 4;
        }
        SlackTerminal slackTerminal = voltageLevel.getExtension(SlackTerminal.class);
        if (slackTerminal != null
                && slackTerminal.getTerminal().getBusView().getBus() != null
                && bus.getId().equals(slackTerminal.getTerminal().getBusView().getBus().getId())) {
            return 3;
        }
        return bus.getGeneratorStream().anyMatch(VoltageLevelConverter::withLocalRegulatingControl) ? 2 : 1;
    }

    private static boolean withLocalRegulatingControl(Generator generator) {
        return generator.isVoltageRegulatorOn()
                && generator.getTerminal().getBusView().getBus().equals(generator.getRegulatingTerminal().getBusView().getBus());
    }

    private static Integer selectCopyBus(PsseSubstation psseSubstation) {
        return psseSubstation != null ? psseSubstation.getNodes().stream().map(PsseSubstationNode::getI).min(Comparator.naturalOrder()).orElseThrow() : null;
    }

    private static List<Integer> nodesAssociatedWithBus(PsseSubstation psseSubstation, List<Integer> nodes, int busI) {
        return psseSubstation.getNodes().stream().filter(n -> n.getI() == busI && nodes.contains(n.getNi())).map(PsseSubstationNode::getNi).toList();
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

    private static int getSideEnd(ThreeSides side) {
        return side == null ? 1 : side.getNum();
    }

    private static VoltageLevel findVoltageLevel(Network network, PsseSubstation psseSubstation) {
        Set<Integer> busesSet = psseSubstation.getNodes().stream().map(PsseSubstationNode::getI).collect(Collectors.toSet());
        String voltageLevelId = getVoltageLevelId(busesSet);
        return network.getVoltageLevel(voltageLevelId);
    }

    static void createAndUpdateSubstations(PssePowerFlowModel psseModel, ContextExport contextExport) {
        contextExport.getNodeBreakerExport().getVoltageLevels().forEach(voltageLevel -> {
            Optional<PsseSubstation> psseSubstation = contextExport.getNodeBreakerExport().getPsseSubstationMappedToVoltageLevel(voltageLevel);
            if (psseSubstation.isPresent()) {
                updateSubstation(voltageLevel, psseSubstation.get(), contextExport);
            } else {
                psseModel.addSubstations(Collections.singletonList(createPsseSubstation(voltageLevel, contextExport)));
            }
        });
    }

    private static void updateSubstation(VoltageLevel voltageLevel, PsseSubstation psseSubstation, ContextExport contextExport) {
        psseSubstation.getNodes().forEach(psseSubstationNode -> {
            int bus = contextExport.getNodeBreakerExport().getNodeBusI(voltageLevel, psseSubstationNode.getNi()).orElseThrow();
            boolean inService = contextExport.getNodeBreakerExport().isNodeInService(voltageLevel, psseSubstationNode.getNi()).orElseThrow();
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

        // Always are created to avoid changing the id of lines, twoWindingsTransformers and threeWindingsTransformers
        // the id must be created with the sorted buses if we want to obtain the equipment with the equipmentTerminal data
        psseSubstation.getEquipmentTerminals().clear();
        psseSubstation.getEquipmentTerminals().addAll(createPsseSubstationEquipmentTerminals(voltageLevel, contextExport));
    }

    private static PsseSubstation createPsseSubstation(VoltageLevel voltageLevel, ContextExport contextExport) {
        return new PsseSubstation(createPsseSubstationSubstationRecord(voltageLevel, contextExport),
                createPsseSubstationNodes(voltageLevel, contextExport),
                createPsseSubstationSwitchingDevices(voltageLevel, contextExport),
                createPsseSubstationEquipmentTerminals(voltageLevel, contextExport));
    }

    private static PsseSubstation.PsseSubstationRecord createPsseSubstationSubstationRecord(VoltageLevel voltageLevel, ContextExport contextExport) {
        PsseSubstation.PsseSubstationRecord substationRecord = new PsseSubstation.PsseSubstationRecord();
        substationRecord.setIs(contextExport.getNewPsseSubstationIs());
        substationRecord.setName(voltageLevel.getId());
        substationRecord.setLati(0.0);
        substationRecord.setLong(0.0);
        substationRecord.setSrg(0.1);
        return substationRecord;
    }

    private static List<PsseSubstation.PsseSubstationNode> createPsseSubstationNodes(VoltageLevel voltageLevel, ContextExport contextExport) {
        List<PsseSubstation.PsseSubstationNode> nodes = new ArrayList<>();
        contextExport.getNodeBreakerExport().getNodes(voltageLevel).forEach(voltageLevelNodeId -> {
            PsseSubstation.PsseSubstationNode node = new PsseSubstationNode();
            int ni = contextExport.getNodeBreakerExport().getNodeNi(voltageLevelNodeId).orElseThrow();
            int busI = contextExport.getNodeBreakerExport().getNodeBusI(voltageLevelNodeId).orElseThrow();
            boolean inService = contextExport.getNodeBreakerExport().getNodeIsInService(voltageLevelNodeId).orElseThrow();
            String busViewBusId = contextExport.getNodeBreakerExport().getNodeBusViewBusId(voltageLevelNodeId).orElse(null);
            Bus bus = busViewBusId != null ? voltageLevel.getBusView().getBus(busViewBusId) : null;

            node.setNi(ni);
            node.setName(voltageLevelNodeId);
            node.setI(busI);
            node.setStatus(inService ? 1 : 0);
            node.setVm(getVm(bus));
            node.setVa(getVa(bus));

            nodes.add(node);
        });
        return nodes;
    }

    // ckt must be unique inside the voltageLevel
    private static List<PsseSubstation.PsseSubstationSwitchingDevice> createPsseSubstationSwitchingDevices(VoltageLevel voltageLevel, ContextExport contextExport) {
        List<PsseSubstation.PsseSubstationSwitchingDevice> switchingDevices = new ArrayList<>();

        voltageLevel.getSwitches().forEach(sw -> {
            int ni = contextExport.getNodeBreakerExport().getSelectedNode(voltageLevel, sw.getVoltageLevel().getNodeBreakerView().getNode1(sw.getId())).orElseThrow();
            int nj = contextExport.getNodeBreakerExport().getSelectedNode(voltageLevel, sw.getVoltageLevel().getNodeBreakerView().getNode2(sw.getId())).orElseThrow();
            PsseSubstation.PsseSubstationSwitchingDevice switchingDevice = new PsseSubstationSwitchingDevice();
            switchingDevice.setNi(ni);
            switchingDevice.setNj(nj);
            switchingDevice.setCkt(contextExport.getEquipmentCkt(voltageLevel, sw.getId(), IdentifiableType.SWITCH, ni, nj));
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

        getEquipmentListToBeExported(voltageLevel).forEach(equipmentId -> getEquipmentNodes(voltageLevel, equipmentId).forEach(node -> {
            int selectedNode = contextExport.getNodeBreakerExport().getSelectedNode(voltageLevel, node).orElseThrow();
            List<Integer> busesList = contextExport.getEquipmentBusesList(equipmentId, voltageLevel, selectedNode);
            if (busesList.size() == 3) {
                Identifiable<?> identifiable = getIdentifiable(voltageLevel, equipmentId);
                PsseSubstation.PsseSubstationEquipmentTerminal equipmentTerminal = new PsseSubstationEquipmentTerminal();
                equipmentTerminal.setNi(selectedNode);
                equipmentTerminal.setType(getPsseEquipmentType(identifiable));
                equipmentTerminal.setId(contextExport.getEquipmentCkt(equipmentId, identifiable.getType(), busesList.get(0), busesList.get(1), busesList.get(2)));
                equipmentTerminal.setI(busesList.get(0));
                equipmentTerminal.setJ(busesList.get(1));
                equipmentTerminal.setK(busesList.get(2));

                equipmentTerminals.add(equipmentTerminal);
            } else {
                throw new PsseException("Unexpected size of the busesList. Must be 3 and it is " + busesList.size());
            }
        }));
        return equipmentTerminals;
    }

    private static Identifiable<?> getIdentifiable(VoltageLevel voltageLevel, String identifiableId) {
        Identifiable<?> identifiable = voltageLevel.getNetwork().getIdentifiable(identifiableId);
        if (identifiable != null) {
            return identifiable;
        } else {
            throw new PsseException("Unexpected identifiableId: " + identifiableId);
        }
    }

    private final PsseBus psseBus;
    private final PerUnitContext perUnitContext;
    private final NodeBreakerValidation nodeBreakerValidation;
    private final NodeBreakerImport nodeBreakerImport;
}
