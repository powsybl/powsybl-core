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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public abstract class AbstractLineTest {

    private static final String DANGLING1_NAME = "dl1_name";

    private static final String INVALID = "invalid";

    private static final String LINE_NAME = "lineName";

    private static final String TO_REMOVE = "toRemove";

    private static final String DUPLICATE = "duplicate";

    private Network network;
    private VoltageLevel voltageLevelA;
    private VoltageLevel voltageLevelB;

    @BeforeEach
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
        assertSame(currentLimits1, acLine.getCurrentLimits1().orElse(null));
        assertSame(currentLimits2, acLine.getCurrentLimits2().orElse(null));
        assertSame(currentLimits1, acLine.getLimits(LimitType.CURRENT, Branch.Side.ONE).orElse(null));
        assertSame(currentLimits2, acLine.getLimits(LimitType.CURRENT, Branch.Side.TWO).orElse(null));

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
    public void testDefaultLine() {
        Line acLine = network.newLine()
                .setId("line")
                .setName(LINE_NAME)
                .setR(1.0)
                .setX(2.0)
                .setBus1("busA")
                .setBus2("busB")
                .add();

        assertEquals(0.0, acLine.getG1(), 0.0);
        assertEquals(0.0, acLine.getG2(), 0.0);
        assertEquals(0.0, acLine.getB1(), 0.0);
        assertEquals(0.0, acLine.getB2(), 0.0);

        assertSame(voltageLevelA, acLine.getTerminal1().getVoltageLevel());
        assertSame(voltageLevelB, acLine.getTerminal2().getVoltageLevel());
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
        ValidationException e = assertThrows(ValidationException.class, () -> createLineBetweenVoltageAB(INVALID, INVALID, Double.NaN, 2.0, 3.0, 3.5, 4.0, 4.5));
        assertTrue(e.getMessage().contains("r is invalid"));
    }

    @Test
    public void invalidX() {
        ValidationException e = assertThrows(ValidationException.class, () -> createLineBetweenVoltageAB(INVALID, INVALID, 1.0, Double.NaN, 3.0, 3.5, 4.0, 4.5));
        assertTrue(e.getMessage().contains("x is invalid"));
    }

    @Test
    public void invalidG1() {
        ValidationException e = assertThrows(ValidationException.class, () -> createLineBetweenVoltageAB(INVALID, INVALID, 1.0, 2.0, Double.NaN, 3.5, 4.0, 4.5));
        assertTrue(e.getMessage().contains("g1 is invalid"));
    }

    @Test
    public void invalidG2() {
        ValidationException e = assertThrows(ValidationException.class, () -> createLineBetweenVoltageAB(INVALID, INVALID, 1.0, 2.0, 3.0, Double.NaN, 4.0, 4.5));
        assertTrue(e.getMessage().contains("g2 is invalid"));
    }

    @Test
    public void invalidB1() {
        ValidationException e = assertThrows(ValidationException.class, () -> createLineBetweenVoltageAB(INVALID, INVALID, 1.0, 2.0, 3.0, 3.5, Double.NaN, 4.5));
        assertTrue(e.getMessage().contains("b1 is invalid"));
    }

    @Test
    public void invalidB2() {
        ValidationException e = assertThrows(ValidationException.class, () -> createLineBetweenVoltageAB(INVALID, INVALID, 1.0, 2.0, 3.0, 3.5, 4.0, Double.NaN));
        assertTrue(e.getMessage().contains("b2 is invalid"));
    }

    @Test
    public void duplicateAcLine() {
        createLineBetweenVoltageAB(DUPLICATE, DUPLICATE, 1.0, 2.0, 3.0, 3.5, 4.0, 4.5);
        assertThrows(PowsyblException.class, () -> createLineBetweenVoltageAB(DUPLICATE, DUPLICATE, 1.0, 2.0, 3.0, 3.5, 4.0, 4.5));
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
        try {
            t1.getBusBreakerView().getBus();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access bus of removed equipment " + TO_REMOVE, e.getMessage());
        }
        try {
            t1.getBusBreakerView().getConnectableBus();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access bus of removed equipment " + TO_REMOVE, e.getMessage());
        }
        try {
            t1.getBusView().getBus();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access bus of removed equipment " + TO_REMOVE, e.getMessage());
        }
        try {
            t1.getVoltageLevel();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access voltage level of removed equipment " + TO_REMOVE, e.getMessage());
        }
        try {
            t2.getBusBreakerView().getBus();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access bus of removed equipment " + TO_REMOVE, e.getMessage());
        }
        try {
            t2.getBusBreakerView().getConnectableBus();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access bus of removed equipment " + TO_REMOVE, e.getMessage());
        }
        try {
            t2.getBusView().getBus();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access bus of removed equipment " + TO_REMOVE, e.getMessage());
        }
        try {
            t2.getVoltageLevel();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access voltage level of removed equipment " + TO_REMOVE, e.getMessage());
        }
        try {
            t1.traverse(Mockito.mock(Terminal.TopologyTraverser.class));
            fail();
        } catch (PowsyblException e) {
            assertEquals("Associated equipment toRemove is removed", e.getMessage());
        }
        try {
            t2.traverse(Mockito.mock(Terminal.TopologyTraverser.class));
            fail();
        } catch (PowsyblException e) {
            assertEquals("Associated equipment toRemove is removed", e.getMessage());
        }
        Terminal.BusBreakerView bbView1 = t1.getBusBreakerView();
        try {
            bbView1.moveConnectable("BUS", true);
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot modify removed equipment " + TO_REMOVE, e.getMessage());
        }
        Terminal.BusBreakerView bbView2 = t2.getBusBreakerView();
        try {
            bbView2.moveConnectable("BUS", true);
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot modify removed equipment " + TO_REMOVE, e.getMessage());
        }
        try {
            line.getNetwork();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access network of removed equipment " + TO_REMOVE, e.getMessage());
        }
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
        DanglingLine dl1 = voltageLevelA.newDanglingLine()
                .setBus("busA")
                .setId("hl1")
                .setEnsureIdUnicity(true)
                .setName(DANGLING1_NAME)
                .setR(r)
                .setX(x)
                .setB(hl1b1 + hl1b2)
                .setG(hl1g1 + hl1g2)
                .setUcteXnodeCode("ucte")
                .add();
        DanglingLine dl2 = voltageLevelB.newDanglingLine()
                .setBus("busB")
                .setId("hl2")
                .setEnsureIdUnicity(true)
                .setR(r2)
                .setX(x2)
                .setB(hl2b1 + hl2b2)
                .setG(hl2g1 + hl2g2)
                .add();
        TieLineAdder adder = network.newTieLine().setId("testTie")
                .setName("testNameTie")
                .setDanglingLine1(dl1.getId())
                .setDanglingLine2(dl2.getId());
        TieLine tieLine = adder.add();
        assertEquals(IdentifiableType.TIE_LINE, tieLine.getType());
        assertEquals("ucte", tieLine.getUcteXnodeCode());
        assertEquals("hl1", tieLine.getDanglingLine1().getId());
        assertEquals(DANGLING1_NAME, tieLine.getDanglingLine1().getOptionalName().orElse(null));
        assertEquals("hl2", tieLine.getDanglingLine2().getId());
        assertEquals(11.0, tieLine.getR(), tol);
        assertEquals(22.0, tieLine.getX(), tol);
        assertEquals(0.065, tieLine.getG1(), tol);
        assertEquals(0.02649999999, tieLine.getG2(), tol);
        assertEquals(0.08499999999, tieLine.getB1(), tol);
        assertEquals(0.0285, tieLine.getB2(), tol);

        DanglingLine danglingLine1 = tieLine.getDanglingLine1();

        DanglingLine danglingLine2 = tieLine.getDanglingLine2();

        // Check notification on DanglingLine changes
        NetworkListener mockedListener = mock(DefaultNetworkListener.class);
        // Add observer changes to current network
        network.addListener(mockedListener);
        // Apply changes on dangling lines
        danglingLine1.setR(r + 1);
        danglingLine1.setX(x + 1);
        danglingLine1.setG(hl1g1 + hl1g2 + 2);
        danglingLine1.setB(hl1b1 + hl1b2 + 2);
        danglingLine2.setR(r + 1);
        danglingLine2.setX(x + 1);
        danglingLine2.setG(hl2g1 + hl1g2 + 2);
        danglingLine2.setB(hl2b1 + hl2b2 + 2);
        verify(mockedListener, times(8)).onUpdate(any(DanglingLine.class), anyString(), any(), any());
        // Remove observer
        network.removeListener(mockedListener);
        // Cancel changes on dangling lines
        danglingLine1.setR(r);
        danglingLine1.setX(x);
        danglingLine1.setG(hl1g1 + hl1g2);
        danglingLine1.setB(hl1b1 + hl1b2);
        danglingLine2.setR(r);
        danglingLine2.setX(x);
        danglingLine2.setG(hl2g1 + hl2g2);
        danglingLine2.setB(hl2b1 + hl2b2);
        // Check no notification
        verifyNoMoreInteractions(mockedListener);

        // Reuse adder
        ValidationException e = assertThrows(ValidationException.class, () -> adder.setId("testTie2").add());
        assertTrue(e.getMessage().contains("already has a tie line"));

        // Update power flows, voltages and angles
        double p1 = -605.0;
        double q1 = -302.5;
        double p2 = 600.0;
        double q2 = 300.0;
        double v1 = 420.0;
        double v2 = 380.0;
        double angle1 = -1e-4;
        double angle2 = -1.7e-3;
        tieLine.getDanglingLine1().getTerminal().setP(p1).setQ(q1).getBusView().getBus().setV(v1).setAngle(angle1);
        tieLine.getDanglingLine2().getTerminal().setP(p2).setQ(q2).getBusView().getBus().setV(v2).setAngle(angle2);

        // test boundaries values
        SV expectedSV1 = new SV(p1, q1, v1, angle1, Branch.Side.ONE);
        SV expectedSV2 = new SV(p2, q2, v2, angle2, Branch.Side.TWO);
        assertEquals(expectedSV1.otherSideP(tieLine.getDanglingLine1(), true), tieLine.getDanglingLine1().getBoundary().getP(), 0.0d);
        assertEquals(expectedSV1.otherSideQ(tieLine.getDanglingLine1(), true), tieLine.getDanglingLine1().getBoundary().getQ(), 0.0d);
        assertEquals(expectedSV2.otherSideP(tieLine.getDanglingLine2(), true), tieLine.getDanglingLine2().getBoundary().getP(), 0.0d);
        assertEquals(expectedSV2.otherSideQ(tieLine.getDanglingLine2(), true), tieLine.getDanglingLine2().getBoundary().getQ(), 0.0d);
        assertEquals(expectedSV1.otherSideU(tieLine.getDanglingLine1(), true), tieLine.getDanglingLine1().getBoundary().getV(), 0.0d);
        assertEquals(expectedSV1.otherSideA(tieLine.getDanglingLine1(), true), tieLine.getDanglingLine1().getBoundary().getAngle(), 0.0d);
        assertEquals(expectedSV2.otherSideU(tieLine.getDanglingLine2(), true), tieLine.getDanglingLine2().getBoundary().getV(), 0.0d);
        assertEquals(expectedSV2.otherSideA(tieLine.getDanglingLine2(), true), tieLine.getDanglingLine2().getBoundary().getAngle(), 0.0d);
    }

    @Test
    public void danglingLine1NotSet() {
        // adder
        ValidationException e = assertThrows(ValidationException.class, () -> network.newTieLine()
                .setId("testTie")
                .setName("testNameTie")
                .add());
        assertTrue(e.getMessage().contains("undefined dangling line"));
    }

    @Test
    public void danglingLine2NotSet() {
        // adder
        DanglingLine dl1 = voltageLevelA.newDanglingLine()
                .setBus("busA")
                .setId("hl1")
                .setName(DANGLING1_NAME)
                .setR(10.0)
                .setX(20.0)
                .setB(80.0)
                .setG(65.0)
                .setUcteXnodeCode("ucte")
                .add();
        // adder
        ValidationException e = assertThrows(ValidationException.class, () -> network.newTieLine()
                .setId("testTie")
                .setName("testNameTie")
                .setDanglingLine1(dl1.getId())
                .add());
        assertTrue(e.getMessage().contains("undefined dangling line"));
    }

    @Test
    public void invalidDanglingLineCharacteristicsR() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTieLineWithDanglingline2ByDefault(INVALID, INVALID, INVALID, Double.NaN, 2.0,
                6.5, 8.5, "code"));
        assertTrue(e.getMessage().contains("r is invalid"));
    }

    @Test
    public void invalidDanglingLineCharacteristicsX() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTieLineWithDanglingline2ByDefault(INVALID, INVALID, INVALID, 1.0, Double.NaN,
                6.5, 8.5, "code"));
        assertTrue(e.getMessage().contains("x is invalid"));
    }

    @Test
    public void invalidDanglingLineCharacteristicsG() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTieLineWithDanglingline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                Double.NaN, 8.5, "code"));
        assertTrue(e.getMessage().contains("g is invalid"));
    }

    @Test
    public void invalidDanglingLineCharacteristicsB() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTieLineWithDanglingline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                6.5, Double.NaN, "code"));
        assertTrue(e.getMessage().contains("b is invalid"));
    }

    @Test
    public void danglingLineIdNull() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> createTieLineWithDanglingline2ByDefault(INVALID, INVALID, null, 1.0, 2.0,
                6.5, 8.5, "code"));
        assertTrue(e.getMessage().contains("Dangling line id is not set"));
    }

    @Test
    public void danglingLineIdEmpty() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> createTieLineWithDanglingline2ByDefault(INVALID, INVALID, "", 1.0, 2.0,
                6.5, 8.5, "code"));
        assertTrue(e.getMessage().contains("Invalid id ''"));
    }

    @Test
    public void duplicate() {
        createTieLineWithDanglingline2ByDefault(DUPLICATE, DUPLICATE, "id1", 1.0, 2.0,
                6.5, 8.5, DUPLICATE);
        assertThrows(PowsyblException.class, () -> createTieLineWithDanglingline2ByDefault(DUPLICATE, DUPLICATE, "id1", 1.0, 2.0,
                6.5, 8.5, DUPLICATE));
    }

    @Test
    public void testRemove() {
        createTieLineWithDanglingline2ByDefault(TO_REMOVE, TO_REMOVE, "id1", 1.0, 2.0,
                6.5, 8.5, TO_REMOVE);
        TieLine line = network.getTieLine(TO_REMOVE);
        assertNotNull(line);
        int count = network.getTieLineCount();
        line.remove();
        assertNull(network.getLine(TO_REMOVE));
        assertNotNull(line);
        assertEquals(count - 1L, network.getTieLineCount());
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

    private void createTieLineWithDanglingline2ByDefault(String id, String name, String danglingLineId, double r, double x,
                                                         double g, double b, String code) {
        DanglingLine dl1 = voltageLevelA.newDanglingLine()
                .setBus("busA")
                .setId(danglingLineId)
                .setName(DANGLING1_NAME)
                .setR(r)
                .setX(x)
                .setB(b)
                .setG(g)
                .add();
        DanglingLine dl2 = voltageLevelB.newDanglingLine()
                .setBus("busB")
                .setId("hl2")
                .setName("half2_name")
                .setR(1.0)
                .setX(2.0)
                .setB(6.5)
                .setG(8.5)
                .setUcteXnodeCode(code)
                .add();
        network.newTieLine()
                .setId(id)
                .setEnsureIdUnicity(true)
                .setName(name)
                .setDanglingLine1(dl1.getId())
                .setDanglingLine2(dl2.getId())
                .add();
    }
}
