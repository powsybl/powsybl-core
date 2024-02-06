/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors.criterion.duration;

import com.powsybl.iidm.network.LoadingLimits;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class EqualityTemporaryDurationCriterionTest {
    @Test
    void getTypeTest() {
        EqualityTemporaryDurationCriterion criterion = new EqualityTemporaryDurationCriterion(10);
        assertEquals(LimitDurationCriterion.LimitDurationType.TEMPORARY, criterion.getType());
    }

    @Test
    void getComparisonTypeTest() {
        EqualityTemporaryDurationCriterion criterion = new EqualityTemporaryDurationCriterion(10);
        assertEquals(AbstractTemporaryDurationCriterion.TemporaryDurationCriterionType.EQUALITY, criterion.getComparisonType());
    }

    @Test
    void isTemporaryLimitWithinCriterionBoundsTest() {
        LoadingLimits.TemporaryLimit tempLimit = Mockito.mock(LoadingLimits.TemporaryLimit.class);
        when(tempLimit.getAcceptableDuration()).thenReturn(10);
        EqualityTemporaryDurationCriterion criterion = new EqualityTemporaryDurationCriterion(10);
        assertTrue(criterion.isTemporaryLimitWithinCriterionBounds(tempLimit));
        when(tempLimit.getAcceptableDuration()).thenReturn(10_000);
        assertFalse(criterion.isTemporaryLimitWithinCriterionBounds(tempLimit));
    }
}
