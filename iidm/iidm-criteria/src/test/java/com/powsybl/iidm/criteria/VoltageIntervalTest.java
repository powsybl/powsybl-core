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
    void closedBoundsTest() {
        SingleNominalVoltageCriterion.VoltageInterval interval = new SingleNominalVoltageCriterion.VoltageInterval(100., 204.53, true, true);
        DoubleRange range = interval.asRange();
        assertDuration(false, null, interval, range);
        assertDuration(false, 1., interval, range);
        assertDuration(false, 100. - EPSILON, interval, range);
        assertDuration(true, 100., interval, range);
        assertDuration(true, 150., interval, range);
        assertDuration(true, 204.53, interval, range);
        assertDuration(false, 204.53 + EPSILON, interval, range);
        assertDuration(false, 500., interval, range);
        assertDuration(false, Double.MAX_VALUE, interval, range);
    }

    void assertDuration(boolean expected, Double testedValue, SingleNominalVoltageCriterion.VoltageInterval interval, DoubleRange range) {
        assertEquals(expected, interval.checkIsBetweenBound(testedValue));
        assertEquals(expected, range.contains(testedValue));
    }

    @Test
    void openedBoundsTest() {
        SingleNominalVoltageCriterion.VoltageInterval interval = new SingleNominalVoltageCriterion.VoltageInterval(100., 204.53, false, false);
        DoubleRange range = interval.asRange();
        assertDuration(false, null, interval, range);
        assertDuration(false, 1., interval, range);
        assertDuration(false, 100., interval, range);
        assertDuration(true, 100. + EPSILON, interval, range);
        assertDuration(true, 150., interval, range);
        assertDuration(true, 204.53 - EPSILON, interval, range);
        assertDuration(false, 204.53, interval, range);
        assertDuration(false, 500., interval, range);
        assertDuration(false, Double.MAX_VALUE, interval, range);
    }

    @Test
    void mixedBoundsTest() {
        SingleNominalVoltageCriterion.VoltageInterval interval = new SingleNominalVoltageCriterion.VoltageInterval(100., 204.53, true, false);
        DoubleRange range = interval.asRange();
        assertDuration(false, null, interval, range);
        assertDuration(false, 1., interval, range);
        assertDuration(false, 100. - EPSILON, interval, range);
        assertDuration(true, 100., interval, range);
        assertDuration(true, 150., interval, range);
        assertDuration(true, 204.53 - EPSILON, interval, range);
        assertDuration(false, 204.53, interval, range);
        assertDuration(false, 500., interval, range);
        assertDuration(false, Double.MAX_VALUE, interval, range);

        interval = new SingleNominalVoltageCriterion.VoltageInterval(100., 204.53, false, true);
        range = interval.asRange();
        assertDuration(false, null, interval, range);
        assertDuration(false, 1., interval, range);
        assertDuration(false, 100., interval, range);
        assertDuration(true, 100. + EPSILON, interval, range);
        assertDuration(true, 150., interval, range);
        assertDuration(true, 204.53, interval, range);
        assertDuration(false, 204.53 + EPSILON, interval, range);
        assertDuration(false, 500., interval, range);
        assertDuration(false, Double.MAX_VALUE, interval, range);
    }

    @Test
    void isNullIntervalTest() {
        SingleNominalVoltageCriterion.VoltageInterval interval = SingleNominalVoltageCriterion.VoltageInterval.nullInterval();
        assertTrue(interval.isNull());

        interval = new SingleNominalVoltageCriterion.VoltageInterval(200., 225., true, true);
        assertFalse(interval.isNull());
    }

    @Test
    void gettersTest() {
        SingleNominalVoltageCriterion.VoltageInterval interval = new SingleNominalVoltageCriterion.VoltageInterval(200.54, 225.12, false, true);
        assertFalse(interval.getLowClosed());
        assertTrue(interval.getHighClosed());
        assertEquals(200.54, interval.getNominalVoltageLowBound(), 0.001);
        assertEquals(225.12, interval.getNominalVoltageHighBound(), 0.001);

        interval = new SingleNominalVoltageCriterion.VoltageInterval(200.87, 225.63, true, false);
        assertTrue(interval.getLowClosed());
        assertFalse(interval.getHighClosed());
        assertEquals(200.87, interval.getNominalVoltageLowBound(), 0.001);
        assertEquals(225.63, interval.getNominalVoltageHighBound(), 0.001);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidBounds")
    void invalidBound(double low, double high) {
        Exception e = assertThrows(IllegalArgumentException.class, () -> new SingleNominalVoltageCriterion.VoltageInterval(low, high, true, true));
        assertEquals("Invalid interval bounds values (must be >= 0 and not infinite).", e.getMessage());
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
