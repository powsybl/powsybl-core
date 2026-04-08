/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network;

import org.junit.jupiter.api.Test;

import static com.powsybl.ucte.network.UcteCountryCode.isUcteCountryCode;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class UcteCountryCodeTest {

    @Test
    void test() {
        final char[] countryCodeNodes = {
            'O', 'A', 'B', 'V', 'W', '3', 'S', 'C', 'D',
            'K', 'E', 'F', '5', 'G', 'M', 'H', 'I', '1',
            '6', '2', '7', 'Y', '9', 'N', 'P', 'Z', 'R',
            '4', '8', 'Q', 'L', 'T', 'U', '0', 'J', '_', 'X',
        };

        final String[] prettyNames = {
            "Austria", "Albania", "Belgium", "Bulgaria", "Bosnia and Herzegovina", "Belarus", "Switzerland", "Czech Republic", "Germany",
            "Denmark", "Spain", "France", "Great Britain", "Greece", "Hungary", "Croatia", "Italy", "Luxemburg",
            "Lithuania", "Morocco", "Moldavia", "FYROM", "Norway", "Netherlands", "Portugal", "Poland", "Romania",
            "Russia", "Sweden", "Slovakia", "Slovenia", "Turkey", "Ukraine", "Montenegro", "Serbia", "Kosovo", "Fictitious border node"
        };

        assertEquals(37, UcteCountryCode.values().length);
        for (int i = 0; i < UcteCountryCode.values().length; ++i) {
            UcteCountryCode code = UcteCountryCode.values()[i];
            assertEquals(countryCodeNodes[i], code.getUcteCode());
            assertEquals(code, UcteCountryCode.fromUcteCode(countryCodeNodes[i]));
            assertEquals(prettyNames[i], code.getPrettyName());
        }
    }

    @Test
    void unknownCountryCode() {
        assertThrows(IllegalArgumentException.class, () -> UcteCountryCode.fromUcteCode('&'));
    }

    @Test
    void isUcteCountryCodeTest() {
        assertTrue(isUcteCountryCode('A'));
        assertTrue(isUcteCountryCode('1'));
        assertFalse(isUcteCountryCode('&'));
    }
}
