/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.iidm.network.Substation;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EntsoeCountryTest {

    @Test
    public void test() {
        Substation substation = Mockito.mock(Substation.class);
        EntsoeCountry country = new EntsoeCountry(substation, EntsoeGeographicalCode.D1);

        assertEquals("entsoeCountry", country.getName());
        assertSame(substation, country.getExtendable());

        assertEquals(EntsoeGeographicalCode.D1, country.getCode());

        try {
            country.setCode(null);
            fail();
        } catch (NullPointerException ignored) {
        }

        country.setCode(EntsoeGeographicalCode.FR);
        assertEquals(EntsoeGeographicalCode.FR, country.getCode());
    }
}
