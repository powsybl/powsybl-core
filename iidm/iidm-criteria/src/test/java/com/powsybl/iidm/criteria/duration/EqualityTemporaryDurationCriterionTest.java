/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.duration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        EqualityTemporaryDurationCriterion criterion = new EqualityTemporaryDurationCriterion(10);
        assertTrue(criterion.filter(10));
        assertFalse(criterion.filter(10_000));
    }

    @Test
    void invalidValueTest() {
        assertThrows(IllegalArgumentException.class, () -> new EqualityTemporaryDurationCriterion(-10));
    }
}
