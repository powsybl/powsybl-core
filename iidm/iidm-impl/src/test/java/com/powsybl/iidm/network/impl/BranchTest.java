/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class BranchTest {

    static Network network1;
    static Network network2;
    static Branch<?> branch1;
    static Branch<?> branch2;

    @BeforeAll
    public static void setUp() {
        network1 = EurostagTutorialExample1Factory.createWithFixedLimits();
        branch1 = network1.getBranch("NHV1_NHV2_2");
        network2 = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        branch2 = network2.getBranch("NHV1_NHV2_2");
    }

    @Test
    void testActivePowerLimits1Branches() {
        // power limits

        Assertions.assertNotNull(branch1.getNullableActivePowerLimits1());
        Assertions.assertEquals(1100, branch1.getNullableActivePowerLimits1().getPermanentLimit());
        Assertions.assertEquals(LimitType.ACTIVE_POWER, branch1.getNullableActivePowerLimits1().getLimitType());
        Assertions.assertEquals(2, branch1.getNullableActivePowerLimits1().getTemporaryLimits().size());

        Assertions.assertNotNull(branch1.getActivePowerLimits(TwoSides.ONE));
        Assertions.assertTrue(branch1.getActivePowerLimits(TwoSides.ONE).isPresent());
        Assertions.assertEquals(1100, branch1.getActivePowerLimits(TwoSides.ONE).get().getPermanentLimit());
        Assertions.assertEquals(LimitType.ACTIVE_POWER, branch1.getActivePowerLimits(TwoSides.ONE).get().getLimitType());
        Assertions.assertEquals(2, branch1.getActivePowerLimits(TwoSides.ONE).get().getTemporaryLimits().size());

        Assertions.assertNotNull(branch1.getNullableLimits(LimitType.ACTIVE_POWER, TwoSides.ONE));
        Assertions.assertEquals(1100, branch1.getNullableLimits(LimitType.ACTIVE_POWER, TwoSides.ONE).getPermanentLimit());
        Assertions.assertEquals(LimitType.ACTIVE_POWER, branch1.getNullableLimits(LimitType.ACTIVE_POWER, TwoSides.ONE).getLimitType());
        Assertions.assertEquals(2, branch1.getNullableLimits(LimitType.ACTIVE_POWER, TwoSides.ONE).getTemporaryLimits().size());
    }

    @Test
    void testActivePowerLimits2Branches() {
        // power limits

        Assertions.assertNotNull(branch1.getNullableActivePowerLimits2());
        Assertions.assertEquals(500, branch1.getNullableActivePowerLimits2().getPermanentLimit());
        Assertions.assertEquals(LimitType.ACTIVE_POWER, branch1.getNullableActivePowerLimits2().getLimitType());
        Assertions.assertEquals(0, branch1.getNullableActivePowerLimits2().getTemporaryLimits().size());

        Assertions.assertNotNull(branch1.getActivePowerLimits(TwoSides.TWO));
        Assertions.assertTrue(branch1.getActivePowerLimits(TwoSides.TWO).isPresent());
        Assertions.assertEquals(500, branch1.getActivePowerLimits(TwoSides.TWO).get().getPermanentLimit());
        Assertions.assertEquals(LimitType.ACTIVE_POWER, branch1.getActivePowerLimits(TwoSides.TWO).get().getLimitType());
        Assertions.assertEquals(0, branch1.getActivePowerLimits(TwoSides.TWO).get().getTemporaryLimits().size());

        Assertions.assertNotNull(branch1.getNullableLimits(LimitType.ACTIVE_POWER, TwoSides.TWO));
        Assertions.assertEquals(500, branch1.getNullableLimits(LimitType.ACTIVE_POWER, TwoSides.TWO).getPermanentLimit());
        Assertions.assertEquals(LimitType.ACTIVE_POWER, branch1.getNullableLimits(LimitType.ACTIVE_POWER, TwoSides.TWO).getLimitType());
        Assertions.assertEquals(0, branch1.getNullableLimits(LimitType.ACTIVE_POWER, TwoSides.TWO).getTemporaryLimits().size());
    }

    @Test
    void testApparentPowerLimits1Branches() {
        // power limits

        Assertions.assertNotNull(branch1.getNullableApparentPowerLimits1());
        Assertions.assertEquals(1100, branch1.getNullableApparentPowerLimits1().getPermanentLimit());
        Assertions.assertEquals(LimitType.APPARENT_POWER, branch1.getNullableApparentPowerLimits1().getLimitType());
        Assertions.assertEquals(2, branch1.getNullableApparentPowerLimits1().getTemporaryLimits().size());

        Assertions.assertNotNull(branch1.getApparentPowerLimits(TwoSides.ONE));
        Assertions.assertTrue(branch1.getApparentPowerLimits(TwoSides.ONE).isPresent());
        Assertions.assertEquals(1100, branch1.getApparentPowerLimits(TwoSides.ONE).get().getPermanentLimit());
        Assertions.assertEquals(LimitType.APPARENT_POWER, branch1.getApparentPowerLimits(TwoSides.ONE).get().getLimitType());
        Assertions.assertEquals(2, branch1.getApparentPowerLimits(TwoSides.ONE).get().getTemporaryLimits().size());

        Assertions.assertNotNull(branch1.getNullableLimits(LimitType.APPARENT_POWER, TwoSides.ONE));
        Assertions.assertEquals(1100, branch1.getNullableLimits(LimitType.APPARENT_POWER, TwoSides.ONE).getPermanentLimit());
        Assertions.assertEquals(LimitType.APPARENT_POWER, branch1.getNullableLimits(LimitType.APPARENT_POWER, TwoSides.ONE).getLimitType());
        Assertions.assertEquals(2, branch1.getNullableLimits(LimitType.APPARENT_POWER, TwoSides.ONE).getTemporaryLimits().size());
    }

    @Test
    void testApparentPowerLimits2Branches() {
        // power limits

        Assertions.assertNotNull(branch1.getNullableApparentPowerLimits2());
        Assertions.assertEquals(500, branch1.getNullableApparentPowerLimits2().getPermanentLimit());
        Assertions.assertEquals(LimitType.APPARENT_POWER, branch1.getNullableApparentPowerLimits2().getLimitType());
        Assertions.assertEquals(0, branch1.getNullableApparentPowerLimits2().getTemporaryLimits().size());

        Assertions.assertNotNull(branch1.getApparentPowerLimits(TwoSides.TWO));
        Assertions.assertTrue(branch1.getApparentPowerLimits(TwoSides.TWO).isPresent());
        Assertions.assertEquals(500, branch1.getApparentPowerLimits(TwoSides.TWO).get().getPermanentLimit());
        Assertions.assertEquals(LimitType.APPARENT_POWER, branch1.getApparentPowerLimits(TwoSides.TWO).get().getLimitType());
        Assertions.assertEquals(0, branch1.getApparentPowerLimits(TwoSides.TWO).get().getTemporaryLimits().size());

        Assertions.assertNotNull(branch1.getNullableLimits(LimitType.APPARENT_POWER, TwoSides.TWO));
        Assertions.assertEquals(500, branch1.getNullableLimits(LimitType.APPARENT_POWER, TwoSides.TWO).getPermanentLimit());
        Assertions.assertEquals(LimitType.APPARENT_POWER, branch1.getNullableLimits(LimitType.APPARENT_POWER, TwoSides.TWO).getLimitType());
        Assertions.assertEquals(0, branch1.getNullableLimits(LimitType.APPARENT_POWER, TwoSides.TWO).getTemporaryLimits().size());
    }

    @Test
    void testCurrentLimits1Branches() {
        // current limits
        Assertions.assertNotNull(branch2.getNullableCurrentLimits1());
        Assertions.assertEquals(LimitType.CURRENT, branch2.getNullableCurrentLimits1().getLimitType());
        Assertions.assertEquals(1100, branch2.getNullableCurrentLimits1().getPermanentLimit());
        Assertions.assertEquals(2, branch2.getNullableCurrentLimits1().getTemporaryLimits().size());

        Assertions.assertNotNull(branch2.getCurrentLimits(TwoSides.ONE));
        Assertions.assertTrue(branch2.getCurrentLimits(TwoSides.ONE).isPresent());
        Assertions.assertEquals(LimitType.CURRENT, branch2.getCurrentLimits(TwoSides.ONE).get().getLimitType());
        Assertions.assertEquals(1100, branch2.getCurrentLimits(TwoSides.ONE).get().getPermanentLimit());
        Assertions.assertEquals(2, branch2.getCurrentLimits(TwoSides.ONE).get().getTemporaryLimits().size());

        Assertions.assertNotNull(branch2.getNullableCurrentLimits(TwoSides.ONE));
        Assertions.assertEquals(LimitType.CURRENT, branch2.getNullableCurrentLimits(TwoSides.ONE).getLimitType());
        Assertions.assertEquals(1100, branch2.getNullableCurrentLimits(TwoSides.ONE).getPermanentLimit());
        Assertions.assertEquals(2, branch2.getNullableCurrentLimits(TwoSides.ONE).getTemporaryLimits().size());

        Assertions.assertNotNull(branch2.getNullableLimits(LimitType.CURRENT, TwoSides.ONE));
        Assertions.assertEquals(LimitType.CURRENT, branch2.getNullableLimits(LimitType.CURRENT, TwoSides.ONE).getLimitType());
        Assertions.assertEquals(1100, branch2.getNullableLimits(LimitType.CURRENT, TwoSides.ONE).getPermanentLimit());
        Assertions.assertEquals(2, branch2.getNullableLimits(LimitType.CURRENT, TwoSides.ONE).getTemporaryLimits().size());
    }

    @Test
    void testCurrentLimits2Branches() {
        // current limits
        Assertions.assertNotNull(branch2.getNullableCurrentLimits2());
        Assertions.assertEquals(LimitType.CURRENT, branch2.getNullableCurrentLimits2().getLimitType());
        Assertions.assertEquals(500, branch2.getNullableCurrentLimits2().getPermanentLimit());
        Assertions.assertEquals(0, branch2.getNullableCurrentLimits2().getTemporaryLimits().size());

        Assertions.assertNotNull(branch2.getCurrentLimits(TwoSides.TWO));
        Assertions.assertTrue(branch2.getCurrentLimits(TwoSides.TWO).isPresent());
        Assertions.assertEquals(LimitType.CURRENT, branch2.getCurrentLimits(TwoSides.TWO).get().getLimitType());
        Assertions.assertEquals(500, branch2.getCurrentLimits(TwoSides.TWO).get().getPermanentLimit());
        Assertions.assertEquals(0, branch2.getCurrentLimits(TwoSides.TWO).get().getTemporaryLimits().size());

        Assertions.assertNotNull(branch2.getNullableCurrentLimits(TwoSides.TWO));
        Assertions.assertEquals(LimitType.CURRENT, branch2.getNullableCurrentLimits(TwoSides.TWO).getLimitType());
        Assertions.assertEquals(500, branch2.getNullableCurrentLimits(TwoSides.TWO).getPermanentLimit());
        Assertions.assertEquals(0, branch2.getNullableCurrentLimits(TwoSides.TWO).getTemporaryLimits().size());

        Assertions.assertNotNull(branch2.getNullableLimits(LimitType.CURRENT, TwoSides.TWO));
        Assertions.assertEquals(LimitType.CURRENT, branch2.getNullableLimits(LimitType.CURRENT, TwoSides.TWO).getLimitType());
        Assertions.assertEquals(500, branch2.getNullableLimits(LimitType.CURRENT, TwoSides.TWO).getPermanentLimit());
        Assertions.assertEquals(0, branch2.getNullableLimits(LimitType.CURRENT, TwoSides.TWO).getTemporaryLimits().size());
    }
}
