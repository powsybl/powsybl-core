/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class LimitViolationTest {

    private final Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();

    private static List<Country> getCountries(Network n, List<LimitViolation> violations) {
        return violations.stream()
                .map(v -> LimitViolationHelper.getCountry(v, n))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Test
    public void testCountry() {
        List<LimitViolation> violations = Security.checkLimits(network);

        List<Country> expectedCountries = Arrays.asList(Country.FR, Country.BE, Country.FR, Country.BE, Country.FR);
        List<Country> countries = getCountries(network, violations);

        assertEquals(expectedCountries, countries);
    }

    @Test
    public void emptyCountry() {
        network.getSubstation("P2").setCountry(null);

        List<LimitViolation> violations = Security.checkLimits(network);

        List<Country> expectedCountries = Arrays.asList(Country.FR, Country.FR, Country.FR);
        List<Country> countries = getCountries(network, violations);

        assertEquals(expectedCountries, countries);
    }

    @Test
    public void testNominalVoltages() {
        List<LimitViolation> violations = Security.checkLimits(network);

        List<Double> expectedVoltages = Arrays.asList(380.0, 380.0, 380.0, 380.0, 380.0);
        List<Double> voltages = violations.stream()
                .map(v -> LimitViolationHelper.getNominalVoltage(v, network))
                .collect(Collectors.toList());

        assertEquals(expectedVoltages, voltages);
    }

    @Test
    public void testVoltageLevelIds() {
        List<LimitViolation> violations = Security.checkLimits(network);

        List<String> expectedVoltageLevelIds = Arrays.asList("VLHV1", "VLHV2", "VLHV1", "VLHV2", "VLHV1");
        List<String> voltages = violations.stream()
                .map(v -> LimitViolationHelper.getVoltageLevelId(v, network))
                .collect(Collectors.toList());

        assertEquals(expectedVoltageLevelIds, voltages);
    }

    @Test
    public void testToString() {
        LimitViolation limitViolation1 = new LimitViolation("testId", null, LimitViolationType.HIGH_VOLTAGE, "high", Integer.MAX_VALUE,
                420, 1, 500, null);
        LimitViolation limitViolation2 = new LimitViolation("testId", null, LimitViolationType.CURRENT, null, 6300,
                1000, 1, 1100, Branch.Side.ONE);
        String expected1 = "Subject id: testId, Subject name: null, limitType: HIGH_VOLTAGE, limit: 420.0, limitName: high, acceptableDuration: 2147483647, limitReduction: 1.0, value: 500.0, side: null";
        String expected2 = "Subject id: testId, Subject name: null, limitType: CURRENT, limit: 1000.0, limitName: null, acceptableDuration: 6300, limitReduction: 1.0, value: 1100.0, side: ONE";

        assertEquals(expected1, limitViolation1.toString());
        assertEquals(expected2, limitViolation2.toString());
    }
}
