/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.google.common.collect.Sets;
import com.powsybl.iidm.network.Country;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EntsoeGeographicalCodeTest {

    @Test
    public void testForCountry() throws Exception {
        assertTrue(EntsoeGeographicalCode.forCountry(Country.FR).equals(Sets.newHashSet(EntsoeGeographicalCode.FR)));
        assertTrue(EntsoeGeographicalCode.forCountry(Country.DE).equals(Sets.newHashSet(
                EntsoeGeographicalCode.DE, EntsoeGeographicalCode.D1, EntsoeGeographicalCode.D2,
                EntsoeGeographicalCode.D4, EntsoeGeographicalCode.D7, EntsoeGeographicalCode.D8)));
        assertEquals(37 + 5, EntsoeGeographicalCode.values().length);
    }
}
