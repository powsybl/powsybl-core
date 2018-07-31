/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class LineTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;
    private VoltageLevel voltageLevelA;
    private VoltageLevel voltageLevelB;

    @Before
    public void setUp() {
        network = NoEquipmentNetworkFactory.create();
        voltageLevelA = network.getVoltageLevel("vl1");
        voltageLevelB = network.getVoltageLevel("vl2");
    }

    @Test
    public void baseAcLineTests() {
        // adder
        Line acLine = network.newLine()
            .setId("line")
            .setName("lineName")
            .setR(1.0)
            .setX(2.0)
            .setG1(3.0)
            .setG2(3.5)
            .setB1(4.0)
            .setB2(4.5)
            .setVoltageLevel1("vl1")
            .setVoltageLevel2("vl2")
            .setBus1("busA")
            .setBus2("busB")
            .setConnectableBus1("busA")
            .setConnectableBus2("busB")
            .add();
        assertEquals("line", acLine.getId());
        assertEquals("lineName", acLine.getName());
        assertEquals(1.0, acLine.getR(), 0.0);
        assertEquals(2.0, acLine.getX(), 0.0);
        assertEquals(3.0, acLine.getG1(), 0.0);
        assertEquals(3.5, acLine.getG2(), 0.0);
        assertEquals(4.0, acLine.getB1(), 0.0);
        assertEquals(4.5, acLine.getB2(), 0.0);

        Bus busA = voltageLevelA.getBusBreakerView().getBus("busA");
        Bus busB = voltageLevelB.getBusBreakerView().getBus("busB");
        assertSame(busA, acLine.getTerminal1().getBusBreakerView().getBus());
        assertSame(busB, acLine.getTerminal2().getBusBreakerView().getBus());
        assertSame(busA, acLine.getTerminal("vl1").getBusBreakerView().getConnectableBus());
        assertSame(busB, acLine.getTerminal("vl2").getBusBreakerView().getConnectableBus());
        assertSame(busA, acLine.getTerminal(Branch.Side.ONE).getBusBreakerView().getConnectableBus());
        assertSame(busB, acLine.getTerminal(Branch.Side.TWO).getBusBreakerView().getConnectableBus());

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
        assertTrue(acLine.checkPermanentLimit1());
        assertNotNull(acLine.checkTemporaryLimits(Branch.Side.ONE, 0.9f));
        assertNotNull(acLine.checkTemporaryLimits(Branch.Side.ONE));

        Terminal terminal2 = acLine.getTerminal2();
        terminal2.setP(1.0);
        terminal2.setQ(Math.sqrt(2.0));
        busB.setV(1.0e3);
        // i2 = 1
        assertFalse(acLine.checkPermanentLimit(Branch.Side.TWO, 0.9f));
        assertFalse(acLine.checkPermanentLimit(Branch.Side.TWO));
        assertFalse(acLine.checkPermanentLimit2());
        assertNull(acLine.checkTemporaryLimits(Branch.Side.TWO, 0.9f));
        assertNull(acLine.checkTemporaryLimits(Branch.Side.TWO));
    }

    @Test
    public void invalidR() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("r is invalid");
        createLineBetweenVoltageAB("invalid", "invalid", Double.NaN, 2.0, 3.0, 3.5, 4.0, 4.5);
    }

    @Test
    public void invalidX() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("x is invalid");
        createLineBetweenVoltageAB("invalid", "invalid", 1.0, Double.NaN, 3.0, 3.5, 4.0, 4.5);
    }

    @Test
    public void invalidG1() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g1 is invalid");
        createLineBetweenVoltageAB("invalid", "invalid", 1.0, 2.0, Double.NaN, 3.5, 4.0, 4.5);
    }

    @Test
    public void invalidG2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g2 is invalid");
        createLineBetweenVoltageAB("invalid", "invalid", 1.0, 2.0, 3.0, Double.NaN, 4.0, 4.5);
    }

    @Test
    public void invalidB1() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b1 is invalid");
        createLineBetweenVoltageAB("invalid", "invalid", 1.0, 2.0, 3.0, 3.5, Double.NaN, 4.5);
    }

    @Test
    public void invalidB2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b2 is invalid");
        createLineBetweenVoltageAB("invalid", "invalid", 1.0, 2.0, 3.0, 3.5, 4.0, Double.NaN);
    }

    @Test
    public void duplicateAcLine() {
        createLineBetweenVoltageAB("duplicate", "duplicate", 1.0, 2.0, 3.0, 3.5, 4.0, 4.5);
        thrown.expect(PowsyblException.class);
        createLineBetweenVoltageAB("duplicate", "duplicate", 1.0, 2.0, 3.0, 3.5, 4.0, 4.5);
    }

    @Test
    public void testRemoveAcLine() {
        createLineBetweenVoltageAB("toRemove", "toRemove", 1.0, 2.0, 3.0, 3.5, 4.0, 4.5);
        Line line = network.getLine("toRemove");
        assertNotNull(line);
        int count = network.getLineCount();
        line.remove();
        assertNotNull(line);
        assertNull(network.getLine("toRemove"));
        assertEquals(count - 1, network.getLineCount());
    }

    @Test
    public void testTieLineAdder() {
        double r = 10.0;
        double r2 = 1.0;
        double x = 20.0;
        double x2 = 2.0;
        double hl1g1 = 30.0;
        double hl1g2 = 35.0;
        double hl1b1 = 40.0;
        double hl1b2 = 45.0;
        double hl2g1 = 130.0;
        double hl2g2 = 135.0;
        double hl2b1 = 140.0;
        double hl2b2 = 145.0;
        double xnodeP = 50.0;
        double xnodeQ = 60.0;

        // adder
        TieLine tieLine = network.newTieLine().setId("testTie")
            .setName("testNameTie")
            .setVoltageLevel1("vl1")
            .setBus1("busA")
            .setConnectableBus1("busA")
            .setVoltageLevel2("vl2")
            .setBus2("busB")
            .setConnectableBus2("busB")
            .setUcteXnodeCode("ucte")
            .line1()
            .setId("hl1")
            .setName("half1_name")
            .setR(r)
            .setX(x)
            .setB1(hl1b1)
            .setB2(hl1b2)
            .setG1(hl1g1)
            .setG2(hl1g2)
            .setXnodeQ(xnodeQ)
            .setXnodeP(xnodeP)
            .line2()
            .setId("hl2")
            .setR(r2)
            .setX(x2)
            .setB1(hl2b1)
            .setB2(hl2b2)
            .setG1(hl2g1)
            .setG2(hl2g2)
            .setXnodeP(xnodeP)
            .setXnodeQ(xnodeQ)
            .add();
        assertTrue(tieLine.isTieLine());
        assertEquals(ConnectableType.LINE, tieLine.getType());
        assertEquals("ucte", tieLine.getUcteXnodeCode());
        assertEquals("half1_name", tieLine.getHalf1().getName());
        assertEquals("hl2", tieLine.getHalf2().getId());
        assertEquals(r + r2, tieLine.getR(), 0.0);
        assertEquals(x + x2, tieLine.getX(), 0.0);
        assertEquals(hl1g1 + hl2g1, tieLine.getG1(), 0.0);
        assertEquals(hl1g2 + hl2g2, tieLine.getG2(), 0.0);
        assertEquals(hl1b1 + hl2b1, tieLine.getB1(), 0.0);
        assertEquals(hl1b2 + hl2b2, tieLine.getB2(), 0.0);

        // invalid set line characteristics on tieLine
        try {
            tieLine.setR(1.0);
            fail();
        } catch (ValidationException ignored) {
        }

        try {
            tieLine.setX(1.0);
            fail();
        } catch (ValidationException ignored) {
        }

        try {
            tieLine.setB1(1.0);
            fail();
        } catch (ValidationException ignored) {
        }

        try {
            tieLine.setB2(1.0);
            fail();
        } catch (ValidationException ignored) {
        }

        try {
            tieLine.setG1(1.0);
            fail();
        } catch (ValidationException ignored) {
        }

        try {
            tieLine.setG2(1.0);
            fail();
        } catch (ValidationException ignored) {
        }

        TieLineImpl.HalfLine half1 = tieLine.getHalf1();
        assertEquals(xnodeP, half1.getXnodeP(), 0.0);
        assertEquals(xnodeQ, half1.getXnodeQ(), 0.0);
    }

    @Test
    public void invalidHalfLineCharacteristicsR() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("r is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", Double.NaN, 2.0,
            3.0, 3.5, 4.0, 4.5, 5.0, 6.0, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsX() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("x is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", 1.0, Double.NaN,
            3.0, 3.5, 4.0, 4.5, 5.0, 6.0, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsG1() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g1 is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", 1.0, 2.0,
            Double.NaN, 3.5, 4.0, 4.5, 5.0, 6.0, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsG2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g2 is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", 1.0, 2.0,
            3.0, Double.NaN, 4.0, 4.5, 5.0, 6.0, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsB1() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b1 is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", 1.0, 2.0,
            3.0, 3.5, Double.NaN, 4.5, 5.0, 6.0, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsB2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b2 is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", 1.0, 2.0,
            3.0, 3.5, 4.0, Double.NaN, 5.0, 6.0, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("xnodeP is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", 1.0, 2.0,
            3.0, 3.5, 4.0, 4.5, Double.NaN, 6.0, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsQ() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("xnodeQ is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", 1.0, 2.0,
            3.0, 3.5, 4.0, 4.5, 5.0, Double.NaN, "code");
    }

    @Test
    public void halfLineIdNull() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("id is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", null, 1.0, 2.0,
            3.0, 3.5, 4.0, 4.5, 5.0, 6.0, "code");
    }

    @Test
    public void uctecodeNull() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("ucteXnodeCode is not set");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", 1.0, 2.0,
            3.0, 3.5, 4.0, 4.5, 5.0, 6.0, null);
    }

    @Test
    public void duplicate() {
        createTieLineWithHalfline2ByDefault("duplicate", "duplicate", "id1", 1.0, 2.0,
            3.0, 3.5, 4.0, 4.5, 5.0, 6.0, "duplicate");
        thrown.expect(PowsyblException.class);
        createTieLineWithHalfline2ByDefault("duplicate", "duplicate", "id1", 1.0, 2.0,
            3.0, 3.5, 4.0, 4.5, 5.0, 6.0, "duplicate");
    }

    @Test
    public void testRemove() {
        createTieLineWithHalfline2ByDefault("toRemove", "toRemove", "id1", 1.0, 2.0,
            3.0, 3.5, 4.0, 4.5, 5.0, 6.0, "toRemove");
        Line line = network.getLine("toRemove");
        assertNotNull(line);
        assertTrue(line.isTieLine());
        int count = network.getLineCount();
        line.remove();
        assertNull(network.getLine("toRemove"));
        assertNotNull(line);
        assertEquals(count - 1, network.getLineCount());
    }

    private void createLineBetweenVoltageAB(String id, String name, double r, double x,
                                            double g1, double g2, double b1, double b2) {
        network.newLine()
            .setId(id)
            .setName(name)
            .setR(r)
            .setX(x)
            .setG1(g1)
            .setG2(g2)
            .setB1(b1)
            .setB2(b2)
            .setVoltageLevel1("vl1")
            .setVoltageLevel2("vl2")
            .setBus1("busA")
            .setBus2("busB")
            .setConnectableBus1("busA")
            .setConnectableBus2("busB")
            .add();
    }

    private void createTieLineWithHalfline2ByDefault(String id, String name, String halfLineId, double r, double x,
                                                     double g1, double g2, double b1, double b2,
                                                     double xnodeP, double xnodeQ, String code) {
        network.newTieLine()
            .setId(id)
            .setName(name)
            .line1()
            .setId(halfLineId)
            .setName("half1_name")
            .setR(r)
            .setX(x)
            .setB1(b1)
            .setB2(b2)
            .setG1(g1)
            .setG2(g2)
            .setXnodeQ(xnodeQ)
            .setXnodeP(xnodeP)
            .line2()
            .setId("hl2")
            .setName("half2_name")
            .setR(1.0)
            .setX(2.0)
            .setB1(3.0)
            .setB2(3.5)
            .setG1(4.0)
            .setG2(4.5)
            .setXnodeP(5.0)
            .setXnodeQ(6.0)
            .setVoltageLevel1("vl1")
            .setBus1("busA")
            .setConnectableBus1("busA")
            .setVoltageLevel2("vl2")
            .setBus2("busB")
            .setConnectableBus2("busB")
            .setUcteXnodeCode(code)
            .add();
    }
}
