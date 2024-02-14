/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitsreduction.criterion.duration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class IntervalTemporaryDurationCriterionTest {
    @Test
    void getTypeTest() {
        IntervalTemporaryDurationCriterion criterion = new IntervalTemporaryDurationCriterion();
        assertEquals(LimitDurationCriterion.LimitDurationType.TEMPORARY, criterion.getType());
    }

    @Test
    void getComparisonTypeTest() {
        IntervalTemporaryDurationCriterion criterion = new IntervalTemporaryDurationCriterion();
        assertEquals(AbstractTemporaryDurationCriterion.TemporaryDurationCriterionType.INTERVAL, criterion.getComparisonType());
    }

    @Test
    void noBoundsTest() {
        IntervalTemporaryDurationCriterion criterion = new IntervalTemporaryDurationCriterion();
        assertAll(
                () -> assertNull(criterion.getLowBound()),
                () -> assertFalse(criterion.isLowClosed()),
                () -> assertNull(criterion.getHighBound()),
                () -> assertFalse(criterion.isHighClosed())
        );
        assertTrue(criterion.isAcceptableDurationWithinCriterionBounds(10));
    }

    @Test
    void lowBoundTest() {
        IntervalTemporaryDurationCriterion criterion = new IntervalTemporaryDurationCriterion().setLowBound(20, false);
        assertAll(
                () -> assertEquals(20, criterion.getLowBound()),
                () -> assertFalse(criterion.isLowClosed()),
                () -> assertNull(criterion.getHighBound()),
                () -> assertFalse(criterion.isHighClosed())
        );
        assertFalse(criterion.isAcceptableDurationWithinCriterionBounds(10));
        assertFalse(criterion.isAcceptableDurationWithinCriterionBounds(20));
        assertTrue(criterion.isAcceptableDurationWithinCriterionBounds(21));

        criterion.setLowBound(30, true);
        assertAll(
                () -> assertEquals(30, criterion.getLowBound()),
                () -> assertTrue(criterion.isLowClosed())
        );
        assertFalse(criterion.isAcceptableDurationWithinCriterionBounds(20));
        assertTrue(criterion.isAcceptableDurationWithinCriterionBounds(30));
        assertTrue(criterion.isAcceptableDurationWithinCriterionBounds(31));
    }

    @Test
    void highBoundTest() {
        IntervalTemporaryDurationCriterion criterion = new IntervalTemporaryDurationCriterion().setHighBound(200, false);
        assertAll(
                () -> assertNull(criterion.getLowBound()),
                () -> assertFalse(criterion.isLowClosed()),
                () -> assertEquals(200, criterion.getHighBound()),
                () -> assertFalse(criterion.isHighClosed())
        );
        assertFalse(criterion.isAcceptableDurationWithinCriterionBounds(300));
        assertFalse(criterion.isAcceptableDurationWithinCriterionBounds(200));
        assertTrue(criterion.isAcceptableDurationWithinCriterionBounds(199));

        criterion.setHighBound(300, true);
        assertAll(
                () -> assertEquals(300, criterion.getHighBound()),
                () -> assertTrue(criterion.isHighClosed())
        );
        assertFalse(criterion.isAcceptableDurationWithinCriterionBounds(500));
        assertTrue(criterion.isAcceptableDurationWithinCriterionBounds(300));
        assertTrue(criterion.isAcceptableDurationWithinCriterionBounds(299));
    }

    @Test
    void bothBoundsTest() {
        IntervalTemporaryDurationCriterion criterion = new IntervalTemporaryDurationCriterion()
                .setLowBound(20, true)
                .setHighBound(300, true);
        assertAll(
                () -> assertEquals(20, criterion.getLowBound()),
                () -> assertTrue(criterion.isLowClosed()),
                () -> assertEquals(300, criterion.getHighBound()),
                () -> assertTrue(criterion.isHighClosed())
        );
        assertFalse(criterion.isAcceptableDurationWithinCriterionBounds(10));
        assertTrue(criterion.isAcceptableDurationWithinCriterionBounds(30));
        assertFalse(criterion.isAcceptableDurationWithinCriterionBounds(500));
    }

    @Test
    void resetBoundsTest() {
        IntervalTemporaryDurationCriterion criterion = new IntervalTemporaryDurationCriterion()
                .setLowBound(20, true)
                .setHighBound(300, true);
        assertAll(
                () -> assertEquals(20, criterion.getLowBound()),
                () -> assertTrue(criterion.isLowClosed()),
                () -> assertEquals(300, criterion.getHighBound()),
                () -> assertTrue(criterion.isHighClosed())
        );
        criterion.resetLowBound();
        assertAll(
                () -> assertNull(criterion.getLowBound()),
                () -> assertFalse(criterion.isLowClosed()),
                () -> assertEquals(300, criterion.getHighBound()),
                () -> assertTrue(criterion.isHighClosed())
        );
        criterion.setLowBound(20, true);
        criterion.resetHighBound();
        assertAll(
                () -> assertEquals(20, criterion.getLowBound()),
                () -> assertTrue(criterion.isLowClosed()),
                () -> assertNull(criterion.getHighBound()),
                () -> assertFalse(criterion.isHighClosed())
        );
    }

    @Test
    void invalidValuesTest() {
        IntervalTemporaryDurationCriterion criterion = new IntervalTemporaryDurationCriterion();
        assertThrows(IllegalArgumentException.class, () -> criterion.setLowBound(-2, false));
        assertThrows(IllegalArgumentException.class, () -> criterion.setHighBound(-2, false));
        criterion.setLowBound(10, false);
        assertThrows(IllegalArgumentException.class, () -> criterion.setHighBound(2, false));
        criterion.resetLowBound();
        criterion.setHighBound(2, false);
        assertThrows(IllegalArgumentException.class, () -> criterion.setLowBound(10, false));
    }
}
