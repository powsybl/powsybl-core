/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractActivePowerLimitsTest {

    private static Network createNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        Line line = network.getLine("NHV1_NHV2_1");
        line.newActivePowerLimits1()
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
        line.newActivePowerLimits2()
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
        testLimits1(l.getActiveActivePowerLimits1().orElse(null));
        testLimits1((ActivePowerLimits) l.getActiveLimits(LimitType.ACTIVE_POWER, Branch.Side.ONE).orElse(null));

        // limits2
        ActivePowerLimits limits2 = l.getActiveActivePowerLimits2().orElseThrow(AssertionError::new);
        testLimits2(limits2);
        testLimits2((ActivePowerLimits) l.getActiveLimits(LimitType.ACTIVE_POWER, Branch.Side.TWO).orElse(null));

        limits2.remove();
        assertTrue(l.getActiveActivePowerLimits2().isEmpty());
    }
}
