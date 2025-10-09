/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class CoordinateTest {

    @Test
    void testEquals() {
        new EqualsTester()
                .addEqualityGroup(new Coordinate(48.1, 1.5), new Coordinate(48.1, 1.5))
                .addEqualityGroup(new Coordinate(48.06, 1.3), new Coordinate(48.06, 1.3))
                .testEquals();
    }

    @Test
    void testToString() {
        assertEquals("Coordinate(lat=48.1, lon=1.5)", new Coordinate(48.1, 1.5).toString());
    }

    @Test
    void testCopy() {
        var c1 = new Coordinate(48.1, 1.5);
        var c1Copy = new Coordinate(c1);
        assertEquals(c1.getLatitude(), c1Copy.getLatitude(), 0);
        assertEquals(c1.getLongitude(), c1Copy.getLongitude(), 0);
    }
}
