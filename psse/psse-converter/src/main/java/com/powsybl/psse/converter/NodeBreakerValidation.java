/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.psse.model.pf.*;
import com.powsybl.psse.model.pf.PsseSubstation.PsseSubstationNode;
import com.powsybl.psse.model.PsseVersion;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.Pseudograph;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.psse.converter.AbstractConverter.PsseEquipmentType.*;
import static com.powsybl.psse.converter.AbstractConverter.getNodeBreakerEquipmentId;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
final class NodeBreakerValidation {
    private final boolean ignoreNodeBreakerTopology;
    private final Map<Integer, List<PsseSubstation>> busSubstations;
    private final Map<Integer, List<String>> busEquipmentTerminals;
    private final Map<Integer, List<NodeBreakerControl>> busControls;

    NodeBreakerValidation(boolean ignoreNodeBreakerTopology) {
        this.ignoreNodeBreakerTopology = ignoreNodeBreakerTopology;
        this.busSubstations = new HashMap<>();
        this.busEquipmentTerminals = new HashMap<>();
        this.busControls = new HashMap<>();
    }

    Optional<PsseSubstation> getSubstationIfOnlyOneExists(Set<Integer> buses) {
        Set<PsseSubstation> psseSubstations = buses.stream()
                .map(this::getSubstationIfOnlyOneExists)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        return psseSubstations.size() == 1 ? Optional.of(psseSubstations.iterator().next()) : Optional.empty();
    }

    // nodeBreaker topology of a bus can only be defined in one substationData
    Optional<PsseSubstation> getSubstationIfOnlyOneExists(int bus) {
        if (busSubstations.containsKey(bus) && busSubstations.get(bus).size() == 1) {
            return Optional.of(busSubstations.get(bus).iterator().next());
        } else {
            return Optional.empty();
        }
    }

    List<NodeBreakerControl> getControls(int bus) {
        return busControls.containsKey(bus) ? busControls.get(bus) : new ArrayList<>();
    }

