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
class IntervalTemporaryDurationCriterionTest {
    @Test
    void getTypeTest() {
        IntervalTemporaryDurationCriterion criterion = IntervalTemporaryDurationCriterion.builder()
                .setLowBound(10, true)
                .build();
        assertEquals(LimitDurationCriterion.LimitDurationType.TEMPORARY, criterion.getType());
    }

    @Test
    void getComparisonTypeTest() {
        IntervalTemporaryDurationCriterion criterion = IntervalTemporaryDurationCriterion.builder()
                .setHighBound(100, true)
                .build();
        assertEquals(AbstractTemporaryDurationCriterion.TemporaryDurationCriterionType.INTERVAL, criterion.getComparisonType());
    }

    @Test
    void openLowBoundTest() {
        IntervalTemporaryDurationCriterion criterion = IntervalTemporaryDurationCriterion.builder()
                .setLowBound(20, false)
                .build();
        assertAll(
                () -> assertEquals(20, criterion.getLowBound().orElseThrow()),
                () -> assertFalse(criterion.isLowClosed()),
                () -> assertTrue(criterion.getHighBound().isEmpty()),
                () -> assertFalse(criterion.isHighClosed())
        );
        assertFalse(criterion.filter(10));
        assertFalse(criterion.filter(20));
        assertTrue(criterion.filter(21));
    }

    @Test
    void closedLowBoundTest() {
        IntervalTemporaryDurationCriterion criterion = IntervalTemporaryDurationCriterion.builder()
                .setLowBound(30, true)
                .build();
        assertAll(
                () -> assertEquals(30, criterion.getLowBound().orElseThrow()),
                () -> assertTrue(criterion.isLowClosed())
        );
        assertFalse(criterion.filter(20));
        assertTrue(criterion.filter(30));
        assertTrue(criterion.filter(31));
    }

    @Test
    void openHighBoundTest() {
        IntervalTemporaryDurationCriterion criterion = IntervalTemporaryDurationCriterion.builder()
                .setHighBound(200, false)
                .build();
        assertAll(
                () -> assertTrue(criterion.getLowBound().isEmpty()),
                () -> assertFalse(criterion.isLowClosed()),
                () -> assertEquals(200, criterion.getHighBound().orElseThrow()),
                () -> assertFalse(criterion.isHighClosed())
        );
        assertFalse(criterion.filter(300));
        assertFalse(criterion.filter(200));
        assertTrue(criterion.filter(199));
    }

    @Test
    void closedHighBoundTest() {
        IntervalTemporaryDurationCriterion criterion = IntervalTemporaryDurationCriterion.builder()
                .setHighBound(300, true)
                .build();
        assertAll(
                () -> assertEquals(300, criterion.getHighBound().orElseThrow()),
                () -> assertTrue(criterion.isHighClosed())
        );
        assertFalse(criterion.filter(500));
        assertTrue(criterion.filter(300));
        assertTrue(criterion.filter(299));
    }

    @Test
    void bothBoundsTest() {
        IntervalTemporaryDurationCriterion criterion = IntervalTemporaryDurationCriterion.builder()
                .setLowBound(20, true)
                .setHighBound(300, true)
                .build();
        assertAll(
                () -> assertEquals(20, criterion.getLowBound().orElseThrow()),
                () -> assertTrue(criterion.isLowClosed()),
                () -> assertEquals(300, criterion.getHighBound().orElseThrow()),
                () -> assertTrue(criterion.isHighClosed())
        );
        assertFalse(criterion.filter(10));
        assertTrue(criterion.filter(30));
        assertFalse(criterion.filter(500));
    }

    @Test
    void invalidValuesTest() {
        IntervalTemporaryDurationCriterion.Builder builder = IntervalTemporaryDurationCriterion.builder();
        assertThrows(IllegalArgumentException.class, () -> builder.setLowBound(-2, false));
        assertThrows(IllegalArgumentException.class, () -> builder.setHighBound(-2, false));
        builder.setLowBound(10, false);
        assertThrows(IllegalArgumentException.class, () -> builder.setHighBound(2, false));

        IntervalTemporaryDurationCriterion.Builder builder2 = IntervalTemporaryDurationCriterion.builder();
        builder2.setHighBound(2, false);
        assertThrows(IllegalArgumentException.class, () -> builder2.setLowBound(10, false));
    }
}
