/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Test;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.TieLineAdder;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ReorientedBranchCharacteristics;
import com.powsybl.iidm.network.util.SV;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class TieLineTest {

    @Test
    public void tieLineTest0() {

        // Line1 from node1 to boundaryNode, Line2 from boundaryNode to node2
        CaseSv caseSv0 = createCase0();
        Network n = createNetworkWithTieLine(NetworkFactory.findDefault(), Branch.Side.TWO, Branch.Side.ONE, caseSv0);
        TieLine tieLine = (TieLine) n.getLine("TWO + ONE");

        SV sv2 = new SV(tieLine.getTerminal1().getP(), tieLine.getTerminal1().getQ(),
            tieLine.getTerminal1().getBusView().getBus().getV(),
            tieLine.getTerminal1().getBusView().getBus().getAngle(),
            Branch.Side.ONE).otherSide(tieLine);
        assertTrue(compare(sv2, caseSv0.node2, caseSv0.line2, Branch.Side.ONE));

        SV sv1 = new SV(tieLine.getTerminal2().getP(), tieLine.getTerminal2().getQ(),
            tieLine.getTerminal2().getBusView().getBus().getV(),
            tieLine.getTerminal2().getBusView().getBus().getAngle(),
            Branch.Side.TWO).otherSide(tieLine);
        assertTrue(compare(sv1, caseSv0.node1, caseSv0.line1, Branch.Side.TWO));

        assertTrue(compare(caseSv0.nodeBoundary.v, tieLine.getHalf1().getBoundary().getV()));
        assertTrue(compare(caseSv0.nodeBoundary.a, tieLine.getHalf1().getBoundary().getAngle()));
        assertTrue(compare(getP(caseSv0.line1, Branch.Side.TWO), tieLine.getHalf1().getBoundary().getP()));
        assertTrue(compare(getQ(caseSv0.line1, Branch.Side.TWO), tieLine.getHalf1().getBoundary().getQ()));

        assertTrue(compare(caseSv0.nodeBoundary.v, tieLine.getHalf2().getBoundary().getV()));
        assertTrue(compare(caseSv0.nodeBoundary.a, tieLine.getHalf2().getBoundary().getAngle()));
        assertTrue(compare(getP(caseSv0.line2, Branch.Side.ONE), tieLine.getHalf2().getBoundary().getP()));
        assertTrue(compare(getQ(caseSv0.line2, Branch.Side.ONE), tieLine.getHalf2().getBoundary().getQ()));
    }

    @Test
    public void tieLineTest1() {

        // Line1 from node1 to boundaryNode, Line2 from node2 to boundaryNode
        CaseSv caseSv1 = createCase1();
        Network n = createNetworkWithTieLine(NetworkFactory.findDefault(), Branch.Side.TWO, Branch.Side.TWO, caseSv1);
        TieLine tieLine = (TieLine) n.getLine("TWO + TWO");

        SV sv2 = new SV(tieLine.getTerminal1().getP(), tieLine.getTerminal1().getQ(),
            tieLine.getTerminal1().getBusView().getBus().getV(),
            tieLine.getTerminal1().getBusView().getBus().getAngle(),
            Branch.Side.ONE).otherSide(tieLine);
        assertTrue(compare(sv2, caseSv1.node2, caseSv1.line2, Branch.Side.TWO));

        SV sv1 = new SV(tieLine.getTerminal2().getP(), tieLine.getTerminal2().getQ(),
            tieLine.getTerminal2().getBusView().getBus().getV(),
            tieLine.getTerminal2().getBusView().getBus().getAngle(),
            Branch.Side.TWO).otherSide(tieLine);
        assertTrue(compare(sv1, caseSv1.node1, caseSv1.line1, Branch.Side.TWO));

        assertTrue(compare(caseSv1.nodeBoundary.v, tieLine.getHalf1().getBoundary().getV()));
        assertTrue(compare(caseSv1.nodeBoundary.a, tieLine.getHalf1().getBoundary().getAngle()));
        assertTrue(compare(getP(caseSv1.line1, Branch.Side.TWO), tieLine.getHalf1().getBoundary().getP()));
        assertTrue(compare(getQ(caseSv1.line1, Branch.Side.TWO), tieLine.getHalf1().getBoundary().getQ()));

        assertTrue(compare(caseSv1.nodeBoundary.v, tieLine.getHalf2().getBoundary().getV()));
        assertTrue(compare(caseSv1.nodeBoundary.a, tieLine.getHalf2().getBoundary().getAngle()));
        assertTrue(compare(getP(caseSv1.line2, Branch.Side.TWO), tieLine.getHalf2().getBoundary().getP()));
        assertTrue(compare(getQ(caseSv1.line2, Branch.Side.TWO), tieLine.getHalf2().getBoundary().getQ()));
    }

    @Test
    public void tieLineTest2() {

        // Line1 from boundaryNode to node1, Line2 from boundaryNode to node2
        CaseSv caseSv2 = createCase2();
        Network n = createNetworkWithTieLine(NetworkFactory.findDefault(), Branch.Side.ONE, Branch.Side.ONE, caseSv2);
        TieLine tieLine = (TieLine) n.getLine("ONE + ONE");

        SV sv2 = new SV(tieLine.getTerminal1().getP(), tieLine.getTerminal1().getQ(),
            tieLine.getTerminal1().getBusView().getBus().getV(),
            tieLine.getTerminal1().getBusView().getBus().getAngle(),
            Branch.Side.ONE).otherSide(tieLine);
        assertTrue(compare(sv2, caseSv2.node2, caseSv2.line2, Branch.Side.ONE));

        SV sv1 = new SV(tieLine.getTerminal2().getP(), tieLine.getTerminal2().getQ(),
            tieLine.getTerminal2().getBusView().getBus().getV(),
            tieLine.getTerminal2().getBusView().getBus().getAngle(),
            Branch.Side.TWO).otherSide(tieLine);
        assertTrue(compare(sv1, caseSv2.node1, caseSv2.line1, Branch.Side.ONE));

        assertTrue(compare(caseSv2.nodeBoundary.v, tieLine.getHalf1().getBoundary().getV()));
        assertTrue(compare(caseSv2.nodeBoundary.a, tieLine.getHalf1().getBoundary().getAngle()));
        assertTrue(compare(getP(caseSv2.line1, Branch.Side.ONE), tieLine.getHalf1().getBoundary().getP()));
        assertTrue(compare(getQ(caseSv2.line1, Branch.Side.ONE), tieLine.getHalf1().getBoundary().getQ()));

        assertTrue(compare(caseSv2.nodeBoundary.v, tieLine.getHalf2().getBoundary().getV()));
        assertTrue(compare(caseSv2.nodeBoundary.a, tieLine.getHalf2().getBoundary().getAngle()));
        assertTrue(compare(getP(caseSv2.line2, Branch.Side.ONE), tieLine.getHalf2().getBoundary().getP()));
        assertTrue(compare(getQ(caseSv2.line2, Branch.Side.ONE), tieLine.getHalf2().getBoundary().getQ()));
    }

    @Test
    public void tieLineTest3() {

        // Line1 from boundaryNode to node1, Line2 from node2 to boundaryNode
        CaseSv caseSv3 = createCase3();
        Network n = createNetworkWithTieLine(NetworkFactory.findDefault(), Branch.Side.ONE, Branch.Side.TWO, caseSv3);
        TieLine tieLine = (TieLine) n.getLine("ONE + TWO");

        SV sv2 = new SV(tieLine.getTerminal1().getP(), tieLine.getTerminal1().getQ(),
            tieLine.getTerminal1().getBusView().getBus().getV(),
            tieLine.getTerminal1().getBusView().getBus().getAngle(),
            Branch.Side.ONE).otherSide(tieLine);
        assertTrue(compare(sv2, caseSv3.node2, caseSv3.line2, Branch.Side.TWO));

        SV sv1 = new SV(tieLine.getTerminal2().getP(), tieLine.getTerminal2().getQ(),
            tieLine.getTerminal2().getBusView().getBus().getV(),
            tieLine.getTerminal2().getBusView().getBus().getAngle(),
            Branch.Side.TWO).otherSide(tieLine);
        assertTrue(compare(sv1, caseSv3.node1, caseSv3.line1, Branch.Side.ONE));

        assertTrue(compare(caseSv3.nodeBoundary.v, tieLine.getHalf1().getBoundary().getV()));
        assertTrue(compare(caseSv3.nodeBoundary.a, tieLine.getHalf1().getBoundary().getAngle()));
        assertTrue(compare(getP(caseSv3.line1, Branch.Side.ONE), tieLine.getHalf1().getBoundary().getP()));
        assertTrue(compare(getQ(caseSv3.line1, Branch.Side.ONE), tieLine.getHalf1().getBoundary().getQ()));

        assertTrue(compare(caseSv3.nodeBoundary.v, tieLine.getHalf2().getBoundary().getV()));
        assertTrue(compare(caseSv3.nodeBoundary.a, tieLine.getHalf2().getBoundary().getAngle()));
        assertTrue(compare(getP(caseSv3.line2, Branch.Side.TWO), tieLine.getHalf2().getBoundary().getP()));
        assertTrue(compare(getQ(caseSv3.line2, Branch.Side.TWO), tieLine.getHalf2().getBoundary().getQ()));
    }

    @Test
    public void tieLineWithDifferentNominalVoltageAtEndsTest() {

        // Line1 from node1 to boundaryNode, Line2 from boundaryNode to node2
        CaseSv caseSv = createCaseDifferentNominalVoltageAtEnds();
        Network n = createNetworkWithTieLineWithDifferentNominalVoltageAtEnds(NetworkFactory.findDefault(), Branch.Side.TWO, Branch.Side.ONE, caseSv);
        TieLine tieLine = (TieLine) n.getLine("TWO + ONE");

        SV sv2 = new SV(tieLine.getTerminal1().getP(), tieLine.getTerminal1().getQ(),
            tieLine.getTerminal1().getBusView().getBus().getV(),
            tieLine.getTerminal1().getBusView().getBus().getAngle(),
            Branch.Side.ONE).otherSide(tieLine);
        assertTrue(compare(sv2, caseSv.node2, caseSv.line2, Branch.Side.ONE));

        SV sv1 = new SV(tieLine.getTerminal2().getP(), tieLine.getTerminal2().getQ(),
            tieLine.getTerminal2().getBusView().getBus().getV(),
            tieLine.getTerminal2().getBusView().getBus().getAngle(),
            Branch.Side.TWO).otherSide(tieLine);
        assertTrue(compare(sv1, caseSv.node1, caseSv.line1, Branch.Side.TWO));

        assertTrue(compare(caseSv.nodeBoundary.v, tieLine.getHalf1().getBoundary().getV()));
        assertTrue(compare(caseSv.nodeBoundary.a, tieLine.getHalf1().getBoundary().getAngle()));
        assertTrue(compare(getP(caseSv.line1, Branch.Side.TWO), tieLine.getHalf1().getBoundary().getP()));
        assertTrue(compare(getQ(caseSv.line1, Branch.Side.TWO), tieLine.getHalf1().getBoundary().getQ()));

        assertTrue(compare(caseSv.nodeBoundary.v, tieLine.getHalf2().getBoundary().getV()));
        assertTrue(compare(caseSv.nodeBoundary.a, tieLine.getHalf2().getBoundary().getAngle()));
        assertTrue(compare(getP(caseSv.line2, Branch.Side.ONE), tieLine.getHalf2().getBoundary().getP()));
        assertTrue(compare(getQ(caseSv.line2, Branch.Side.ONE), tieLine.getHalf2().getBoundary().getQ()));
    }

    private static Network createNetworkWithTieLine(NetworkFactory networkFactory,
        Branch.Side boundarySide1, Branch.Side boundarySide2, CaseSv caseSv) {

        Network network = networkFactory.createNetwork("TieLine-BusBreaker", "test");
        network.setCaseDate(DateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.setForecastDistance(0);

        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel s1vl1 = s1.newVoltageLevel()
                .setId("S1VL1")
                .setNominalV(1.0)
                .setLowVoltageLimit(0.95)
                .setHighVoltageLimit(1.05)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();

        createBus(s1vl1, "S1VL1-BUS");

        Substation s2 = network.newSubstation()
            .setId("S2")
            .add();
        VoltageLevel s2vl1 = s2.newVoltageLevel()
            .setId("S2VL1")
            .setNominalV(1.0)
            .setLowVoltageLimit(0.95)
            .setHighVoltageLimit(1.05)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();

        createBus(s2vl1, "S2VL1-BUS");

        ReorientedBranchCharacteristics brp1 = new ReorientedBranchCharacteristics(0.019, 0.059, 0.02, 0.075, 0.03, 0.065, isLine1Reoriented(boundarySide1));
        ReorientedBranchCharacteristics brp2 = new ReorientedBranchCharacteristics(0.038, 0.118, 0.015, 0.050, 0.025, 0.080, isLine2Reoriented(boundarySide2));

        TieLineAdder adder = network.newTieLine()
            .setId(boundarySide1.name() + " + " + boundarySide2.name())
            .setName(boundarySide1.name() + " + " + boundarySide2.name())
            .newHalfLine1()
            .setId(boundarySide1.name())
            .setName(boundarySide1.name())
            .setR(brp1.getR())
            .setX(brp1.getX())
            .setG1(brp1.getG1())
            .setB1(brp1.getB1())
            .setG2(brp1.getG2())
            .setB2(brp1.getB2())
            .add()
            .newHalfLine2()
            .setId(boundarySide2.name())
            .setName(boundarySide2.name())
            .setR(brp2.getR())
            .setX(brp2.getX())
            .setG1(brp2.getG1())
            .setB1(brp2.getB1())
            .setG2(brp2.getG2())
            .setB2(brp2.getB2())
            .add();

        adder.setVoltageLevel1("S1VL1")
            .setBus1("S1VL1-BUS")
            .setVoltageLevel2("S2VL1")
            .setBus2("S2VL1-BUS")
            .setUcteXnodeCode("UcteNode");

        TieLine tieLine = adder.add();
        tieLine.getTerminal1().getBusView().getBus().setV(caseSv.node1.v);
        tieLine.getTerminal1().getBusView().getBus().setAngle(caseSv.node1.a);
        tieLine.getTerminal1().setP(getOtherSideP(caseSv.line1, boundarySide1));
        tieLine.getTerminal1().setQ(getOtherSideQ(caseSv.line1, boundarySide1));

        tieLine.getTerminal2().getBusView().getBus().setV(caseSv.node2.v);
        tieLine.getTerminal2().getBusView().getBus().setAngle(caseSv.node2.a);
        tieLine.getTerminal2().setP(getOtherSideP(caseSv.line2, boundarySide2));
        tieLine.getTerminal2().setQ(getOtherSideQ(caseSv.line2, boundarySide2));

        return network;
    }

    private static Network createNetworkWithTieLineWithDifferentNominalVoltageAtEnds(NetworkFactory networkFactory,
        Branch.Side boundarySide1, Branch.Side boundarySide2, CaseSv caseSv) {

        Network network = networkFactory.createNetwork("TieLine-BusBreaker", "test");
        network.setCaseDate(DateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.setForecastDistance(0);

        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel s1vl1 = s1.newVoltageLevel()
                .setId("S1VL1")
                .setNominalV(138.0)
                .setLowVoltageLimit(110.0)
                .setHighVoltageLimit(150.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();

        createBus(s1vl1, "S1VL1-BUS");

        Substation s2 = network.newSubstation()
            .setId("S2")
            .add();
        VoltageLevel s2vl1 = s2.newVoltageLevel()
            .setId("S2VL1")
            .setNominalV(220.0)
            .setLowVoltageLimit(195.0)
            .setHighVoltageLimit(240.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();

        createBus(s2vl1, "S2VL1-BUS");

        ReorientedBranchCharacteristics brp1 = new ReorientedBranchCharacteristics(2.1672071999999996, 9.5543748, 0.0, 1.648813274522159E-4, 0.0, 1.648813274522159E-4, isLine1Reoriented(boundarySide1));
        ReorientedBranchCharacteristics brp2 = new ReorientedBranchCharacteristics(3.1513680000000006, 14.928011999999999, 0.008044414674299755, -0.03791520949675112, -0.005046041932060755, 0.023978278075869598, isLine2Reoriented(boundarySide2));

        TieLineAdder adder = network.newTieLine()
            .setId(boundarySide1.name() + " + " + boundarySide2.name())
            .setName(boundarySide1.name() + " + " + boundarySide2.name())
            .newHalfLine1()
            .setId(boundarySide1.name())
            .setName(boundarySide1.name())
            .setR(brp1.getR())
            .setX(brp1.getX())
            .setG1(brp1.getG1())
            .setB1(brp1.getB1())
            .setG2(brp1.getG2())
            .setB2(brp1.getB2())
            .add()
            .newHalfLine2()
            .setId(boundarySide2.name())
            .setName(boundarySide2.name())
            .setR(brp2.getR())
            .setX(brp2.getX())
            .setG1(brp2.getG1())
            .setB1(brp2.getB1())
            .setG2(brp2.getG2())
            .setB2(brp2.getB2())
            .add();

        adder.setVoltageLevel1("S1VL1")
            .setBus1("S1VL1-BUS")
            .setVoltageLevel2("S2VL1")
            .setBus2("S2VL1-BUS")
            .setUcteXnodeCode("UcteNode");

        TieLine tieLine = adder.add();
        tieLine.getTerminal1().getBusView().getBus().setV(caseSv.node1.v);
        tieLine.getTerminal1().getBusView().getBus().setAngle(caseSv.node1.a);
        tieLine.getTerminal1().setP(getOtherSideP(caseSv.line1, boundarySide1));
        tieLine.getTerminal1().setQ(getOtherSideQ(caseSv.line1, boundarySide1));

        tieLine.getTerminal2().getBusView().getBus().setV(caseSv.node2.v);
        tieLine.getTerminal2().getBusView().getBus().setAngle(caseSv.node2.a);
        tieLine.getTerminal2().setP(getOtherSideP(caseSv.line2, boundarySide2));
        tieLine.getTerminal2().setQ(getOtherSideQ(caseSv.line2, boundarySide2));

        return network;
    }

    private static void createBus(VoltageLevel voltageLevel, String id) {
        voltageLevel.getBusBreakerView()
            .newBus()
            .setName(id)
            .setId(id)
            .add();
    }

    private static double getOtherSideP(LineSv line, Branch.Side boundarySide) {
        if (boundarySide == Branch.Side.ONE) {
            return line.p2;
        } else {
            return line.p1;
        }
    }

    private static double getOtherSideQ(LineSv line, Branch.Side boundarySide) {
        if (boundarySide == Branch.Side.ONE) {
            return line.q2;
        } else {
            return line.q1;
        }
    }

    private static double getP(LineSv line, Branch.Side boundarySide) {
        if (boundarySide == Branch.Side.ONE) {
            return line.p1;
        } else {
            return line.p2;
        }
    }

    private static double getQ(LineSv line, Branch.Side boundarySide) {
        if (boundarySide == Branch.Side.ONE) {
            return line.q1;
        } else {
            return line.q2;
        }
    }

    private static boolean compare(SV sv, NodeSv nodeSv, LineSv lineSv, Branch.Side boundarySide) {
        double tol = 0.00001;
        if (Math.abs(sv.getP() - getOtherSideP(lineSv, boundarySide)) > tol) {
            return false;
        }
        if (Math.abs(sv.getQ() - getOtherSideQ(lineSv, boundarySide)) > tol) {
            return false;
        }
        if (Math.abs(sv.getU() - nodeSv.v) > tol) {
            return false;
        }
        if (Math.abs(sv.getA() - nodeSv.a) > tol) {
            return false;
        }
        return true;
    }

    private static boolean compare(double expected, double actual) {
        double tol = 0.00001;
        if (Math.abs(actual - expected) > tol) {
            return false;
        }
        return true;
    }

    // Line1 from node1 to nodeBoundary, Line2 from nodeBoundary to node2
    private static CaseSv createCase0() {
        NodeSv node1 = new NodeSv(1.06000000, Math.toDegrees(0.0));
        NodeSv nodeBoundary = new NodeSv(1.05913402, Math.toDegrees(-0.01700730));
        NodeSv node2 = new NodeSv(1.04546576, Math.toDegrees(-0.04168907));

        LineSv line1 = new LineSv(0.32101578, -0.16210107, -0.26328124, 0.00991455);
        LineSv line2 = new LineSv(0.26328124, -0.00991455, -0.21700000, -0.12700000);
        return new CaseSv(node1, node2, nodeBoundary, line1, line2);
    }

    // Line1 from node1 to nodeBoundary, Line2 from node2 to nodeBoundary
    private static CaseSv createCase1() {
        NodeSv node1 = new NodeSv(1.06000000, Math.toDegrees(0.0));
        NodeSv nodeBoundary = new NodeSv(1.05916756, Math.toDegrees(-0.01702560));
        NodeSv node2 = new NodeSv(1.04216358, Math.toDegrees(-0.03946400));

        LineSv line1 = new LineSv(0.32116645, -0.16274609, -0.26342655, 0.01056498);
        LineSv line2 = new LineSv(-0.21700000, -0.12700000, 0.26342655, -0.01056498);
        return new CaseSv(node1, node2, nodeBoundary, line1, line2);
    }

    // Line1 from nodeBoundary to node1, Line2 from nodeBoundary to node2
    private static CaseSv createCase2() {
        NodeSv node1 = new NodeSv(1.06000000, Math.toDegrees(0.0));
        NodeSv nodeBoundary = new NodeSv(1.05998661, Math.toDegrees(-0.01660626));
        NodeSv node2 = new NodeSv(1.04634503, Math.toDegrees(-0.04125738));

        LineSv line1 = new LineSv(-0.26335112, 0.01016197, 0.32106283, -0.16270573);
        LineSv line2 = new LineSv(0.26335112, -0.01016197, -0.21700000, -0.12700000);
        return new CaseSv(node1, node2, nodeBoundary, line1, line2);
    }

    // Line1 from nodeBoundary to node1, Line2 from node2 to nodeBoundary
    private static CaseSv createCase3() {
        NodeSv node1 = new NodeSv(1.06000000, Math.toDegrees(0.0));
        NodeSv nodeBoundary = new NodeSv(1.06002014, Math.toDegrees(-0.01662448));
        NodeSv node2 = new NodeSv(1.04304009, Math.toDegrees(-0.03903205));

        LineSv line1 = new LineSv(-0.26349561, 0.01081185, 0.32121215, -0.16335034);
        LineSv line2 = new LineSv(-0.21700000, -0.12700000, 0.26349561, -0.01081185);
        return new CaseSv(node1, node2, nodeBoundary, line1, line2);
    }

    // Line1 from nodeBoundary to node1, Line2 from node2 to nodeBoundary
    // Line1 from node1 to nodeBoundary, Line2 from nodeBoundary to node2
    // Different nominal voltage at node1 and node2
    private static CaseSv createCaseDifferentNominalVoltageAtEnds() {
        NodeSv node1 = new NodeSv(145.2861673277147, Math.toDegrees(-0.01745197));
        NodeSv nodeBoundary = new NodeSv(145.42378472578227, Math.toDegrees(-0.02324020));
        NodeSv node2 = new NodeSv(231.30269602522478, Math.toDegrees(-0.02818192));

        LineSv line1 = new LineSv(11.729938, -8.196614, -11.713527, 1.301712);
        LineSv line2 = new LineSv(11.713527, -1.301712, -11.700000, -6.700000);
        return new CaseSv(node1, node2, nodeBoundary, line1, line2);
    }

    private static boolean isLine1Reoriented(Branch.Side boundarySide) {
        return boundarySide.equals(Branch.Side.ONE);
    }

    private static boolean isLine2Reoriented(Branch.Side boundarySide) {
        return boundarySide.equals(Branch.Side.TWO);
    }

    private static final class CaseSv {
        private final NodeSv node1;
        private final NodeSv node2;
        private final NodeSv nodeBoundary;
        private final LineSv line1;
        private final LineSv line2;

        private CaseSv(NodeSv node1, NodeSv node2, NodeSv nodeBoundary, LineSv line1, LineSv line2) {
            this.node1 = node1;
            this.node2 = node2;
            this.nodeBoundary = nodeBoundary;
            this.line1 = line1;
            this.line2 = line2;
        }
    }

    private static final class NodeSv {
        private final double v;
        private final double a;

        private NodeSv(double v, double a) {
            this.v = v;
            this.a = a;
        }
    }

    private static final class LineSv {
        private final double p1;
        private final double q1;
        private final double p2;
        private final double q2;

        private LineSv(double p1, double q1, double p2, double q2) {
            this.p1 = p1;
            this.q1 = q1;
            this.p2 = p2;
            this.q2 = q2;
        }
    }
}
