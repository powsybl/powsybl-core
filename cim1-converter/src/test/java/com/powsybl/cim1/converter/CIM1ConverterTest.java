/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cim1.converter;

import com.google.common.collect.ImmutableMap;
import com.powsybl.iidm.network.Country;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class CIM1ConverterTest {
    @Test
    public void testCountryFromSubregionName() {
        ImmutableMap.<String, Country>builder()
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
                .forEach((name, country) ->
                        assertEquals(country, CIM1Converter.getCountryFromSubregionName(name)));
        assertEquals(null, CIM1Converter.getCountryFromSubregionName("XYZ"));
    }
}
