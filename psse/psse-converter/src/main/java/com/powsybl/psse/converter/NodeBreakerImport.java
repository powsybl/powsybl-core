/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
final class NodeBreakerImport {

    private final Set<Integer> topologicalBuses;
    private final Map<String, Integer> equipmentIdBusNode;
    private final Map<String, NodeBreakerControlNode> equipmentIdBusControlNode;

    private final Map<Integer, NodeBreakerControlNode> slackBusControlNode;

    NodeBreakerImport() {
        this.topologicalBuses = new HashSet<>(); // buses not used for defining the connectivity
        this.equipmentIdBusNode = new HashMap<>();
        this.equipmentIdBusControlNode = new HashMap<>();
        this.slackBusControlNode = new HashMap<>();
    }

    void addTopologicalBuses(Set<Integer> buses) {
        topologicalBuses.addAll(buses);
    }

    boolean isTopologicalBus(int bus) {
        return topologicalBuses.contains(bus);
    }

    void addEquipment(String equipmentIdBus, int node) {
        equipmentIdBusNode.put(equipmentIdBus, node);
    }

    OptionalInt getNode(String equipmentIdBus) {
        if (equipmentIdBusNode.containsKey(equipmentIdBus)) {
            return OptionalInt.of(equipmentIdBusNode.get(equipmentIdBus));
        } else {
            return OptionalInt.empty();
        }
    }

    void addControl(String equipmentIdBus, String voltageLevelId, int node) {
        equipmentIdBusControlNode.put(equipmentIdBus, new NodeBreakerControlNode(voltageLevelId, node));
    }

    Optional<NodeBreakerControlNode> getControlNode(String equipmentIdBus) {
        if (equipmentIdBusControlNode.containsKey(equipmentIdBus)) {
            return Optional.of(equipmentIdBusControlNode.get(equipmentIdBus));
        } else {
            return Optional.empty();
        }
    }

    void addSlackControl(int bus, String voltageLevelId, int node) {
        slackBusControlNode.put(bus, new NodeBreakerControlNode(voltageLevelId, node));
    }

    Optional<NodeBreakerControlNode> getSlackControlNode(int bus) {
        if (slackBusControlNode.containsKey(bus)) {
            return Optional.of(slackBusControlNode.get(bus));
        } else {
            return Optional.empty();
        }
    }

    static final class NodeBreakerControlNode {
        private final String voltageLevelId;
        private final int node;

        private NodeBreakerControlNode(String voltageLevelId, int node) {
            this.voltageLevelId = voltageLevelId;
            this.node = node;
        }

        String getVoltageLevelId() {
            return voltageLevelId;
        }

        int getNode() {
            return node;
        }
    }
}
