/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class UcteCountryCodeTest {

    @Test
    public void test() {
        final char[] countryCodeNodes = {
            'O', 'A', 'B', 'V', 'W', '3', 'S', 'C', 'D',
            'K', 'E', 'F', '5', 'G', 'M', 'H', 'I', '1',
            '6', '2', '7', 'Y', '9', 'N', 'P', 'Z', 'R',
            '4', '8', 'Q', 'L', 'T', 'U', '0', 'J', 'X',
        };

        final String[] prettyNames = {
            "Austria", "Albania", "Belgium", "Bulgaria", "Bosnia and Herzegovina", "Belarus", "Switzerland", "Czech Republic", "Germany",
            "Denmark", "Spain", "France", "Great Britain", "Greece", "Hungary", "Croatia", "Italy", "Luxemburg",
            "Lithuania", "Morocco", "Moldavia", "FYROM", "Norway", "Netherlands", "Portugal", "Poland", "Romania",
            "Russia", "Sweden", "Slovakia", "Slovenia", "Turkey", "Ukraine", "Montenegro", "Serbia", "Fictitious border node"
        };

        assertEquals(36, UcteCountryCode.values().length);
        for (int i = 0; i < UcteCountryCode.values().length; ++i) {
            UcteCountryCode code = UcteCountryCode.values()[i];
            assertEquals(countryCodeNodes[i], code.getUcteCode());
            assertEquals(code, UcteCountryCode.fromUcteCode(countryCodeNodes[i]));
            assertEquals(prettyNames[i], code.getPrettyName());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownCountryCode() {
        UcteCountryCode.fromUcteCode('_');
    }
}
