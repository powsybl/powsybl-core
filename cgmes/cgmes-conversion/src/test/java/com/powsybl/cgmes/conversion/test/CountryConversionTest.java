/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.powsybl.cgmes.conversion.CountryConversion;
import com.powsybl.iidm.network.Country;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CountryConversionTest {
    @Test
    public void countryFromRegion() {
        ImmutableMap.<String, Country> builder()
                .put("D1", Country.DE)
                .put("D4", Country.DE)
                .put("D7", Country.DE)
                .put("D8", Country.DE)
                .build()
                .forEach((name, country) -> assertEquals(country,
                        CountryConversion.fromRegionName(name).get()));
        assertEquals(Optional.empty(), CountryConversion.fromRegionName("XYZ"));
    }

    @Test
    public void countryFromSubregion() {
        ImmutableMap.<String, Country> builder()
                .put("NO1", Country.NO)
                .put("NO2", Country.NO)
                .put("NO3", Country.NO)
                .put("NO4", Country.NO)
                .put("NO5", Country.NO)
                .put("SE1", Country.SE)
                .put("SE2", Country.SE)
                .put("SE3", Country.SE)
                .put("SE4", Country.SE)
                .put("DK1", Country.DK)
                .put("DK2", Country.DK)
                .put("FI1", Country.FI)
                .put("EE1", Country.EE)
                .put("LV1", Country.LV)
                .put("LT1", Country.LT)
                .build()
                .forEach((name, country) -> assertEquals(country,
                        CountryConversion.fromSubregionName(name).get()));
        assertEquals(Optional.empty(), CountryConversion.fromSubregionName("XYZ"));
    }
}
