/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.duration;

import org.apache.commons.lang3.IntegerRange;
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
                () -> assertTrue(criterion.isHighClosed())
        );
        IntegerRange range = criterion.asRange();
        assertDuration(false, -1, criterion, range);
        assertDuration(false, 0, criterion, range);
        assertDuration(false, 10, criterion, range);
        assertDuration(false, 20, criterion, range);
        assertDuration(true, 21, criterion, range);
        assertDuration(true, Integer.MAX_VALUE, criterion, range);
    }

    void assertDuration(boolean expected, int duration, IntervalTemporaryDurationCriterion criterion, IntegerRange range) {
        assertEquals(expected, criterion.filter(duration));
        assertEquals(expected, range.contains(duration));
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
        IntegerRange range = criterion.asRange();
        assertDuration(false, -1, criterion, range);
        assertDuration(false, 0, criterion, range);
        assertDuration(false, 20, criterion, range);
        assertDuration(true, 30, criterion, range);
        assertDuration(true, 31, criterion, range);
        assertDuration(true, Integer.MAX_VALUE, criterion, range);
    }

    @Test
    void openHighBoundTest() {
        IntervalTemporaryDurationCriterion criterion = IntervalTemporaryDurationCriterion.builder()
                .setHighBound(200, false)
                .build();
        assertAll(
                () -> assertTrue(criterion.getLowBound().isEmpty()),
                () -> assertTrue(criterion.isLowClosed()),
                () -> assertEquals(200, criterion.getHighBound().orElseThrow()),
                () -> assertFalse(criterion.isHighClosed())
        );
        IntegerRange range = criterion.asRange();
        assertDuration(false, Integer.MAX_VALUE, criterion, range);
        assertDuration(false, 300, criterion, range);
        assertDuration(false, 200, criterion, range);
        assertDuration(true, 199, criterion, range);
        assertDuration(true, 0, criterion, range);
        assertDuration(false, -1, criterion, range);
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
        IntegerRange range = criterion.asRange();
        assertDuration(false, Integer.MAX_VALUE, criterion, range);
        assertDuration(false, 500, criterion, range);
        assertDuration(true, 300, criterion, range);
        assertDuration(true, 299, criterion, range);
        assertDuration(true, 0, criterion, range);
        assertDuration(false, -1, criterion, range);
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
        IntegerRange range = criterion.asRange();
        assertDuration(false, -1, criterion, range);
        assertDuration(false, 10, criterion, range);
        assertDuration(true, 30, criterion, range);
        assertDuration(false, 500, criterion, range);
        assertDuration(false, Integer.MAX_VALUE, criterion, range);
    }

    @Test
    void invalidValuesTest() {
        IntervalTemporaryDurationCriterion.Builder builder = IntervalTemporaryDurationCriterion.builder();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, builder::build);
        assertEquals("Invalid interval criterion: at least one bound must be defined.", e.getMessage());

        // Invalid negative bound
        assertThrows(IllegalArgumentException.class, () -> builder.setLowBound(-2, false));
        assertThrows(IllegalArgumentException.class, () -> builder.setHighBound(-2, false));
        builder.setLowBound(10, false);

        // Invalid ]10 ; 2[ (when setting higher bound)
        assertThrows(IllegalArgumentException.class, () -> builder.setHighBound(2, false));

        // Invalid ]10 ; 2[ (when setting lower bound)
        IntervalTemporaryDurationCriterion.Builder builder2 = IntervalTemporaryDurationCriterion.builder();
        builder2.setHighBound(2, false);
        assertThrows(IllegalArgumentException.class, () -> builder2.setLowBound(10, false));

        IntervalTemporaryDurationCriterion.Builder builder3 = IntervalTemporaryDurationCriterion.builder();
        builder3.setLowBound(2, false);
        // Invalid empty bound ]2 ; 2] (when setting higher bound)
        e = assertThrows(IllegalArgumentException.class, () -> builder3.setHighBound(2, true));
        assertEquals("Invalid interval: it should not be empty", e.getMessage());
        builder3.setHighBound(4, false);
        // Invalid empty bound ]4 ; 4[ (when setting lower bound)
        assertThrows(IllegalArgumentException.class, () -> builder3.setLowBound(4, false));

        // Invalid interval [0 ; 0[
        IntervalTemporaryDurationCriterion.Builder builder4 = IntervalTemporaryDurationCriterion.builder();
        assertThrows(IllegalArgumentException.class, () -> builder4.setHighBound(0, false));
        assertDoesNotThrow(() -> builder4.setHighBound(0, true)); // [0 ; 0] is valid

        // Invalid interval [MAX_VALUE ; MAX_VALUE[
        IntervalTemporaryDurationCriterion.Builder builder5 = IntervalTemporaryDurationCriterion.builder();
        assertThrows(IllegalArgumentException.class, () -> builder5.setLowBound(Integer.MAX_VALUE, false));
        assertDoesNotThrow(() -> builder5.setLowBound(Integer.MAX_VALUE, true)); // [MAX_VALUE ; MAX_VALUE] is valid
    }
}
