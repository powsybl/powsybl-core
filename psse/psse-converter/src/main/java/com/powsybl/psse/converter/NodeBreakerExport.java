/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import org.jgrapht.alg.util.Pair;

import java.util.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
final class NodeBreakerExport {
    private int maxPsseBus;
    private final Map<Integer, NodeBreakerNewBus> newBuses;
    private final Map<Integer, Pair<String, Integer>> buses;
    private final Set<Integer> isolatedBuses;
    private final Map<String, Pair<Integer, Boolean>> nodeBus;
    private final Map<String, Integer> equipmentIdBusBus;

    NodeBreakerExport(int maxPsseBus) {
        this.maxPsseBus = maxPsseBus;
        this.newBuses = new HashMap<>();
        this.buses = new HashMap<>();
        this.isolatedBuses = new HashSet<>();
        this.nodeBus = new HashMap<>();
        this.equipmentIdBusBus = new HashMap<>();
    }

    int getNewPsseBus() {
        return ++maxPsseBus;
    }

    void addNewBus(int newBus, int copyBus, String busBreakerBusId, int type) {
        newBuses.put(newBus, new NodeBreakerNewBus(copyBus, busBreakerBusId, type));
    }

    Set<Integer> getNewBusesSet() {
        return newBuses.keySet();
    }

    OptionalInt getNewBusCopyBus(int newBus) {
        return newBuses.containsKey(newBus) ? OptionalInt.of(newBuses.get(newBus).busCopy) : OptionalInt.empty();
    }

    Optional<String> getNewBusBusBreakerId(int newBus) {
        return newBuses.containsKey(newBus) ? Optional.of(newBuses.get(newBus).busBreakerId) : Optional.empty();
    }

    OptionalInt getNewBusType(int newBus) {
        return newBuses.containsKey(newBus) ? OptionalInt.of(newBuses.get(newBus).type) : OptionalInt.empty();
    }

    void addBusMapping(int bus, String busBreakerBusId, int type) {
        this.buses.put(bus, Pair.of(busBreakerBusId, type));
    }

    Optional<String> getBusBreakerBusId(int bus) {
        return this.buses.containsKey(bus) ? Optional.of(this.buses.get(bus).getFirst()) : Optional.empty();
    }

    OptionalInt getBusType(int bus) {
        return this.buses.containsKey(bus) ? OptionalInt.of(this.buses.get(bus).getSecond()) : OptionalInt.empty();
    }

    void addIsolatedBus(int bus) {
        this.isolatedBuses.add(bus);
    }

    boolean isFreeBus(int bus) {
        return !(this.buses.containsKey(bus) || this.isolatedBuses.contains(bus));
    }

    void addNodesBusMapping(String voltageLevelId, List<Integer> nodes, int bus, boolean inService) {
        nodes.forEach(node -> addNodeBusMapping(voltageLevelId, node, bus, inService));
    }

    void addNodeBusMapping(String voltageLevelId, int node, int bus, boolean inService) {
        this.nodeBus.put(getNodeId(voltageLevelId, node), Pair.of(bus, inService));
    }

    OptionalInt getNodeBus(String voltageLevelId, int node) {
        return this.nodeBus.containsKey(getNodeId(voltageLevelId, node)) ? OptionalInt.of(this.nodeBus.get(getNodeId(voltageLevelId, node)).getFirst()) : OptionalInt.empty();
    }

    Optional<Boolean> isNodeInService(String voltageLevelId, int node) {
        return this.nodeBus.containsKey(getNodeId(voltageLevelId, node)) ? Optional.of(this.nodeBus.get(getNodeId(voltageLevelId, node)).getSecond()) : Optional.empty();
    }

    boolean isFreeNode(String voltageLevelId, int node) {
        return !this.nodeBus.containsKey(getNodeId(voltageLevelId, node));
    }

    private static String getNodeId(String voltageLevelId, int node) {
        return voltageLevelId + "-" + node;
    }

    void addEquipmentIdBus(String equipmentIdBusBus, int bus) {
        this.equipmentIdBusBus.put(equipmentIdBusBus, bus);
    }

    OptionalInt getEquipmentIdBusBus(String equipmentIdBusBus) {
        return this.equipmentIdBusBus.containsKey(equipmentIdBusBus) ? OptionalInt.of(this.equipmentIdBusBus.get(equipmentIdBusBus)) : OptionalInt.empty();
    }

    private static final class NodeBreakerNewBus {
        private final int busCopy;
        private final String busBreakerId;
        private final int type;

        private NodeBreakerNewBus(int busCopy, String busBreakerId, int type) {
            this.busCopy = busCopy;
            this.busBreakerId = busBreakerId;
            this.type = type;
        }
    }
}
