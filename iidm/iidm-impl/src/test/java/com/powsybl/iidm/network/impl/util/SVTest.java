/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import org.junit.jupiter.api.Test;

import com.powsybl.iidm.network.util.SV;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class SVTest {

    @Test
    void testOlfRealNetwork() {
        Network network = createNodeBreakerDanglingLineNetwork();
        svOlfDataToNetwork(network);

        Line line = network.getLine("Line-2-2");
        DanglingLine dl = network.getDanglingLine("Dl-3");
        Bus bus1 = network.getBusBreakerView().getBus("S1VL1_0");
        Bus bus2 = network.getBusBreakerView().getBus("S2VL1_0");

        double tol = 0.00001;
        SV svL1 = new SV(line.getTerminal1().getP(), line.getTerminal1().getQ(), bus1.getV(), bus1.getAngle(), TwoSides.ONE);
        SV svL1other = svL1.otherSide(line);
        assertEquals(line.getTerminal2().getP(), svL1other.getP(), tol);
        assertEquals(line.getTerminal2().getQ(), svL1other.getQ(), tol);
        assertEquals(bus2.getV(), svL1other.getU(), tol);
        assertEquals(bus2.getAngle(), svL1other.getA(), tol);

        SV svL2 = new SV(line.getTerminal2().getP(), line.getTerminal2().getQ(), bus2.getV(), bus2.getAngle(), TwoSides.TWO);
        SV svL2other = svL2.otherSide(line);
        assertEquals(line.getTerminal1().getP(), svL2other.getP(), tol);
        assertEquals(line.getTerminal1().getQ(), svL2other.getQ(), tol);
        assertEquals(bus1.getV(), svL2other.getU(), tol);
        assertEquals(bus1.getAngle(), svL2other.getA(), tol);

        assertEquals(line.getTerminal1().getP(), svL2.otherSideP(line.getR(), line.getX(), line.getG1(), line.getB1(), line.getG2(), line.getB2(), 1.0, 0.0), tol);
        assertEquals(line.getTerminal1().getQ(), svL2.otherSideQ(line.getR(), line.getX(), line.getG1(), line.getB1(), line.getG2(), line.getB2(), 1.0, 0.0), tol);
        assertEquals(bus1.getV(), svL2.otherSideU(line.getR(), line.getX(), line.getG1(), line.getB1(), line.getG2(), line.getB2(), 1.0, 0.0), tol);
        assertEquals(bus1.getAngle(), svL2.otherSideA(line.getR(), line.getX(), line.getG1(), line.getB1(), line.getG2(), line.getB2(), 1.0, 0.0), tol);

        SV svDl1 = new SV(dl.getTerminal().getP(), dl.getTerminal().getQ(), bus2.getV(), bus2.getAngle(), TwoSides.ONE);
        SV svDl1other = svDl1.otherSide(dl);
        assertEquals(-dl.getP0(), svDl1other.getP(), tol);
        assertEquals(-dl.getQ0(), svDl1other.getQ(), tol);
        assertEquals(225.1798987500, svDl1other.getU(), tol);
        assertEquals(-0.4183680524, svDl1other.getA(), tol);

        assertEquals(-dl.getP0(), dl.getBoundary().getP(), tol);
        assertEquals(-dl.getQ0(), dl.getBoundary().getQ(), tol);
    }

    @Test
    void testDcOlfRealNetwork() {
        Network network = createNodeBreakerDanglingLineNetwork();
        svDcOlfDataToNetwork(network);

        Line line = network.getLine("Line-2-2");
        DanglingLine dl = network.getDanglingLine("Dl-3");
        Bus bus1 = network.getBusBreakerView().getBus("S1VL1_0");
        Bus bus2 = network.getBusBreakerView().getBus("S2VL1_0");

        double tol = 0.00001;
        SV svL1 = new SV(line.getTerminal1().getP(), line.getTerminal1().getQ(), bus1.getV(), bus1.getAngle(), TwoSides.ONE);
        SV svL1other = svL1.otherSide(line);
        assertEquals(line.getTerminal2().getP(), svL1other.getP(), tol);
        assertEquals(bus2.getAngle(), svL1other.getA(), tol);

        SV svL2 = new SV(line.getTerminal2().getP(), line.getTerminal2().getQ(), bus2.getV(), bus2.getAngle(), TwoSides.TWO);
        SV svL2other = svL2.otherSide(line);
        assertEquals(line.getTerminal1().getP(), svL2other.getP(), tol);
        assertEquals(bus1.getAngle(), svL2other.getA(), tol);

        SV svDl1 = new SV(dl.getTerminal().getP(), dl.getTerminal().getQ(), bus2.getV(), bus2.getAngle(), TwoSides.ONE);
        SV svDl1other = svDl1.otherSide(dl);
        assertEquals(-dl.getP0(), svDl1other.getP(), tol);
        assertEquals(-0.4187543391573424, svDl1other.getA(), tol);

        assertEquals(-dl.getP0(), dl.getBoundary().getP(), tol);
    }

    private static void svOlfDataToNetwork(Network network) {
        Line line = network.getLine("Line-2-2");
        DanglingLine dl = network.getDanglingLine("Dl-3");
        Bus bus1 = network.getBusBreakerView().getBus("S1VL1_0");
        Bus bus2 = network.getBusBreakerView().getBus("S2VL1_0");

        // Voltages at the buses
        bus1.setV(225.0).setAngle(0.0);
        bus2.setV(225.2726820000).setAngle(-0.2603514491);

        line.getTerminal1().setP(115.003788).setQ(-56.302621);
        line.getTerminal2().setP(-115.000986).setQ(6.176675);

        dl.getTerminal().setP(70.000986).setQ(-15.176675);
    }

    private static void svDcOlfDataToNetwork(Network network) {
        Line line = network.getLine("Line-2-2");
        DanglingLine dl = network.getDanglingLine("Dl-3");
        Bus bus1 = network.getBusBreakerView().getBus("S1VL1_0");
        Bus bus2 = network.getBusBreakerView().getBus("S2VL1_0");

        // Voltages at the buses
        bus1.setAngle(0.0);
        bus2.setAngle(-0.26030675136807774);

        line.getTerminal1().setP(115.0);
        line.getTerminal2().setP(-115.0);

        dl.getTerminal().setP(70.0);
    }

    private static Network createNodeBreakerDanglingLineNetwork() {
        return createNodeBreakerDanglingLineNetwork(NetworkFactory.findDefault());
    }

    private static Network createNodeBreakerDanglingLineNetwork(NetworkFactory networkFactory) {

        Network network = networkFactory.createNetwork("twoBusesWithLineAndDanglingLine", "test");
        double vn = 225.0;

        // First substation
        Substation s1 = network.newSubstation()
            .setId("S1")
            .add();
        VoltageLevel s1vl1 = s1.newVoltageLevel()
            .setId("S1VL1")
            .setNominalV(vn)
            .setLowVoltageLimit(vn * 0.9)
            .setHighVoltageLimit(vn * 1.1)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();

        createBusbarSection(s1vl1, "S1VL1_BBS0A", "S1VL1_BBS0A", 0);
        createInternalConnection(s1vl1, 0, 1);
        createInternalConnection(s1vl1, 0, 2);
        createGenerator(s1vl1, "S1VL1-Generator", vn, 80.0, 10.0, 1);

        // Second substation
        Substation s2 = network.newSubstation()
            .setId("S2")
            .add();
        VoltageLevel s2vl1 = s2.newVoltageLevel()
            .setId("S2VL1")
            .setNominalV(vn)
            .setLowVoltageLimit(vn * 0.9)
            .setHighVoltageLimit(vn * 1.1)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();

        createBusbarSection(s2vl1, "S2VL1_BBS0", "S2VL1_BBS0", 0);
        createInternalConnection(s2vl1, 0, 1);
        createInternalConnection(s2vl1, 0, 2);
        createInternalConnection(s2vl1, 0, 3);
        createLoad(s2vl1, "S2VL1-Load", 45.0, 9.0, 1);

        createDanglingLine(network, "S2VL1", "Dl-3", 70.0, 10.0, "ucteNode", 3);

        // Line between both substations
        createLine(network, "S1VL1", "S2VL1", "Line-2-2", 2, 2);

        return network;
    }

    private static void createBusbarSection(VoltageLevel vl, String id, String name, int node) {
        vl.getNodeBreakerView().newBusbarSection()
            .setId(id)
            .setName(name)
            .setNode(node)
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

    private static void createGenerator(VoltageLevel vl, String id, double targetV, double p, double q, int node) {
        Generator generator = vl.newGenerator()
            .setId(id)
            .setEnergySource(EnergySource.HYDRO)
            .setMinP(-500.0)
            .setMaxP(500.0)
            .setVoltageRegulatorOn(true)
            .setTargetP(p)
            .setTargetV(targetV)
            .setTargetQ(q)
            .setNode(node)
            .add();
        generator.newMinMaxReactiveLimits()
            .setMinQ(-500.0)
            .setMaxQ(500.0);
        generator.getTerminal().setP(-p).setQ(-q);
    }

    private static void createLine(Network network, String vl1id, String vl2id, String id, int node1, int node2) {
        network.newLine()
            .setId(id)
            .setR(0.01)
            .setX(2.0)
            .setG1(0.0)
            .setB1(0.0005)
            .setG2(0.0)
            .setB2(0.0005)
            .setNode1(node1)
            .setVoltageLevel1(vl1id)
            .setNode2(node2)
            .setVoltageLevel2(vl2id)
            .add();
    }

    private static void createDanglingLine(Network network, String vlId, String id, double p0, double q0, String ucteCode, int node) {
        network.getVoltageLevel(vlId).newDanglingLine()
            .setId(id)
            .setR(0.01)
            .setX(2.0)
            .setG(0.0)
            .setB(0.0005)
            .setP0(p0)
            .setQ0(q0)
            .setPairingKey(ucteCode)
            .setNode(node)
            .setEnsureIdUnicity(false)
            .add();
    }

    @Test
    void testWithGeneration() {
        double tol = 0.001;
        Network network = DanglingLineNetworkFactory.createWithGeneration();
        DanglingLine danglingLine = network.getDanglingLine("DL");
        assertTrue(Double.isNaN(danglingLine.getBoundary().getP())); // there is no good solution here.
        // we run an DC load flow and fill state variable
        danglingLine.getTerminal().setP(-298.937);
        danglingLine.getTerminal().setQ(Double.NaN);
        danglingLine.getTerminal().getBusView().getBus().setAngle(0.0);
        danglingLine.getTerminal().getBusView().getBus().setV(Double.NaN);
        assertEquals(298.937, danglingLine.getBoundary().getP(), tol);
        assertEquals(1.712783, danglingLine.getBoundary().getAngle(), tol);
        // we run an AC load flow
        danglingLine.getTerminal().setP(-298.937);
        danglingLine.getTerminal().setQ(-7.413);
        danglingLine.getTerminal().getBusView().getBus().setAngle(0.0);
        danglingLine.getTerminal().getBusView().getBus().setV(100.0);
        assertEquals(389.953, danglingLine.getBoundary().getP(), tol);
        assertEquals(16.314, danglingLine.getBoundary().getQ(), tol);
        assertEquals(130.087, danglingLine.getBoundary().getV(), tol);
        assertEquals(0.999, danglingLine.getBoundary().getAngle(), tol);
    }

    @Test
    void testWithZeroImpedanceDanglingLineWithGeneration() {
        double tol = 0.001;
        Network network = DanglingLineNetworkFactory.createWithGeneration();
        DanglingLine danglingLine = network.getDanglingLine("DL");
        danglingLine.setR(0.0).setX(0.0);
        danglingLine.getTerminal().setP(-298.937);
        danglingLine.getTerminal().setQ(-7.413);
        danglingLine.getTerminal().getBusView().getBus().setAngle(0.0);
        danglingLine.getTerminal().getBusView().getBus().setV(100.0);
        assertEquals(298.937, danglingLine.getBoundary().getP(), tol);
        assertEquals(7.413, danglingLine.getBoundary().getQ(), tol);
        assertEquals(100.0, danglingLine.getBoundary().getV(), tol);
        assertEquals(0.0, danglingLine.getBoundary().getAngle(), tol);
    }

    @Test
    void testWithZeroImpedanceDanglingLineWithoutGeneration() {
        double tol = 0.001;
        Network network = DanglingLineNetworkFactory.create();
        DanglingLine danglingLine = network.getDanglingLine("DL");
        danglingLine.setR(0.0).setX(0.0);
        danglingLine.getTerminal().setP(50.0);
        danglingLine.getTerminal().setQ(30.0);
        danglingLine.getTerminal().getBusView().getBus().setAngle(0.0);
        danglingLine.getTerminal().getBusView().getBus().setV(100.0);
        assertEquals(-50.0, danglingLine.getBoundary().getP(), tol);
        assertEquals(-30.0, danglingLine.getBoundary().getQ(), tol);
        assertEquals(100.0, danglingLine.getBoundary().getV(), tol);
        assertEquals(0.0, danglingLine.getBoundary().getAngle(), tol);
    }
}