    void fill(PssePowerFlowModel pssePowerFlowModel, PsseVersion version) {
        if (version.major() != V35) {
            return;
        }

        pssePowerFlowModel.getSubstations().forEach(psseSubstation -> {
            Set<Integer> buses = psseSubstation.getNodes().stream().map(PsseSubstationNode::getI).collect(Collectors.toSet());
            buses.forEach(bus -> busSubstations.computeIfAbsent(bus, k -> new ArrayList<>()).add(psseSubstation));
        });

        pssePowerFlowModel.getLoads().forEach(psseLoad -> {
            String id = getNodeBreakerEquipmentId(PSSE_LOAD, psseLoad.getI(), psseLoad.getId());
            busEquipmentTerminals.computeIfAbsent(psseLoad.getI(), k -> new ArrayList<>()).add(id);
        });
        pssePowerFlowModel.getFixedShunts().forEach(psseFixedShunt -> {
            String id = getNodeBreakerEquipmentId(PSSE_FIXED_SHUNT, psseFixedShunt.getI(), psseFixedShunt.getId());
            busEquipmentTerminals.computeIfAbsent(psseFixedShunt.getI(), k -> new ArrayList<>()).add(id);
        });
        pssePowerFlowModel.getGenerators().forEach(psseGenerator -> {
            String id = getNodeBreakerEquipmentId(PSSE_GENERATOR, psseGenerator.getI(), psseGenerator.getId());
            busEquipmentTerminals.computeIfAbsent(psseGenerator.getI(), k -> new ArrayList<>()).add(id);
            if (psseGenerator.getIreg() != 0) {
                busControls.computeIfAbsent(psseGenerator.getIreg(), k -> new ArrayList<>()).add(new NodeBreakerControl(id, psseGenerator.getNreg()));
            }
        });
        pssePowerFlowModel.getNonTransformerBranches().forEach(psseNonTransformerBranch -> {
            String id = getNodeBreakerEquipmentId(PSSE_BRANCH, psseNonTransformerBranch.getI(), psseNonTransformerBranch.getJ(), psseNonTransformerBranch.getCkt());
            busEquipmentTerminals.computeIfAbsent(psseNonTransformerBranch.getI(), k -> new ArrayList<>()).add(id);
            busEquipmentTerminals.computeIfAbsent(psseNonTransformerBranch.getJ(), k -> new ArrayList<>()).add(id);
        });
        pssePowerFlowModel.getTransformers().forEach(psseTransformer -> {
            if (psseTransformer.getK() == 0) {
                twoWindingsTransformerNetworkValidation(psseTransformer);
            } else {
                threeWindingsTransformerNetworkValidation(psseTransformer);
            }
        });
        pssePowerFlowModel.getTwoTerminalDcTransmissionLines().forEach(psseTwoTerminalDcTransmissionLine -> {
            String idRectifier = getNodeBreakerEquipmentId(PSSE_TWO_TERMINAL_DC_LINE, psseTwoTerminalDcTransmissionLine.getRectifier().getIp(), psseTwoTerminalDcTransmissionLine.getName());
            String idInverter = getNodeBreakerEquipmentId(PSSE_TWO_TERMINAL_DC_LINE, psseTwoTerminalDcTransmissionLine.getInverter().getIp(), psseTwoTerminalDcTransmissionLine.getName());
            busEquipmentTerminals.computeIfAbsent(psseTwoTerminalDcTransmissionLine.getRectifier().getIp(), k -> new ArrayList<>()).add(idRectifier);
            busEquipmentTerminals.computeIfAbsent(psseTwoTerminalDcTransmissionLine.getInverter().getIp(), k -> new ArrayList<>()).add(idInverter);
        });
        pssePowerFlowModel.getVoltageSourceConverterDcTransmissionLines().forEach(psseVscDcTransmissionLine -> {
            String idConverter1 = getNodeBreakerEquipmentId(PSSE_VSC_DC_LINE, psseVscDcTransmissionLine.getConverter1().getIbus(), psseVscDcTransmissionLine.getName());
            String idConverter2 = getNodeBreakerEquipmentId(PSSE_VSC_DC_LINE, psseVscDcTransmissionLine.getConverter2().getIbus(), psseVscDcTransmissionLine.getName());
            busEquipmentTerminals.computeIfAbsent(psseVscDcTransmissionLine.getConverter1().getIbus(), k -> new ArrayList<>()).add(idConverter1);
            busEquipmentTerminals.computeIfAbsent(psseVscDcTransmissionLine.getConverter2().getIbus(), k -> new ArrayList<>()).add(idConverter2);
            if (psseVscDcTransmissionLine.getConverter1().getVsreg() != 0) {
                busControls.computeIfAbsent(psseVscDcTransmissionLine.getConverter1().getVsreg(), k -> new ArrayList<>()).add(new NodeBreakerControl(idConverter1, psseVscDcTransmissionLine.getConverter1().getNreg()));
            }
            if (psseVscDcTransmissionLine.getConverter2().getVsreg() != 0) {
                busControls.computeIfAbsent(psseVscDcTransmissionLine.getConverter2().getVsreg(), k -> new ArrayList<>()).add(new NodeBreakerControl(idConverter2, psseVscDcTransmissionLine.getConverter2().getNreg()));
            }
        });
        pssePowerFlowModel.getSwitchedShunts().forEach(psseSwitchedShunt -> {
            String id = getNodeBreakerEquipmentId(PSSE_SWITCHED_SHUNT, psseSwitchedShunt.getI(), psseSwitchedShunt.getId());
            busEquipmentTerminals.computeIfAbsent(psseSwitchedShunt.getI(), k -> new ArrayList<>()).add(id);
            if (psseSwitchedShunt.getSwreg() != 0) {
                busControls.computeIfAbsent(psseSwitchedShunt.getSwreg(), k -> new ArrayList<>()).add(new NodeBreakerControl(id, psseSwitchedShunt.getNreg()));
            }
        });
    }

