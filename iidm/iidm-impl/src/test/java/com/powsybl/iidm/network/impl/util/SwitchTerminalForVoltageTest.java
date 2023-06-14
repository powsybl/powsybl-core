/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.SwitchTerminalForVoltage;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class SwitchTerminalForVoltageTest {

    @Test
    void switchTerminalForVoltageNodeBreakerTest() {

        Network network = createNodeBreaker();

        SwitchTerminalForVoltage s1vl1st01 = new SwitchTerminalForVoltage(network.getSwitch("S1VL1-Sw-0-1"));
        assertTrue(s1vl1st01.getTerminal1().map(t -> compareTerminal(t, "S1VL1_BBS0")).orElse(false));
        assertTrue(s1vl1st01.getTerminal2().map(t -> compareTerminal(t, "Twt-S1VL1-S1VL2-7-5")).orElse(false));

        SwitchTerminalForVoltage s1vl1st12 = new SwitchTerminalForVoltage(network.getSwitch("S1VL1-Sw-1-2"));
        assertTrue(s1vl1st12.getTerminal1().map(t -> compareTerminal(t, "Line-S1VL1-S2VL1-8-3")).orElse(false));
        assertTrue(s1vl1st12.getTerminal2().map(t -> compareTerminal(t, "Twt-S1VL1-S1VL2-7-5")).orElse(false));

        SwitchTerminalForVoltage s1vl1st13 = new SwitchTerminalForVoltage(network.getSwitch("S1VL1-Sw-1-3"));
        assertTrue(s1vl1st13.getTerminal1().map(t -> compareTerminal(t, "Twt-S1VL1-S1VL2-7-5")).orElse(false));
        assertTrue(s1vl1st13.getTerminal2().map(t -> compareTerminal(t, "Line-S1VL1-S2VL1-8-3")).orElse(false));

        SwitchTerminalForVoltage s1vl1st34 = new SwitchTerminalForVoltage(network.getSwitch("S1VL1-Sw-3-4"));
        assertTrue(s1vl1st34.getTerminal1().map(t -> compareTerminal(t, "Twt-S1VL1-S1VL2-7-5")).orElse(false));
        assertTrue(s1vl1st34.getTerminal2().map(t -> compareTerminal(t, "Line-S1VL1-S2VL1-8-3")).orElse(false));

        SwitchTerminalForVoltage s1vl1st06 = new SwitchTerminalForVoltage(network.getSwitch("S1VL1-Sw-0-6"));
        assertTrue(s1vl1st06.getTerminal1().map(t -> compareTerminal(t, "S1VL1_BBS0")).orElse(false));
        assertTrue(s1vl1st06.getTerminal2().map(t -> compareTerminal(t, "Line-S1VL1-S2VL2-9-4")).orElse(false));

        SwitchTerminalForVoltage s1vl2st01 = new SwitchTerminalForVoltage(network.getSwitch("S1VL2-Sw-0-1"));
        assertTrue(s1vl2st01.getTerminal1().isEmpty());
        assertTrue(s1vl2st01.getTerminal2().isEmpty());

        SwitchTerminalForVoltage s1vl2st12 = new SwitchTerminalForVoltage(network.getSwitch("S1VL2-Sw-1-2"));
        assertTrue(s1vl2st12.getTerminal1().isEmpty());
        assertTrue(s1vl2st12.getTerminal2().isEmpty());

        SwitchTerminalForVoltage s1vl2st03 = new SwitchTerminalForVoltage(network.getSwitch("S1VL2-Sw-0-3"));
        assertTrue(s1vl2st03.getTerminal1().isEmpty());
        assertTrue(s1vl2st03.getTerminal2().isEmpty());

        SwitchTerminalForVoltage s1vl2st34 = new SwitchTerminalForVoltage(network.getSwitch("S1VL2-Sw-3-4"));
        assertTrue(s1vl2st34.getTerminal1().isEmpty());
        assertTrue(s1vl2st34.getTerminal2().isEmpty());

        SwitchTerminalForVoltage s2vl1st01 = new SwitchTerminalForVoltage(network.getSwitch("S2VL1-Sw-0-1"));
        assertTrue(s2vl1st01.getTerminal1().map(t -> compareTerminal(t, "S2VL1_BBS0")).orElse(false));
        assertTrue(s2vl1st01.getTerminal2().map(t -> compareTerminal(t, "Line-S1VL1-S2VL1-8-3")).orElse(false));

        SwitchTerminalForVoltage s2vl1st02 = new SwitchTerminalForVoltage(network.getSwitch("S2VL1-Sw-0-2"));
        assertTrue(s2vl1st02.getTerminal1().map(t -> compareTerminal(t, "S2VL1_BBS0")).orElse(false));
        assertTrue(s2vl1st02.getTerminal2().map(t -> compareTerminal(t, "Line-S1VL1-S2VL2-9-4")).orElse(false));
    }

    private static Network createNodeBreaker() {
        return createNodeBreaker(NetworkFactory.findDefault());
    }

    private static Network createNodeBreaker(NetworkFactory networkFactory) {

        Network network = networkFactory.createNetwork("SwitchTerminalForVoltage-NodeBreaker", "test");
        network.setCaseDate(DateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.setForecastDistance(0);

        // First substation
        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel s1vl1 = s1.newVoltageLevel()
                .setId("S1VL1")
                .setNominalV(225.0)
                .setLowVoltageLimit(220.0)
                .setHighVoltageLimit(240.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        createBusbarSection(s1vl1, "S1VL1_BBS0", "S1VL1_BBS0", 0);

        VoltageLevel s1vl2 = s1.newVoltageLevel()
                .setId("S1VL2")
                .setNominalV(400.0)
                .setLowVoltageLimit(380.0)
                .setHighVoltageLimit(430.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        createBusbarSection(s1vl2, "S1VL2_BBS0", "S1VL2_BBS0", 0);

        // Second substation
        Substation s2 = network.newSubstation()
                .setId("S2")
                .add();
        VoltageLevel s2vl1 = s2.newVoltageLevel()
                .setId("S2VL1")
                .setNominalV(225.0)
                .setLowVoltageLimit(220.0)
                .setHighVoltageLimit(240.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        createBusbarSection(s2vl1, "S2VL1_BBS0", "S2VL1_BBS0", 0);

        createSwitch(s1vl1, "S1VL1-Sw-0-1", SwitchKind.BREAKER, true, 0, 1);
        createSwitch(s1vl1, "S1VL1-Sw-1-2", SwitchKind.BREAKER, false, 1, 2);
        createSwitch(s1vl1, "S1VL1-Sw-1-3", SwitchKind.BREAKER, false, 1, 3);
        createSwitch(s1vl1, "S1VL1-Sw-3-4", SwitchKind.BREAKER, false, 3, 4);
        createSwitch(s1vl1, "S1VL1-Sw-0-6", SwitchKind.BREAKER, false, 0, 6);
        createSwitch(s1vl1, "S1VL1-Sw-0-5", SwitchKind.BREAKER, true, 0, 5);

        createInternalConnection(s1vl1, 2, 7);
        createInternalConnection(s1vl1, 4, 8);
        createInternalConnection(s1vl1, 6, 9);
        createInternalConnection(s1vl1, 5, 10);

        createSwitch(s1vl2, "S1VL2-Sw-0-1", SwitchKind.DISCONNECTOR, true, 0, 1);
        createSwitch(s1vl2, "S1VL2-Sw-1-2", SwitchKind.DISCONNECTOR, false, 1, 2);
        createSwitch(s1vl2, "S1VL2-Sw-0-3", SwitchKind.DISCONNECTOR, false, 0, 3);
        createSwitch(s1vl2, "S1VL2-Sw-3-4", SwitchKind.DISCONNECTOR, true, 3, 4);

        createInternalConnection(s1vl2, 2, 5);
        createInternalConnection(s1vl2, 4, 6);

        createSwitch(s2vl1, "S2VL1-Sw-0-1", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(s2vl1, "S2VL1-Sw-0-2", SwitchKind.DISCONNECTOR, false, 0, 2);

        createInternalConnection(s2vl1, 1, 3);
        createInternalConnection(s2vl1, 2, 4);

        createGenerator(s1vl1, "S1VL1-Generator-10", 10);
        createLoad(s1vl2, "S1VL2-Load-6", 6);
        createTwoWindingsTransformer(s1, "S1VL1", "S1VL2", "Twt-S1VL1-S1VL2-7-5", 7, 5);

        createLine(network, "S1VL1", "S2VL1", "Line-S1VL1-S2VL1-8-3", 8, 3);
        createLine(network, "S1VL1", "S2VL1", "Line-S1VL1-S2VL2-9-4", 9, 4);

        return network;
    }

    private static void createBusbarSection(VoltageLevel vl, String id, String name, int node) {
        vl.getNodeBreakerView().newBusbarSection()
            .setId(id)
            .setName(name)
            .setNode(node)
            .add();
    }

    private static void createSwitch(VoltageLevel vl, String id, SwitchKind kind, boolean open, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
                .setId(id)
                .setName(id)
                .setKind(kind)
                .setRetained(kind.equals(SwitchKind.BREAKER))
                .setOpen(open)
                .setFictitious(false)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }

    private static void createInternalConnection(VoltageLevel vl, int node1, int node2) {
        vl.getNodeBreakerView().newInternalConnection()
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }

    private static void createLoad(VoltageLevel vl, String id, int node) {
        vl.newLoad()
            .setId(id)
            .setLoadType(LoadType.UNDEFINED)
            .setP0(0.0)
            .setQ0(0.0)
            .setNode(node)
            .add();
    }

    private static void createGenerator(VoltageLevel vl, String id, int node) {
        Generator generator = vl.newGenerator()
            .setId(id)
            .setEnergySource(EnergySource.HYDRO)
            .setMinP(0.0)
            .setMaxP(100.0)
            .setVoltageRegulatorOn(false)
            .setTargetP(0.0)
            .setTargetV(225.0)
            .setTargetQ(0.0)
            .setNode(node)
            .add();
        generator.newMinMaxReactiveLimits()
            .setMinQ(-100)
            .setMaxQ(100)
            .add();
    }

    private static void createLine(Network network, String vl1id, String vl2id, String id, int node1, int node2) {
        network.newLine()
            .setId(id)
            .setR(0.01)
            .setX(20.0)
            .setG1(0.0)
            .setB1(0.0)
            .setG2(0.0)
            .setB2(0.0)
            .setNode1(node1)
            .setVoltageLevel1(vl1id)
            .setNode2(node2)
            .setVoltageLevel2(vl2id)
            .add();
    }

    private static void createTwoWindingsTransformer(Substation s, String vl1id, String vl2id, String id, int node1, int node2) {
        TwoWindingsTransformer twt = s.newTwoWindingsTransformer()
            .setId(id)
            .setR(2.0)
            .setX(14.)
            .setG(0.0)
            .setB(0.0)
            .setRatedU1(225.0)
            .setRatedU2(400.0)
            .setNode1(node1)
            .setVoltageLevel1(vl1id)
            .setNode2(node2)
            .setVoltageLevel2(vl2id)
            .add();
        twt.newCurrentLimits1()
            .setPermanentLimit(1030.0)
            .add();
        twt.newCurrentLimits2()
            .setPermanentLimit(1030.0)
            .add();
    }

    @Test
    void switchTerminalForVoltageBusBreakerTest() {

        Network network = createBusBreaker();

        SwitchTerminalForVoltage s1vl1st01 = new SwitchTerminalForVoltage(network.getSwitch("S1VL1-Sw-BUS0-BUS1"));
        assertTrue(s1vl1st01.getTerminal1().map(t -> compareTerminal(t, "S1VL1-Load")).orElse(false));
        assertTrue(s1vl1st01.getTerminal2().map(t -> compareTerminal(t, "Twt-BUS2-BUS0")).orElse(false));

        SwitchTerminalForVoltage s1vl1st12 = new SwitchTerminalForVoltage(network.getSwitch("S1VL1-Sw-BUS1-BUS2"));
        assertTrue(s1vl1st12.getTerminal1().map(t -> compareTerminal(t, "S1VL1-Load")).orElse(false));
        assertTrue(s1vl1st12.getTerminal2().map(t -> compareTerminal(t, "Twt-BUS2-BUS0")).orElse(false));
    }

    private static Network createBusBreaker() {
        return createBusBreaker(NetworkFactory.findDefault());
    }

    private static Network createBusBreaker(NetworkFactory networkFactory) {

        Network network = networkFactory.createNetwork("SwitchTerminalForVoltage-BusBreaker", "test");
        network.setCaseDate(DateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.setForecastDistance(0);

        // First substation
        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel s1vl1 = s1.newVoltageLevel()
                .setId("S1VL1")
                .setNominalV(225.0)
                .setLowVoltageLimit(220.0)
                .setHighVoltageLimit(240.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();

        createBus(s1vl1, "S1VL1-BUS0");
        createBus(s1vl1, "S1VL1-BUS1");
        createBus(s1vl1, "S1VL1-BUS2");

        VoltageLevel s1vl2 = s1.newVoltageLevel()
            .setId("S1VL2")
            .setNominalV(400.0)
            .setLowVoltageLimit(380.0)
            .setHighVoltageLimit(430.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();

        createBus(s1vl2, "S1VL2-BUS0");

        // Switches
        createSwitch(s1vl1, "S1VL1-Sw-BUS0-BUS1", false, "S1VL1-BUS0", "S1VL1-BUS1");
        createSwitch(s1vl1, "S1VL1-Sw-BUS1-BUS2", false, "S1VL1-BUS1", "S1VL1-BUS2");

        createLoad(s1vl1, "S1VL1-Load", "S1VL1-BUS0");

        // Other voltage level

        createLoad(s1vl2, "S1VL2-Load", "S1VL2-BUS0");

        // Inside the substation
        createTwoWindingsTransformer(s1, "S1VL1", "S1VL2", "Twt-BUS2-BUS0", "S1VL1-BUS2", "S1VL2-BUS0");

        return network;
    }

    private static void createBus(VoltageLevel voltageLevel, String id) {
        voltageLevel.getBusBreakerView()
            .newBus()
            .setName(id)
            .setId(id)
            .add();
    }

    private static void createSwitch(VoltageLevel voltageLevel, String id, boolean open, String busId1, String busId2) {
        voltageLevel.getBusBreakerView()
            .newSwitch()
            .setId(id)
            .setOpen(open)
            .setBus1(id)
            .setBus1(busId1)
            .setBus2(busId2)
            .add();
    }

    private static void createLoad(VoltageLevel voltageLevel, String id, String busId) {
        voltageLevel.newLoad()
            .setId(id)
            .setLoadType(LoadType.UNDEFINED)
            .setP0(0.0)
            .setQ0(0.0)
            .setBus(busId)
            .add();
    }

    private static void createTwoWindingsTransformer(Substation s, String vl1id, String vl2id, String id,
        String busId1, String busId2) {
        TwoWindingsTransformer twt = s.newTwoWindingsTransformer()
            .setId(id)
            .setR(2.0)
            .setX(14.)
            .setG(0.0)
            .setB(0.0)
            .setRatedU1(225.0)
            .setRatedU2(400.0)
            .setBus1(busId1)
            .setVoltageLevel1(vl1id)
            .setBus2(busId2)
            .setVoltageLevel2(vl2id)
            .add();
        twt.newCurrentLimits1()
            .setPermanentLimit(1030.0)
            .add();
        twt.newCurrentLimits2()
            .setPermanentLimit(1030.0)
            .add();
    }

    private static boolean compareTerminal(Terminal terminal, String eqId) {
        return eqId.equals(terminal.getConnectable().getId());
    }
}
