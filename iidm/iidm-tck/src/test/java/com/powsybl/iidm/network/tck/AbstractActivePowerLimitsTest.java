/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract class AbstractActivePowerLimitsTest extends AbstractIdenticalLimitsTest {

    private static Network createNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        Line line = network.getLine("NHV1_NHV2_1");
        line.getOrCreateSelectedOperationalLimitsGroup1().newActivePowerLimits()
                .setPermanentLimit(350)
                .beginTemporaryLimit()
                    .setValue(370)
                    .setAcceptableDuration(20 * 60)
                    .setName("20'")
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setValue(380)
                    .setAcceptableDuration(10 * 60)
                    .setName("10'")
                .endTemporaryLimit()
                .add();
        line.getOrCreateSelectedOperationalLimitsGroup2().newActivePowerLimits()
                .setPermanentLimit(400)
                .add();
        return network;
    }

    private static void testLimits1(ActivePowerLimits limits1) {
        assertNotNull(limits1);
        assertEquals(LimitType.ACTIVE_POWER, limits1.getLimitType());
        assertEquals(350, limits1.getPermanentLimit(), 0.0);
        assertEquals(2, limits1.getTemporaryLimits().size());
        assertEquals("20'", limits1.getTemporaryLimit(20 * 60).getName());
        assertEquals(370, limits1.getTemporaryLimit(20 * 60).getValue(), 0.0);
        assertEquals("10'", limits1.getTemporaryLimit(10 * 60).getName());
        assertEquals(380, limits1.getTemporaryLimit(10 * 60).getValue(), 0.0);
    }

    private static void testLimits2(ActivePowerLimits limits2) {
        assertNotNull(limits2);
        assertEquals(LimitType.ACTIVE_POWER, limits2.getLimitType());
        assertEquals(400, limits2.getPermanentLimit(), 0.0);
        assertTrue(limits2.getTemporaryLimits().isEmpty());
    }

    @Test
    public void test() {
        Network network = createNetwork();
        Line l = network.getLine("NHV1_NHV2_1");

        // limits1
        testLimits1(l.getActivePowerLimits1().orElse(null));
        testLimits1((ActivePowerLimits) l.getLimits(LimitType.ACTIVE_POWER, TwoSides.ONE).orElse(null));

        // limits2
        ActivePowerLimits limits2 = l.getActivePowerLimits2().orElseThrow(IllegalStateException::new);
        testLimits2(limits2);
        testLimits2((ActivePowerLimits) l.getLimits(LimitType.ACTIVE_POWER, TwoSides.TWO).orElse(null));

        limits2.remove();
        assertTrue(l.getActivePowerLimits2().isEmpty());
    }

    @Test
    public void testAdderByCopy() {
        // First limit
        Network network = createNetwork();
        Line line = network.getLine("NHV1_NHV2_2");

        ActivePowerLimitsAdder adder = line.getOrCreateSelectedOperationalLimitsGroup1().newActivePowerLimits()
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
        ActivePowerLimits limits1 = line.getActivePowerLimits1().get();

        // Second limit
        ActivePowerLimitsAdder adder2 = line.getOrCreateSelectedOperationalLimitsGroup2().newActivePowerLimits(limits1);

        adder2.add();

        Optional<ActivePowerLimits> optionalLimits2 = line.getActivePowerLimits2();
        assertTrue(optionalLimits2.isPresent());
        ActivePowerLimits limits2 = optionalLimits2.get();

        // Tests
        assertTrue(areLimitsIdentical(limits1, limits2));

        adder = line.getOrCreateSelectedOperationalLimitsGroup1().newActivePowerLimits(limits2);
        adder.add();

        assertTrue(areLimitsIdentical(limits1, limits2));
    }
}
