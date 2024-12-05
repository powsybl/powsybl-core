/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractCurrentLimitsTest extends AbstractIdenticalLimitsTest {

    private static Network createNetwork() {
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        Line l = network.newLine()
                .setId("L")
                .setVoltageLevel1("VL1")
                .setConnectableBus1("B1")
                .setBus1("B1")
                .setVoltageLevel2("VL2")
                .setConnectableBus2("B2")
                .setBus2("B2")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0)
                .add();
        l.newCurrentLimits1()
                .setPermanentLimit(1000.0)
                .beginTemporaryLimit()
                    .setName("20'")
                    .setAcceptableDuration(20 * 60)
                    .setValue(1200.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("5'")
                    .setAcceptableDuration(5 * 60)
                    .setValue(1400.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("1'")
                    .setAcceptableDuration(60)
                    .setValue(1600.0)
                .endTemporaryLimit()
            .add();
        return network;
    }

    private static Network createNetworkForThreeWindingsTransformer() {
        Network network = Network.create("test_3wt", "test");
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        VoltageLevel vl2 = s1.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        VoltageLevel vl3 = s1.newVoltageLevel()
                .setId("VL3")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("B3")
                .add();
        ThreeWindingsTransformer transformer = s1.newThreeWindingsTransformer()
                .setId("3WT")
                .setRatedU0(132.0)
                .newLeg1()
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(132.0)
                .setVoltageLevel("VL1")
                .setBus("B1")
                .add()
                .newLeg2() // not used for the test
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(132.0)
                .setVoltageLevel("VL2")
                .setBus("B2")
                .add()
                .newLeg3() // not used for the test
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(132.0)
                .setVoltageLevel("VL3")
                .setBus("B3")
                .add()
                .add();
        transformer.getLeg1().newCurrentLimits()
                .setPermanentLimit(1000.0)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("5'")
                .setAcceptableDuration(5 * 60)
                .setValue(1400.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("1'")
                .setAcceptableDuration(60)
                .setValue(1600.0)
                .endTemporaryLimit()
                .add();
        transformer.getLeg2().newCurrentLimits()
                .setPermanentLimit(1000.0)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("5'")
                .setAcceptableDuration(5 * 60)
                .setValue(1400.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("1'")
                .setAcceptableDuration(60)
                .setValue(1600.0)
                .endTemporaryLimit()
                .add();
        transformer.getLeg3().newCurrentLimits()
                .setPermanentLimit(1000.0)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("5'")
                .setAcceptableDuration(5 * 60)
                .setValue(1400.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("1'")
                .setAcceptableDuration(60)
                .setValue(1600.0)
                .endTemporaryLimit()
                .add();
        return network;
    }

    @Test
    public void test() {
        Network network = createNetwork();
        Line l = network.getLine("L");
        assertFalse(l.isOverloaded());
        l.getTerminal1().getBusBreakerView().getBus().setV(390.0);
        l.getTerminal1().setP(100.0).setQ(50.0); // i = 165.51212
        assertFalse(Double.isNaN(l.getTerminal1().getI()));
        assertFalse(l.isOverloaded());
        assertFalse(l.checkPermanentLimit1(LimitType.CURRENT));
        assertNull(l.checkTemporaryLimits1(LimitType.CURRENT));

        l.getTerminal1().setP(800.0).setQ(400.0); // i = 1324.0969
        assertTrue(l.isOverloaded());
        assertEquals(5 * 60L, l.getOverloadDuration());
        assertTrue(l.checkPermanentLimit1(LimitType.CURRENT));
        assertNotNull(l.checkTemporaryLimits1(LimitType.CURRENT));
        assertEquals(5 * 60L, l.checkTemporaryLimits1(LimitType.CURRENT).getTemporaryLimit().getAcceptableDuration());
        assertEquals(1200.0, l.checkTemporaryLimits1(LimitType.CURRENT).getPreviousLimit(), 0.0);

        l.getTerminal1().setP(900.0).setQ(500.0); // i = 1524.1499
        assertEquals(60, l.getOverloadDuration());
        assertNotNull(l.checkTemporaryLimits1(LimitType.CURRENT));
        assertEquals(60, l.checkTemporaryLimits1(LimitType.CURRENT).getTemporaryLimit().getAcceptableDuration());
        assertEquals(1400.0, l.checkTemporaryLimits1(LimitType.CURRENT).getPreviousLimit(), 0.0);
    }

    @Test
    public void testForThreeWindingsTransformerLeg1() {
        Network network = createNetworkForThreeWindingsTransformer();
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer("3WT");
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();

        assertFalse(transformer.isOverloaded());
        leg1.getTerminal().getBusBreakerView().getBus().setV(390.0);
        leg1.getTerminal().setP(100.0).setQ(50.0); // i = 165.51212
        assertFalse(Double.isNaN(leg1.getTerminal().getI()));
        assertFalse(transformer.isOverloaded());
        assertFalse(transformer.checkPermanentLimit1(LimitType.CURRENT));
        assertNull(transformer.checkTemporaryLimits1(LimitType.CURRENT));

        leg1.getTerminal().setP(800.0).setQ(400.0); // i = 1324.0969
        assertTrue(transformer.isOverloaded());
        assertEquals(5 * 60L, transformer.getOverloadDuration());
        assertTrue(transformer.checkPermanentLimit1(LimitType.CURRENT));
        Overload tmpLimit1 = transformer.checkTemporaryLimits1(LimitType.CURRENT);
        assertNotNull(tmpLimit1);
        assertEquals(5 * 60L, tmpLimit1.getTemporaryLimit().getAcceptableDuration());
        assertEquals(1200.0, tmpLimit1.getPreviousLimit(), 0.0);

        leg1.getTerminal().setP(900.0).setQ(500.0); // i = 1524.1499
        assertEquals(60, transformer.getOverloadDuration());
        Overload tmpLimit1Bis = transformer.checkTemporaryLimits1(LimitType.CURRENT);
        assertNotNull(tmpLimit1Bis);
        assertEquals(60, tmpLimit1Bis.getTemporaryLimit().getAcceptableDuration());
        assertEquals(1400.0, tmpLimit1Bis.getPreviousLimit(), 0.0);
    }

    @Test
    public void testForThreeWindingsTransformerLeg2() {
        Network network = createNetworkForThreeWindingsTransformer();
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer("3WT");
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();

        assertFalse(transformer.isOverloaded());
        leg2.getTerminal().getBusBreakerView().getBus().setV(390.0);
        leg2.getTerminal().setP(100.0).setQ(50.0); // i = 165.51212
        assertFalse(Double.isNaN(leg2.getTerminal().getI()));
        assertFalse(transformer.isOverloaded());
        assertFalse(transformer.checkPermanentLimit2(LimitType.CURRENT));
        assertNull(transformer.checkTemporaryLimits2(LimitType.CURRENT));

        leg2.getTerminal().setP(800.0).setQ(400.0); // i = 1324.0969
        assertTrue(transformer.isOverloaded());
        assertEquals(5 * 60L, transformer.getOverloadDuration());
        assertTrue(transformer.checkPermanentLimit2(LimitType.CURRENT));
        Overload tmpLimit2 = transformer.checkTemporaryLimits2(LimitType.CURRENT);
        assertNotNull(tmpLimit2);
        assertEquals(5 * 60L, tmpLimit2.getTemporaryLimit().getAcceptableDuration());
        assertEquals(1200.0, tmpLimit2.getPreviousLimit(), 0.0);

        leg2.getTerminal().setP(900.0).setQ(500.0); // i = 1524.1499
        assertEquals(60, transformer.getOverloadDuration());
        Overload tmpLimit2Bis = transformer.checkTemporaryLimits2(LimitType.CURRENT);
        assertNotNull(tmpLimit2Bis);
        assertEquals(60, tmpLimit2Bis.getTemporaryLimit().getAcceptableDuration());
        assertEquals(1400.0, tmpLimit2Bis.getPreviousLimit(), 0.0);
    }

    @Test
    public void testForThreeWindingsTransformerLeg3() {
        Network network = createNetworkForThreeWindingsTransformer();
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer("3WT");
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();

        assertFalse(transformer.isOverloaded());
        leg3.getTerminal().getBusBreakerView().getBus().setV(390.0);
        leg3.getTerminal().setP(100.0).setQ(50.0); // i = 165.51212
        assertFalse(Double.isNaN(leg3.getTerminal().getI()));
        assertFalse(transformer.isOverloaded());
        assertFalse(transformer.checkPermanentLimit3(LimitType.CURRENT));
        assertNull(transformer.checkTemporaryLimits3(LimitType.CURRENT));

        leg3.getTerminal().setP(800.0).setQ(400.0); // i = 1324.0969
        assertTrue(transformer.isOverloaded());
        assertEquals(5 * 60L, transformer.getOverloadDuration());
        assertTrue(transformer.checkPermanentLimit3(LimitType.CURRENT));
        Overload tmpLimit3 = transformer.checkTemporaryLimits3(LimitType.CURRENT);
        assertNotNull(tmpLimit3);
        assertEquals(5 * 60L, tmpLimit3.getTemporaryLimit().getAcceptableDuration());
        assertEquals(1200.0, tmpLimit3.getPreviousLimit(), 0.0);

        leg3.getTerminal().setP(900.0).setQ(500.0); // i = 1524.1499
        assertEquals(60, transformer.getOverloadDuration());
        Overload tmpLimit3Bis = transformer.checkTemporaryLimits3(LimitType.CURRENT);
        assertNotNull(tmpLimit3Bis);
        assertEquals(60, tmpLimit3Bis.getTemporaryLimit().getAcceptableDuration());
        assertEquals(1400.0, tmpLimit3Bis.getPreviousLimit(), 0.0);
    }

    @Test
    public void testLimitWithoutTempLimit() {
        Line l = createNetwork().getLine("L");
        l.newCurrentLimits1().setPermanentLimit(1000.0).add();
        l.getTerminal1().getBusBreakerView().getBus().setV(390.0);
        l.getTerminal1().setP(800.0).setQ(400.0); // i = 1324.0969
        assertTrue(l.isOverloaded());
    }

    @Test
    public void testSetterGetter() {
        Line line = createNetwork().getLine("L");
        CurrentLimitsAdder currentLimitsAdder = line.newCurrentLimits1()
                                        .setPermanentLimit(100.0)
                                            .beginTemporaryLimit()
                                            .setName("20'")
                                            .setAcceptableDuration(20 * 60)
                                            .setValue(1200.0)
                                        .endTemporaryLimit();

        try {
            currentLimitsAdder.beginTemporaryLimit()
                    .setAcceptableDuration(5 * 60)
                    .setName("fail")
                    .setFictitious(true)
                    .endTemporaryLimit();
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        try {
            currentLimitsAdder.beginTemporaryLimit()
                    .setAcceptableDuration(5 * 60)
                    .setName("fail")
                    .setValue(-1200.0)
                    .setFictitious(true)
                    .endTemporaryLimit();
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        try {
            currentLimitsAdder.beginTemporaryLimit()
                    .setAcceptableDuration(-1)
                    .setName("fail")
                    .setValue(1200.0)
                    .setFictitious(true)
                    .endTemporaryLimit();
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        try {
            currentLimitsAdder.beginTemporaryLimit()
                    .setName("fail")
                    .setValue(1200.0)
                    .setFictitious(true)
                    .endTemporaryLimit();
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        try {
            currentLimitsAdder.beginTemporaryLimit()
                    .setAcceptableDuration(5 * 60)
                    .setValue(1400.0)
                    .setFictitious(true)
                    .endTemporaryLimit();
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        CurrentLimits currentLimits = currentLimitsAdder.beginTemporaryLimit()
                    .setName("5'")
                    .setAcceptableDuration(5 * 60)
                    .setValue(1400.0)
                    .setFictitious(true)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("1'")
                    .setAcceptableDuration(60)
                    .setValue(1600.0)
                .endTemporaryLimit()
                .add();

        try {
            currentLimits.setPermanentLimit(-0.5);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        assertEquals(LimitType.CURRENT, currentLimits.getLimitType());
        currentLimits.setPermanentLimit(1000.0);
        assertEquals(1000.0, currentLimits.getPermanentLimit(), 0.0);
        assertEquals(3, currentLimits.getTemporaryLimits().size());
        assertTrue(Double.isNaN(currentLimits.getTemporaryLimitValue(2)));

        CurrentLimits.TemporaryLimit temporaryLimit300 = currentLimits.getTemporaryLimit(300);
        assertEquals("5'", temporaryLimit300.getName());
        assertTrue(temporaryLimit300.isFictitious());
        assertEquals(1400.0, temporaryLimit300.getValue(), 0.0);
        assertEquals(300, temporaryLimit300.getAcceptableDuration());

        currentLimits.remove();
        assertTrue(line.getCurrentLimits1().isEmpty());
    }

    @Test
    public void ensureNameUnicity() {
        Line line = createNetwork().getLine("L");
        CurrentLimitsAdder adder = line.newCurrentLimits1()
                .setPermanentLimit(100.0)
                .beginTemporaryLimit()
                    .setName("TL")
                    .setAcceptableDuration(20 * 60)
                    .setValue(1200.0)
                    .ensureNameUnicity()
                .endTemporaryLimit();

        assertEquals(100.0, adder.getPermanentLimit(), 0.0);
        assertEquals(1200.0, adder.getTemporaryLimitValue(20 * 60), 0.0);

        CurrentLimits currentLimits = adder.beginTemporaryLimit()
                    .setName("TL")
                    .setAcceptableDuration(10 * 60)
                    .setValue(1400.0)
                    .ensureNameUnicity()
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("TL")
                    .setAcceptableDuration(5 * 60)
                    .setValue(1500.0)
                    .ensureNameUnicity()
                .endTemporaryLimit()
                .add();

        assertEquals("TL", currentLimits.getTemporaryLimit(20 * 60).getName());
        assertEquals("TL#0", currentLimits.getTemporaryLimit(10 * 60).getName());
        assertEquals("TL#1", currentLimits.getTemporaryLimit(5 * 60).getName());
    }

    @Test
    public void testNameDuplicationIsAllowed() {
        Line line = createNetwork().getLine("L");
        CurrentLimits currentLimits = line.newCurrentLimits1()
                .setPermanentLimit(100.0)
                .beginTemporaryLimit()
                    .setName("TL")
                    .setAcceptableDuration(20 * 60)
                    .setValue(1200.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("TL")
                    .setAcceptableDuration(10 * 60)
                    .setValue(1400.0)
                .endTemporaryLimit()
                .add();

        assertEquals("TL", currentLimits.getTemporaryLimit(20 * 60).getName());
        assertEquals("TL", currentLimits.getTemporaryLimit(10 * 60).getName());
    }

    @Test
    public void testAdderGetOwner() {
        Line line = createNetwork().getLine("L");
        CurrentLimitsAdder adder = line.newCurrentLimits1();
        assertEquals("L", adder.getOwnerId());
    }

    @Test
    public void testAdderGetTemporaryLimitValue() {
        Line line = createNetwork().getLine("L");
        CurrentLimitsAdder adder = line.newCurrentLimits1()
                .setPermanentLimit(1000.)
                .beginTemporaryLimit()
                    .setName("TL1")
                    .setAcceptableDuration(20 * 60)
                    .setValue(1200.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("TL2")
                    .setAcceptableDuration(10 * 60)
                    .setValue(1400.0)
                .endTemporaryLimit();

        assertEquals(1200., adder.getTemporaryLimitValue("TL1"));
        assertEquals(1400., adder.getTemporaryLimitValue("TL2"));
        assertEquals(Double.NaN, adder.getTemporaryLimitValue("Unknown"));
    }

    @Test
    public void testAdderGetTemporaryLimitAcceptableDuration() {
        Line line = createNetwork().getLine("L");
        CurrentLimitsAdder adder = line.newCurrentLimits1()
                .setPermanentLimit(1000.)
                .beginTemporaryLimit()
                    .setName("TL1")
                    .setAcceptableDuration(20 * 60)
                    .setValue(1200.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("TL2")
                    .setAcceptableDuration(10 * 60)
                    .setValue(1400.0)
                .endTemporaryLimit();

        assertEquals(20 * 60, adder.getTemporaryLimitAcceptableDuration("TL1"));
        assertEquals(10 * 60, adder.getTemporaryLimitAcceptableDuration("TL2"));
        assertEquals(Integer.MAX_VALUE, adder.getTemporaryLimitAcceptableDuration("Unknown"));
    }

    @Test
    public void testAdderGetLowestTemporaryLimitValue() {
        Line line = createNetwork().getLine("L");
        CurrentLimitsAdder adder = line.newCurrentLimits1();
        assertEquals(Double.NaN, adder.getLowestTemporaryLimitValue(), 0.0);

        adder.setPermanentLimit(1000.)
            .beginTemporaryLimit()
                .setName("TL1")
                .setAcceptableDuration(20 * 60)
                .setValue(1200.0)
            .endTemporaryLimit()
            .beginTemporaryLimit()
                .setName("TL2")
                .setAcceptableDuration(10 * 60)
                .setValue(1400.0)
            .endTemporaryLimit();

        assertEquals(1200.0, adder.getLowestTemporaryLimitValue(), 0.0);
    }

    @Test
    public void testAdderHasTemporaryLimits() {
        Line line = createNetwork().getLine("L");
        CurrentLimitsAdder adder = line.newCurrentLimits1();
        assertFalse(adder.hasTemporaryLimits());

        adder.setPermanentLimit(1000.)
            .beginTemporaryLimit()
                .setName("TL1")
                .setAcceptableDuration(20 * 60)
                .setValue(1200.0)
            .endTemporaryLimit();
        assertTrue(adder.hasTemporaryLimits());
    }

    @Test
    public void testAdderRemoveTemporaryLimit() {
        Line line = createNetwork().getLine("L");
        CurrentLimitsAdder adder = line.newCurrentLimits1()
                .setPermanentLimit(1000.)
                .beginTemporaryLimit()
                    .setName("TL1")
                    .setAcceptableDuration(20 * 60)
                    .setValue(1200.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("TL2")
                    .setAcceptableDuration(10 * 60)
                    .setValue(1400.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("TL3")
                    .setAcceptableDuration(5 * 60)
                    .setValue(1600.0)
                .endTemporaryLimit();

        Collection<String> names = adder.getTemporaryLimitNames();
        assertEquals(3, names.size());
        assertTrue(names.contains("TL1"));
        assertTrue(names.contains("TL2"));
        assertTrue(names.contains("TL3"));

        adder.removeTemporaryLimit("TL2");

        names = adder.getTemporaryLimitNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("TL1"));
        assertFalse(names.contains("TL2"));
        assertTrue(names.contains("TL3"));

        adder.removeTemporaryLimit("TL1");
        adder.removeTemporaryLimit("TL3");
        assertDoesNotThrow(() -> adder.removeTemporaryLimit("TL3"));

        assertTrue(adder.getTemporaryLimitNames().isEmpty());
    }

    @Test
    public void testAdderFixPermanentLimit() {
        Line line = createNetwork().getLine("L");
        CurrentLimitsAdder adder = line.newCurrentLimits1()
                .setPermanentLimit(Double.NaN)
                .beginTemporaryLimit()
                    .setName("TL1")
                    .setAcceptableDuration(20 * 60)
                    .setValue(1200.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("TL2")
                    .setAcceptableDuration(10 * 60)
                    .setValue(1400.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("TL3")
                    .setAcceptableDuration(5 * 60)
                    .setValue(1600.0)
                .endTemporaryLimit();

        assertEquals(Double.NaN, adder.getPermanentLimit(), 0.0);
        adder.fixLimits(90.);
        assertEquals(1080., adder.getPermanentLimit(), 0.0);
    }

    @Test
    public void testAdderPermanentLimitAlreadySet() {
        Line line = createNetwork().getLine("L");
        CurrentLimitsAdder adder = line.newCurrentLimits1()
                .setPermanentLimit(1000.)
                .beginTemporaryLimit()
                    .setName("TL1")
                    .setAcceptableDuration(20 * 60)
                    .setValue(1200.0)
                .endTemporaryLimit();
        adder.fixLimits(90.);
        assertEquals(1000., adder.getPermanentLimit(), 0.0);
    }

    @Test
    public void testAdderSetPermanentLimitWithInfiniteDurationValue() {
        Line line = createNetwork().getLine("L");
        CurrentLimitsAdder adder = line.newCurrentLimits1()
                .setPermanentLimit(Double.NaN)
                .beginTemporaryLimit()
                    .setName("INFINITE")
                    .setAcceptableDuration(Integer.MAX_VALUE)
                    .setValue(800.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("TL1")
                    .setAcceptableDuration(20 * 60)
                    .setValue(1200.0)
                .endTemporaryLimit();

        assertEquals(Double.NaN, adder.getPermanentLimit(), 0.0);
        adder.fixLimits(90.);
        assertEquals(800., adder.getPermanentLimit(), 0.0);

        Collection<String> names = adder.getTemporaryLimitNames();
        assertEquals(1, names.size());
        assertTrue(names.contains("TL1"));
    }

    @Test
    public void testAdderWithValueZero() {
        Line line = createNetwork().getLine("L");
        CurrentLimitsAdder adder = line.newCurrentLimits1()
                .setPermanentLimit(0)
                .beginTemporaryLimit()
                    .setName("TEST")
                    .setAcceptableDuration(Integer.MAX_VALUE)
                    .setValue(0)
                .endTemporaryLimit();
        adder.add();
        Optional<CurrentLimits> optionalLimits = line.getCurrentLimits(TwoSides.ONE);
        assertTrue(optionalLimits.isPresent());
        CurrentLimits limits = optionalLimits.get();
        assertEquals(0, limits.getPermanentLimit());
        assertEquals(0, limits.getTemporaryLimit(Integer.MAX_VALUE).getValue());
    }

    @Test
    public void testAdderByCopy() {
        // First limit
        Line line = createNetwork().getLine("L");
        CurrentLimitsAdder adder = line.newCurrentLimits1()
                .setPermanentLimit(1000.)
                .beginTemporaryLimit()
                .setName("TL1")
                .setAcceptableDuration(20 * 60)
                .setValue(1200.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("TL2")
                .setAcceptableDuration(10 * 60)
                .setValue(1400.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("TL3")
                .setAcceptableDuration(5 * 60)
                .setValue(1600.0)
                .endTemporaryLimit();
        adder.add();
        CurrentLimits limits1 = line.getCurrentLimits1().get();

        // Second limit
        CurrentLimitsAdder adder2 = line.newCurrentLimits2(limits1);

        adder2.add();

        Optional<CurrentLimits> optionalLimits2 = line.getCurrentLimits2();
        assertTrue(optionalLimits2.isPresent());
        CurrentLimits limits2 = optionalLimits2.get();

        // Tests
        assertTrue(areLimitsIdentical(limits1, limits2));

        adder = line.newCurrentLimits1(limits2);
        adder.add();

        assertTrue(areLimitsIdentical(limits1, limits2));
        assertThrows(PowsyblException.class, () -> line.newCurrentLimits1(null));
    }

    @Test
    public void testSetTemporaryLimitValue() {
        Line line = createNetwork().getLine("L");
        CurrentLimitsAdder adder = line.newCurrentLimits1()
                .setPermanentLimit(1000.)
                .beginTemporaryLimit()
                .setName("TL1")
                .setAcceptableDuration(20 * 60)
                .setValue(1200.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("TL2")
                .setAcceptableDuration(10 * 60)
                .setValue(1400.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("TL3")
                .setAcceptableDuration(5 * 60)
                .setValue(1600.0)
                .endTemporaryLimit();
        adder.add();

        Optional<CurrentLimits> optionalLimits = line.getCurrentLimits(TwoSides.ONE);
        assertTrue(optionalLimits.isPresent());
        CurrentLimits limits = optionalLimits.get();

        limits.setTemporaryLimitValue(20 * 60, 1050.0);
        assertEquals(1050.0, limits.getTemporaryLimit(20 * 60).getValue());

        limits.setTemporaryLimitValue(10 * 60, 1450.0);
        assertEquals(1450.0, limits.getTemporaryLimit(10 * 60).getValue());

        limits.setTemporaryLimitValue(5 * 60, 1750.0);
        assertEquals(1750.0, limits.getTemporaryLimit(5 * 60).getValue());

        // Tests with invalid values
        assertEquals(1750.0, limits.getTemporaryLimit(5 * 60).getValue());
        limits.setTemporaryLimitValue(5 * 60, 1250.0);
        assertThrows(ValidationException.class, () -> limits.setTemporaryLimitValue(7 * 60, 1550.0));
        assertThrows(ValidationException.class, () -> limits.setTemporaryLimitValue(5 * 60, Double.NaN));
        assertThrows(ValidationException.class, () -> limits.setTemporaryLimitValue(5 * 60, -6.0));
    }
}


