/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

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

    private static final double EPSILON = 0.000_001;

    @Test
    void closedBoundsTest() {
        SingleNominalVoltageCriterion.VoltageInterval interval = new SingleNominalVoltageCriterion.VoltageInterval(100., 204.53, true, true);
        assertDuration(false, 1., interval);
        assertDuration(false, 100. - EPSILON, interval);
        assertDuration(true, 100., interval);
        assertDuration(true, 150., interval);
        assertDuration(true, 204.53, interval);
        assertDuration(false, 204.53 + EPSILON, interval);
        assertDuration(false, 500., interval);
        assertDuration(false, Double.MAX_VALUE, interval);
    }

    void assertDuration(boolean expected, double testedValue, SingleNominalVoltageCriterion.VoltageInterval interval) {
        assertEquals(expected, interval.checkIsBetweenBound(testedValue));
    }

    @Test
    void openedBoundsTest() {
        SingleNominalVoltageCriterion.VoltageInterval interval = new SingleNominalVoltageCriterion.VoltageInterval(100., 204.53, false, false);
        assertDuration(false, 1., interval);
        assertDuration(false, 100., interval);
        assertDuration(true, 100. + EPSILON, interval);
        assertDuration(true, 150., interval);
        assertDuration(true, 204.53 - EPSILON, interval);
        assertDuration(false, 204.53, interval);
        assertDuration(false, 500., interval);
        assertDuration(false, Double.MAX_VALUE, interval);
    }

    @Test
    void mixedBoundsTest() {
        SingleNominalVoltageCriterion.VoltageInterval interval = new SingleNominalVoltageCriterion.VoltageInterval(100., 204.53, true, false);
        assertDuration(false, 1., interval);
        assertDuration(false, 100. - EPSILON, interval);
        assertDuration(true, 100., interval);
        assertDuration(true, 150., interval);
        assertDuration(true, 204.53 - EPSILON, interval);
        assertDuration(false, 204.53, interval);
        assertDuration(false, 500., interval);
        assertDuration(false, Double.MAX_VALUE, interval);

        interval = new SingleNominalVoltageCriterion.VoltageInterval(100., 204.53, false, true);
        assertDuration(false, 1., interval);
        assertDuration(false, 100., interval);
        assertDuration(true, 100. + EPSILON, interval);
        assertDuration(true, 150., interval);
        assertDuration(true, 204.53, interval);
        assertDuration(false, 204.53 + EPSILON, interval);
        assertDuration(false, 500., interval);
        assertDuration(false, Double.MAX_VALUE, interval);
    }

    @Test
    void isNullIntervalTest() {
        SingleNominalVoltageCriterion.VoltageInterval interval = SingleNominalVoltageCriterion.VoltageInterval.nullInterval();
        assertTrue(interval.isNull());

        interval = new SingleNominalVoltageCriterion.VoltageInterval(200., 225., true, true);
        assertFalse(interval.isNull());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidBounds")
    void invalidBound(double low, double high) {
        Exception e = assertThrows(IllegalArgumentException.class, () -> new SingleNominalVoltageCriterion.VoltageInterval(low, high, true, true));
        assertEquals("Invalid interval bounds values (must be >= 0).", e.getMessage());
    }

    private static Stream<Arguments> provideInvalidBounds() {
        return Stream.of(
          Arguments.of(Double.NaN, 100.),
          Arguments.of(100., Double.NaN),
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
