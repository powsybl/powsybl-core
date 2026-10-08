/**
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.iidm.network.*;
import com.powsybl.ucte.network.UcteCountryCode;

/**
 * Networks with voltage levels without substation ("orphans"), mimicking CGMES imports.
 * All voltage levels are 380 kV. Substation "S_&lt;name&gt;" holds voltage level "VL_&lt;name&gt;"; orphan voltage
 * levels are named "VL_&lt;name&gt;".
 *
 * @author Arthur Michaut {@literal <arthur.michaut at artelys.com>}
 */
final class OrphanVoltageLevelNetworks {

    /**
     * Bus and branch ids: valid UCTE codes (for the Default naming strategy) or free ids (Counter naming strategy).
     */
    enum Ids {
        UCTE,
        FREE;

        /** @param name 5 characters */
        String bus(Country country, String name, char busbar) {
            return this == UCTE
                    ? UcteCountryCode.fromCountry(country).getUcteCode() + name + "1" + busbar
                    : "BUS_" + name + "_" + busbar;
        }

        String branch(String bus1, String bus2) {
            return this == UCTE ? bus1 + " " + bus2 + " 1" : "BR_" + bus1 + "_" + bus2;
        }
    }

    private OrphanVoltageLevelNetworks() {
    }

    static Network newNetwork() {
        return Network.create("orphan-voltage-levels", "test");
    }

    /**
     * Adds substation "S_name" (no country if {@code country} is null) with bus-breaker voltage level "VL_name"
     * holding one bus. Returns the bus id (UCTE ids use NL when there is no country).
     */
    static String addSubstationBus(Network network, Ids ids, Country country, String name) {
        SubstationAdder substationAdder = network.newSubstation().setId("S_" + name);
        if (country != null) {
            substationAdder.setCountry(country);
        }
        VoltageLevel voltageLevel = substationAdder.add().newVoltageLevel()
                .setId("VL_" + name)
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        return addBus(voltageLevel, ids, name, '1');
    }

    /** Adds bus-breaker voltage level "VL_name" without substation. */
    static VoltageLevel addOrphanVoltageLevel(Network network, String name) {
        return network.newVoltageLevel()
                .setId("VL_" + name)
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
    }

    /** Adds a bus to a bus-breaker voltage level; orphan buses use NL for UCTE ids. Returns the bus id. */
    static String addBus(VoltageLevel voltageLevel, Ids ids, String name, char busbar) {
        Country country = voltageLevel.getSubstation().flatMap(Substation::getCountry).orElse(Country.NL);
        String busId = ids.bus(country, name, busbar);
        voltageLevel.getBusBreakerView().newBus().setId(busId).add();
        return busId;
    }

    static Line addLine(Network network, Ids ids, String bus1, String bus2) {
        return network.newLine()
                .setId(ids.branch(bus1, bus2))
                .setVoltageLevel1(network.getBusBreakerView().getBus(bus1).getVoltageLevel().getId())
                .setBus1(bus1)
                .setConnectableBus1(bus1)
                .setVoltageLevel2(network.getBusBreakerView().getBus(bus2).getVoltageLevel().getId())
                .setBus2(bus2)
                .setConnectableBus2(bus2)
                .setR(1)
                .setX(10)
                .add();
    }

    static void addLoad(Network network, String busId, String loadId) {
        network.getBusBreakerView().getBus(busId).getVoltageLevel().newLoad()
                .setId(loadId)
                .setBus(busId)
                .setConnectableBus(busId)
                .setP0(42)
                .setQ0(7)
                .add();
    }

