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
    private final Map<PsseSubstation, List<Integer>> substationBuses;
    private final Map<Integer, Set<Integer>> busNodesSet;
    private final Map<Integer, List<String>> busEquipmentTerminals;
    private final Set<PsseSubstation> invalidSubstations;

    NodeBreakerValidation(boolean ignoreNodeBreakerTopology) {
        this.ignoreNodeBreakerTopology = ignoreNodeBreakerTopology;
        this.busSubstations = new HashMap<>();
        this.substationBuses = new HashMap<>();
        this.busNodesSet = new HashMap<>();
        this.busEquipmentTerminals = new HashMap<>();
        this.invalidSubstations = new HashSet<>();
    }

    void fillAndValidate(PssePowerFlowModel pssePowerFlowModel, PsseVersion version) {
        if (version.major() != V35 || this.ignoreNodeBreakerTopology) {
            return;
        }

        // Fill psse substation data, only valid psse substations are used
        pssePowerFlowModel.getSubstations().forEach(psseSubstation -> {
            if (validInternalConnectivity(psseSubstation)) {
                fill(psseSubstation);
            }
        });

        // Do the validations

        // a bus can only be included in one psseSubstation
        busSubstations.forEach((bus, substationList) -> {
            if (substationList.size() >= 2) {
                invalidSubstations.addAll(substationList);
            }
        });

        // validate equipment terminals and node controls
        pssePowerFlowModel.getLoads().forEach(psseLoad -> {
            String id = getNodeBreakerEquipmentId(PSSE_LOAD, psseLoad.getI(), psseLoad.getId());
            checkEquipment(psseLoad.getI(), id);
        });
        pssePowerFlowModel.getFixedShunts().forEach(psseFixedShunt -> {
            String id = getNodeBreakerEquipmentId(PSSE_FIXED_SHUNT, psseFixedShunt.getI(), psseFixedShunt.getId());
            checkEquipment(psseFixedShunt.getI(), id);
        });
        pssePowerFlowModel.getGenerators().forEach(psseGenerator -> {
            String id = getNodeBreakerEquipmentId(PSSE_GENERATOR, psseGenerator.getI(), psseGenerator.getId());
            checkEquipment(psseGenerator.getI(), id);
            checkControl(psseGenerator.getIreg(), psseGenerator.getNreg());
        });
        pssePowerFlowModel.getNonTransformerBranches().forEach(psseNonTransformerBranch -> {
            String id = getNodeBreakerEquipmentId(PSSE_BRANCH, psseNonTransformerBranch.getI(), psseNonTransformerBranch.getJ(), psseNonTransformerBranch.getCkt());
            checkEquipment(psseNonTransformerBranch.getI(), id);
            checkEquipment(psseNonTransformerBranch.getJ(), id);
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
            checkEquipment(psseTwoTerminalDcTransmissionLine.getRectifier().getIp(), idRectifier);
            checkEquipment(psseTwoTerminalDcTransmissionLine.getInverter().getIp(), idInverter);
        });
        pssePowerFlowModel.getSwitchedShunts().forEach(psseSwitchedShunt -> {
            String id = getNodeBreakerEquipmentId(PSSE_SWITCHED_SHUNT, psseSwitchedShunt.getI(), psseSwitchedShunt.getId());
            checkEquipment(psseSwitchedShunt.getI(), id);
            checkControl(psseSwitchedShunt.getSwreg(), psseSwitchedShunt.getNreg());
        });
    }

    private void twoWindingsTransformerNetworkValidation(PsseTransformer psseTransformer) {
        String id = getNodeBreakerEquipmentId(PSSE_TWO_WINDING, psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getCkt());
        checkEquipment(psseTransformer.getI(), id);
        checkEquipment(psseTransformer.getJ(), id);
        checkControl(psseTransformer.getWinding1().getCont(), psseTransformer.getWinding1().getNode());
    }

    private void threeWindingsTransformerNetworkValidation(PsseTransformer psseTransformer) {
        String id = getNodeBreakerEquipmentId(PSSE_THREE_WINDING, psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getK(), psseTransformer.getCkt());
        checkEquipment(psseTransformer.getI(), id);
        checkEquipment(psseTransformer.getJ(), id);
        checkEquipment(psseTransformer.getK(), id);
        checkControl(psseTransformer.getWinding1().getCont(), psseTransformer.getWinding1().getNode());
        checkControl(psseTransformer.getWinding2().getCont(), psseTransformer.getWinding2().getNode());
        checkControl(psseTransformer.getWinding3().getCont(), psseTransformer.getWinding3().getNode());
    }

    // All the nodes connected by switches (without considering the status) must be a bus (topological bus)
    private static boolean validInternalConnectivity(PsseSubstation psseSubstation) {
        Set<Integer> nodesSet = psseSubstation.getNodes().stream().map(PsseSubstationNode::getNi).collect(Collectors.toSet());

        Graph<Integer, Pair<Integer, Integer>> sGraph = new Pseudograph<>(null, null, false);
        nodesSet.forEach(sGraph::addVertex);
        psseSubstation.getSwitchingDevices().forEach(switchingDevice -> sGraph.addEdge(switchingDevice.getNi(), switchingDevice.getNj(), Pair.of(switchingDevice.getNi(), switchingDevice.getNj())));

        List<Set<Integer>> connectedSets = new ConnectivityInspector<>(sGraph).connectedSets();
        for (Set<Integer> connectedSet : connectedSets) {
            Set<Integer> associatedBuses = findAssociatedBuses(psseSubstation, connectedSet);
            if (associatedBuses.size() != 1) {
                return false;
            }
        }
        return true;
    }

    private static Set<Integer> findAssociatedBuses(PsseSubstation psseSubstation, Set<Integer> connectedSet) {
        return psseSubstation.getNodes().stream().filter(psseNode -> connectedSet.contains(psseNode.getNi())).map(PsseSubstationNode::getI).collect(Collectors.toSet());
    }

    private void fill(PsseSubstation psseSubstation) {
        Set<Integer> buses = psseSubstation.getNodes().stream().map(PsseSubstationNode::getI).collect(Collectors.toSet());
        buses.forEach(bus -> {
            busSubstations.computeIfAbsent(bus, k -> new ArrayList<>()).add(psseSubstation);
            Set<Integer> nodesSet = psseSubstation.getNodes().stream().filter(psseNode -> psseNode.getI() == bus).map(PsseSubstationNode::getNi).collect(Collectors.toSet());
            busNodesSet.put(bus, nodesSet);
        });
        substationBuses.put(psseSubstation, buses.stream().toList());

        psseSubstation.getEquipmentTerminals().forEach(equipmentTerminal -> {
            String equipmentId = getNodeBreakerEquipmentId(equipmentTerminal.getType(), equipmentTerminal.getI(), equipmentTerminal.getJ(), equipmentTerminal.getK(), equipmentTerminal.getId());
            busEquipmentTerminals.computeIfAbsent(equipmentTerminal.getI(), k -> new ArrayList<>()).add(equipmentId);
        });
    }

    private void checkEquipment(int bus, String equipmentId) {
        if (!validEquipment(bus, equipmentId)) {
            invalidateSubstationAssociatedWithBus(bus);
        }
    }

    private boolean validEquipment(int bus, String equipmentId) {
        return busEquipmentTerminals.containsKey(bus) && busEquipmentTerminals.get(bus).contains(equipmentId);
    }

    private void checkControl(int bus, int node) {
        if (!validControl(bus, node)) {
            invalidateSubstationAssociatedWithBus(bus);
        }
    }

    private boolean validControl(int bus, int node) {
        return bus == 0 || node == 0 || busNodesSet.containsKey(bus) && busNodesSet.get(bus).contains(node);
    }

    private void invalidateSubstationAssociatedWithBus(int bus) {
        if (busSubstations.containsKey(bus)) {
            invalidSubstations.addAll(busSubstations.get(bus));
        }
    }

    List<PsseSubstation> getValidSubstations() {
        return substationBuses.keySet().stream().filter(psseSubstation -> !invalidSubstations.contains(psseSubstation)).toList();
    }

    List<Integer> getBuses(PsseSubstation psseSubstation) {
        return substationBuses.containsKey(psseSubstation) ? substationBuses.get(psseSubstation) : new ArrayList<>();
    }

    Set<Integer> getValidSubstationsIds(Set<Integer> buses) {
        if (buses.stream().anyMatch(bus -> !busAssociatedWithValidSubstation(bus))) {
            return new HashSet<>();
        }
        return buses.stream()
                .filter(busSubstations::containsKey)
                .flatMap(bus -> busSubstations.get(bus).stream())
                .map(PsseSubstation::getIs)
                .collect(Collectors.toSet());
    }

    private boolean busAssociatedWithValidSubstation(int bus) {
        return busSubstations.containsKey(bus) && busSubstations.get(bus).stream().noneMatch(invalidSubstations::contains);
    }

    boolean isConsideredNodeBreaker(Set<Integer> busesSet) {
        return getValidSubstationsIds(busesSet).size() == 1;
    }

    Optional<PsseSubstation> getTheOnlySubstation(int bus) {
        return getTheOnlySubstation(Set.of(bus));
    }

    Optional<PsseSubstation> getTheOnlySubstation(Set<Integer> busesSet) {
        Set<PsseSubstation> psseSubstationSet = busesSet.stream()
                .filter(busSubstations::containsKey)
                .flatMap(bus -> busSubstations.get(bus).stream())
                .filter(psseSubstation -> !invalidSubstations.contains(psseSubstation))
                .collect(Collectors.toSet());
        return psseSubstationSet.size() == 1 ? Optional.of(psseSubstationSet.iterator().next()) : Optional.empty();
    }
}
