/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

import com.powsybl.iidm.network.util.LinkData;
import com.powsybl.iidm.network.util.SV;
import com.powsybl.iidm.network.util.LinkData.BranchAdmittanceMatrix;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.math3.complex.Complex;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class TieLineTest {

    @Test
    void tieLineTest0() {

        // Line1 from node1 to boundaryNode, Line2 from boundaryNode to node2
        CaseSv caseSv0 = createCase0();
        Network n = createNetworkWithTieLine(NetworkFactory.findDefault(), TwoSides.TWO, TwoSides.ONE, caseSv0);
        Branch<?> branch = n.getBranch("TWO + ONE");
        assertTrue(branch instanceof TieLine);

        TieLine tieLine = (TieLine) branch;
        SV sv2 = new SV(tieLine.getDanglingLine1().getTerminal().getP(), tieLine.getDanglingLine1().getTerminal().getQ(),
            tieLine.getDanglingLine1().getTerminal().getBusView().getBus().getV(),
            tieLine.getDanglingLine1().getTerminal().getBusView().getBus().getAngle(),
            TwoSides.ONE).otherSide(tieLine);
        SV isv2 = initialSv2(caseSv0, initialModelCase(TwoSides.TWO, TwoSides.ONE), TwoSides.TWO, TwoSides.ONE);
        assertTrue(compare(sv2, caseSv0.node2, caseSv0.line2, TwoSides.ONE, isv2));

        SV sv1 = new SV(tieLine.getDanglingLine2().getTerminal().getP(), tieLine.getDanglingLine2().getTerminal().getQ(),
            tieLine.getDanglingLine2().getTerminal().getBusView().getBus().getV(),
            tieLine.getDanglingLine2().getTerminal().getBusView().getBus().getAngle(),
            TwoSides.TWO).otherSide(tieLine);
        SV isv1 = initialSv1(caseSv0, initialModelCase(TwoSides.TWO, TwoSides.ONE), TwoSides.TWO, TwoSides.ONE);
        assertTrue(compare(sv1, caseSv0.node1, caseSv0.line1, TwoSides.TWO, isv1));

        SV isvHalf1 = initialHalf1SvBoundary(caseSv0, initialModelCase(TwoSides.TWO, TwoSides.ONE), TwoSides.TWO);
        assertTrue(compare(caseSv0.nodeBoundary.v, tieLine.getDanglingLine1().getBoundary().getV(), isvHalf1.getU()));
        assertTrue(compare(caseSv0.nodeBoundary.a, tieLine.getDanglingLine1().getBoundary().getAngle(), isvHalf1.getA()));
        assertTrue(compare(getP(caseSv0.line1, TwoSides.TWO), tieLine.getDanglingLine1().getBoundary().getP(), isvHalf1.getP()));
        assertTrue(compare(getQ(caseSv0.line1, TwoSides.TWO), tieLine.getDanglingLine1().getBoundary().getQ(), isvHalf1.getQ()));

        SV isvHalf2 = initialHalf2SvBoundary(caseSv0, initialModelCase(TwoSides.TWO, TwoSides.ONE), TwoSides.ONE);
        assertTrue(compare(caseSv0.nodeBoundary.v, tieLine.getDanglingLine2().getBoundary().getV(), isvHalf2.getU()));
        assertTrue(compare(caseSv0.nodeBoundary.a, tieLine.getDanglingLine2().getBoundary().getAngle(), isvHalf2.getA()));
        assertTrue(compare(getP(caseSv0.line2, TwoSides.ONE), tieLine.getDanglingLine2().getBoundary().getP(), isvHalf2.getP()));
        assertTrue(compare(getQ(caseSv0.line2, TwoSides.ONE), tieLine.getDanglingLine2().getBoundary().getQ(), isvHalf2.getQ()));
    }

    @Test
    void tieLineTest1() {

        // Line1 from node1 to boundaryNode, Line2 from node2 to boundaryNode
        CaseSv caseSv1 = createCase1();
        Network n = createNetworkWithTieLine(NetworkFactory.findDefault(), TwoSides.TWO, TwoSides.TWO, caseSv1);
        TieLine tieLine = n.getTieLine("TWO + TWO");

        SV sv2 = new SV(tieLine.getDanglingLine1().getTerminal().getP(), tieLine.getDanglingLine1().getTerminal().getQ(),
            tieLine.getDanglingLine1().getTerminal().getBusView().getBus().getV(),
            tieLine.getDanglingLine1().getTerminal().getBusView().getBus().getAngle(),
            TwoSides.ONE).otherSide(tieLine);
        SV isv2 = initialSv2(caseSv1, initialModelCase(TwoSides.TWO, TwoSides.TWO), TwoSides.TWO, TwoSides.TWO);
        assertTrue(compare(sv2, caseSv1.node2, caseSv1.line2, TwoSides.TWO, isv2));

        SV sv1 = new SV(tieLine.getDanglingLine2().getTerminal().getP(), tieLine.getDanglingLine2().getTerminal().getQ(),
            tieLine.getDanglingLine2().getTerminal().getBusView().getBus().getV(),
            tieLine.getDanglingLine2().getTerminal().getBusView().getBus().getAngle(),
            TwoSides.TWO).otherSide(tieLine);
        SV isv1 = initialSv1(caseSv1, initialModelCase(TwoSides.TWO, TwoSides.TWO), TwoSides.TWO, TwoSides.TWO);
        assertTrue(compare(sv1, caseSv1.node1, caseSv1.line1, TwoSides.TWO, isv1));

        SV isvHalf1 = initialHalf1SvBoundary(caseSv1, initialModelCase(TwoSides.TWO, TwoSides.TWO), TwoSides.TWO);
        assertTrue(compare(caseSv1.nodeBoundary.v, tieLine.getDanglingLine1().getBoundary().getV(), isvHalf1.getU()));
        assertTrue(compare(caseSv1.nodeBoundary.a, tieLine.getDanglingLine1().getBoundary().getAngle(), isvHalf1.getA()));
        assertTrue(compare(getP(caseSv1.line1, TwoSides.TWO), tieLine.getDanglingLine1().getBoundary().getP(), isvHalf1.getP()));
        assertTrue(compare(getQ(caseSv1.line1, TwoSides.TWO), tieLine.getDanglingLine1().getBoundary().getQ(), isvHalf1.getQ()));

        SV isvHalf2 = initialHalf2SvBoundary(caseSv1, initialModelCase(TwoSides.TWO, TwoSides.TWO), TwoSides.TWO);
        assertTrue(compare(caseSv1.nodeBoundary.v, tieLine.getDanglingLine2().getBoundary().getV(), isvHalf2.getU()));
        assertTrue(compare(caseSv1.nodeBoundary.a, tieLine.getDanglingLine2().getBoundary().getAngle(), isvHalf2.getA()));
        assertTrue(compare(getP(caseSv1.line2, TwoSides.TWO), tieLine.getDanglingLine2().getBoundary().getP(), isvHalf2.getP()));
        assertTrue(compare(getQ(caseSv1.line2, TwoSides.TWO), tieLine.getDanglingLine2().getBoundary().getQ(), isvHalf2.getQ()));
    }

    @Test
    void tieLineTest2() {

        // Line1 from boundaryNode to node1, Line2 from boundaryNode to node2
        CaseSv caseSv2 = createCase2();
        Network n = createNetworkWithTieLine(NetworkFactory.findDefault(), TwoSides.ONE, TwoSides.ONE, caseSv2);
        TieLine tieLine = n.getTieLine("ONE + ONE");

        SV sv2 = new SV(tieLine.getDanglingLine1().getTerminal().getP(), tieLine.getDanglingLine1().getTerminal().getQ(),
            tieLine.getDanglingLine1().getTerminal().getBusView().getBus().getV(),
            tieLine.getDanglingLine1().getTerminal().getBusView().getBus().getAngle(),
            TwoSides.ONE).otherSide(tieLine);
        SV isv2 = initialSv2(caseSv2, initialModelCase(TwoSides.ONE, TwoSides.ONE), TwoSides.ONE, TwoSides.ONE);
        assertTrue(compare(sv2, caseSv2.node2, caseSv2.line2, TwoSides.ONE, isv2));

        SV sv1 = new SV(tieLine.getDanglingLine2().getTerminal().getP(), tieLine.getDanglingLine2().getTerminal().getQ(),
            tieLine.getDanglingLine2().getTerminal().getBusView().getBus().getV(),
            tieLine.getDanglingLine2().getTerminal().getBusView().getBus().getAngle(),
            TwoSides.TWO).otherSide(tieLine);
        SV isv1 = initialSv1(caseSv2, initialModelCase(TwoSides.ONE, TwoSides.ONE), TwoSides.ONE, TwoSides.ONE);
        assertTrue(compare(sv1, caseSv2.node1, caseSv2.line1, TwoSides.ONE, isv1));

        SV isvHalf1 = initialHalf1SvBoundary(caseSv2, initialModelCase(TwoSides.ONE, TwoSides.ONE), TwoSides.ONE);
        assertTrue(compare(caseSv2.nodeBoundary.v, tieLine.getDanglingLine1().getBoundary().getV(), isvHalf1.getU()));
        assertTrue(compare(caseSv2.nodeBoundary.a, tieLine.getDanglingLine1().getBoundary().getAngle(), isvHalf1.getA()));
        assertTrue(compare(getP(caseSv2.line1, TwoSides.ONE), tieLine.getDanglingLine1().getBoundary().getP(), isvHalf1.getP()));
        assertTrue(compare(getQ(caseSv2.line1, TwoSides.ONE), tieLine.getDanglingLine1().getBoundary().getQ(), isvHalf1.getQ()));

        SV isvHalf2 = initialHalf2SvBoundary(caseSv2, initialModelCase(TwoSides.ONE, TwoSides.ONE), TwoSides.ONE);
        assertTrue(compare(caseSv2.nodeBoundary.v, tieLine.getDanglingLine2().getBoundary().getV(), isvHalf2.getU()));
        assertTrue(compare(caseSv2.nodeBoundary.a, tieLine.getDanglingLine2().getBoundary().getAngle(), isvHalf2.getA()));
        assertTrue(compare(getP(caseSv2.line2, TwoSides.ONE), tieLine.getDanglingLine2().getBoundary().getP(), isvHalf2.getP()));
        assertTrue(compare(getQ(caseSv2.line2, TwoSides.ONE), tieLine.getDanglingLine2().getBoundary().getQ(), isvHalf2.getQ()));
    }

    @Test
    void tieLineTest3() {

        // Line1 from boundaryNode to node1, Line2 from node2 to boundaryNode
        CaseSv caseSv3 = createCase3();
        Network n = createNetworkWithTieLine(NetworkFactory.findDefault(), TwoSides.ONE, TwoSides.TWO, caseSv3);
        TieLine tieLine = n.getTieLine("ONE + TWO");

        SV sv2 = new SV(tieLine.getDanglingLine1().getTerminal().getP(), tieLine.getDanglingLine1().getTerminal().getQ(),
            tieLine.getDanglingLine1().getTerminal().getBusView().getBus().getV(),
            tieLine.getDanglingLine1().getTerminal().getBusView().getBus().getAngle(),
            TwoSides.ONE).otherSide(tieLine);
        SV isv2 = initialSv2(caseSv3, initialModelCase(TwoSides.ONE, TwoSides.TWO), TwoSides.ONE, TwoSides.TWO);
        assertTrue(compare(sv2, caseSv3.node2, caseSv3.line2, TwoSides.TWO, isv2));

        SV sv1 = new SV(tieLine.getDanglingLine2().getTerminal().getP(), tieLine.getDanglingLine2().getTerminal().getQ(),
            tieLine.getDanglingLine2().getTerminal().getBusView().getBus().getV(),
            tieLine.getDanglingLine2().getTerminal().getBusView().getBus().getAngle(),
            TwoSides.TWO).otherSide(tieLine);
        SV isv1 = initialSv1(caseSv3, initialModelCase(TwoSides.ONE, TwoSides.TWO), TwoSides.ONE, TwoSides.TWO);
        assertTrue(compare(sv1, caseSv3.node1, caseSv3.line1, TwoSides.ONE, isv1));

        SV isvHalf1 = initialHalf1SvBoundary(caseSv3, initialModelCase(TwoSides.ONE, TwoSides.TWO), TwoSides.ONE);
        assertTrue(compare(caseSv3.nodeBoundary.v, tieLine.getDanglingLine1().getBoundary().getV(), isvHalf1.getU()));
        assertTrue(compare(caseSv3.nodeBoundary.a, tieLine.getDanglingLine1().getBoundary().getAngle(), isvHalf1.getA()));
        assertTrue(compare(getP(caseSv3.line1, TwoSides.ONE), tieLine.getDanglingLine1().getBoundary().getP(), isvHalf1.getP()));
        assertTrue(compare(getQ(caseSv3.line1, TwoSides.ONE), tieLine.getDanglingLine1().getBoundary().getQ(), isvHalf1.getQ()));

        SV isvHalf2 = initialHalf2SvBoundary(caseSv3, initialModelCase(TwoSides.ONE, TwoSides.TWO), TwoSides.TWO);
        assertTrue(compare(caseSv3.nodeBoundary.v, tieLine.getDanglingLine2().getBoundary().getV(), isvHalf2.getU()));
        assertTrue(compare(caseSv3.nodeBoundary.a, tieLine.getDanglingLine2().getBoundary().getAngle(), isvHalf2.getA()));
        assertTrue(compare(getP(caseSv3.line2, TwoSides.TWO), tieLine.getDanglingLine2().getBoundary().getP(), isvHalf2.getP()));
        assertTrue(compare(getQ(caseSv3.line2, TwoSides.TWO), tieLine.getDanglingLine2().getBoundary().getQ(), isvHalf2.getQ()));
    }

    @Test
    void tieLineWithDifferentNominalVoltageAtEndsTest() {

        // Line1 from node1 to boundaryNode, Line2 from boundaryNode to node2
        CaseSv caseSv = createCaseDifferentNominalVoltageAtEnds();
        Network n = createNetworkWithTieLineWithDifferentNominalVoltageAtEnds(NetworkFactory.findDefault(), TwoSides.TWO, TwoSides.ONE, caseSv);
        TieLine tieLine = n.getTieLine("TWO + ONE");

        SV sv2 = new SV(tieLine.getDanglingLine1().getTerminal().getP(), tieLine.getDanglingLine1().getTerminal().getQ(),
            tieLine.getDanglingLine1().getTerminal().getBusView().getBus().getV(),
            tieLine.getDanglingLine1().getTerminal().getBusView().getBus().getAngle(),
            TwoSides.ONE).otherSide(tieLine);
        SV isv2 = initialSv2(caseSv, initialModelDifferentVlCase(TwoSides.TWO, TwoSides.ONE), TwoSides.TWO, TwoSides.ONE);
        assertTrue(compare(sv2, caseSv.node2, caseSv.line2, TwoSides.ONE, isv2));

        SV sv1 = new SV(tieLine.getDanglingLine2().getTerminal().getP(), tieLine.getDanglingLine2().getTerminal().getQ(),
            tieLine.getDanglingLine2().getTerminal().getBusView().getBus().getV(),
            tieLine.getDanglingLine2().getTerminal().getBusView().getBus().getAngle(),
            TwoSides.TWO).otherSide(tieLine);
        SV isv1 = initialSv1(caseSv, initialModelDifferentVlCase(TwoSides.TWO, TwoSides.ONE), TwoSides.TWO, TwoSides.ONE);
        assertTrue(compare(sv1, caseSv.node1, caseSv.line1, TwoSides.TWO, isv1));

        SV isvHalf1 = initialHalf1SvBoundary(caseSv, initialModelDifferentVlCase(TwoSides.TWO, TwoSides.ONE), TwoSides.TWO);
        assertTrue(compare(caseSv.nodeBoundary.v, tieLine.getDanglingLine1().getBoundary().getV(), isvHalf1.getU()));
        assertTrue(compare(caseSv.nodeBoundary.a, tieLine.getDanglingLine1().getBoundary().getAngle(), isvHalf1.getA()));
        assertTrue(compare(getP(caseSv.line1, TwoSides.TWO), tieLine.getDanglingLine1().getBoundary().getP(), isvHalf1.getP()));
        assertTrue(compare(getQ(caseSv.line1, TwoSides.TWO), tieLine.getDanglingLine1().getBoundary().getQ(), isvHalf1.getQ()));

        SV isvHalf2 = initialHalf2SvBoundary(caseSv, initialModelDifferentVlCase(TwoSides.TWO, TwoSides.ONE), TwoSides.ONE);
        assertTrue(compare(caseSv.nodeBoundary.v, tieLine.getDanglingLine2().getBoundary().getV(), isvHalf2.getU()));
        assertTrue(compare(caseSv.nodeBoundary.a, tieLine.getDanglingLine2().getBoundary().getAngle(), isvHalf2.getA()));
        assertTrue(compare(getP(caseSv.line2, TwoSides.ONE), tieLine.getDanglingLine2().getBoundary().getP(), isvHalf2.getP()));
        assertTrue(compare(getQ(caseSv.line2, TwoSides.ONE), tieLine.getDanglingLine2().getBoundary().getQ(), isvHalf2.getQ()));
    }

    @Test
    void testDefaultValuesTieLine() {

        Network network = NoEquipmentNetworkFactory.create();

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

        TwoSides boundarySide1 = TwoSides.ONE;
        TwoSides boundarySide2 = TwoSides.TWO;
        DanglingLine dl1 = s1vl1.newDanglingLine()
                .setId(boundarySide1.name())
                .setName(boundarySide1.name())
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.0)
                .setX(2.0)
                .setBus("S1VL1-BUS")
                .setPairingKey("key")
                .add();
        DanglingLine dl2 = s2vl1.newDanglingLine()
                .setId(boundarySide2.name())
                .setName(boundarySide2.name())
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.0)
                .setX(2.0)
                .setBus("S2VL1-BUS")
                .setPairingKey("key")
                .add();

        TieLine tieLine = network.newTieLine()
                .setId(boundarySide1.name() + " + " + boundarySide2.name())
                .setName(boundarySide1.name() + " + " + boundarySide2.name())
                .setDanglingLine1(dl1.getId())
                .setDanglingLine2(dl2.getId())
                .add();

        assertEquals(0.0, tieLine.getDanglingLine1().getG(), 0.0);
        assertEquals(0.0, tieLine.getDanglingLine1().getB(), 0.0);
        assertEquals(0.0, tieLine.getDanglingLine2().getG(), 0.0);
        assertEquals(0.0, tieLine.getDanglingLine2().getB(), 0.0);

        assertSame(s1vl1, tieLine.getDanglingLine1().getTerminal().getVoltageLevel());
        assertSame(s2vl1, tieLine.getDanglingLine2().getTerminal().getVoltageLevel());
    }

    private static Network createNetworkWithTieLine(NetworkFactory networkFactory,
                                                    TwoSides boundarySide1, TwoSides boundarySide2, CaseSv caseSv) {

        Network network = networkFactory.createNetwork("TieLine-BusBreaker", "test");
        network.setCaseDate(ZonedDateTime.parse("2017-06-25T17:43:00.000+01:00"));
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

        // The initial parameters for AcLineSegment 1 are R = 0.019, X = 0.059, G1 = 0.02, B1 = 0.075, G2 = 0.03, B2 = 0.065
        // The initial parameters for AcLineSegment 2 are R = 0.038, X = 0.118, G1 = 0.015,B1 = 0.050, G2 = 0.025,B2 = 0.080
        // AcLinesegment 1 must be reoriented if boundary side is at end 1
        // AcLinesegment 2 must be reoriented if boundary side is at end 2
        // Current model does not allow shunt admittances at both ends, so it does not make sense to reorient the AcLineSegments

        DanglingLine dl1 = s1vl1.newDanglingLine()
                .setBus("S1VL1-BUS")
                .setId(boundarySide1.name())
                .setEnsureIdUnicity(true)
                .setName(boundarySide1.name())
                .setP0(0.0)
                .setQ0(0.0)
                .setR(0.019)
                .setX(0.059)
                .setG(0.05)
                .setB(0.14)
                .add();
        DanglingLine dl2 = s2vl1.newDanglingLine()
                .setBus("S2VL1-BUS")
                .setId(boundarySide2.name())
                .setEnsureIdUnicity(true)
                .setName(boundarySide2.name())
                .setP0(0.0)
                .setQ0(0.0)
                .setR(0.038)
                .setX(0.118)
                .setG(0.04)
                .setB(0.13)
                .setPairingKey("key")
                .add();

        TieLine tieLine = network.newTieLine()
            .setId(boundarySide1.name() + " + " + boundarySide2.name())
            .setName(boundarySide1.name() + " + " + boundarySide2.name())
            .setDanglingLine1(dl1.getId())
            .setDanglingLine2(dl2.getId())
            .add();
        tieLine.getDanglingLine1().getTerminal().getBusView().getBus().setV(caseSv.node1.v);
        tieLine.getDanglingLine1().getTerminal().getBusView().getBus().setAngle(caseSv.node1.a);
        tieLine.getDanglingLine1().getTerminal().setP(getOtherSideP(caseSv.line1, boundarySide1));
        tieLine.getDanglingLine1().getTerminal().setQ(getOtherSideQ(caseSv.line1, boundarySide1));

        tieLine.getDanglingLine2().getTerminal().getBusView().getBus().setV(caseSv.node2.v);
        tieLine.getDanglingLine2().getTerminal().getBusView().getBus().setAngle(caseSv.node2.a);
        tieLine.getDanglingLine2().getTerminal().setP(getOtherSideP(caseSv.line2, boundarySide2));
        tieLine.getDanglingLine2().getTerminal().setQ(getOtherSideQ(caseSv.line2, boundarySide2));

        return network;
    }

    private static Network createNetworkWithTieLineWithDifferentNominalVoltageAtEnds(NetworkFactory networkFactory,
                                                                                     TwoSides boundarySide1, TwoSides boundarySide2, CaseSv caseSv) {

        Network network = networkFactory.createNetwork("TieLine-BusBreaker", "test");
        network.setCaseDate(ZonedDateTime.parse("2017-06-25T17:43:00.000+01:00"));
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

        // The initial parameters for AcLineSegment 1 are R = 2.1672071999999996, X = 9.5543748, G1 = 0.0, B1 = 1.648813274522159E-4, G2 = 0.0, B2 = 1.648813274522159E-4
        // AcLinesegment 1 must be reoriented if boundary side is at end 1
        // Current model does not allow shunt admittances at both ends, so it does not make sense to reorient it

        // The initial parameters for AcLineSegment 2 are R = 3.1513680000000006, X = 14.928011999999999, G1 = 0.008044414674299755, B1 = -0.03791520949675112, G2 = -0.005046041932060755, B2 = 0.023978278075869598
        // AcLinesegment 2 must be reoriented if boundary side is at end 2
        // Current model does not allow shunt admittances at both ends, so it does not make sense to reorient it
        DanglingLine dl1 = s1vl1.newDanglingLine()
                .setBus("S1VL1-BUS")
                .setId(boundarySide1.name())
                .setName(boundarySide1.name())
                .setP0(0.0)
                .setQ0(0.0)
                .setR(2.1672071999999996)
                .setX(9.5543748)
                .setG(0.0)
                .setB(0.00032976265)
                .setPairingKey("key")
                .add();
        DanglingLine dl2 = s2vl1.newDanglingLine()
                .setBus("S2VL1-BUS")
                .setId(boundarySide2.name())
                .setName(boundarySide2.name())
                .setP0(0.0)
                .setQ0(0.0)
                .setR(3.1513680000000006)
                .setX(14.928011999999999)
                .setG(0.00299837274)
                .setB(-0.01393693142)
                .add();

        TieLine tieLine = network.newTieLine()
                .setId(boundarySide1.name() + " + " + boundarySide2.name())
                .setName(boundarySide1.name() + " + " + boundarySide2.name())
                .setDanglingLine1(dl1.getId())
                .setDanglingLine2(dl2.getId())
                .add();

        tieLine.getDanglingLine1().getTerminal().getBusView().getBus().setV(caseSv.node1.v);
        tieLine.getDanglingLine1().getTerminal().getBusView().getBus().setAngle(caseSv.node1.a);
        tieLine.getDanglingLine1().getTerminal().setP(getOtherSideP(caseSv.line1, boundarySide1));
        tieLine.getDanglingLine1().getTerminal().setQ(getOtherSideQ(caseSv.line1, boundarySide1));

        tieLine.getDanglingLine2().getTerminal().getBusView().getBus().setV(caseSv.node2.v);
        tieLine.getDanglingLine2().getTerminal().getBusView().getBus().setAngle(caseSv.node2.a);
        tieLine.getDanglingLine2().getTerminal().setP(getOtherSideP(caseSv.line2, boundarySide2));
        tieLine.getDanglingLine2().getTerminal().setQ(getOtherSideQ(caseSv.line2, boundarySide2));

        return network;
    }

    private static void createBus(VoltageLevel voltageLevel, String id) {
        voltageLevel.getBusBreakerView()
            .newBus()
            .setName(id)
            .setId(id)
            .add();
    }

    private static double getOtherSideP(LineSv line, TwoSides boundarySide) {
        if (boundarySide == TwoSides.ONE) {
            return line.p2;
        } else {
            return line.p1;
        }
    }

    private static double getOtherSideQ(LineSv line, TwoSides boundarySide) {
        if (boundarySide == TwoSides.ONE) {
            return line.q2;
        } else {
            return line.q1;
        }
    }

    private static double getP(LineSv line, TwoSides boundarySide) {
        if (boundarySide == TwoSides.ONE) {
            return line.p1;
        } else {
            return line.p2;
        }
    }

    private static double getQ(LineSv line, TwoSides boundarySide) {
        if (boundarySide == TwoSides.ONE) {
            return line.q1;
        } else {
            return line.q2;
        }
    }

    // We define an error by value to adjust the case. The error is calculated by difference between
    // the calculated value with both models, the initial model of the case and the current model of the danglingLine
    // Errors are due to the danglingLine model (it does not allow shunt admittance at both ends)
    private static boolean compare(SV sv, NodeSv nodeSv, LineSv lineSv, TwoSides boundarySide, SV initialModelSv) {
        double tol = 0.00001;
        double errorP = initialModelSv.getP() - sv.getP();
        double errorQ = initialModelSv.getQ() - sv.getQ();
        double errorU = initialModelSv.getU() - sv.getU();
        double errorA = initialModelSv.getA() - sv.getA();
        if (Math.abs(sv.getP() - getOtherSideP(lineSv, boundarySide)) > tol + Math.abs(errorP)) {
            return false;
        }
        if (Math.abs(sv.getQ() - getOtherSideQ(lineSv, boundarySide)) > tol + Math.abs(errorQ)) {
            return false;
        }
        if (Math.abs(sv.getU() - nodeSv.v) > tol + Math.abs(errorU)) {
            return false;
        }
        if (Math.abs(sv.getA() - nodeSv.a) > tol + Math.abs(errorA)) {
            return false;
        }
        return true;
    }

    // We define an error to adjust the case. The error is calculated by difference between
    // the calculated value with both models, the initial model of the case and the current model of the danglingLine
    // Errors are due to the danglingLine model (it does not allow shunt admittance at both ends)
    private static boolean compare(double expected, double actual, double initialActual) {
        double tol = 0.00001;
        double error = initialActual - actual;
        if (Math.abs(actual - expected) > tol + Math.abs(error)) {
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

    private static SV initialSv1(CaseSv initialCase, TieLineInitialModel tlim, TwoSides half1Boundary, TwoSides half2Boundary) {
        return new SV(getOtherSideP(initialCase.line2, half2Boundary),
            getOtherSideQ(initialCase.line2, half2Boundary),
            initialCase.node2.v, initialCase.node2.a, TwoSides.TWO).otherSide(tlim.tieLine.r, tlim.tieLine.x,
                tlim.tieLine.g1, tlim.tieLine.b1, tlim.tieLine.g2, tlim.tieLine.b2, 1.0, 0.0);
    }

    private static SV initialSv2(CaseSv initialCase, TieLineInitialModel tlim, TwoSides half1Boundary, TwoSides half2Boundary) {
        return new SV(getOtherSideP(initialCase.line1, half1Boundary),
            getOtherSideQ(initialCase.line1, half1Boundary),
            initialCase.node1.v, initialCase.node1.a, TwoSides.ONE).otherSide(tlim.tieLine.r, tlim.tieLine.x,
                tlim.tieLine.g1, tlim.tieLine.b1, tlim.tieLine.g2, tlim.tieLine.b2, 1.0, 0.0);
    }

    private static SV initialHalf1SvBoundary(CaseSv initialCase, TieLineInitialModel tlim, TwoSides half1Boundary) {
        return new SV(getOtherSideP(initialCase.line1, half1Boundary),
            getOtherSideQ(initialCase.line1, half1Boundary),
            initialCase.node1.v, initialCase.node1.a,
            half1Boundary.equals(TwoSides.ONE) ? TwoSides.TWO : TwoSides.ONE).otherSide(tlim.half1.r,
                tlim.half1.x, tlim.half1.g1, tlim.half1.b1, tlim.half1.g2, tlim.half1.b2, 1.0, 0.0);
    }

    private static SV initialHalf2SvBoundary(CaseSv initialCase, TieLineInitialModel tlim, TwoSides half2Boundary) {
        return new SV(getOtherSideP(initialCase.line2, half2Boundary),
            getOtherSideQ(initialCase.line2, half2Boundary),
            initialCase.node2.v, initialCase.node2.a,
            half2Boundary.equals(TwoSides.ONE) ? TwoSides.TWO : TwoSides.ONE).otherSide(tlim.half2.r,
                tlim.half2.x, tlim.half2.g1, tlim.half2.b1, tlim.half2.g2, tlim.half2.b2, 1.0, 0.0);
    }

    private static TieLineInitialModel initialModelCase(TwoSides half1Boundary, TwoSides half2Boundary) {
        return new TieLineInitialModel(new LineInitialModel(0.019, 0.059, 0.02, 0.075, 0.03, 0.065), half1Boundary,
            new LineInitialModel(0.038, 0.118, 0.015, 0.050, 0.025, 0.080), half2Boundary);
    }

    private static TieLineInitialModel initialModelDifferentVlCase(TwoSides half1Boundary, TwoSides half2Boundary) {
        return new TieLineInitialModel(
            new LineInitialModel(2.1672071999999996, 9.5543748, 0.0, 1.648813274522159E-4, 0.0, 1.648813274522159E-4), half1Boundary,
            new LineInitialModel(3.1513680000000006, 14.928011999999999, 0.008044414674299755, -0.03791520949675112,
                -0.005046041932060755, 0.023978278075869598), half2Boundary);
    }

    private static final class TieLineInitialModel {
        private final LineInitialModel half1;
        private final LineInitialModel half2;
        private final LineInitialModel tieLine;

        private TieLineInitialModel(LineInitialModel half1, TwoSides half1Boundary, LineInitialModel half2, TwoSides half2Boundary) {
            this.half1 = half1;
            this.half2 = half2;

            BranchAdmittanceMatrix adm1 = LinkData.calculateBranchAdmittance(half1.r, half1.x, 1.0, 0.0, 1.0, 0.0,
                new Complex(half1.g1, half1.b1), new Complex(half1.g2, half1.b2));
            BranchAdmittanceMatrix adm2 = LinkData.calculateBranchAdmittance(half2.r, half2.x, 1.0, 0.0, 1.0, 0.0,
                new Complex(half2.g1, half2.b1), new Complex(half2.g2, half2.b2));

            BranchAdmittanceMatrix adm = LinkData.kronChain(adm1, half1Boundary, adm2, half2Boundary);
            this.tieLine = new LineInitialModel(adm.y12().negate().reciprocal().getReal(),
                adm.y12().negate().reciprocal().getImaginary(),
                adm.y11().add(adm.y12()).getReal(),
                adm.y11().add(adm.y12()).getImaginary(),
                adm.y22().add(adm.y21()).getReal(),
                adm.y22().add(adm.y21()).getImaginary());
        }
    }

    private static final class LineInitialModel {
        private final double r;
        private final double x;
        private final double g1;
        private final double b1;
        private final double g2;
        private final double b2;

        private LineInitialModel(double r, double x, double g1, double b1, double g2, double b2) {
            this.r = r;
            this.x = x;
            this.g1 = g1;
            this.b1 = b1;
            this.g2 = g2;
            this.b2 = b2;
        }
    }
}
