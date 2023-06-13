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
        assertNotNull(tieLine.getDanglingLine1());
        assertNotNull(tieLine.getDanglingLine2());

        assertSame(tieLine.getDanglingLine1(), tieLine.getDanglingLine1().getBoundary().getDanglingLine());
        assertSame(tieLine.getDanglingLine1(), tieLine.getDanglingLine1().getBoundary().getDanglingLine());
        assertSame(tieLine.getDanglingLine1().getTerminal().getVoltageLevel(), tieLine.getDanglingLine1().getBoundary().getNetworkSideVoltageLevel());

        assertSame(tieLine.getDanglingLine2(), tieLine.getDanglingLine2().getBoundary().getDanglingLine());
        assertSame(tieLine.getDanglingLine2(), tieLine.getDanglingLine2().getBoundary().getDanglingLine());
        assertSame(tieLine.getDanglingLine2().getTerminal().getVoltageLevel(), tieLine.getDanglingLine2().getBoundary().getNetworkSideVoltageLevel());

        checkDanglingLine(tieLine.getDanglingLine1());
        checkDanglingLine(tieLine.getDanglingLine2());
    }

    private static void checkDanglingLine(DanglingLine danglingLine) {
        assertEquals(1.0, danglingLine.getR(), 0.0);
        danglingLine.setR(2.0);
        assertEquals(2.0, danglingLine.getR(), 0.0);
        assertEquals(1.0, danglingLine.getX(), 0.0);
        danglingLine.setX(2.0);
        assertEquals(2.0, danglingLine.getX(), 0.0);
        assertEquals(0.0, danglingLine.getG(), 0.0);
        danglingLine.setG(0.5);
        assertEquals(0.5, danglingLine.getG(), 0.0);
        assertEquals(0.0, danglingLine.getB(), 0.0);
        danglingLine.setB(0.5);
        assertEquals(0.5, danglingLine.getB(), 0.0);
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
        DanglingLine dl1 = vl1.newDanglingLine()
                .setBus("b1")
                .setId("l1")
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.0)
                .setX(1.0)
                .setUcteXnodeCode("XNODE")
                .add();
        DanglingLine dl2 = vl2.newDanglingLine()
                .setBus("b2")
                .setId("l2")
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.0)
                .setX(1.0)
                .setUcteXnodeCode("XNODE")
                .add();
        n.newTieLine()
                .setId("l1 + l2")
                .setDanglingLine1(dl1.getId())
                .setDanglingLine2(dl2.getId())
                .add();
        return n;
    }
}
