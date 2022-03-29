/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import com.powsybl.iidm.network.util.SV;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

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

        assertEquals(1, Iterables.size(voltageLevelA.getLines()));
        assertEquals(1, voltageLevelA.getLineStream().count());
        assertEquals(1, voltageLevelA.getLineCount());
        assertSame(acLine, voltageLevelA.getLines().iterator().next());
        assertSame(acLine, voltageLevelA.getLineStream().findFirst().get());

        Bus busA = voltageLevelA.getBusBreakerView().getBus("busA");
        Bus busB = voltageLevelB.getBusBreakerView().getBus("busB");
        assertSame(busA, acLine.getTerminal1().getBusBreakerView().getBus());
        assertSame(busB, acLine.getTerminal2().getBusBreakerView().getBus());
        assertSame(busA, acLine.getTerminal("vl1").getBusBreakerView().getConnectableBus());
        assertSame(busB, acLine.getTerminal("vl2").getBusBreakerView().getConnectableBus());
        assertSame(busA, acLine.getTerminal(Branch.Side.ONE).getBusBreakerView().getConnectableBus());
        assertSame(busB, acLine.getTerminal(Branch.Side.TWO).getBusBreakerView().getConnectableBus());

        assertFalse(acLine.isTieLine());
        assertEquals(IdentifiableType.LINE, acLine.getType());

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
        assertTrue(acLine.checkPermanentLimit(Branch.Side.ONE, 0.9f, LimitType.CURRENT));
        assertTrue(acLine.checkPermanentLimit(Branch.Side.ONE, LimitType.CURRENT));
        assertTrue(acLine.checkPermanentLimit1(LimitType.CURRENT));
        assertNotNull(acLine.checkTemporaryLimits(Branch.Side.ONE, 0.9f, LimitType.CURRENT));
        assertNotNull(acLine.checkTemporaryLimits(Branch.Side.ONE, LimitType.CURRENT));

        Terminal terminal2 = acLine.getTerminal2();
        terminal2.setP(1.0);
        terminal2.setQ(Math.sqrt(2.0));
        busB.setV(1.0e3);
        // i2 = 1
        assertFalse(acLine.checkPermanentLimit(Branch.Side.TWO, 0.9f, LimitType.CURRENT));
        assertFalse(acLine.checkPermanentLimit(Branch.Side.TWO, LimitType.CURRENT));
        assertFalse(acLine.checkPermanentLimit2(LimitType.CURRENT));
        assertNull(acLine.checkTemporaryLimits(Branch.Side.TWO, 0.9f, LimitType.CURRENT));
        assertNull(acLine.checkTemporaryLimits(Branch.Side.TWO, LimitType.CURRENT));
    }

    @Test
    public void testMove1Bb() {
        Line line = createLineBetweenVoltageAB("line", LINE_NAME, 1.0, 2.0, 3.0, 3.5, 4.0, 4.5);
        Bus busC = voltageLevelA.getBusBreakerView().newBus()
                .setId("busC")
                .add();

        VoltageLevel vlNb = network.getSubstation("sub").newVoltageLevel()
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setId("VL_NB")
                .setNominalV(400.0)
                .setLowVoltageLimit(380.0)
                .setHighVoltageLimit(420.0)
                .add();
        line.getTerminal1().getNodeBreakerView().moveConnectable(0, vlNb.getId());
        assertEquals(0, line.getTerminal1().getNodeBreakerView().getNode());
        assertSame(vlNb, line.getTerminal1().getVoltageLevel());
        Terminal.NodeBreakerView tNbv = line.getTerminal1().getNodeBreakerView();
        try {
            tNbv.moveConnectable(0, "vl1");
            fail();
        } catch (RuntimeException e) {
            assertEquals("Trying to move connectable line to node 0 of voltage level vl1, which is a bus breaker voltage level", e.getMessage());
        }

        line.getTerminal1().getBusBreakerView().moveConnectable(busC.getId(), true);
        assertSame(busC, line.getTerminal1().getBusBreakerView().getConnectableBus());
        assertSame(busC, line.getTerminal1().getBusBreakerView().getBus());

        line.getTerminal1().getBusBreakerView().moveConnectable("busA", false);
        assertSame(network.getBusBreakerView().getBus("busA"), line.getTerminal1().getBusBreakerView().getConnectableBus());
        assertNull(line.getTerminal1().getBusBreakerView().getBus());
    }

    @Test
    public void testMove1NbNetwork() {
        Network fictitiousSwitchNetwork = FictitiousSwitchFactory.create();
        Line line = fictitiousSwitchNetwork.getLine("CJ");

        VoltageLevel vlBb = fictitiousSwitchNetwork.getSubstation("A")
                .newVoltageLevel()
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setId("VL_BB")
                .setNominalV(400.0)
                .setLowVoltageLimit(380.0)
                .setHighVoltageLimit(420.0)
                .add();
        Bus bus = vlBb.getBusBreakerView().newBus().setId("bus").add();
        line.getTerminal1().getBusBreakerView().moveConnectable(bus.getId(), true);
        assertSame(bus, line.getTerminal1().getBusBreakerView().getConnectableBus());
        assertSame(bus, line.getTerminal1().getBusBreakerView().getBus());

        String busCcId = fictitiousSwitchNetwork.getGenerator("CC").getTerminal().getBusBreakerView().getBus().getId();
        Terminal.BusBreakerView tBbv = line.getTerminal1().getBusBreakerView();
        try {
            tBbv.moveConnectable(busCcId, true);
            fail();
        } catch (RuntimeException e) {
            assertEquals("Trying to move connectable CJ to bus N_14 of voltage level N, which is a node breaker voltage level",
                    e.getMessage());
        }

        line.getTerminal1().getNodeBreakerView().moveConnectable(6, "C");
        assertEquals(6, line.getTerminal1().getNodeBreakerView().getNode());
        assertSame(fictitiousSwitchNetwork.getVoltageLevel("C"), line.getTerminal1().getVoltageLevel());

        line.getTerminal1().getNodeBreakerView().moveConnectable(4, "C");
        assertEquals(4, line.getTerminal1().getNodeBreakerView().getNode());
        assertSame(fictitiousSwitchNetwork.getVoltageLevel("C"), line.getTerminal1().getVoltageLevel());
    }

    @Test
    public void testMove2Bb() {
        Line line = createLineBetweenVoltageAB("line", LINE_NAME, 1.0, 2.0, 3.0, 3.5, 4.0, 4.5);
        Bus busC = voltageLevelB.getBusBreakerView().newBus()
                .setId("busC")
                .add();
        VoltageLevel vlNb = network.getSubstation("sub").newVoltageLevel()
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setId("VL_NB")
                .setNominalV(400.0)
                .setLowVoltageLimit(380.0)
                .setHighVoltageLimit(420.0)
                .add();
        line.getTerminal2().getNodeBreakerView().moveConnectable(0, vlNb.getId());
        assertEquals(0, line.getTerminal2().getNodeBreakerView().getNode());
        assertSame(vlNb, line.getTerminal2().getVoltageLevel());
        Terminal.NodeBreakerView tNbv = line.getTerminal2().getNodeBreakerView();
        try {
            tNbv.moveConnectable(0, "vl2");
            fail();
        } catch (RuntimeException e) {
            assertEquals("Trying to move connectable line to node 0 of voltage level vl2, which is a bus breaker voltage level", e.getMessage());
        }
        line.getTerminal2().getBusBreakerView().moveConnectable(busC.getId(), true);
        assertSame(busC, line.getTerminal2().getBusBreakerView().getConnectableBus());
        assertSame(busC, line.getTerminal2().getBusBreakerView().getBus());
        line.getTerminal2().getBusBreakerView().moveConnectable("busB", false);
        assertSame(network.getBusBreakerView().getBus("busB"), line.getTerminal2().getBusBreakerView().getConnectableBus());
        assertNull(line.getTerminal2().getBusBreakerView().getBus());
    }

    @Test
    public void testMove2Nb() {
        Network fictitiousSwitchNetwork = FictitiousSwitchFactory.create();
        Line line = fictitiousSwitchNetwork.getLine("CJ");
        VoltageLevel vlBb = fictitiousSwitchNetwork.getSubstation("A")
                .newVoltageLevel()
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setId("VL_BB")
                .setNominalV(400.0)
                .setLowVoltageLimit(380.0)
                .setHighVoltageLimit(420.0)
                .add();
        Bus bus = vlBb.getBusBreakerView().newBus().setId("bus").add();
        line.getTerminal2().getBusBreakerView().moveConnectable(bus.getId(), true);
        assertSame(bus, line.getTerminal2().getBusBreakerView().getConnectableBus());
        assertSame(bus, line.getTerminal2().getBusBreakerView().getBus());

        String calculatedBusCHId = fictitiousSwitchNetwork.getLoad("CH").getTerminal().getBusBreakerView().getBus().getId();
        Terminal.BusBreakerView tBbv0 = line.getTerminal2().getBusBreakerView();
        try {
            tBbv0.moveConnectable(calculatedBusCHId, true);
            fail();
        } catch (RuntimeException e) {
            assertEquals("Trying to move connectable CJ to bus N_22 of voltage level N, which is a node breaker voltage level", e.getMessage());
        }
        line.getTerminal2().getNodeBreakerView().moveConnectable(6, "N");
        assertEquals(6, line.getTerminal2().getNodeBreakerView().getNode());
        assertSame(fictitiousSwitchNetwork.getVoltageLevel("N"), line.getTerminal2().getVoltageLevel());
        line.getTerminal2().getNodeBreakerView().moveConnectable(5, "N");
        assertEquals(5, line.getTerminal2().getNodeBreakerView().getNode());
        assertSame(fictitiousSwitchNetwork.getVoltageLevel("N"), line.getTerminal2().getVoltageLevel());

        String calculatedBusCId = fictitiousSwitchNetwork.getVoltageLevel("C").getNodeBreakerView().getTerminal(0).getBusBreakerView().getBus().getId();
        Terminal.BusBreakerView tBbv1 = line.getTerminal2().getBusBreakerView();
        try {
            tBbv1.moveConnectable(calculatedBusCId, true);
            fail();
        } catch (RuntimeException e) {
            assertEquals("Trying to move connectable CJ to bus C_0 of voltage level C, which is a node breaker voltage level", e.getMessage());
        }
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
        Terminal t1 = line.getTerminal1();
        Terminal t2 = line.getTerminal2();
        assertNotNull(t1);
        assertNotNull(t2);
        assertNull(t1.getBusBreakerView().getBus());
        assertNull(t1.getBusBreakerView().getConnectableBus());
        assertNull(t1.getBusView().getBus());
        assertNull(t1.getVoltageLevel());
        assertNull(t2.getBusBreakerView().getBus());
        assertNull(t2.getBusBreakerView().getConnectableBus());
        assertNull(t2.getBusView().getBus());
        assertNull(t2.getVoltageLevel());
        try {
            t1.traverse(Mockito.mock(Terminal.TopologyTraverser.class));
            fail();
        } catch (PowsyblException e) {
            assertEquals("Associated equipment is removed", e.getMessage());
        }
        try {
            t2.traverse(Mockito.mock(Terminal.TopologyTraverser.class));
            fail();
        } catch (PowsyblException e) {
            assertEquals("Associated equipment is removed", e.getMessage());
        }
        Terminal.BusBreakerView bbView1 = t1.getBusBreakerView();
        try {
            bbView1.moveConnectable("BUS", true);
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot modify removed equipment", e.getMessage());
        }
        Terminal.BusBreakerView bbView2 = t2.getBusBreakerView();
        try {
            bbView2.moveConnectable("BUS", true);
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot modify removed equipment", e.getMessage());
        }
        assertNull(line.getNetwork());
        assertNull(network.getLine(TO_REMOVE));
        assertEquals(count - 1L, network.getLineCount());
    }

    @Test
    public void testTieLineAdder() {
        double tol = 0.0000001;

        double r = 10.0;
        double r2 = 1.0;
        double x = 20.0;
        double x2 = 2.0;
        double hl1g1 = 0.03;
        double hl1g2 = 0.035;
        double hl1b1 = 0.04;
        double hl1b2 = 0.045;
        double hl2g1 = 0.013;
        double hl2g2 = 0.0135;
        double hl2b1 = 0.014;
        double hl2b2 = 0.0145;

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
        assertEquals(IdentifiableType.LINE, tieLine.getType());
        assertEquals("ucte", tieLine.getUcteXnodeCode());
        assertEquals("hl1", tieLine.getHalf1().getId());
        assertEquals(HALF1_NAME, tieLine.getHalf1().getName());
        assertEquals("hl2", tieLine.getHalf2().getId());
        assertEquals(7.20, tieLine.getR(), tol);
        assertEquals(22.15, tieLine.getX(), tol);
        assertEquals(0.03539991244, tieLine.getG1(), tol);
        assertEquals(0.06749912436, tieLine.getG2(), tol);
        assertEquals(0.04491554716, tieLine.getB1(), tol);
        assertEquals(0.06365547158, tieLine.getB2(), tol);

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

        // Reuse adder
        TieLine tieLine2 = adder.setId("testTie2").add();
        assertNotSame(tieLine.getHalf1(), tieLine2.getHalf1());
        assertNotSame(tieLine.getHalf2(), tieLine2.getHalf2());

        // Update power flows, voltages and angles
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

        // test boundaries values
        SV expectedSV1 = new SV(p1, q1, v1, angle1, Branch.Side.ONE);
        SV expectedSV2 = new SV(p2, q2, v2, angle2, Branch.Side.TWO);
        assertEquals(expectedSV1.otherSideP(tieLine.getHalf1()), tieLine.getHalf1().getBoundary().getP(), 0.0d);
        assertEquals(expectedSV1.otherSideQ(tieLine.getHalf1()), tieLine.getHalf1().getBoundary().getQ(), 0.0d);
        assertEquals(expectedSV2.otherSideP(tieLine.getHalf2()), tieLine.getHalf2().getBoundary().getP(), 0.0d);
        assertEquals(expectedSV2.otherSideQ(tieLine.getHalf2()), tieLine.getHalf2().getBoundary().getQ(), 0.0d);
        assertEquals(expectedSV1.otherSideU(tieLine.getHalf1()), tieLine.getHalf1().getBoundary().getV(), 0.0d);
        assertEquals(expectedSV1.otherSideA(tieLine.getHalf1()), tieLine.getHalf1().getBoundary().getAngle(), 0.0d);
        assertEquals(expectedSV2.otherSideU(tieLine.getHalf2()), tieLine.getHalf2().getBoundary().getV(), 0.0d);
        assertEquals(expectedSV2.otherSideA(tieLine.getHalf2()), tieLine.getHalf2().getBoundary().getAngle(), 0.0d);
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

    private Line createLineBetweenVoltageAB(String id, String name, double r, double x,
                                            double g1, double g2, double b1, double b2) {
        return network.newLine()
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
