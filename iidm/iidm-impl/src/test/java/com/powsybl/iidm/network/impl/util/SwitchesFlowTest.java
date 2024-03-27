/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.SwitchesFlow;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class SwitchesFlowTest {

    @Test
    void switchesFlowNodeBreaker() {

        Network network = createNodeBreaker();

        VoltageLevel voltageLevel11 = network.getVoltageLevel("S1VL1");
        SwitchesFlow switchesFlow11 = new SwitchesFlow(voltageLevel11);

        assertTrue(compareSwitchFlow(switchesFlow11, "S1VL1-SW-0B-1B", -180.0, -19.0));
        assertTrue(compareSwitchFlow(switchesFlow11, "S1VL1-SW-0A-1A", 10.0, -1.0));
        assertTrue(compareSwitchFlow(switchesFlow11, "S1VL1-SW-1B-2B", 70.0, 10.0));
        assertTrue(compareSwitchFlow(switchesFlow11, "S1VL1-SW-0B-10", 100.0, 10.0));
        assertTrue(compareSwitchFlow(switchesFlow11, "S1VL1-SW-1A-8", -80.0, -10.0));
        assertTrue(compareSwitchFlow(switchesFlow11, "S1VL1-SW-0A-0B", -80.0, -9.0));
        assertTrue(compareSwitchFlow(switchesFlow11, "S1VL1-SW-2A-2B", 0.0, 0.0));
        assertTrue(compareSwitchFlow(switchesFlow11, "S1VL1-SW-1A-1B", 0.0, 0.0));
        assertTrue(compareSwitchFlow(switchesFlow11, "S1VL1-SW-2B-12", 70.0, 10.0));
        assertTrue(compareSwitchFlow(switchesFlow11, "S1VL1-SW-1A-2A", 90.0, 9.0));

        VoltageLevel voltageLevel12 = network.getVoltageLevel("S1VL2");
        SwitchesFlow switchesFlow12 = new SwitchesFlow(voltageLevel12);

        assertTrue(compareSwitchFlow(switchesFlow12, "S1VL2-SW-0-1", 251.0, 35.0));
        assertTrue(compareSwitchFlow(switchesFlow12, "S1VL2-SW-0-2", -251.0, -35.0));

        VoltageLevel voltageLevel21 = network.getVoltageLevel("S2VL1");
        SwitchesFlow switchesFlow21 = new SwitchesFlow(voltageLevel21);

        assertTrue(compareSwitchFlow(switchesFlow21, "S2VL1-SW-0-1", -75.0, -15.0));
        assertTrue(compareSwitchFlow(switchesFlow21, "S2VL1-SW-0-2", 75.0, 15.0));

        VoltageLevel voltageLevel31 = network.getVoltageLevel("S3VL1");
        SwitchesFlow switchesFlow31 = new SwitchesFlow(voltageLevel31);

        assertTrue(compareSwitchFlow(switchesFlow31, "S3VL1-SW-0-1", -71.0, -11.0));
        assertTrue(compareSwitchFlow(switchesFlow31, "S3VL1-SW-0-2", 71.0, 11.0));
    }

    private static Network createNodeBreaker() {
        return createNodeBreaker(NetworkFactory.findDefault());
    }

    private static Network createNodeBreaker(NetworkFactory networkFactory) {

        Network network = networkFactory.createNetwork("SwitchesFlow-NodeBreaker", "test");
        network.setCaseDate(ZonedDateTime.parse("2017-06-25T17:43:00.000+01:00"));
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

        createBusbarSection(s1vl1, "S1VL1_BBS0A", "S1VL1_BBS0A", 0);
        createBusbarSection(s1vl1, "S1VL1_BBS1A", "S1VL1_BBS1A", 1);
        createBusbarSection(s1vl1, "S1VL1_BBS2A", "S1VL1_BBS2A", 2);

        createBusbarSection(s1vl1, "S1VL1_BBS0B", "S1VL1_BBS0B", 3);
        createBusbarSection(s1vl1, "S1VL1_BBS1B", "S1VL1_BBS1B", 4);
        createBusbarSection(s1vl1, "S1VL1_BBS2B", "S1VL1_BBS2B", 5);

        createBusbarSection(s1vl1, "S1VL1_BBS0C", "S1VL1_BBS0C", 6);

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

        // Third substation
        Substation s3 = network.newSubstation()
                .setId("S3")
                .add();
        VoltageLevel s3vl1 = s3.newVoltageLevel()
                .setId("S3VL1")
                .setNominalV(225.0)
                .setLowVoltageLimit(220.0)
                .setHighVoltageLimit(240.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        createBusbarSection(s3vl1, "S3VL1_BBS0", "S3VL1_BBS0", 0);

        // Switches and internalConnections on the first voltage level of substation 1
        // Connect a load on the first voltage level of substation 1
        createSwitch(s1vl1, "S1VL1-SW-0A-1A", SwitchKind.BREAKER, false, 0, 1);
        createSwitch(s1vl1, "S1VL1-SW-1A-2A", SwitchKind.BREAKER, false, 1, 2);
        createSwitch(s1vl1, "S1VL1-SW-0B-1B", SwitchKind.BREAKER, false, 3, 4);
        createSwitch(s1vl1, "S1VL1-SW-1B-2B", SwitchKind.BREAKER, false, 4, 5);

        createSwitch(s1vl1, "S1VL1-SW-0A-0B", SwitchKind.BREAKER, false, 0, 3);
        createSwitch(s1vl1, "S1VL1-SW-1A-1B", SwitchKind.BREAKER, false, 1, 4);
        createSwitch(s1vl1, "S1VL1-SW-2A-2B", SwitchKind.BREAKER, false, 2, 5);

        createSwitch(s1vl1, "S1VL1-SW-0A-1B", SwitchKind.DISCONNECTOR, true, 0, 4);

        createSwitch(s1vl1, "S1VL1-SW-1A-8", SwitchKind.DISCONNECTOR, false, 1, 8);
        createSwitch(s1vl1, "S1VL1-SW-0B-10", SwitchKind.DISCONNECTOR, false, 3, 10);
        createSwitch(s1vl1, "S1VL1-SW-2B-12", SwitchKind.DISCONNECTOR, false, 5, 12);

        createSwitch(s1vl1, "S1VL1-SW-0C-13", SwitchKind.DISCONNECTOR, true, 6, 13);

        createInternalConnection(s1vl1, 0, 7);
        createInternalConnection(s1vl1, 2, 9);
        createInternalConnection(s1vl1, 4, 11);
        createInternalConnection(s1vl1, 5, 12);
        createInternalConnection(s1vl1, 13, 14);

        // Add two nodes
        createSwitch(s1vl2, "S1VL2-SW-0-1", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(s1vl2, "S1VL2-SW-0-2", SwitchKind.DISCONNECTOR, false, 0, 2);

        createSwitch(s2vl1, "S2VL1-SW-0-1", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(s2vl1, "S2VL1-SW-0-2", SwitchKind.DISCONNECTOR, false, 0, 2);

        createSwitch(s3vl1, "S3VL1-SW-0-1", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(s3vl1, "S3VL1-SW-0-2", SwitchKind.DISCONNECTOR, false, 0, 2);

        // Create Loads
        createLoad(s1vl1, "S1VL1-Load-9", 90.0, 9.0, 9);
        createLoad(s1vl1, "S1VL1-Load-10", 100.0, 10.0, 10);
        createLoad(s1vl1, "S1VL1-Load-14", 0.0, 0.0, 14);

        // Generators
        createGenerator(s1vl1, "S1VL1-Generator-8", 80.0, 10.0, 8);

        createTwoWindingsTransformer(s1, "S1VL1", "S1VL2", "S1-TWT-11-0", -250.0, -29.0, 251.0, 35.0, 11, 1);

        // Connect a load at the first substation, second voltage level
        createLoad(s1vl2, "S1VL2-Load-0", -251.0, -35.0, 2);

        // Connect a load at the second substation
        createLoad(s2vl1, "S2VL1-Load-0", 75.0, 15.0, 2);

        // Connect a load at the third substation
        createLoad(s3vl1, "S3VL1-Load-0", 71.0, 11.0, 2);

        // Create two lines
        createLine(network, "S1VL1", "S2VL1", "Line-7-S2VL1", 70.0, 10.0, -75.0, -15.0, 7, 1);
        createLine(network, "S1VL1", "S3VL1", "Line-12-S3VL1", 70.0, 10.0, -71.0, -11.0, 12, 1);

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

    private static void createLoad(VoltageLevel vl, String id, double p, double q, int node) {
        Load load = vl.newLoad()
            .setId(id)
            .setLoadType(LoadType.UNDEFINED)
            .setP0(p)
            .setQ0(q)
            .setNode(node)
            .add();
        load.getTerminal().setP(p).setQ(q);
    }

    private static void createGenerator(VoltageLevel vl, String id, double p, double q, int node) {
        Generator generator = vl.newGenerator()
            .setId(id)
            .setEnergySource(EnergySource.HYDRO)
            .setMinP(0.0)
            .setMaxP(100.0)
            .setVoltageRegulatorOn(false)
            .setTargetP(p)
            .setTargetV(225.0)
            .setTargetQ(q)
            .setNode(node)
            .add();
        generator.newMinMaxReactiveLimits()
            .setMinQ(-100)
            .setMaxQ(100)
            .add();
        generator.getTerminal().setP(-p).setQ(-q);

    }

    private static void createLine(Network network, String vl1id, String vl2id, String id, double p1, double q1,
        double p2, double q2, int node1, int node2) {
        Line line = network.newLine()
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
        line.getTerminal1().setP(p1).setQ(q1);
        line.getTerminal2().setP(p2).setQ(q2);
    }

    private static void createTwoWindingsTransformer(Substation s, String vl1id, String vl2id, String id,
        double p1, double q1, double p2, double q2, int node1, int node2) {
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
        twt.getTerminal1().setP(p1).setQ(q1);
        twt.getTerminal2().setP(p2).setQ(q2);
    }

    @Test
    void switchesFlowBusBreaker() {

        Network network = createBusBreaker();

        VoltageLevel voltageLevel11 = network.getVoltageLevel("S1VL1");
        SwitchesFlow switchesFlow11 = new SwitchesFlow(voltageLevel11);

        assertTrue(compareSwitchFlow(switchesFlow11, "S1VL1-SW-BUS0-BUS1", -25.0, -10.5));
        assertTrue(compareSwitchFlow(switchesFlow11, "S1VL1-SW-BUS1-BUS2", -25.0, -10.5));
        assertTrue(compareSwitchFlow(switchesFlow11, "S1VL1-SW-BUS0-BUS2", 0.0, 0.0));

        VoltageLevel voltageLevel12 = network.getVoltageLevel("S1VL2");
        SwitchesFlow switchesFlow12 = new SwitchesFlow(voltageLevel12);

        assertTrue(switchesFlow12.isEmpty());
    }

    private static Network createBusBreaker() {
        return createBusBreaker(NetworkFactory.findDefault());
    }

    private static Network createBusBreaker(NetworkFactory networkFactory) {

        Network network = networkFactory.createNetwork("SwitchesFlow-BusBreaker", "test");
        network.setCaseDate(ZonedDateTime.parse("2017-06-25T17:43:00.000+01:00"));
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
        createSwitch(s1vl1, "S1VL1-SW-BUS0-BUS1", false, "S1VL1-BUS0", "S1VL1-BUS1");
        createSwitch(s1vl1, "S1VL1-SW-BUS1-BUS2", false, "S1VL1-BUS1", "S1VL1-BUS2");
        createSwitch(s1vl1, "S1VL1-SW-BUS0-BUS2", false, "S1VL1-BUS0", "S1VL1-BUS2");

        createLoad(s1vl1, "S1VL1-Load", 25.0, 10.5, "S1VL1-BUS0");

        // Other voltage level

        createLoad(s1vl2, "S1VL2-Load", -26.0, -12.5, "S1VL2-BUS0");

        // Inside the substation
        createTwoWindingsTransformer(s1, "S1VL1", "S1VL2", "S1-TWT-BUS2-BUS0", -25.0, -10.5, 26.0, 12.5, "S1VL1-BUS2", "S1VL2-BUS0");

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

    private static void createLoad(VoltageLevel voltageLevel, String id, double p, double q, String busId) {
        Load load = voltageLevel.newLoad()
            .setId(id)
            .setLoadType(LoadType.UNDEFINED)
            .setP0(p)
            .setQ0(q)
            .setBus(busId)
            .add();
        load.getTerminal().setP(p).setQ(q);
    }

    private static void createTwoWindingsTransformer(Substation s, String vl1id, String vl2id, String id,
        double p1, double q1, double p2, double q2, String busId1, String busId2) {
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
        twt.getTerminal1().setP(p1).setQ(q1);
        twt.getTerminal2().setP(p2).setQ(q2);
    }

    private static boolean compareSwitchFlow(SwitchesFlow switchesFlow, String id, double p1, double q1) {
        double tol = 0.000001;
        if (Math.abs(p1 - switchesFlow.getP1(id)) > tol) {
            return false;
        }
        if (Math.abs(q1 - switchesFlow.getQ1(id)) > tol) {
            return false;
        }
        if (Math.abs(-p1 - switchesFlow.getP2(id)) > tol) {
            return false;
        }
        if (Math.abs(-q1 - switchesFlow.getQ2(id)) > tol) {
            return false;
        }
        return true;
    }
}
