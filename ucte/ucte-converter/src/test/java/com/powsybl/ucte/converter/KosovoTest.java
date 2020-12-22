/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.powsybl.entsoe.util.EntsoeGeographicalCode;
import com.powsybl.iidm.network.Country;
import com.powsybl.ucte.network.UcteCountryCode;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mathieu BAGUE {@literal <mathieu.bague at rte-france.com>}
 */
public class KosovoTest {

    @Test
    public void testCountryMapping() {
        // Old buggy implementation considers that the Entsoe code matches with the ISO 3166 code
        Assert.assertThrows("No emum constant for com.powsybl.iidm.network.Country.KS", IllegalArgumentException.class, () -> Country.valueOf(UcteCountryCode.KS.name()));

        // New code uses the mapping table to find the ISO 3166 code
        Assert.assertEquals(Country.XK, EntsoeGeographicalCode.valueOf(UcteCountryCode.KS.name()).getCountry());
    }

}
