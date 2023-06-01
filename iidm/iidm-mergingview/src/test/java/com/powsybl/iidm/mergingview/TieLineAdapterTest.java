/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class TieLineAdapterTest {

    private MergingView network;

    @BeforeEach
    void setUp() {
        network = MergingView.create("TieLineAdapterTest", "iidm");
        network.merge(createNetwork());
    }

    @Test
    void testSetterGetter() {
        final TieLine tieLine = network.getTieLine("l1 + l2");

        assertEquals("XNODE", tieLine.getUcteXnodeCode());
        assertNotNull(tieLine.getBoundaryLine1());
        assertNotNull(tieLine.getBoundaryLine2());

        assertSame(tieLine.getBoundaryLine1(), tieLine.getBoundaryLine1().getBoundary().getBoundaryLine());
        assertSame(tieLine.getBoundaryLine1(), tieLine.getBoundaryLine1().getBoundary().getBoundaryLine());
        assertSame(tieLine.getBoundaryLine1().getTerminal().getVoltageLevel(), tieLine.getBoundaryLine1().getBoundary().getNetworkSideVoltageLevel());

        assertSame(tieLine.getBoundaryLine2(), tieLine.getBoundaryLine2().getBoundary().getBoundaryLine());
        assertSame(tieLine.getBoundaryLine2(), tieLine.getBoundaryLine2().getBoundary().getBoundaryLine());
        assertSame(tieLine.getBoundaryLine2().getTerminal().getVoltageLevel(), tieLine.getBoundaryLine2().getBoundary().getNetworkSideVoltageLevel());

        checkBoundaryLine(tieLine.getBoundaryLine1());
        checkBoundaryLine(tieLine.getBoundaryLine2());
    }

    private static void checkBoundaryLine(BoundaryLine boundaryLine) {
        assertEquals(1.0, boundaryLine.getR(), 0.0);
        boundaryLine.setR(2.0);
        assertEquals(2.0, boundaryLine.getR(), 0.0);
        assertEquals(1.0, boundaryLine.getX(), 0.0);
        boundaryLine.setX(2.0);
        assertEquals(2.0, boundaryLine.getX(), 0.0);
        assertEquals(0.0, boundaryLine.getG(), 0.0);
        boundaryLine.setG(0.5);
        assertEquals(0.5, boundaryLine.getG(), 0.0);
        assertEquals(0.0, boundaryLine.getB(), 0.0);
        boundaryLine.setB(0.5);
        assertEquals(0.5, boundaryLine.getB(), 0.0);
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
        BoundaryLine bl1 = vl1.newBoundaryLine()
                .setBus("b1")
                .setId("l1")
                .setR(1.0)
                .setX(1.0)
                .setUcteXnodeCode("XNODE")
                .add();
        BoundaryLine bl2 = vl2.newBoundaryLine()
                .setBus("b2")
                .setId("l2")
                .setR(1.0)
                .setX(1.0)
                .setUcteXnodeCode("XNODE")
                .add();
        n.newTieLine()
                .setId("l1 + l2")
                .setBoundaryLine1(bl1.getId())
                .setBoundaryLine2(bl2.getId())
                .add();
        return n;
    }
}
