/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class LimitViolationTest {

    private final Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();

    private static List<Country> getCountries(Network n, List<LimitViolation> violations) {
        return violations.stream()
                .map(v -> LimitViolationHelper.getCountry(v, n))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Test
    void testCountry() {
        List<LimitViolation> violations = Security.checkLimits(network);

        List<Country> expectedCountries = Arrays.asList(Country.FR, Country.BE, Country.FR, Country.BE, Country.FR);
        List<Country> countries = getCountries(network, violations);

        assertEquals(expectedCountries, countries);
    }

    @Test
    void emptyCountry() {
        network.getSubstation("P2").setCountry(null);

        List<LimitViolation> violations = Security.checkLimits(network);

        List<Country> expectedCountries = Arrays.asList(Country.FR, Country.FR, Country.FR);
        List<Country> countries = getCountries(network, violations);

        assertEquals(expectedCountries, countries);
    }

    @Test
    void testNominalVoltages() {
        List<LimitViolation> violations = Security.checkLimits(network);

        List<Double> expectedVoltages = Arrays.asList(380.0, 380.0, 380.0, 380.0, 380.0);
        List<Double> voltages = violations.stream()
                .map(v -> LimitViolationHelper.getNominalVoltage(v, network))
                .toList();

        assertEquals(expectedVoltages, voltages);
    }

    @Test
    void testVoltageLevelIds() {
        List<LimitViolation> violations = Security.checkLimits(network);

        List<String> expectedVoltageLevelIds = Arrays.asList("VLHV1", "VLHV2", "VLHV1", "VLHV2", "VLHV1");
        List<String> voltages = violations.stream()
                .map(v -> LimitViolationHelper.getVoltageLevelId(v, network))
                .toList();

        assertEquals(expectedVoltageLevelIds, voltages);
    }

    @Test
    void testToString() {
        LimitViolation limitViolation1 = new LimitViolation("testId", null, LimitViolationType.HIGH_VOLTAGE, "high", Integer.MAX_VALUE,
                420, 1, 500);
        LimitViolation limitViolation2 = new LimitViolation("testId", null, LimitViolationType.CURRENT, null, 6300,
                1000, 1, 1100, TwoSides.ONE);
        LimitViolation limitViolation3 = new LimitViolation("testId", null, LimitViolationType.APPARENT_POWER, null, 6300,
                1000, 1, 1100, ThreeSides.THREE);
        String expected1 = "Subject id: VoltageLevelViolationLocation{subjectId='testId'}, Subject name: null, limitType: HIGH_VOLTAGE, limit: 420.0, limitName: high, acceptableDuration: 2147483647, limitReduction: 1.0, value: 500.0, side: null";
        String expected2 = "Subject id: VoltageLevelViolationLocation{subjectId='testId'}, Subject name: null, limitType: CURRENT, limit: 1000.0, limitName: null, acceptableDuration: 6300, limitReduction: 1.0, value: 1100.0, side: ONE";
        String expected3 = "Subject id: VoltageLevelViolationLocation{subjectId='testId'}, Subject name: null, limitType: APPARENT_POWER, limit: 1000.0, limitName: null, acceptableDuration: 6300, limitReduction: 1.0, value: 1100.0, side: THREE";
        assertEquals(expected1, limitViolation1.toString());
        assertEquals(expected2, limitViolation2.toString());
        assertEquals(expected3, limitViolation3.toString());

    }
}
