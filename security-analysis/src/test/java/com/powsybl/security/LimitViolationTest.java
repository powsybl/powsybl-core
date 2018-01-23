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
import static org.junit.Assert.assertTrue;

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
            .map(v -> LimitViolationHelper.getCountry(v, network))
            .collect(Collectors.toList());

        assertEquals(expectedCountries, countries);
    }

    @Test
    public void testNominalVoltages() {
        List<LimitViolation> violations = Security.checkLimits(network);

        List<Float> expectedVoltages = Arrays.asList(380.0f, 380.0f, 380.0f, 380.0f, 380.0f);
        List<Float> voltages = violations.stream()
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
    public void testDeprecated() {
        LimitViolation violation = new LimitViolation("L1", LimitViolationType.CURRENT, 100.0f, "current", 0.95f, 97.0f, Country.FR, 225.0f);
        assertEquals(Country.FR, violation.getCountry());
        assertEquals(225.0f, violation.getBaseVoltage(), 0.0f);

        violation = new LimitViolation("B1", LimitViolationType.HIGH_VOLTAGE, 100.0f, "voltage", 97.0f);
        assertNull(violation.getCountry());
        assertTrue(Float.isNaN(violation.getBaseVoltage()));
    }

}
