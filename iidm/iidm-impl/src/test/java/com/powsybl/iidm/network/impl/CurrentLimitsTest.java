/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CurrentLimitsTest {

    private Network createNetwork() {
        Network network = NetworkFactory.create("test", "test");
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400f)
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
                .setNominalV(400f)
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
                .setR(1)
                .setX(1)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0)
                .add();
        l.newCurrentLimits1()
                .setPermanentLimit(1000)
                .beginTemporaryLimit()
                    .setName("20'")
                    .setAcceptableDuration(20 * 60)
                    .setValue(1200)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("5'")
                    .setAcceptableDuration(5 * 60)
                    .setValue(1400)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("1'")
                    .setAcceptableDuration(60)
                    .setValue(1600)
                .endTemporaryLimit()
            .add();
        return network;
    }

    @Test
    public void test() {
        Network network = createNetwork();
        Line l = network.getLine("L");
        assertFalse(l.isOverloaded());
        l.getTerminal1().getBusBreakerView().getBus().setV(390);
        l.getTerminal1().setP(100).setQ(50); // i = 165.51212
        assertFalse(Float.isNaN(l.getTerminal1().getI()));
        assertFalse(l.isOverloaded());
        assertFalse(l.checkPermanentLimit1());
        assertNull(l.checkTemporaryLimits1());

        l.getTerminal1().setP(800).setQ(400); // i = 1324.0969
        assertTrue(l.isOverloaded());
        assertEquals(5 * 60, l.getOverloadDuration());
        assertTrue(l.checkPermanentLimit1());
        assertNotNull(l.checkTemporaryLimits1());
        assertEquals(5 * 60, l.checkTemporaryLimits1().getTemporaryLimit().getAcceptableDuration());
        assertEquals(1200.0f, l.checkTemporaryLimits1().getPreviousLimit(), 0.0f);

        l.getTerminal1().setP(900).setQ(500); // i = 1524.1499
        assertEquals(60, l.getOverloadDuration());
        assertNotNull(l.checkTemporaryLimits1());
        assertEquals(60, l.checkTemporaryLimits1().getTemporaryLimit().getAcceptableDuration());
        assertEquals(1400.0f, l.checkTemporaryLimits1().getPreviousLimit(), 0.0f);
    }

    @Test
    public void testLimitWithoutTempLimit() {
        Line l = createNetwork().getLine("L");
        l.newCurrentLimits1().setPermanentLimit(1000.0f).add();
        l.getTerminal1().getBusBreakerView().getBus().setV(390);
        l.getTerminal1().setP(800).setQ(400); // i = 1324.0969
        assertTrue(l.isOverloaded());
    }

    @Test
    public void testSetterGetter() {
        Line line = createNetwork().getLine("L");
        CurrentLimits currentLimits = line.newCurrentLimits1()
                                        .setPermanentLimit(100)
                                            .beginTemporaryLimit()
                                            .setName("20'")
                                            .setAcceptableDuration(20 * 60)
                                            .setValue(1200)
                                        .endTemporaryLimit()
                                        .beginTemporaryLimit()
                                            .setName("5'")
                                            .setAcceptableDuration(5 * 60)
                                            .setValue(1400)
                                            .setFictitious(true)
                                        .endTemporaryLimit()
                                        .beginTemporaryLimit()
                                            .setName("1'")
                                            .setAcceptableDuration(60)
                                            .setValue(1600)
                                        .endTemporaryLimit()
                                    .add();
        try {
            currentLimits.setPermanentLimit(-0.5f);
            fail();
        } catch (ValidationException ignored) {
        }
        currentLimits.setPermanentLimit(1000f);
        assertEquals(1000f, currentLimits.getPermanentLimit(), 0.0f);
        assertEquals(3, currentLimits.getTemporaryLimits().size());
        assertTrue(Float.isNaN(currentLimits.getTemporaryLimitValue(2)));

        CurrentLimits.TemporaryLimit temporaryLimit300 = currentLimits.getTemporaryLimit(300);
        assertEquals("5'", temporaryLimit300.getName());
        assertTrue(temporaryLimit300.isFictitious());
        assertEquals(1400f, temporaryLimit300.getValue(), 0.0f);
        assertEquals(300, temporaryLimit300.getAcceptableDuration());
    }
}
