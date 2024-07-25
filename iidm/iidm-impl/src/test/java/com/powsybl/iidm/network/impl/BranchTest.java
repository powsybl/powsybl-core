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
import org.junit.jupiter.api.Test;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class BranchTest {

    @Test
    void testPowerLimitsBranches() {
        // power limits
        Network network1 = EurostagTutorialExample1Factory.createWithFixedLimits();
        Branch<?> branch1 = network1.getBranch("NHV1_NHV2_2");
        Assertions.assertNotNull(branch1.getNullableActivePowerLimits1());
        Assertions.assertEquals(1100, branch1.getNullableActivePowerLimits1().getPermanentLimit());
        Assertions.assertEquals(LimitType.ACTIVE_POWER, branch1.getNullableActivePowerLimits1().getLimitType());
        Assertions.assertEquals(2, branch1.getNullableActivePowerLimits1().getTemporaryLimits().size());

        Assertions.assertNotNull(branch1.getNullableActivePowerLimits2());
        Assertions.assertEquals(500, branch1.getNullableActivePowerLimits2().getPermanentLimit());
        Assertions.assertEquals(LimitType.ACTIVE_POWER, branch1.getNullableActivePowerLimits2().getLimitType());
        Assertions.assertEquals(0, branch1.getNullableActivePowerLimits2().getTemporaryLimits().size());

        Assertions.assertNotNull(branch1.getNullableApparentPowerLimits1());
        Assertions.assertEquals(1100, branch1.getNullableApparentPowerLimits1().getPermanentLimit());
        Assertions.assertEquals(LimitType.APPARENT_POWER, branch1.getNullableApparentPowerLimits1().getLimitType());
        Assertions.assertEquals(2, branch1.getNullableApparentPowerLimits1().getTemporaryLimits().size());

        Assertions.assertNotNull(branch1.getNullableApparentPowerLimits2());
        Assertions.assertEquals(500, branch1.getNullableApparentPowerLimits2().getPermanentLimit());
        Assertions.assertEquals(LimitType.APPARENT_POWER, branch1.getNullableApparentPowerLimits2().getLimitType());
        Assertions.assertEquals(0, branch1.getNullableApparentPowerLimits2().getTemporaryLimits().size());

        Assertions.assertNull(branch1.getNullableCurrentLimits1());
        Assertions.assertNull(branch1.getNullableCurrentLimits2());

        Assertions.assertNotNull(branch1.getActivePowerLimits(TwoSides.ONE));
        Assertions.assertNotNull(branch1.getActivePowerLimits(TwoSides.TWO));
        Assertions.assertNotNull(branch1.getNullableActivePowerLimits(TwoSides.ONE));
        Assertions.assertNotNull(branch1.getNullableActivePowerLimits(TwoSides.TWO));
        Assertions.assertNotNull(branch1.getApparentPowerLimits(TwoSides.ONE));
        Assertions.assertNotNull(branch1.getApparentPowerLimits(TwoSides.TWO));
        Assertions.assertNotNull(branch1.getNullableApparentPowerLimits(TwoSides.ONE));
        Assertions.assertNotNull(branch1.getNullableApparentPowerLimits(TwoSides.TWO));

        Assertions.assertNotNull(branch1.getNullableLimits(LimitType.ACTIVE_POWER, TwoSides.ONE));
        Assertions.assertNotNull(branch1.getNullableLimits(LimitType.ACTIVE_POWER, TwoSides.TWO));
        Assertions.assertNotNull(branch1.getNullableLimits(LimitType.APPARENT_POWER, TwoSides.ONE));
        Assertions.assertNotNull(branch1.getNullableLimits(LimitType.APPARENT_POWER, TwoSides.TWO));
    }

    @Test
    void testCurrentLimitsBranches() {
        // current limits
        Network network2 = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        Branch<?> branch2 = network2.getBranch("NHV1_NHV2_2");
        Assertions.assertNull(branch2.getNullableActivePowerLimits1());
        Assertions.assertNull(branch2.getNullableActivePowerLimits2());
        Assertions.assertNull(branch2.getNullableApparentPowerLimits1());
        Assertions.assertNull(branch2.getNullableApparentPowerLimits2());

        Assertions.assertNotNull(branch2.getNullableCurrentLimits1());
        Assertions.assertEquals(LimitType.CURRENT, branch2.getNullableCurrentLimits1().getLimitType());
        Assertions.assertEquals(1100, branch2.getNullableCurrentLimits1().getPermanentLimit());
        Assertions.assertEquals(2, branch2.getNullableCurrentLimits1().getTemporaryLimits().size());

        Assertions.assertNotNull(branch2.getNullableCurrentLimits2());
        Assertions.assertEquals(LimitType.CURRENT, branch2.getNullableCurrentLimits2().getLimitType());
        Assertions.assertEquals(500, branch2.getNullableCurrentLimits2().getPermanentLimit());
        Assertions.assertEquals(0, branch2.getNullableCurrentLimits2().getTemporaryLimits().size());

        Assertions.assertNotNull(branch2.getCurrentLimits(TwoSides.ONE));
        Assertions.assertNotNull(branch2.getCurrentLimits(TwoSides.TWO));
        Assertions.assertNotNull(branch2.getNullableCurrentLimits(TwoSides.ONE));
        Assertions.assertNotNull(branch2.getNullableCurrentLimits(TwoSides.TWO));

        Assertions.assertNotNull(branch2.getNullableLimits(LimitType.CURRENT, TwoSides.ONE));
        Assertions.assertNotNull(branch2.getNullableLimits(LimitType.CURRENT, TwoSides.TWO));
    }
}
