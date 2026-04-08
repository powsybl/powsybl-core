/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.translation;

import com.powsybl.iidm.criteria.NetworkElementCriterion;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.ThreeSides;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class NetworkElementTest {

    private static NetworkElement networkElement;

    @BeforeAll
    public static void init() {
        networkElement = new NetworkElement() {
            @Override
            public String getId() {
                return "id";
            }

            @Override
            public Optional<Country> getCountry1() {
                return Optional.of(Country.DE);
            }

            @Override
            public Optional<Country> getCountry2() {
                return Optional.of(Country.ES);
            }

            @Override
            public Optional<Country> getCountry3() {
                return Optional.of(Country.BE);
            }

            @Override
            public Optional<Country> getCountry() {
                return getCountry1();
            }

            @Override
            public Optional<Double> getNominalVoltage1() {
                return Optional.of(400.);
            }

            @Override
            public Optional<Double> getNominalVoltage2() {
                return Optional.of(225.);
            }

            @Override
            public Optional<Double> getNominalVoltage3() {
                return Optional.of(90.);
            }

            @Override
            public Optional<Double> getNominalVoltage() {
                return getNominalVoltage1();
            }

            @Override
            public boolean isValidFor(NetworkElementCriterion.NetworkElementCriterionType networkElementCriterionType) {
                return false;
            }
        };
    }

    @Test
    void testCountries() {
        assertEquals(Country.DE, networkElement.getCountry(ThreeSides.ONE).orElse(null));
        assertEquals(Country.ES, networkElement.getCountry(ThreeSides.TWO).orElse(null));
        assertEquals(Country.BE, networkElement.getCountry(ThreeSides.THREE).orElse(null));
    }

    @Test
    void testVoltageLevels() {
        assertEquals(400., networkElement.getNominalVoltage(ThreeSides.ONE).orElse(Double.NaN), 0.01);
        assertEquals(225., networkElement.getNominalVoltage(ThreeSides.TWO).orElse(Double.NaN), 0.01);
        assertEquals(90., networkElement.getNominalVoltage(ThreeSides.THREE).orElse(Double.NaN), 0.01);
    }
}
