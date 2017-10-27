/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class LimitViolationTest {

    private final Network network = TestingNetworkFactory.createFromEurostag();

    @Test
    public void testCountry() {
        List<LimitViolation> violations = Security.checkLimits(network);

        List<Country> expectedCountries = Arrays.asList(Country.FR, Country.BE, Country.FR, Country.BE, Country.FR);
        List<Country> countries = violations.stream()
            .map(v -> LimitViolation.getCountry(v, network))
            .collect(Collectors.toList());

        assertEquals(expectedCountries, countries);
    }

    @Test
    public void testNominalVoltages() {
        List<LimitViolation> violations = Security.checkLimits(network);

        List<Float> expectedVoltages = Arrays.asList(380.0f, 380.0f, 380.0f, 380.0f, 380.0f);
        List<Float> voltages = violations.stream()
            .map(v -> LimitViolation.getNominalVoltage(v, network))
            .collect(Collectors.toList());

        assertEquals(expectedVoltages, voltages);
    }

    @Test
    public void testDeprecatedConstructors() {
        LimitViolation limitViolation = new LimitViolation("subjectId", LimitViolationType.HIGH_VOLTAGE, 100.0f, "limitName", 110.0f);
        assertEquals("subjectId", limitViolation.getSubjectId());
        assertEquals(LimitViolationType.HIGH_VOLTAGE, limitViolation.getLimitType());
        assertEquals("limitName", limitViolation.getLimitName());
        assertEquals(100.0f, limitViolation.getLimit(), 0.0f);
        assertEquals(1.0f, limitViolation.getLimitReduction(), 0.0f);
        assertEquals(110.0f, limitViolation.getValue(), 0.0f);
        assertNull(limitViolation.getCountry());
        assertEquals(Float.NaN, limitViolation.getBaseVoltage(), 0.0f);
    }
}
