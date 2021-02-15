/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import com.powsybl.iidm.network.util.SV;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public abstract class AbstractLineTest {

    private static final String HALF1_NAME = "half1_name";

    private static final String INVALID = "invalid";

    private static final String LINE_NAME = "lineName";

    private static final String TO_REMOVE = "toRemove";

    private static final String DUPLICATE = "duplicate";

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
                .setName(LINE_NAME)
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
        assertEquals(LINE_NAME, acLine.getOptionalName().orElse(null));
        assertEquals(LINE_NAME, acLine.getNameOrId());
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
    public void testChangesNotification() {
        // Changes listener
        NetworkListener exceptionListener = mock(DefaultNetworkListener.class);
        doThrow(new UnsupportedOperationException()).when(exceptionListener).onUpdate(any(), anyString(), any(), any());
        doThrow(new UnsupportedOperationException()).when(exceptionListener).onUpdate(any(), any(), anyString(), any(),
                any());

        NetworkListener mockedListener = mock(DefaultNetworkListener.class);
        // Add observer changes to current network
        network.addListener(exceptionListener);
        network.addListener(mockedListener);

        // Tested instance
        Line acLine = network.newLine()
                .setId("line")
                .setName(LINE_NAME)
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
        verify(mockedListener, times(1)).onCreation(acLine);
        // Get initial values
        double p0OldValue = acLine.getTerminal1().getP();
        double q0OldValue = acLine.getTerminal1().getQ();
        // Change values P1 & Q1
        acLine.getTerminal1().setP(1.0);
        acLine.getTerminal1().setQ(Math.sqrt(2.0));

        // Check update notification
        verify(mockedListener, times(1)).onUpdate(acLine, "p1", INITIAL_VARIANT_ID, p0OldValue, 1.0);
        verify(mockedListener, times(1)).onUpdate(acLine, "q1", INITIAL_VARIANT_ID, q0OldValue, Math.sqrt(2.0));

        // Change values that not depend on the variant
        acLine.setR(1.5);
        verify(mockedListener, times(1)).onUpdate(acLine, "r", 1.0, 1.5);

        // Simulate exception for onUpdate calls
        doThrow(new PowsyblException())
                .when(mockedListener)
                .onUpdate(any(Line.class), anyString(), anyDouble(), anyDouble());
        // Change P1 value
        try {
            acLine.getTerminal1().setP(1.1);
            verify(mockedListener, times(1)).onUpdate(acLine, "p1", INITIAL_VARIANT_ID, 1.0, 1.1);
        } catch (PowsyblException notExpected) {
            fail();
        }

        // At this point
        // no more changes is taking into account

        // Case when same values P1 & Q1 are set
        acLine.getTerminal1().setP(1.1);
        acLine.getTerminal1().setQ(Math.sqrt(2.0));
        // Case when no listener is registered
        network.removeListener(mockedListener);
        acLine.getTerminal1().setP(2.0);
        acLine.getTerminal1().setQ(1.0);

        // Check no notification
        verifyNoMoreInteractions(mockedListener);
    }

    @Test
    public void invalidR() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("r is invalid");
        createLineBetweenVoltageAB(INVALID, INVALID, Double.NaN, 2.0, 3.0, 3.5, 4.0, 4.5);
    }

    @Test
    public void invalidX() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("x is invalid");
        createLineBetweenVoltageAB(INVALID, INVALID, 1.0, Double.NaN, 3.0, 3.5, 4.0, 4.5);
    }

    @Test
    public void invalidG1() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g1 is invalid");
        createLineBetweenVoltageAB(INVALID, INVALID, 1.0, 2.0, Double.NaN, 3.5, 4.0, 4.5);
    }

    @Test
    public void invalidG2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g2 is invalid");
        createLineBetweenVoltageAB(INVALID, INVALID, 1.0, 2.0, 3.0, Double.NaN, 4.0, 4.5);
    }

    @Test
    public void invalidB1() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b1 is invalid");
        createLineBetweenVoltageAB(INVALID, INVALID, 1.0, 2.0, 3.0, 3.5, Double.NaN, 4.5);
    }

    @Test
    public void invalidB2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b2 is invalid");
        createLineBetweenVoltageAB(INVALID, INVALID, 1.0, 2.0, 3.0, 3.5, 4.0, Double.NaN);
    }

    @Test
    public void duplicateAcLine() {
        createLineBetweenVoltageAB(DUPLICATE, DUPLICATE, 1.0, 2.0, 3.0, 3.5, 4.0, 4.5);
        thrown.expect(PowsyblException.class);
        createLineBetweenVoltageAB(DUPLICATE, DUPLICATE, 1.0, 2.0, 3.0, 3.5, 4.0, 4.5);
    }

    @Test
    public void testRemoveAcLine() {
        createLineBetweenVoltageAB(TO_REMOVE, TO_REMOVE, 1.0, 2.0, 3.0, 3.5, 4.0, 4.5);
        Line line = network.getLine(TO_REMOVE);
        assertNotNull(line);
        int count = network.getLineCount();
        line.remove();
        assertNotNull(line);
        assertNull(network.getLine(TO_REMOVE));
        assertEquals(count - 1L, network.getLineCount());
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

        // adder
        TieLineAdder adder = network.newTieLine().setId("testTie")
                .setName("testNameTie")
                .setVoltageLevel1("vl1")
                .setBus1("busA")
                .setConnectableBus1("busA")
                .setVoltageLevel2("vl2")
                .setBus2("busB")
                .setConnectableBus2("busB")
                .setUcteXnodeCode("ucte")
                .newHalfLine1()
                .setId("hl1")
                .setName(HALF1_NAME)
                .setR(r)
                .setX(x)
                .setB1(hl1b1)
                .setB2(hl1b2)
                .setG1(hl1g1)
                .setG2(hl1g2)
                .add()
                .newHalfLine2()
                .setId("hl2")
                .setR(r2)
                .setX(x2)
                .setB1(hl2b1)
                .setB2(hl2b2)
                .setG1(hl2g1)
                .setG2(hl2g2)
                .add();
        TieLine tieLine = adder.add();
        assertTrue(tieLine.isTieLine());
        assertEquals(ConnectableType.LINE, tieLine.getType());
        assertEquals("ucte", tieLine.getUcteXnodeCode());
        assertEquals("hl1", tieLine.getHalf1().getId());
        assertEquals(HALF1_NAME, tieLine.getHalf1().getName());
        assertEquals("hl2", tieLine.getHalf2().getId());
        assertEquals(r + r2, tieLine.getR(), 0.0);
        assertEquals(x + x2, tieLine.getX(), 0.0);
        assertEquals(hl1g1 + hl1g2, tieLine.getG1(), 0.0);
        assertEquals(hl2g1 + hl2g2, tieLine.getG2(), 0.0);
        assertEquals(hl1b1 + hl1b2, tieLine.getB1(), 0.0);
        assertEquals(hl2b1 + hl2b2, tieLine.getB2(), 0.0);

        // invalid set line characteristics on tieLine
        try {
            tieLine.setR(1.0);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        try {
            tieLine.setX(1.0);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        try {
            tieLine.setB1(1.0);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        try {
            tieLine.setB2(1.0);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        try {
            tieLine.setG1(1.0);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        try {
            tieLine.setG2(1.0);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        TieLine.HalfLine half1 = tieLine.getHalf1();

        TieLine.HalfLine half2 = tieLine.getHalf2();

        // Check notification on HalfLine changes
        NetworkListener mockedListener = mock(DefaultNetworkListener.class);
        // Add observer changes to current network
        network.addListener(mockedListener);
        // Apply changes on Half lines
        half1.setR(r + 1);
        half1.setX(x + 1);
        half1.setG1(hl1g1 + 1);
        half1.setG2(hl1g2 + 1);
        half1.setB1(hl1b1 + 1);
        half1.setB2(hl1b2 + 1);
        half2.setR(r + 1);
        half2.setX(x + 1);
        half2.setG1(hl2g1 + 1);
        half2.setG2(hl2g2 + 1);
        half2.setB1(hl2b1 + 1);
        half2.setB2(hl2b2 + 1);
        verify(mockedListener, times(12)).onUpdate(any(TieLine.class), anyString(), any(), any());
        // Remove observer
        network.removeListener(mockedListener);
        // Cancel changes on Half lines
        half1.setR(r);
        half1.setX(x);
        half1.setG1(hl1g1);
        half1.setG2(hl1g2);
        half1.setB1(hl1b1);
        half1.setB2(hl1b2);
        half2.setR(r);
        half2.setX(x);
        half2.setG1(hl2g1);
        half2.setG2(hl2g2);
        half2.setB1(hl2b1);
        half2.setB2(hl2b2);
        // Check no notification
        verifyNoMoreInteractions(mockedListener);

        double p1 = -605.0;
        double q1 = -302.5;
        double p2 = 600.0;
        double q2 = 300.0;
        double v1 = 420.0;
        double v2 = 380.0;
        double angle1 = -1e-4;
        double angle2 = -1.7e-3;
        tieLine.getTerminal1().setP(p1).setQ(q1).getBusView().getBus().setV(v1).setAngle(angle1);
        tieLine.getTerminal2().setP(p2).setQ(q2).getBusView().getBus().setV(v2).setAngle(angle2);
        SV expectedSV1 = new SV(p1, q1, v1, angle1);
        SV expectedSV2 = new SV(p2, q2, v2, angle2);
        Boundary boundary1 = tieLine.getHalf1().getBoundary();
        Boundary boundary2 = tieLine.getHalf2().getBoundary();
        assertNotNull(boundary1);
        assertNotNull(boundary2);
        assertEquals(expectedSV1.otherSideP(tieLine.getHalf1()), boundary1.getP(), 0.0);
        assertEquals(expectedSV1.otherSideQ(tieLine.getHalf1()), boundary1.getQ(), 0.0);
        assertEquals(expectedSV2.otherSideP(tieLine.getHalf2()), boundary2.getP(), 0.0);
        assertEquals(expectedSV2.otherSideQ(tieLine.getHalf2()), boundary2.getQ(), 0.0);
        assertEquals(expectedSV1.otherSideU(tieLine.getHalf1()), boundary1.getV(), 0.0d);
        assertEquals(expectedSV1.otherSideA(tieLine.getHalf1()), boundary1.getAngle(), 0.0d);
        assertEquals(expectedSV2.otherSideU(tieLine.getHalf2()), boundary2.getV(), 0.0d);
        assertEquals(expectedSV2.otherSideA(tieLine.getHalf2()), boundary2.getAngle(), 0.0d);
        Boundary.BoundaryTerminal boundaryTerminal1 = boundary1.getTerminal();
        Boundary.BoundaryTerminal boundaryTerminal2 = boundary2.getTerminal();
        assertNotNull(boundaryTerminal1);
        assertNotNull(boundaryTerminal2);
        assertEquals(expectedSV1.otherSideP(tieLine.getHalf1()), boundaryTerminal1.getP(), 0.0);
        assertEquals(expectedSV1.otherSideQ(tieLine.getHalf1()), boundaryTerminal1.getQ(), 0.0);
        assertEquals(expectedSV2.otherSideP(tieLine.getHalf2()), boundaryTerminal2.getP(), 0.0);
        assertEquals(expectedSV2.otherSideQ(tieLine.getHalf2()), boundaryTerminal2.getQ(), 0.0);
        double expectedI1 = Math.hypot(expectedSV1.otherSideP(tieLine.getHalf1()), expectedSV1.otherSideQ(tieLine.getHalf1())) / (Math.sqrt(3.) * expectedSV1.otherSideU(tieLine.getHalf1()) / 1000);
        double expectedI2 = Math.hypot(expectedSV2.otherSideP(tieLine.getHalf2()), expectedSV2.otherSideQ(tieLine.getHalf2())) / (Math.sqrt(3.) * expectedSV2.otherSideU(tieLine.getHalf2()) / 1000);
        assertEquals(expectedI1, boundaryTerminal1.getI(), 0.0);
        assertEquals(expectedI2, boundaryTerminal2.getI(), 0.0);
        assertSame(tieLine.getTerminal1().getVoltageLevel(), boundaryTerminal1.getVoltageLevel());
        assertSame(tieLine.getTerminal2().getVoltageLevel(), boundaryTerminal2.getVoltageLevel());
        assertSame(tieLine, boundaryTerminal1.getConnectable());
        assertSame(tieLine, boundaryTerminal2.getConnectable());
        assertTrue(boundaryTerminal1.isConnected());
        assertTrue(boundaryTerminal2.isConnected());

        // Reuse adder
        TieLine tieLine2 = adder.setId("testTie2").add();
        assertNotSame(tieLine.getHalf1(), tieLine2.getHalf1());
        assertNotSame(tieLine.getHalf2(), tieLine2.getHalf2());
    }

    @Test
    public void halfLine1NotSet() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("half line 1 is not set");
        // adder
        network.newTieLine()
                .setId("testTie")
                .setName("testNameTie")
                .setVoltageLevel1("vl1")
                .setBus1("busA")
                .setConnectableBus1("busA")
                .setVoltageLevel2("vl2")
                .setBus2("busB")
                .setConnectableBus2("busB")
                .setUcteXnodeCode("ucte")
                .add();
    }

    @Test
    public void halfLine2NotSet() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("half line 2 is not set");
        // adder
        network.newTieLine()
                .setId("testTie")
                .setName("testNameTie")
                .setVoltageLevel1("vl1")
                .setBus1("busA")
                .setConnectableBus1("busA")
                .setVoltageLevel2("vl2")
                .setBus2("busB")
                .setConnectableBus2("busB")
                .setUcteXnodeCode("ucte")
                .newHalfLine1()
                .setId("hl1")
                .setName(HALF1_NAME)
                .setR(10.0)
                .setX(20.0)
                .setB1(40.0)
                .setB2(45.0)
                .setG1(30.0)
                .setG2(35.0)
                .add()
                .add();
    }

    @Test
    public void invalidHalfLineCharacteristicsR() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("r is not set for half line 1");
        createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, Double.NaN, 2.0,
                3.0, 3.5, 4.0, 4.5, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsX() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("x is not set for half line 1");
        createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, Double.NaN,
                3.0, 3.5, 4.0, 4.5, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsG1() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g1 is not set for half line 1");
        createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                Double.NaN, 3.5, 4.0, 4.5, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsG2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g2 is not set for half line 1");
        createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                3.0, Double.NaN, 4.0, 4.5, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsB1() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b1 is not set for half line 1");
        createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                3.0, 3.5, Double.NaN, 4.5, "code");
    }

    @Test
    public void invalidHalfLineCharacteristicsB2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b2 is not set for half line 1");
        createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                3.0, 3.5, 4.0, Double.NaN, "code");
    }

    @Test
    public void halfLineIdNull() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("id is not set for half line 1");
        createTieLineWithHalfline2ByDefault(INVALID, INVALID, null, 1.0, 2.0,
                3.0, 3.5, 4.0, 4.5, "code");
    }

    @Test
    public void halfLineIdEmpty() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("id is not set for half line 1");
        createTieLineWithHalfline2ByDefault(INVALID, INVALID, "", 1.0, 2.0,
                3.0, 3.5, 4.0, 4.5, "code");
    }

    @Test
    public void uctecodeNull() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("ucteXnodeCode is not set");
        createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                3.0, 3.5, 4.0, 4.5, null);
    }

    @Test
    public void duplicate() {
        createTieLineWithHalfline2ByDefault(DUPLICATE, DUPLICATE, "id1", 1.0, 2.0,
                3.0, 3.5, 4.0, 4.5, DUPLICATE);
        thrown.expect(PowsyblException.class);
        createTieLineWithHalfline2ByDefault(DUPLICATE, DUPLICATE, "id1", 1.0, 2.0,
                3.0, 3.5, 4.0, 4.5, DUPLICATE);
    }

    @Test
    public void testRemove() {
        createTieLineWithHalfline2ByDefault(TO_REMOVE, TO_REMOVE, "id1", 1.0, 2.0,
                3.0, 3.5, 4.0, 4.5,  TO_REMOVE);
        Line line = network.getLine(TO_REMOVE);
        assertNotNull(line);
        assertTrue(line.isTieLine());
        int count = network.getLineCount();
        line.remove();
        assertNull(network.getLine(TO_REMOVE));
        assertNotNull(line);
        assertEquals(count - 1L, network.getLineCount());
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
                                                     double g1, double g2, double b1, double b2, String code) {
        network.newTieLine()
                .setId(id)
                .setName(name)
                .newHalfLine1()
                .setId(halfLineId)
                .setName(HALF1_NAME)
                .setR(r)
                .setX(x)
                .setB1(b1)
                .setB2(b2)
                .setG1(g1)
                .setG2(g2)
                .add()
                .newHalfLine2()
                .setId("hl2")
                .setName("half2_name")
                .setR(1.0)
                .setX(2.0)
                .setB1(3.0)
                .setB2(3.5)
                .setG1(4.0)
                .setG2(4.5)
                .add()
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
