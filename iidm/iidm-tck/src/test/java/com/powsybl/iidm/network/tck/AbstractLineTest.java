/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public abstract class AbstractLineTest {

    private static final String INVALID = "invalid";

    private static final String LINE_NAME = "lineName";

    private static final String TO_REMOVE = "toRemove";

    private static final String DUPLICATE = "duplicate";

    private Network network;
    private VoltageLevel voltageLevelA;
    private VoltageLevel voltageLevelB;

    public boolean areLinesIdentical(Line line1, Line line2) {
        boolean areIdentical = false;

        if (line1 != null && line2 != null) {
            areIdentical = line1.getR() == line2.getR()
                    && line1.getX() == line2.getX()
                    && line1.getG1() == line2.getG1()
                    && line1.getG2() == line2.getG2()
                    && line1.getB1() == line2.getB1()
                    && line1.getB2() == line2.getB2()
                    && Objects.equals(line1.getTerminal1().getVoltageLevel().getId(), line2.getTerminal1().getVoltageLevel().getId())
                    && Objects.equals(line1.getTerminal2().getVoltageLevel().getId(), line2.getTerminal2().getVoltageLevel().getId());
        }
        return areIdentical;
    }

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
        assertSame(busA, acLine.getTerminal(TwoSides.ONE).getBusBreakerView().getConnectableBus());
        assertSame(busB, acLine.getTerminal(TwoSides.TWO).getBusBreakerView().getConnectableBus());

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
        currentLimits1.setPermanentLimit(110);
        assertEquals(110, currentLimits1.getPermanentLimit(), 0.0);
        assertEquals(currentLimits1.getPermanentLimit(), acLine.getCurrentLimits1().orElseThrow().getPermanentLimit(), 0.0);
        currentLimits2.setPermanentLimit(120);
        assertEquals(120, currentLimits2.getPermanentLimit(), 0.0);
        assertEquals(currentLimits2.getPermanentLimit(), acLine.getCurrentLimits2().orElseThrow().getPermanentLimit(), 0.0);

        // add power on line
        Terminal terminal1 = acLine.getTerminal1();
        terminal1.setP(1.0);
        terminal1.setQ(Math.sqrt(2.0));
        busA.setV(1.0);
        // i1 = 1000
        assertTrue(acLine.checkPermanentLimit(TwoSides.ONE, 0.9f, LimitType.CURRENT));
        assertTrue(acLine.checkPermanentLimit(TwoSides.ONE, LimitType.CURRENT));
        assertTrue(acLine.checkPermanentLimit1(LimitType.CURRENT));
        assertNotNull(acLine.checkTemporaryLimits(TwoSides.ONE, 0.9f, LimitType.CURRENT));
        assertNotNull(acLine.checkTemporaryLimits(TwoSides.ONE, LimitType.CURRENT));

        Terminal terminal2 = acLine.getTerminal2();
        terminal2.setP(1.0);
        terminal2.setQ(Math.sqrt(2.0));
        busB.setV(1.0e3);
        // i2 = 1
        assertFalse(acLine.checkPermanentLimit(TwoSides.TWO, 0.9f, LimitType.CURRENT));
        assertFalse(acLine.checkPermanentLimit(TwoSides.TWO, LimitType.CURRENT));
        assertFalse(acLine.checkPermanentLimit2(LimitType.CURRENT));
        assertNull(acLine.checkTemporaryLimits(TwoSides.TWO, 0.9f, LimitType.CURRENT));
        assertNull(acLine.checkTemporaryLimits(TwoSides.TWO, LimitType.CURRENT));
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
    public void testLineCopier() {
        // First limit normally created
        LineAdder acLineAdder1 = network.newLine()
                .setId("line1")
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
                .setConnectableBus2("busB");
        acLineAdder1.add();
        Line acLine1 = network.getLine("line1");
        // Group and limits creation 1
        acLine1.newOperationalLimitsGroup1("group1").newCurrentLimits().setPermanentLimit(220.0).add();
        acLine1.setSelectedOperationalLimitsGroup1("group1");
        Optional<CurrentLimits> optionalLimits1 = acLine1.getCurrentLimits1();
        assertTrue(optionalLimits1.isPresent());
        CurrentLimits limits1 = optionalLimits1.get();
        assertNotNull(limits1);

        acLine1.getOperationalLimitsGroup1("group1").get().newActivePowerLimits().setPermanentLimit(220.0).add();
        acLine1.setSelectedOperationalLimitsGroup1("group1");
        Optional<ActivePowerLimits> optionalActivePowerLimits1 = acLine1.getActivePowerLimits1();
        assertTrue(optionalActivePowerLimits1.isPresent());
        ActivePowerLimits activePowerLimits1 = optionalActivePowerLimits1.get();
        assertNotNull(activePowerLimits1);

        acLine1.getOperationalLimitsGroup1("group1").get().newApparentPowerLimits().setPermanentLimit(220.0).add();
        acLine1.setSelectedOperationalLimitsGroup1("group1");
        Optional<ApparentPowerLimits> optionalApparentPowerLimits1 = acLine1.getApparentPowerLimits1();
        assertTrue(optionalApparentPowerLimits1.isPresent());
        ApparentPowerLimits apparentPowerLimits1 = optionalApparentPowerLimits1.get();
        assertNotNull(apparentPowerLimits1);

        // Group and limit creation 2
        acLine1.newOperationalLimitsGroup2("group2").newCurrentLimits().setPermanentLimit(80.0).add();
        acLine1.setSelectedOperationalLimitsGroup2("group2");
        Optional<CurrentLimits> optionalLimits2 = acLine1.getCurrentLimits2();
        assertTrue(optionalLimits2.isPresent());
        CurrentLimits limits2 = optionalLimits2.get();
        assertNotNull(limits2);

        // Second limit created by copy
        LineAdder acLineAdder2 = network.newLine(acLine1);
        acLineAdder2
                .setId("line2")
                .setName(LINE_NAME)
                .setBus1("busA")
                .setBus2("busB")
                .setConnectableBus1("busA")
                .setConnectableBus2("busB");
        acLineAdder2.add();
        Line acLine2 = network.getLine("line2");
        // Limits check to set up test
        Optional<CurrentLimits> optionalLimits3 = acLine2.getCurrentLimits1();
        assertTrue(optionalLimits3.isPresent());
        CurrentLimits limits3 = optionalLimits3.get();

        // Tests
        assertNotNull(acLine2);
        assertTrue(areLinesIdentical(acLine1, acLine2));
        assertEquals(limits1.getPermanentLimit(), limits3.getPermanentLimit());
        assertNotNull(acLine2.getOperationalLimitsGroup2("group2"));
        assertEquals(acLine1.getSelectedOperationalLimitsGroupId2(), acLine2.getSelectedOperationalLimitsGroupId2());

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
        doThrow(new UnsupportedOperationException()).when(exceptionListener).onUpdate(any(), anyString(), anyString(), any(), any());

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
        verify(mockedListener, times(1)).onUpdate(acLine, "r", null, 1.0, 1.5);

        // Simulate exception for onUpdate calls
        doThrow(new PowsyblException())
                .when(mockedListener)
                .onUpdate(any(Line.class), anyString(), anyString(), anyDouble(), anyDouble());
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("getLimitType")
    void createSelectedOperationalLimitsGroupWhenMissing(LimitType limitType) {
        Line line = network.newLine()
                .setId("line")
                .setName(LINE_NAME)
                .setR(1.0)
                .setX(2.0)
                .setBus1("busA")
                .setBus2("busB")
                .add();
        assertTrue(line.getSelectedOperationalLimitsGroup1().isEmpty());
        assertTrue(line.getSelectedOperationalLimitsGroupId1().isEmpty());
        assertTrue(line.getOperationalLimitsGroups1().isEmpty());
        LoadingLimitsAdder<?, ?> adder = getAdder(line, limitType);
        adder.setPermanentLimit(1000.);
        adder.add();
        // The default group is automatically created
        assertTrue(line.getSelectedOperationalLimitsGroup1().isPresent());
        Collection<OperationalLimitsGroup> operationalLimitsGroups1 = line.getOperationalLimitsGroups1();
        assertEquals(1, operationalLimitsGroups1.size());
        OperationalLimitsGroup group = operationalLimitsGroups1.iterator().next();
        assertEquals("DEFAULT", group.getId());
        assertTrue(getLimits(group, limitType).isPresent());
        assertEquals(1000., getLimits(group, limitType).get().getPermanentLimit());
    }

    static Stream<Arguments> getLimitType() {
        return Stream.of(
                Arguments.of(LimitType.CURRENT),
                Arguments.of(LimitType.APPARENT_POWER),
                Arguments.of(LimitType.ACTIVE_POWER)
        );
    }

    private LoadingLimitsAdder<?, ?> getAdder(Line l, LimitType limitType) {
        return switch (limitType) {
            case CURRENT -> l.newCurrentLimits1();
            case ACTIVE_POWER -> l.newActivePowerLimits1();
            case APPARENT_POWER -> l.newApparentPowerLimits1();
            default -> throw new IllegalArgumentException("Invalid type");
        };
    }

    private Optional<LoadingLimits> getLimits(OperationalLimitsGroup group, LimitType limitType) {
        return switch (limitType) {
            case CURRENT -> group.getCurrentLimits().map(LoadingLimits.class::cast);
            case ACTIVE_POWER -> group.getActivePowerLimits().map(LoadingLimits.class::cast);
            case APPARENT_POWER -> group.getApparentPowerLimits().map(LoadingLimits.class::cast);
            default -> throw new IllegalArgumentException("Invalid type");
        };
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getLimitType")
    void dontChangeSelectedOperationalLimitsGroupIfAdderNotUsed(LimitType limitType) {
        Line line = network.newLine()
                .setId("line")
                .setName(LINE_NAME)
                .setR(1.0)
                .setX(2.0)
                .setBus1("busA")
                .setBus2("busB")
                .add();
        assertTrue(line.getSelectedOperationalLimitsGroup1().isEmpty());
        assertTrue(line.getSelectedOperationalLimitsGroupId1().isEmpty());
        assertTrue(line.getOperationalLimitsGroups1().isEmpty());
        LoadingLimitsAdder<?, ?> adder = getAdder(line, limitType);
        adder.setPermanentLimit(1000.);
        // The adder is voluntarily NOT added. The default group should not be updated.
        assertTrue(line.getSelectedOperationalLimitsGroup1().isEmpty());
        assertTrue(line.getOperationalLimitsGroups1().isEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getLimitType")
    void dontChangeDefaultOperationalLimitsGroupIfAdderValidationFails(LimitType limitType) {
        Line line = network.newLine()
                .setId("line")
                .setName(LINE_NAME)
                .setR(1.0)
                .setX(2.0)
                .setBus1("busA")
                .setBus2("busB")
                .add();
        assertTrue(line.getSelectedOperationalLimitsGroup1().isEmpty());
        assertTrue(line.getSelectedOperationalLimitsGroupId1().isEmpty());
        assertTrue(line.getOperationalLimitsGroups1().isEmpty());
        LoadingLimitsAdder<?, ?> adder = getAdder(line, limitType);
        adder.setPermanentLimit(Double.NaN);
        adder.beginTemporaryLimit().setName("10'").setValue(500.).setAcceptableDuration(600).endTemporaryLimit();
        assertThrows(ValidationException.class, adder::add);
        // The limits' validation of the adder fails. The default group should not be updated.
        assertTrue(line.getSelectedOperationalLimitsGroup1().isEmpty());
        assertTrue(line.getOperationalLimitsGroups1().isEmpty());
    }
}
