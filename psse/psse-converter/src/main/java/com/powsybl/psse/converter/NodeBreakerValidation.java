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

    boolean isNodeBreakerSubstationCoherent(Set<Integer> expectedBuses) {
        if (ignoreNodeBreakerTopology) {
            return false;
        }
        if (expectedBuses.isEmpty()) {
            return false;
        }
        Set<PsseSubstation> expectedSubstations = expectedBuses.stream()
                .map(this::getSubstationIfOnlyOneExists)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        if (expectedSubstations.size() != 1) {
            return false;
        }
        PsseSubstation expectedSubstation = expectedSubstations.iterator().next();
        if (!areControlsCoherent(expectedSubstation, expectedBuses)) {
            return false;
        }
        Set<String> expectedEquipmentTerminals = expectedBuses.stream()
                .flatMap(bus -> getEquipmentTerminals(bus).stream())
                .collect(Collectors.toSet());
        List<Set<Integer>> nodeComponents = obtainNodeComponents(expectedSubstation);

        Set<Integer> actualBuses = new HashSet<>();
        Set<String> actualEquipmentTerminals = new HashSet<>();
        for (Set<Integer> nodeComponentSet : nodeComponents) {
            Set<Integer> busComponentSet = expectedSubstation.getNodes().stream()
                    .filter(n -> nodeComponentSet.contains(n.getNi()))
                    .map(PsseSubstationNode::getI).collect(Collectors.toSet());
            Set<String> equipmentTerminalComponent = expectedSubstation.getEquipmentTerminals().stream()
                    .filter(eqt -> nodeComponentSet.contains(eqt.getNi()))
                    .map(eqt -> getNodeBreakerEquipmentId(eqt.getType(), eqt.getI(), eqt.getJ(), eqt.getK(), eqt.getId())).collect(Collectors.toSet());

            actualBuses.addAll(busComponentSet);
            actualEquipmentTerminals.addAll(equipmentTerminalComponent);
        }
        if (expectedBuses.size() != actualBuses.size()) {
            return false;
        }
        if (expectedEquipmentTerminals.size() != actualEquipmentTerminals.size()) {
            return false;
        }
        return expectedBuses.containsAll(actualBuses) && expectedEquipmentTerminals.containsAll(actualEquipmentTerminals);
    }

    // All the nodes associated with controls must be inside the substation data
    private boolean areControlsCoherent(PsseSubstation psseSubstation, Set<Integer> buses) {
        Set<Integer> nodesSet = psseSubstation.getNodes().stream().map(PsseSubstationNode::getNi).collect(Collectors.toSet());
        for (int bus : buses) {
            List<NodeBreakerControl> controls = getControls(bus);
            if (controls.stream().map(NodeBreakerControl::getNode).anyMatch(node -> !nodesSet.contains(node))) {
                return false;
            }
        }
        return true;
    }

    private static List<Set<Integer>> obtainNodeComponents(PsseSubstation psseSubstation) {
        Set<Integer> nodesSet = psseSubstation.getNodes().stream().map(PsseSubstationNode::getNi).collect(Collectors.toSet());

        Graph<Integer, Pair<Integer, Integer>> sGraph = new Pseudograph<>(null, null, false);
        nodesSet.forEach(sGraph::addVertex);
        psseSubstation.getSwitchingDevices().forEach(switchingDevice -> {
            if (switchingDevice.getStatus() == 1) { // Only closed switches
                sGraph.addEdge(switchingDevice.getNi(), switchingDevice.getNj(), Pair.of(switchingDevice.getNi(), switchingDevice.getNj()));
            }
        });
        return new ConnectivityInspector<>(sGraph).connectedSets();
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
