/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class UcteFormatVersionTest {

    @Test
    public void test() {
        String[] dates = {"2003.09.01", "2007.05.01"};

        assertEquals(2, UcteFormatVersion.values().length);
        for (int i = 0; i < 2; ++i) {
            UcteFormatVersion version = UcteFormatVersion.values()[i];
            assertEquals(dates[i], version.getDate());
            assertEquals(version, UcteFormatVersion.findByDate(dates[i]));
        }

        assertNull(UcteFormatVersion.findByDate(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownFormatVersion() {
        UcteFormatVersion.findByDate("1970.01.01");
    }
}
