/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

import org.apache.commons.lang3.DoubleRange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class VoltageIntervalTest {

    private static final double EPSILON = 0.000_000_001;

    @Test
    void openLowBoundTest() {
        VoltageInterval interval = VoltageInterval.builder()
                .setLowBound(100.3, false)
                .build();
        assertAll(
                () -> assertEquals(100.3, interval.getNominalVoltageLowBound().orElseThrow(), 0.001),
                () -> assertFalse(interval.isLowClosed()),
                () -> assertTrue(interval.getNominalVoltageHighBound().isEmpty()),
                () -> assertTrue(interval.isHighClosed())
        );
        DoubleRange range = interval.asRange();
        assertValue(false, null, interval, range);
        assertValue(false, -1., interval, range);
        assertValue(false, 1., interval, range);
        assertValue(false, 100.3, interval, range);
        assertValue(true, 100.3 + EPSILON, interval, range);
        assertValue(true, 150., interval, range);
        assertValue(true, Double.MAX_VALUE, interval, range);
    }

    @Test
    void closedLowBoundTest() {
        VoltageInterval interval = VoltageInterval.builder()
                .setLowBound(100., true)
                .build();
        assertAll(
                () -> assertEquals(100., interval.getNominalVoltageLowBound().orElseThrow(), 0.01),
                () -> assertTrue(interval.isLowClosed())
        );
        DoubleRange range = interval.asRange();
        assertValue(false, null, interval, range);
        assertValue(false, -1., interval, range);
        assertValue(false, 1., interval, range);
        assertValue(false, 100. - EPSILON, interval, range);
        assertValue(true, 100., interval, range);
        assertValue(true, 150., interval, range);
        assertValue(true, Double.MAX_VALUE, interval, range);
    }

    @Test
    void openHighBoundTest() {
        VoltageInterval interval = VoltageInterval.builder()
                .setHighBound(204.53, false)
                .build();
        assertAll(
                () -> assertTrue(interval.getNominalVoltageLowBound().isEmpty()),
                () -> assertTrue(interval.isLowClosed()),
                () -> assertEquals(204.53, interval.getNominalVoltageHighBound().orElseThrow()),
                () -> assertFalse(interval.isHighClosed())
        );
        DoubleRange range = interval.asRange();
        assertValue(false, null, interval, range);
        assertValue(false, -1., interval, range);
        assertValue(true, 0., interval, range);
        assertValue(true, 150., interval, range);
        assertValue(true, 204.53 - EPSILON, interval, range);
        assertValue(false, 204.53, interval, range);
        assertValue(false, 500., interval, range);
        assertValue(false, Double.MAX_VALUE, interval, range);
    }

    @Test
    void closedHighBoundTest() {
        VoltageInterval interval = VoltageInterval.builder()
                .setHighBound(329.12, true)
                .build();
        assertAll(
                () -> assertTrue(interval.getNominalVoltageLowBound().isEmpty()),
                () -> assertTrue(interval.isLowClosed()),
                () -> assertEquals(329.12, interval.getNominalVoltageHighBound().orElseThrow()),
                () -> assertTrue(interval.isHighClosed())
        );
        DoubleRange range = interval.asRange();
        assertValue(false, null, interval, range);
        assertValue(false, -1., interval, range);
        assertValue(true, 0., interval, range);
        assertValue(true, 150., interval, range);
        assertValue(true, 329.12, interval, range);
        assertValue(false, 329.12 + EPSILON, interval, range);
        assertValue(false, 500., interval, range);
        assertValue(false, Double.MAX_VALUE, interval, range);
    }

    @Test
    void bothBoundsTest() {
        VoltageInterval interval = VoltageInterval.builder()
                .setLowBound(100., true)
                .setHighBound(204.53, true)
                .build();
        assertAll(
                () -> assertEquals(100., interval.getNominalVoltageLowBound().orElseThrow(), 0.001),
                () -> assertTrue(interval.isLowClosed()),
                () -> assertEquals(204.53, interval.getNominalVoltageHighBound().orElseThrow(), 0.001),
                () -> assertTrue(interval.isHighClosed())
        );
        DoubleRange range = interval.asRange();
        assertValue(false, null, interval, range);
        assertValue(false, -1., interval, range);
        assertValue(false, 1., interval, range);
        assertValue(false, 100. - EPSILON, interval, range);
        assertValue(true, 100., interval, range);
        assertValue(true, 150., interval, range);
        assertValue(true, 204.53, interval, range);
        assertValue(false, 204.53 + EPSILON, interval, range);
        assertValue(false, 500., interval, range);
        assertValue(false, Double.MAX_VALUE, interval, range);
    }

    void assertValue(boolean expected, Double testedValue, VoltageInterval interval, DoubleRange range) {
        assertEquals(expected, interval.checkIsBetweenBound(testedValue));
        assertEquals(expected, range.contains(testedValue));
    }

    @Test
    void invalidValuesTest() {
        VoltageInterval.Builder builder = VoltageInterval.builder();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, builder::build);
        assertEquals("Invalid interval: at least one bound must be defined.", e.getMessage());

        // Invalid negative bound
        assertThrows(IllegalArgumentException.class, () -> builder.setLowBound(-2., false));
        assertThrows(IllegalArgumentException.class, () -> builder.setHighBound(-2., false));
        builder.setLowBound(10., false);

        // Invalid ]10 ; 2[ (when setting higher bound)
        assertThrows(IllegalArgumentException.class, () -> builder.setHighBound(2., false));

        // Invalid ]10 ; 2[ (when setting lower bound)
        VoltageInterval.Builder builder2 = VoltageInterval.builder();
        builder2.setHighBound(2., false);
        assertThrows(IllegalArgumentException.class, () -> builder2.setLowBound(10., false));

        VoltageInterval.Builder builder3 = VoltageInterval.builder();
        builder3.setLowBound(2., false);
        // Invalid empty bound ]2 ; 2] (when setting higher bound)
        e = assertThrows(IllegalArgumentException.class, () -> builder3.setHighBound(2., true));
        assertEquals("Invalid interval: it should not be empty", e.getMessage());
        builder3.setHighBound(4., false);
        // Invalid empty bound ]4 ; 4[ (when setting lower bound)
        assertThrows(IllegalArgumentException.class, () -> builder3.setLowBound(4., false));

        // Invalid interval [0 ; 0[
        VoltageInterval.Builder builder4 = VoltageInterval.builder();
        assertThrows(IllegalArgumentException.class, () -> builder4.setHighBound(0., false));
        assertDoesNotThrow(() -> builder4.setHighBound(0, true)); // [0 ; 0] is valid

        // Invalid interval [MAX_VALUE ; MAX_VALUE[
        VoltageInterval.Builder builder5 = VoltageInterval.builder();
        assertThrows(IllegalArgumentException.class, () -> builder5.setLowBound(Double.MAX_VALUE, false));
        assertDoesNotThrow(() -> builder5.setLowBound(Double.MAX_VALUE, true)); // [MAX_VALUE ; MAX_VALUE] is valid
    }

    @Test
    void gettersTest() {
        VoltageInterval interval = VoltageInterval.builder()
                .setLowBound(200.54, false)
                .setHighBound(225.12, true)
                .build();
        assertFalse(interval.isLowClosed());
        assertTrue(interval.isHighClosed());
        assertEquals(200.54, interval.getNominalVoltageLowBound().orElse(0.), 0.001);
        assertEquals(225.12, interval.getNominalVoltageHighBound().orElse(0.), 0.001);

        interval = VoltageInterval.builder()
                .setLowBound(200.87, true)
                .setHighBound(225.63, false)
                .build();
        assertTrue(interval.isLowClosed());
        assertFalse(interval.isHighClosed());
        assertEquals(200.87, interval.getNominalVoltageLowBound().orElse(0.), 0.001);
        assertEquals(225.63, interval.getNominalVoltageHighBound().orElse(0.), 0.001);
    }

    @Test
    void betweenTest() {
        VoltageInterval interval = VoltageInterval.between(200.54, 225.12, false, true);
        assertFalse(interval.isLowClosed());
        assertTrue(interval.isHighClosed());
        assertEquals(200.54, interval.getNominalVoltageLowBound().orElse(0.), 0.001);
        assertEquals(225.12, interval.getNominalVoltageHighBound().orElse(0.), 0.001);

        interval = VoltageInterval.between(200.87, 225.63, true, false);
        assertTrue(interval.isLowClosed());
        assertFalse(interval.isHighClosed());
        assertEquals(200.87, interval.getNominalVoltageLowBound().orElse(0.), 0.001);
        assertEquals(225.63, interval.getNominalVoltageHighBound().orElse(0.), 0.001);
    }

    @Test
    void lowerThanTest() {
        VoltageInterval interval = VoltageInterval.lowerThan(225.12, true);
        assertTrue(interval.getNominalVoltageLowBound().isEmpty());
        assertEquals(225.12, interval.getNominalVoltageHighBound().orElse(0.), 0.001);
        assertTrue(interval.isHighClosed());

        interval = VoltageInterval.lowerThan(225.63, false);
        assertTrue(interval.getNominalVoltageLowBound().isEmpty());
        assertEquals(225.63, interval.getNominalVoltageHighBound().orElse(0.), 0.001);
        assertFalse(interval.isHighClosed());
    }

    @Test
    void greaterThanTest() {
        VoltageInterval interval = VoltageInterval.greaterThan(200.54, false);
        assertEquals(200.54, interval.getNominalVoltageLowBound().orElse(0.), 0.001);
        assertTrue(interval.getNominalVoltageHighBound().isEmpty());
        assertFalse(interval.isLowClosed());

        interval = VoltageInterval.greaterThan(200.87, true);
        assertEquals(200.87, interval.getNominalVoltageLowBound().orElse(0.), 0.001);
        assertTrue(interval.getNominalVoltageHighBound().isEmpty());
        assertTrue(interval.isLowClosed());
    }
}
