/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.entsoe.util;

import com.google.common.collect.Sets;
import com.powsybl.iidm.network.Country;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class EntsoeGeographicalCodeTest {

    @Test
    void testForCountry() {
        assertEquals(Collections.singleton(EntsoeGeographicalCode.FR), EntsoeGeographicalCode.forCountry(Country.FR));

        Set<EntsoeGeographicalCode> expected = Sets.newHashSet(
                EntsoeGeographicalCode.DE, EntsoeGeographicalCode.D1, EntsoeGeographicalCode.D2,
                EntsoeGeographicalCode.D4, EntsoeGeographicalCode.D7, EntsoeGeographicalCode.D8);
        assertEquals(expected, EntsoeGeographicalCode.forCountry(Country.DE));

        long countryCount = 38;
        long diCount = 5;
        assertEquals(countryCount + diCount, EntsoeGeographicalCode.values().length);
    }
}
