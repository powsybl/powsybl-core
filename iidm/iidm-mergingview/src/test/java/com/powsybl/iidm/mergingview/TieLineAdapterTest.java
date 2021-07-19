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

        assertEquals(Branch.Side.ONE, tieLine.getHalf1().getBoundary().getSide());
        assertSame(tieLine, tieLine.getHalf1().getBoundary().getConnectable());
        assertSame(tieLine.getTerminal1().getVoltageLevel(), tieLine.getHalf1().getBoundary().getVoltageLevel());

        assertEquals(Branch.Side.TWO, tieLine.getHalf2().getBoundary().getSide());
        assertSame(tieLine, tieLine.getHalf2().getBoundary().getConnectable());
        assertSame(tieLine.getTerminal2().getVoltageLevel(), tieLine.getHalf2().getBoundary().getVoltageLevel());

        checkHalfLine(tieLine.getHalf1());
        checkHalfLine(tieLine.getHalf2());
    }

    private static void checkHalfLine(TieLine.HalfLine half) {
        assertEquals(1.0, half.getR(), 0.0);
        half.setR(2.0);
        assertEquals(2.0, half.getR(), 0.0);
        assertEquals(1.0, half.getX(), 0.0);
        half.setX(2.0);
        assertEquals(2.0, half.getX(), 0.0);
        assertEquals(0.0, half.getG1(), 0.0);
        half.setG1(0.5);
        assertEquals(0.5, half.getG1(), 0.0);
        assertEquals(0.0, half.getG2(), 0.0);
        half.setG2(0.5);
        assertEquals(0.5, half.getG2(), 0.0);
        assertEquals(0.0, half.getB1(), 0.0);
        half.setB1(0.5);
        assertEquals(0.5, half.getB1(), 0.0);
        assertEquals(0.0, half.getB2(), 0.0);
        half.setB2(0.5);
        assertEquals(0.5, half.getB2(), 0.0);
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
        Bus b1 = vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        Generator g1 = vl1.newGenerator()
                .setId("g1")
                .setBus("b1")
                .setConnectableBus("b1")
                .setTargetP(100.0)
                .setTargetQ(0.0)
                .setMinP(50.0)
                .setMaxP(150.0)
                .add();
        g1.setRegulatingTerminal(g1.getTerminal())
            .setTargetV(400.0)
            .setVoltageRegulatorOn(true);
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
