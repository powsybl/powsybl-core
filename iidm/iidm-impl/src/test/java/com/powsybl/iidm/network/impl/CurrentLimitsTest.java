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

    @Test
    public void test() {
        Network network = createNetwork();
        Line l = network.getLine("L");
        assertFalse(l.isOverloaded());
        l.getTerminal1().getBusBreakerView().getBus().setV(390.0);
        l.getTerminal1().setP(100.0).setQ(50.0); // i = 165.51212
        assertFalse(Double.isNaN(l.getTerminal1().getI()));
        assertFalse(l.isOverloaded());
        assertFalse(l.checkPermanentLimit1());
        assertNull(l.checkTemporaryLimits1());

        l.getTerminal1().setP(800.0).setQ(400.0); // i = 1324.0969
        assertTrue(l.isOverloaded());
        assertEquals(5 * 60, l.getOverloadDuration());
        assertTrue(l.checkPermanentLimit1());
        assertNotNull(l.checkTemporaryLimits1());
        assertEquals(5 * 60, l.checkTemporaryLimits1().getTemporaryLimit().getAcceptableDuration());
        assertEquals(1200.0, l.checkTemporaryLimits1().getPreviousLimit(), 0.0);

        l.getTerminal1().setP(900.0).setQ(500.0); // i = 1524.1499
        assertEquals(60, l.getOverloadDuration());
        assertNotNull(l.checkTemporaryLimits1());
        assertEquals(60, l.checkTemporaryLimits1().getTemporaryLimit().getAcceptableDuration());
        assertEquals(1400.0, l.checkTemporaryLimits1().getPreviousLimit(), 0.0);
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
        CurrentLimits currentLimits = line.newCurrentLimits1()
                                        .setPermanentLimit(100.0)
                                            .beginTemporaryLimit()
                                            .setName("20'")
                                            .setAcceptableDuration(20 * 60)
                                            .setValue(1200.0)
                                        .endTemporaryLimit()
                                        .beginTemporaryLimit()
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
        }
        currentLimits.setPermanentLimit(1000.0);
        assertEquals(1000.0, currentLimits.getPermanentLimit(), 0.0);
        assertEquals(3, currentLimits.getTemporaryLimits().size());
        assertTrue(Double.isNaN(currentLimits.getTemporaryLimitValue(2)));

        CurrentLimits.TemporaryLimit temporaryLimit300 = currentLimits.getTemporaryLimit(300);
        assertEquals("5'", temporaryLimit300.getName());
        assertTrue(temporaryLimit300.isFictitious());
        assertEquals(1400.0, temporaryLimit300.getValue(), 0.0);
        assertEquals(300, temporaryLimit300.getAcceptableDuration());
    }
}
