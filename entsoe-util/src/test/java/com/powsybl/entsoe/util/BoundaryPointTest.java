/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.entsoe.util;

import com.powsybl.iidm.network.Country;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class BoundaryPointTest {

    @Test
    void test() {
        BoundaryPoint bp = new BoundaryPoint("name", Country.FR, Country.BE);
        assertEquals("name", bp.getName());
        assertEquals("name", bp.toString());
        assertSame(Country.FR, bp.getBorderFrom());
        assertSame(Country.BE, bp.getBorderTo());

        assertThrows(NullPointerException.class, () -> new BoundaryPoint(null, Country.FR, Country.BE));
        assertThrows(NullPointerException.class, () -> new BoundaryPoint("name", null, Country.BE));
        assertThrows(NullPointerException.class, () -> new BoundaryPoint("name", Country.FR, null));
    }
}
