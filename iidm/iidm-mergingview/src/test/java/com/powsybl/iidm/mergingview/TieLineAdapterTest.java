/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TieLineAdapterTest {

    private MergingView network;

    @Before
    public void setUp() {
        network = MergingView.create("TieLineAdapterTest", "iidm");
        network.merge(createNetwork());
    }

    @Test
    public void testSetterGetter() {
        final TieLine tieLine = (TieLineAdapter) network.getLine("l1 + l2");
        assertTrue(tieLine.isTieLine());

        assertEquals("XNODE", tieLine.getUcteXnodeCode());
        assertNotNull(tieLine.getHalf1());
        assertNotNull(tieLine.getHalf2());

        TieLine.HalfLine half1 = tieLine.getHalf1();
        TieLine.HalfLine half2 = tieLine.getHalf2();

        assertEquals(1.0, half1.getR(), 0.0);
        assertEquals(1.0, half1.getX(), 0.0);
        assertEquals(0.0, half1.getG1(), 0.0);
        assertEquals(0.0, half1.getG2(), 0.0);
        assertEquals(0.0, half1.getB1(), 0.0);
        assertEquals(0.0, half1.getB2(), 0.0);
        assertEquals("l1", half1.getId());
        assertEquals("l1", half1.getName());

        assertEquals(1.0, half2.getR(), 0.0);
        assertEquals(1.0, half2.getX(), 0.0);
        assertEquals(0.0, half2.getG1(), 0.0);
        assertEquals(0.0, half2.getG2(), 0.0);
        assertEquals(0.0, half2.getB1(), 0.0);
        assertEquals(0.0, half2.getB2(), 0.0);
        assertEquals("l2", half2.getId());
        assertEquals("l2", half2.getName());

        Boundary boundary1 = half1.getBoundary();
        Boundary boundary2 = half2.getBoundary();

        assertTrue(Double.isNaN(boundary1.getP()));
        assertTrue(Double.isNaN(boundary1.getQ()));
        assertTrue(Double.isNaN(boundary1.getV()));
        assertTrue(Double.isNaN(boundary1.getAngle()));

        assertTrue(Double.isNaN(boundary2.getP()));
        assertTrue(Double.isNaN(boundary2.getQ()));
        assertTrue(Double.isNaN(boundary2.getV()));
        assertTrue(Double.isNaN(boundary2.getAngle()));

        Terminal boundaryTerminal1 = boundary1.getTerminal();
        Terminal boundaryTerminal2 = boundary2.getTerminal();

        assertTrue(Double.isNaN(boundaryTerminal1.getP()));
        assertTrue(Double.isNaN(boundaryTerminal1.getQ()));
        assertTrue(Double.isNaN(boundaryTerminal2.getP()));
        assertTrue(Double.isNaN(boundaryTerminal2.getQ()));

        assertSame(tieLine.getTerminal1().getVoltageLevel(), boundaryTerminal1.getVoltageLevel());
        assertSame(tieLine.getTerminal2().getVoltageLevel(), boundaryTerminal2.getVoltageLevel());
        assertSame(tieLine, boundaryTerminal1.getConnectable());
        assertSame(tieLine, boundaryTerminal2.getConnectable());

        assertTrue(boundaryTerminal1.isConnected());
        assertTrue(boundaryTerminal2.isConnected());
    }

    private static Network createNetwork() {
        Network n = Network.create("n", "test");
        Substation s1 = n.newSubstation()
                .setId("s1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl1.newGenerator()
                .setId("g1")
                .setBus("b1")
                .setConnectableBus("b1")
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .setMinP(50.0)
                .setMaxP(150.0)
                .add();
        Substation s2 = n.newSubstation()
                .setId("s2")
                .setCountry(Country.BE)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b2 = vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        vl2.newLoad()
                .setId("ld1")
                .setConnectableBus("b2")
                .setBus("b2")
                .setP0(0.0)
                .setQ0(0.0)
                .add();
        n.newTieLine()
                .setId("l1 + l2")
                .setVoltageLevel1("vl1")
                .setConnectableBus1("b1")
                .setBus1("b1")
                .setVoltageLevel2("vl2")
                .setConnectableBus2("b2")
                .setBus2("b2")
                .newHalfLine1()
                    .setId("l1")
                    .setR(1.0)
                    .setX(1.0)
                    .setG1(0.0)
                    .setG2(0.0)
                    .setB1(0.0)
                    .setB2(0.0)
                    .add()
                .newHalfLine2()
                    .setId("l2")
                    .setR(1.0)
                    .setX(1.0)
                    .setG1(0.0)
                    .setG2(0.0)
                    .setB1(0.0)
                    .setB2(0.0)
                    .add()
                .setUcteXnodeCode("XNODE")
                .add();
        return n;
    }
}
