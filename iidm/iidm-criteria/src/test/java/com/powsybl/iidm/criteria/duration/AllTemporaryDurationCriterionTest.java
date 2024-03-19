/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.duration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class AllTemporaryDurationCriterionTest {
    @Test
    void getTypeTest() {
        AllTemporaryDurationCriterion criterion = new AllTemporaryDurationCriterion();
        assertEquals(LimitDurationCriterion.LimitDurationType.TEMPORARY, criterion.getType());
    }

    @Test
    void getComparisonTypeTest() {
        AllTemporaryDurationCriterion criterion = new AllTemporaryDurationCriterion();
        assertEquals(AbstractTemporaryDurationCriterion.TemporaryDurationCriterionType.ALL, criterion.getComparisonType());
    }

    @Test
    void isTemporaryLimitWithinCriterionBoundsTest() {
        AllTemporaryDurationCriterion criterion = new AllTemporaryDurationCriterion();
        assertTrue(criterion.filter(10));
        assertTrue(criterion.filter(10_000));
    }
}
