/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.psse.model.pf.PsseBus;
import com.powsybl.psse.model.pf.PsseSubstation;

import java.util.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
final class ContextExport {
    private final Map<Integer, PsseBus> ibusToPsseBus;
    private final Map<Integer, BusR> buses;
    private final NodeBreakerExport nodeBreakerExport;

    ContextExport() {
        this.ibusToPsseBus = new HashMap<>();
        this.buses = new HashMap<>();
        this.nodeBreakerExport = new NodeBreakerExport();
    }

    void addIbus(int busI, PsseBus psseBus) {
        ibusToPsseBus.put(busI, psseBus);
    }

    Optional<PsseBus> getPsseBus(int busI) {
        return ibusToPsseBus.containsKey(busI) ? Optional.of(ibusToPsseBus.get(busI)) : Optional.empty();
    }

    void addBus(int busI, String busViewId, int type) {
        buses.put(busI, new BusR(busViewId, type));
    }

    Optional<String> getBusViewBusId(int bus) {
        return buses.containsKey(bus) ? Optional.of(buses.get(bus).busViewId()) : Optional.empty();
    }

    OptionalInt getType(int bus) {
        return buses.containsKey(bus) ? OptionalInt.of(buses.get(bus).type()) : OptionalInt.empty();
    }

    NodeBreakerExport getNodeBreakerExport() {
        return this.nodeBreakerExport;
    }

    static class NodeBreakerExport {
        private final Map<VoltageLevel, PsseSubstation> voltageLevelPsseSubstation;
        private final Map<String, String> nodes;

        NodeBreakerExport() {
            this.voltageLevelPsseSubstation = new HashMap<>();
            this.nodes = new HashMap<>();
        }

        void addVoltageLevelPsseSubstation(VoltageLevel voltageLevel, PsseSubstation psseSubstation) {
            voltageLevelPsseSubstation.put(voltageLevel, psseSubstation);
        }

        Optional<PsseSubstation> getVoltageLevelPsseSubstation(VoltageLevel voltageLevel) {
            return Optional.ofNullable(voltageLevelPsseSubstation.get(voltageLevel));
        }

        void addNode(VoltageLevel voltageLevel, int node, String busViewBusId) {
            nodes.put(getNodeId(voltageLevel, node), busViewBusId);
        }

        Optional<String> getNodeViewBusId(VoltageLevel voltageLevel, int node) {
            return nodes.containsKey(getNodeId(voltageLevel, node)) ? Optional.of(nodes.get(getNodeId(voltageLevel, node))) : Optional.empty();
        }

        private static String getNodeId(VoltageLevel voltageLevel, int node) {
            return voltageLevel.getId() + "-" + node;
        }
    }

    private record BusR(String busViewId, int type) {
    }
}
