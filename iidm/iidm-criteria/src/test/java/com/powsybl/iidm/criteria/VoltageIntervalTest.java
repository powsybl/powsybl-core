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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class VoltageIntervalTest {

    private static final double EPSILON = 0.000_000_001;

    @Test
    void openLowBoundTest() {
        SingleNominalVoltageCriterion.VoltageInterval interval = SingleNominalVoltageCriterion.VoltageInterval.builder()
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
        SingleNominalVoltageCriterion.VoltageInterval interval = SingleNominalVoltageCriterion.VoltageInterval.builder()
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
        SingleNominalVoltageCriterion.VoltageInterval interval = SingleNominalVoltageCriterion.VoltageInterval.builder()
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
        SingleNominalVoltageCriterion.VoltageInterval interval = SingleNominalVoltageCriterion.VoltageInterval.builder()
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
        SingleNominalVoltageCriterion.VoltageInterval interval = SingleNominalVoltageCriterion.VoltageInterval.builder()
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

    void assertValue(boolean expected, Double testedValue, SingleNominalVoltageCriterion.VoltageInterval interval, DoubleRange range) {
        assertEquals(expected, interval.checkIsBetweenBound(testedValue));
        assertEquals(expected, range.contains(testedValue));
    }

    @Test
    void invalidValuesTest() {
        SingleNominalVoltageCriterion.VoltageInterval.Builder builder = SingleNominalVoltageCriterion.VoltageInterval.builder();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, builder::build);
        assertEquals("Invalid interval: at least one bound must be defined.", e.getMessage());

        // Invalid negative bound
        assertThrows(IllegalArgumentException.class, () -> builder.setLowBound(-2., false));
        assertThrows(IllegalArgumentException.class, () -> builder.setHighBound(-2., false));
        builder.setLowBound(10., false);

        // Invalid ]10 ; 2[ (when setting higher bound)
        assertThrows(IllegalArgumentException.class, () -> builder.setHighBound(2., false));

        // Invalid ]10 ; 2[ (when setting lower bound)
        SingleNominalVoltageCriterion.VoltageInterval.Builder builder2 = SingleNominalVoltageCriterion.VoltageInterval.builder();
        builder2.setHighBound(2., false);
        assertThrows(IllegalArgumentException.class, () -> builder2.setLowBound(10., false));

        SingleNominalVoltageCriterion.VoltageInterval.Builder builder3 = SingleNominalVoltageCriterion.VoltageInterval.builder();
        builder3.setLowBound(2., false);
        // Invalid empty bound ]2 ; 2] (when setting higher bound)
        e = assertThrows(IllegalArgumentException.class, () -> builder3.setHighBound(2., true));
        assertEquals("Invalid interval: it should not be empty", e.getMessage());
        builder3.setHighBound(4., false);
        // Invalid empty bound ]4 ; 4[ (when setting lower bound)
        assertThrows(IllegalArgumentException.class, () -> builder3.setLowBound(4., false));

        // Invalid interval [0 ; 0[
        SingleNominalVoltageCriterion.VoltageInterval.Builder builder4 = SingleNominalVoltageCriterion.VoltageInterval.builder();
        assertThrows(IllegalArgumentException.class, () -> builder4.setHighBound(0., false));
        assertDoesNotThrow(() -> builder4.setHighBound(0, true)); // [0 ; 0] is valid

        // Invalid interval [MAX_VALUE ; MAX_VALUE[
        SingleNominalVoltageCriterion.VoltageInterval.Builder builder5 = SingleNominalVoltageCriterion.VoltageInterval.builder();
        assertThrows(IllegalArgumentException.class, () -> builder5.setLowBound(Double.MAX_VALUE, false));
        assertDoesNotThrow(() -> builder5.setLowBound(Double.MAX_VALUE, true)); // [MAX_VALUE ; MAX_VALUE] is valid
    }

    @Test
    void gettersTest() {
        SingleNominalVoltageCriterion.VoltageInterval interval = new SingleNominalVoltageCriterion.VoltageInterval(200.54, 225.12, false, true);
        assertFalse(interval.isLowClosed());
        assertTrue(interval.isHighClosed());
        assertEquals(200.54, interval.getNominalVoltageLowBound().orElse(0.), 0.001);
        assertEquals(225.12, interval.getNominalVoltageHighBound().orElse(0.), 0.001);

        interval = new SingleNominalVoltageCriterion.VoltageInterval(200.87, 225.63, true, false);
        assertTrue(interval.isLowClosed());
        assertFalse(interval.isHighClosed());
        assertEquals(200.87, interval.getNominalVoltageLowBound().orElse(0.), 0.001);
        assertEquals(225.63, interval.getNominalVoltageHighBound().orElse(0.), 0.001);
    }

    @Test
    void closedBoundsTest() {
        SingleNominalVoltageCriterion.VoltageInterval interval = new SingleNominalVoltageCriterion.VoltageInterval(100., 204.53, true, true);
        DoubleRange range = interval.asRange();
        assertValue(false, null, interval, range);
        assertValue(false, 1., interval, range);
        assertValue(false, 100. - EPSILON, interval, range);
        assertValue(true, 100., interval, range);
        assertValue(true, 150., interval, range);
        assertValue(true, 204.53, interval, range);
        assertValue(false, 204.53 + EPSILON, interval, range);
        assertValue(false, 500., interval, range);
        assertValue(false, Double.MAX_VALUE, interval, range);
    }

    @Test
    void openedBoundsTest() {
        SingleNominalVoltageCriterion.VoltageInterval interval = new SingleNominalVoltageCriterion.VoltageInterval(100., 204.53, false, false);
        DoubleRange range = interval.asRange();
        assertValue(false, null, interval, range);
        assertValue(false, 1., interval, range);
        assertValue(false, 100., interval, range);
        assertValue(true, 100. + EPSILON, interval, range);
        assertValue(true, 150., interval, range);
        assertValue(true, 204.53 - EPSILON, interval, range);
        assertValue(false, 204.53, interval, range);
        assertValue(false, 500., interval, range);
        assertValue(false, Double.MAX_VALUE, interval, range);
    }

    @Test
    void mixedBoundsTest() {
        SingleNominalVoltageCriterion.VoltageInterval interval = new SingleNominalVoltageCriterion.VoltageInterval(100., 204.53, true, false);
        DoubleRange range = interval.asRange();
        assertValue(false, null, interval, range);
        assertValue(false, 1., interval, range);
        assertValue(false, 100. - EPSILON, interval, range);
        assertValue(true, 100., interval, range);
        assertValue(true, 150., interval, range);
        assertValue(true, 204.53 - EPSILON, interval, range);
        assertValue(false, 204.53, interval, range);
        assertValue(false, 500., interval, range);
        assertValue(false, Double.MAX_VALUE, interval, range);

        interval = new SingleNominalVoltageCriterion.VoltageInterval(100., 204.53, false, true);
        range = interval.asRange();
        assertValue(false, null, interval, range);
        assertValue(false, 1., interval, range);
        assertValue(false, 100., interval, range);
        assertValue(true, 100. + EPSILON, interval, range);
        assertValue(true, 150., interval, range);
        assertValue(true, 204.53, interval, range);
        assertValue(false, 204.53 + EPSILON, interval, range);
        assertValue(false, 500., interval, range);
        assertValue(false, Double.MAX_VALUE, interval, range);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidBounds")
    void invalidBound(double low, double high) {
        Exception e = assertThrows(IllegalArgumentException.class, () -> new SingleNominalVoltageCriterion.VoltageInterval(low, high, true, true));
        assertEquals("Invalid interval bound value (must be >= 0 and not infinite).", e.getMessage());
    }

    private static Stream<Arguments> provideInvalidBounds() {
        return Stream.of(
                Arguments.of(Double.NaN, 100.),
                Arguments.of(100., Double.NaN),
                Arguments.of(Double.NEGATIVE_INFINITY, 100.),
                Arguments.of(100., Double.POSITIVE_INFINITY),
                Arguments.of(-5., 100.),
                Arguments.of(100., -1.)
        );
    }

    @Test
    void boundsInWrongOrder() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> new SingleNominalVoltageCriterion.VoltageInterval(100., 50., true, true));
        assertEquals("Invalid interval bounds values (nominalVoltageLowBound must be <= nominalVoltageHighBound).", e.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideEmptyIntervalBounds")
    void emptyInterval(double low, double high, boolean lowClosed, boolean highClosed) {
        Exception e = assertThrows(IllegalArgumentException.class, () -> new SingleNominalVoltageCriterion.VoltageInterval(low, high, lowClosed, highClosed));
        assertEquals("Invalid interval: it should not be empty", e.getMessage());
    }

    private static Stream<Arguments> provideEmptyIntervalBounds() {
        return Stream.of(
                Arguments.of(10., 10., false, false),
                Arguments.of(20., 20., true, false),
                Arguments.of(30., 30., false, true)
        );
    }
}
