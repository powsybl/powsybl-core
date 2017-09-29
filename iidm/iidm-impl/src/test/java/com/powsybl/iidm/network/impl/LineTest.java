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
            .setR(1.0f)
            .setX(2.0f)
            .setG1(3.0f)
            .setG2(3.5f)
            .setB1(4.0f)
            .setB2(4.5f)
            .setVoltageLevel1("vl1")
            .setVoltageLevel2("vl2")
            .setBus1("busA")
            .setBus2("busB")
            .setConnectableBus1("busA")
            .setConnectableBus2("busB")
            .add();
        assertEquals("line", acLine.getId());
        assertEquals("lineName", acLine.getName());
        assertEquals(1.0f, acLine.getR(), 0.0f);
        assertEquals(2.0f, acLine.getX(), 0.0f);
        assertEquals(3.0f, acLine.getG1(), 0.0f);
        assertEquals(3.5f, acLine.getG2(), 0.0f);
        assertEquals(4.0f, acLine.getB1(), 0.0f);
        assertEquals(4.5f, acLine.getB2(), 0.0f);

        Bus busA = voltageLevelA.getBusBreakerView().getBus("busA");
        Bus busB = voltageLevelB.getBusBreakerView().getBus("busB");
        assertSame(busA, acLine.getTerminal1().getBusBreakerView().getBus());
        assertSame(busB, acLine.getTerminal2().getBusBreakerView().getBus());
        assertSame(busA, acLine.getTerminal("vl1").getBusBreakerView().getConnectableBus());
        assertSame(busB, acLine.getTerminal("vl2").getBusBreakerView().getConnectableBus());
        assertSame(busA, acLine.getTerminal(TwoTerminalsConnectable.Side.ONE).getBusBreakerView().getConnectableBus());
        assertSame(busB, acLine.getTerminal(TwoTerminalsConnectable.Side.TWO).getBusBreakerView().getConnectableBus());

        assertFalse(acLine.isTieLine());
        assertEquals(ConnectableType.LINE, acLine.getType());

        // setter getter
        float r = 10.0f;
        float x = 20.0f;
        float g1 = 30.0f;
        float g2 = 35.0f;
        float b1 = 40.0f;
        float b2 = 45.0f;
        acLine.setR(r);
        assertEquals(r, acLine.getR(), 0.0f);
        acLine.setX(x);
        assertEquals(x, acLine.getX(), 0.0f);
        acLine.setG1(g1);
        assertEquals(g1, acLine.getG1(), 0.0f);
        acLine.setG2(g2);
        assertEquals(g2, acLine.getG2(), 0.0f);
        acLine.setB1(b1);
        assertEquals(b1, acLine.getB1(), 0.0f);
        acLine.setB2(b2);
        assertEquals(b2, acLine.getB2(), 0.0f);
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
        terminal1.setP(1.0f);
        terminal1.setQ((float) Math.sqrt(2.0f));
        busA.setV(1.0f);
        // i1 = 1000
        assertTrue(acLine.checkPermanentLimit(TwoTerminalsConnectable.Side.ONE, 0.9f));
        assertTrue(acLine.checkPermanentLimit(TwoTerminalsConnectable.Side.ONE));
        assertTrue(acLine.checkPermanentLimit1());
        assertNotNull(acLine.checkTemporaryLimits(TwoTerminalsConnectable.Side.ONE, 0.9f));
        assertNotNull(acLine.checkTemporaryLimits(TwoTerminalsConnectable.Side.ONE));

        Terminal terminal2 = acLine.getTerminal2();
        terminal2.setP(1.0f);
        terminal2.setQ((float) Math.sqrt(2.0f));
        busB.setV(1.0e3f);
        // i2 = 1
        assertFalse(acLine.checkPermanentLimit(TwoTerminalsConnectable.Side.TWO, 0.9f));
        assertFalse(acLine.checkPermanentLimit(TwoTerminalsConnectable.Side.TWO));
        assertFalse(acLine.checkPermanentLimit2());
        assertNull(acLine.checkTemporaryLimits(TwoTerminalsConnectable.Side.TWO, 0.9f));
        assertNull(acLine.checkTemporaryLimits(TwoTerminalsConnectable.Side.TWO));
    }

    @Test
    public void invalidR() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("r is invalid");
        createLineBetweenVoltageAB("invalid", "invalid", Float.NaN, 2.0f, 3.0f, 3.5f, 4.0f, 4.5f);
    }

    @Test
    public void invalidX() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("x is invalid");
        createLineBetweenVoltageAB("invalid", "invalid", 1.0f, Float.NaN, 3.0f, 3.5f, 4.0f, 4.5f);
    }

    @Test
    public void invalidG1() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g1 is invalid");
        createLineBetweenVoltageAB("invalid", "invalid", 1.0f, 2.0f, Float.NaN, 3.5f, 4.0f, 4.5f);
    }

    @Test
    public void invalidG2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g2 is invalid");
        createLineBetweenVoltageAB("invalid", "invalid", 1.0f, 2.0f, 3.0f, Float.NaN, 4.0f, 4.5f);
    }

    @Test
    public void invalidB1() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b1 is invalid");
        createLineBetweenVoltageAB("invalid", "invalid", 1.0f, 2.0f, 3.0f, 3.5f, Float.NaN, 4.5f);
    }

    @Test
    public void invalidB2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b2 is invalid");
        createLineBetweenVoltageAB("invalid", "invalid", 1.0f, 2.0f, 3.0f, 3.5f, 4.0f, Float.NaN);
    }

    @Test
    public void duplicateAcLine() {
        createLineBetweenVoltageAB("duplicate", "duplicate", 1.0f, 2.0f, 3.0f, 3.5f, 4.0f, 4.5f);
        thrown.expect(PowsyblException.class);
        createLineBetweenVoltageAB("duplicate", "duplicate", 1.0f, 2.0f, 3.0f, 3.5f, 4.0f, 4.5f);
    }

    @Test
    public void testRemoveAcLine() {
        createLineBetweenVoltageAB("toRemove", "toRemove", 1.0f, 2.0f, 3.0f, 3.5f, 4.0f, 4.5f);
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
        float r = 10.0f;
        float r2 = 1.0f;
        float x = 20.0f;
        float x2 = 2.0f;
        float hl1g1 = 30.0f;
        float hl1g2 = 35.0f;
        float hl1b1 = 40.0f;
        float hl1b2 = 45.0f;
        float hl2g1 = 130.0f;
        float hl2g2 = 135.0f;
        float hl2b1 = 140.0f;
        float hl2b2 = 145.0f;
        float xnodeP = 50.0f;
        float xnodeQ = 60.0f;

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
        assertEquals(r + r2, tieLine.getR(), 0.0f);
        assertEquals(x + x2, tieLine.getX(), 0.0f);
        assertEquals(hl1g1 + hl2g1, tieLine.getG1(), 0.0f);
        assertEquals(hl1g2 + hl2g2, tieLine.getG2(), 0.0f);
        assertEquals(hl1b1 + hl2b1, tieLine.getB1(), 0.0f);
        assertEquals(hl1b2 + hl2b2, tieLine.getB2(), 0.0f);

        // invalid set line characteristics on tieLine
        try {
            tieLine.setR(1.0f);
            fail();
        } catch (ValidationException ignored) {
        }

        try {
            tieLine.setX(1.0f);
            fail();
        } catch (ValidationException ignored) {
        }

        try {
            tieLine.setB1(1.0f);
            fail();
        } catch (ValidationException ignored) {
        }

        try {
            tieLine.setB2(1.0f);
            fail();
        } catch (ValidationException ignored) {
        }

        try {
            tieLine.setG1(1.0f);
            fail();
        } catch (ValidationException ignored) {
        }

        try {
            tieLine.setG2(1.0f);
            fail();
        } catch (ValidationException ignored) {
        }

        TieLineImpl.HalfLine half1 = tieLine.getHalf1();
        assertEquals(xnodeP, half1.getXnodeP(), 0.0f);
        assertEquals(xnodeQ, half1.getXnodeQ(), 0.0f);
    }

    @Test
    public void invalidHalfLineCharacteristicsR() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("r is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", Float.NaN, 2.0f,
            3.0f, 3.5f, 4.0f, 4.5f, 5.0f, 6.0f, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsX() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("x is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", 1.0f, Float.NaN,
            3.0f, 3.5f, 4.0f, 4.5f, 5.0f, 6.0f, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsG1() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g1 is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", 1.0f, 2.0f,
            Float.NaN, 3.5f, 4.0f, 4.5f, 5.0f, 6.0f, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsG2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g2 is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", 1.0f, 2.0f,
            3.0f, Float.NaN, 4.0f, 4.5f, 5.0f, 6.0f, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsB1() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b1 is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", 1.0f, 2.0f,
            3.0f, 3.5f, Float.NaN, 4.5f, 5.0f, 6.0f, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsB2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b2 is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", 1.0f, 2.0f,
            3.0f, 3.5f, 4.0f, Float.NaN, 5.0f, 6.0f, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("xnodeP is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", 1.0f, 2.0f,
            3.0f, 3.5f, 4.0f, 4.5f, Float.NaN, 6.0f, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsQ() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("xnodeQ is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", 1.0f, 2.0f,
            3.0f, 3.5f, 4.0f, 4.5f, 5.0f, Float.NaN, "code");
    }

    @Test
    public void halfLineIdNull() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("id is not set for half line");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", null, 1.0f, 2.0f,
            3.0f, 3.5f, 4.0f, 4.5f, 5.0f, 6.0f, "code");
    }

    @Test
    public void uctecodeNull() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("ucteXnodeCode is not set");
        createTieLineWithHalfline2ByDefault("invalid", "invalid", "invalid", 1.0f, 2.0f,
            3.0f, 3.5f, 4.0f, 4.5f, 5.0f, 6.0f, null);
    }

    @Test
    public void duplicate() {
        createTieLineWithHalfline2ByDefault("duplicate", "duplicate", "id1", 1.0f, 2.0f,
            3.0f, 3.5f, 4.0f, 4.5f, 5.0f, 6.0f, "duplicate");
        thrown.expect(PowsyblException.class);
        createTieLineWithHalfline2ByDefault("duplicate", "duplicate", "id1", 1.0f, 2.0f,
            3.0f, 3.5f, 4.0f, 4.5f, 5.0f, 6.0f, "duplicate");
    }

    @Test
    public void testRemove() {
        createTieLineWithHalfline2ByDefault("toRemove", "toRemove", "id1", 1.0f, 2.0f,
            3.0f, 3.5f, 4.0f, 4.5f, 5.0f, 6.0f, "toRemove");
        Line line = network.getLine("toRemove");
        assertNotNull(line);
        assertTrue(line.isTieLine());
        int count = network.getLineCount();
        line.remove();
        assertNull(network.getLine("toRemove"));
        assertNotNull(line);
        assertEquals(count - 1, network.getLineCount());
    }

    private void createLineBetweenVoltageAB(String id, String name, float r, float x,
                                            float g1, float g2, float b1, float b2) {
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

    private void createTieLineWithHalfline2ByDefault(String id, String name, String halfLineId, float r, float x,
                                                     float g1, float g2, float b1, float b2,
                                                     float xnodeP, float xnodeQ, String code) {
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
            .setR(1.0f)
            .setX(2.0f)
            .setB1(3.0f)
            .setB2(3.5f)
            .setG1(4.0f)
            .setG2(4.5f)
            .setXnodeP(5.0f)
            .setXnodeQ(6.0f)
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