    private void twoWindingsTransformerNetworkValidation(PsseTransformer psseTransformer) {
        String id = getNodeBreakerEquipmentId(PSSE_TWO_WINDING, psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getCkt());
        busEquipmentTerminals.computeIfAbsent(psseTransformer.getI(), k -> new ArrayList<>()).add(id);
        busEquipmentTerminals.computeIfAbsent(psseTransformer.getJ(), k -> new ArrayList<>()).add(id);

        addWindingControl(psseTransformer.getWinding1(), id);
    }

    private void threeWindingsTransformerNetworkValidation(PsseTransformer psseTransformer) {
        String id = getNodeBreakerEquipmentId(PSSE_THREE_WINDING, psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getK(), psseTransformer.getCkt());
        busEquipmentTerminals.computeIfAbsent(psseTransformer.getI(), k -> new ArrayList<>()).add(id);
        busEquipmentTerminals.computeIfAbsent(psseTransformer.getJ(), k -> new ArrayList<>()).add(id);
        busEquipmentTerminals.computeIfAbsent(psseTransformer.getK(), k -> new ArrayList<>()).add(id);

        addWindingControl(psseTransformer.getWinding1(), id);
        addWindingControl(psseTransformer.getWinding2(), id);
        addWindingControl(psseTransformer.getWinding3(), id);
    }

    private void addWindingControl(PsseTransformerWinding psseTransformerWinding, String id) {
        int iReg = Math.abs(psseTransformerWinding.getCont());
        if (iReg != 0) {
            busControls.computeIfAbsent(iReg, k -> new ArrayList<>()).add(new NodeBreakerControl(id, psseTransformerWinding.getNode()));
        }
    }

    // All the buses must be defined inside the same psseSubstation
    // if two buses of the same voltageLevel are defined in different psseSubstations then the node numbers could be incoherent (can be overlapped)
    boolean isNodeBreakerSubstationDataCoherent(Set<Integer> expectedBuses) {
        if (ignoreNodeBreakerTopology) {
            return false;
        }
        if (expectedBuses.isEmpty()) {
            return false;
        }
        Optional<PsseSubstation> expectedPsseSubstation = getSubstationIfOnlyOneExists(expectedBuses);
        return expectedPsseSubstation.filter(substation -> expectedBuses.stream().allMatch(bus -> isNodeBreakerSubstationDataCoherent(substation, bus))).isPresent();
    }

    private boolean isNodeBreakerSubstationDataCoherent(PsseSubstation psseSubstation, int bus) {
        Set<Integer> nodesSetByDefinition = psseSubstation.getNodes().stream().filter(psseNode -> psseNode.getI() == bus).map(PsseSubstationNode::getNi).collect(Collectors.toSet());
        Set<Integer> nodesSetBySwitching = obtainNodeComponentBySwitching(psseSubstation, bus);
        if (!isValidNodesSet(psseSubstation, bus, nodesSetByDefinition, nodesSetBySwitching)) {
            return false;
        }
        if (!areControlsCoherent(bus, nodesSetBySwitching)) {
            return false;
        }
        List<String> equipmentTerminalsByPsseBlocks = getEquipmentTerminals(bus);
        Set<String> equipmentTerminalByEquipmentTerminalData = psseSubstation.getEquipmentTerminals().stream()
                .filter(eqt -> eqt.getI() == bus)
                .map(eqt -> getNodeBreakerEquipmentId(eqt.getType(), eqt.getI(), eqt.getJ(), eqt.getK(), eqt.getId())).collect(Collectors.toSet());
        Set<String> equipmentTerminalByNodeSet = psseSubstation.getEquipmentTerminals().stream()
                .filter(eqt -> nodesSetBySwitching.contains(eqt.getNi()))
                .map(eqt -> getNodeBreakerEquipmentId(eqt.getType(), eqt.getI(), eqt.getJ(), eqt.getK(), eqt.getId())).collect(Collectors.toSet());

        return equipmentTerminalsByPsseBlocks.size() == equipmentTerminalByEquipmentTerminalData.size()
                && equipmentTerminalsByPsseBlocks.size() == equipmentTerminalByNodeSet.size()
                && equipmentTerminalByEquipmentTerminalData.containsAll(equipmentTerminalsByPsseBlocks)
                && equipmentTerminalByEquipmentTerminalData.containsAll(equipmentTerminalByNodeSet);
    }

