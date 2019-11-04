/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LineAdapterTest {

    private MergingView network;
    private VoltageLevel voltageLevelA;
    private VoltageLevel voltageLevelB;

    @Before
    public void setUp() {
        network = MergingView.create("LineAdapterTest", "iidm");
        network.merge(BatteryNetworkFactory.create());
        voltageLevelA = network.getVoltageLevel("VLGEN");
        voltageLevelB = network.getVoltageLevel("VLBAT");
    }

    @Test
    public void baseAcLineTests() {
        // adder
        Line acLine = network.getLine("NHV1_NHV2_1");
        assertTrue(acLine instanceof  AbstractAdapter<?>);
        assertEquals("NHV1_NHV2_1", acLine.getId());
        assertEquals("NHV1_NHV2_1", acLine.getName());
        assertEquals(3.0, acLine.getR(), 0.0);
        assertEquals(33.0, acLine.getX(), 0.0);
        assertEquals(0.0, acLine.getG1(), 0.0);
        assertEquals(0.0, acLine.getG2(), 0.0);
        assertEquals(386E-6 / 2, acLine.getB1(), 0.0);
        assertEquals(386E-6 / 2, acLine.getB2(), 0.0);

        Bus busA = voltageLevelA.getBusBreakerView().getBus("NGEN");
        Bus busB = voltageLevelB.getBusBreakerView().getBus("NBAT");
        assertSame(busA, acLine.getTerminal1().getBusBreakerView().getBus());
        assertSame(busB, acLine.getTerminal2().getBusBreakerView().getBus());
        assertSame(busA, acLine.getTerminal("VLGEN").getBusBreakerView().getConnectableBus());
        assertSame(busB, acLine.getTerminal("VLBAT").getBusBreakerView().getConnectableBus());
        assertSame(busA, acLine.getTerminal(Branch.Side.ONE).getBusBreakerView().getConnectableBus());
        assertSame(busB, acLine.getTerminal(Branch.Side.TWO).getBusBreakerView().getConnectableBus());
        assertSame(Branch.Side.ONE, acLine.getSide(acLine.getTerminal1()));

        assertFalse(acLine.isTieLine());
        assertEquals(ConnectableType.LINE, acLine.getType());

        // setter getter
        double r = 10.0;
        double x = 20.0;
        double g1 = 30.0;
        double g2 = 35.0;
        double b1 = 40.0;
        double b2 = 45.0;
        acLine.setR(r);
        assertEquals(r, acLine.getR(), 0.0);
        acLine.setX(x);
        assertEquals(x, acLine.getX(), 0.0);
        acLine.setG1(g1);
        assertEquals(g1, acLine.getG1(), 0.0);
        acLine.setG2(g2);
        assertEquals(g2, acLine.getG2(), 0.0);
        acLine.setB1(b1);
        assertEquals(b1, acLine.getB1(), 0.0);
        acLine.setB2(b2);
        assertEquals(b2, acLine.getB2(), 0.0);
        assertFalse(acLine.isTieLine());

        CurrentLimits currentLimits1 = acLine.newCurrentLimits1()
            .setPermanentLimit(100)
            .beginTemporaryLimit()
            .setName("5'")
            .setAcceptableDuration(5 * 60)
            .setValue(1400)
            .endTemporaryLimit()
            .add();
        CurrentLimits currentLimits2 = acLine.newCurrentLimits2()
            .setPermanentLimit(50)
            .beginTemporaryLimit()
            .setName("20'")
            .setAcceptableDuration(20 * 60)
            .setValue(1200)
            .endTemporaryLimit()
            .add();
        assertSame(currentLimits1, acLine.getCurrentLimits1());
        assertSame(currentLimits2, acLine.getCurrentLimits2());
        assertSame(currentLimits1, acLine.getCurrentLimits(Branch.Side.ONE));
        assertSame(currentLimits2, acLine.getCurrentLimits(Branch.Side.TWO));

        // add power on line
        Terminal terminal1 = acLine.getTerminal1();
        terminal1.setP(1.0);
        terminal1.setQ(Math.sqrt(2.0));
        busA.setV(1.0);
        // i1 = 1000
        assertTrue(acLine.checkPermanentLimit(Branch.Side.ONE, 0.9f));
        assertTrue(acLine.checkPermanentLimit(Branch.Side.ONE));
        assertTrue(acLine.checkPermanentLimit1(0.9f));
        assertTrue(acLine.checkPermanentLimit1());
        assertNotNull(acLine.checkTemporaryLimits(Branch.Side.ONE, 0.9f));
        assertNotNull(acLine.checkTemporaryLimits(Branch.Side.ONE));
        assertNotNull(acLine.checkTemporaryLimits1());
        assertNotNull(acLine.checkTemporaryLimits1(0.9f));

        Terminal terminal2 = acLine.getTerminal2();
        terminal2.setP(1.0);
        terminal2.setQ(Math.sqrt(2.0));
        busB.setV(1.0e3);
        // i2 = 1
        assertFalse(acLine.checkPermanentLimit(Branch.Side.TWO, 0.9f));
        assertFalse(acLine.checkPermanentLimit(Branch.Side.TWO));
        assertFalse(acLine.checkPermanentLimit2(0.9f));
        assertFalse(acLine.checkPermanentLimit2());
        assertNull(acLine.checkTemporaryLimits(Branch.Side.TWO, 0.9f));
        assertNull(acLine.checkTemporaryLimits(Branch.Side.TWO));
        assertNull(acLine.checkTemporaryLimits2(0.9f));
        assertNull(acLine.checkTemporaryLimits2());

        assertTrue(acLine.isOverloaded());
        assertTrue(acLine.isOverloaded(0.9f));
        assertEquals(300, acLine.getOverloadDuration());

        assertEquals(2, acLine.getTerminals().size());
    }
}
