/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class UcteFormatVersionTest {

    @Test
    void test() {
        String[] dates = {"2003.09.01", "2007.05.01"};

        assertEquals(2, UcteFormatVersion.values().length);
        for (int i = 0; i < 2; ++i) {
            UcteFormatVersion version = UcteFormatVersion.values()[i];
            assertEquals(dates[i], version.getDate());
            assertEquals(version, UcteFormatVersion.findByDate(dates[i]));
        }

        assertNull(UcteFormatVersion.findByDate(null));
    }

    @Test
    void testUnknownFormatVersion() {
        assertThrows(IllegalArgumentException.class, () -> UcteFormatVersion.findByDate("1970.01.01"));
    }
}
