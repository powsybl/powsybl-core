/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.iidm.network.Country;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class BoundaryPointTest {

    @Test
    public void test() {
        BoundaryPoint bp = new BoundaryPoint("name", Country.FR, Country.BE);
        assertEquals("name", bp.getName());
        assertEquals("name", bp.toString());
        assertSame(Country.FR, bp.getBorderFrom());
        assertSame(Country.BE, bp.getBorderTo());

        try {
            new BoundaryPoint(null, Country.FR, Country.BE);
            fail();
        } catch (Exception ignored) {
        }

        try {
            new BoundaryPoint("name", null, Country.BE);
            fail();
        } catch (Exception ignored) {
        }

        try {
            new BoundaryPoint("name", Country.FR, null);
            fail();
        } catch (Exception ignored) {
        }
    }
}
