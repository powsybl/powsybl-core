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
    private final Map<Integer, ControlR> busControlR;

    private final Map<Integer, ControlR> slackBusControlNode;

    NodeBreakerImport() {
        this.topologicalBuses = new HashSet<>(); // buses not used for defining the connectivity
        this.equipmentIdBusNode = new HashMap<>();
        this.busControlR = new HashMap<>();
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

    void addBusControl(int bus, String voltageLevelId, int defaultNode) {
        busControlR.put(bus, new ControlR(voltageLevelId, defaultNode));
    }

    Optional<ControlR> getControl(int bus) {
        if (busControlR.containsKey(bus)) {
            return Optional.of(busControlR.get(bus));
        } else {
            return Optional.empty();
        }
    }

    void addSlackControl(int bus, String voltageLevelId, int node) {
        slackBusControlNode.put(bus, new ControlR(voltageLevelId, node));
    }

    Optional<ControlR> getSlackControlNode(int bus) {
        if (slackBusControlNode.containsKey(bus)) {
            return Optional.of(slackBusControlNode.get(bus));
        } else {
            return Optional.empty();
        }
    }

    record ControlR(String voltageLevelId, int node) {
    }
}