    /** T-line: substations AAAAA (NL), BBBBB ({@code countryB}), CCCCC (NL), each linked to orphan junction TJUNC. */
    static Network tLine(Ids ids, Country countryB) {
        Network network = newNetwork();
        String a = addSubstationBus(network, ids, Country.NL, "AAAAA");
        String b = addSubstationBus(network, ids, countryB, "BBBBB");
        String c = addSubstationBus(network, ids, Country.NL, "CCCCC");
        String junction = addBus(addOrphanVoltageLevel(network, "TJUNC"), ids, "TJUNC", '1');
        addLine(network, ids, a, junction);
        addLine(network, ids, b, junction);
        addLine(network, ids, c, junction);
        return network;
    }

    /** Chained segments AAAAA (NL) - SEGMT bus 1 - SEGMT bus 2 - BBBBB (NL), both SEGMT buses in orphan VL_SEGMT. */
    static Network chainedSegmentsOneVoltageLevel(Ids ids) {
        Network network = newNetwork();
        String a = addSubstationBus(network, ids, Country.NL, "AAAAA");
        String b = addSubstationBus(network, ids, Country.NL, "BBBBB");
        VoltageLevel segments = addOrphanVoltageLevel(network, "SEGMT");
        String s1 = addBus(segments, ids, "SEGMT", '1');
        String s2 = addBus(segments, ids, "SEGMT", '2');
        addLine(network, ids, a, s1);
        addLine(network, ids, s1, s2);
        addLine(network, ids, s2, b);
        return network;
    }

    /** Chained segments AAAAA (NL) - SEGAA - SEGBB - BBBBB (NL), SEGAA and SEGBB in two orphan voltage levels. */
    static Network chainedSegmentsTwoVoltageLevels(Ids ids) {
        Network network = newNetwork();
        String a = addSubstationBus(network, ids, Country.NL, "AAAAA");
        String b = addSubstationBus(network, ids, Country.NL, "BBBBB");
        String s1 = addBus(addOrphanVoltageLevel(network, "SEGAA"), ids, "SEGAA", '1');
        String s2 = addBus(addOrphanVoltageLevel(network, "SEGBB"), ids, "SEGBB", '1');
        addLine(network, ids, a, s1);
        addLine(network, ids, s1, s2);
        addLine(network, ids, s2, b);
        return network;
    }

    /**
     * Substation AAAAA (NL) linked to orphan VL_ORPHA holding buses ORPHA 1 and 2, a closed switch between them and
     * load "LOAD_ORPHA" (p0 = 42) on bus 1.
     */
    static Network orphanWithSwitchAndLoad(Ids ids) {
        Network network = newNetwork();
        String a = addSubstationBus(network, ids, Country.NL, "AAAAA");
        VoltageLevel orphan = addOrphanVoltageLevel(network, "ORPHA");
        String b1 = addBus(orphan, ids, "ORPHA", '1');
        String b2 = addBus(orphan, ids, "ORPHA", '2');
        orphan.getBusBreakerView().newSwitch()
                .setId(ids.branch(b1, b2))
                .setBus1(b1)
                .setBus2(b2)
                .setOpen(false)
                .add();
        addLoad(network, b1, "LOAD_ORPHA");
        addLine(network, ids, a, b1);
        return network;
    }

    /** Node-breaker T-line, free ids: substations AAAAA, BBBBB, CCCCC (NL) linked to nodes 1, 2, 3 of orphan VL_TJUNC. */
    static Network nodeBreakerTLine() {
        Network network = newNetwork();
        VoltageLevel junction = network.newVoltageLevel()
                .setId("VL_TJUNC")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        int node = 1;
        for (String name : new String[] {"AAAAA", "BBBBB", "CCCCC"}) {
            String bus = addSubstationBus(network, Ids.FREE, Country.NL, name);
            junction.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(node).add();
            network.newLine()
                    .setId("BR_" + name)
                    .setVoltageLevel1("VL_" + name)
                    .setBus1(bus)
                    .setConnectableBus1(bus)
                    .setVoltageLevel2("VL_TJUNC")
                    .setNode2(node)
                    .setR(1)
                    .setX(10)
                    .add();
            node++;
        }
        return network;
    }
}