    // based on real cases and substation configurations created automatically by psse
    // isolated nodes are discarded
    // nodes are in the same component if they are connected by switches without considering their status (open / close)
    private static Set<Integer> obtainNodeComponentBySwitching(PsseSubstation psseSubstation, int bus) {
        Set<Integer> nodesSet = psseSubstation.getNodes().stream().filter(psseNode -> psseNode.getStatus() == 1).map(PsseSubstationNode::getNi).collect(Collectors.toSet());

        Graph<Integer, Pair<Integer, Integer>> sGraph = new Pseudograph<>(null, null, false);
        nodesSet.forEach(sGraph::addVertex);
        psseSubstation.getSwitchingDevices().forEach(switchingDevice -> sGraph.addEdge(switchingDevice.getNi(), switchingDevice.getNj(), Pair.of(switchingDevice.getNi(), switchingDevice.getNj())));

        List<Set<Integer>> nodeComponents = new ConnectivityInspector<>(sGraph).connectedSets().stream().filter(nodeComponent -> nodeComponentAssociatedWithBus(psseSubstation, nodeComponent, bus)).toList();
        return nodeComponents.size() == 1 ? nodeComponents.get(0) : new HashSet<>();
    }

    private static boolean nodeComponentAssociatedWithBus(PsseSubstation psseSubstation, Set<Integer> nodeComponent, int bus) {
        return psseSubstation.getNodes().stream().anyMatch(psseNode -> psseNode.getI() == bus && nodeComponent.contains(psseNode.getNi()));
    }

    private static boolean isValidNodesSet(PsseSubstation psseSubstation, int bus, Set<Integer> nodesSetByDefinition, Set<Integer> nodesSetBySwitching) {
        if (nodesSetBySwitching.isEmpty()) {
            return false;
        }
        // nodesSetByDefinition must be equal to isolatedNodesSet + nodesSetBySwitching
        Set<Integer> isolatedNodesSet = psseSubstation.getNodes().stream().filter(psseNode -> psseNode.getStatus() == 0 && psseNode.getI() == bus).map(PsseSubstationNode::getNi).collect(Collectors.toSet());
        isolatedNodesSet.addAll(nodesSetBySwitching);
        return isolatedNodesSet.size() == nodesSetByDefinition.size() && nodesSetByDefinition.containsAll(isolatedNodesSet);
    }

    // All the nodes associated with controls must be inside the nodesSetBySwitching
    private boolean areControlsCoherent(int bus, Set<Integer> nodesSetBySwitching) {
        List<NodeBreakerControl> controls = getControls(bus);
        return controls.stream().map(NodeBreakerControl::getNode).allMatch(nodesSetBySwitching::contains);
    }

    private List<String> getEquipmentTerminals(int bus) {
        return busEquipmentTerminals.containsKey(bus) ? busEquipmentTerminals.get(bus) : new ArrayList<>();
    }

    static final class NodeBreakerControl {
        private final String equipmentId;
        private final int node;

        private NodeBreakerControl(String equipmentId, int node) {
            this.equipmentId = equipmentId;
            this.node = node;
        }

        String getEquipmentId() {
            return equipmentId;
        }

        int getNode() {
            return node;
        }
    }
}
