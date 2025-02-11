/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class StringPointTest {

    @Test
    void testGetters() {
        StringPoint point = new StringPoint(0, Instant.parse("2015-01-01T00:00:00Z"), "a");
        assertEquals(0, point.getIndex());
        assertEquals(Instant.parse("2015-01-01T00:00:00Z"), point.getInstant());
        assertEquals(Instant.parse("2015-01-01T00:00:00Z").toEpochMilli(), point.getTime());
        assertEquals("a", point.getValue());
        assertEquals("StringPoint(index=0, instant=2015-01-01T00:00:00Z, value=a)", point.toString());
    }

    @Test
    void testEquals() {
        new EqualsTester()
                .addEqualityGroup(new StringPoint(0, Instant.parse("2015-01-01T00:00:00Z"), "a"),
                                  new StringPoint(0, Instant.parse("2015-01-01T00:00:00Z"), "a"))
                .addEqualityGroup(new StringPoint(1, Instant.parse("2015-01-01T00:15:00Z"), "b"),
                                  new StringPoint(1, Instant.parse("2015-01-01T00:15:00Z"), "b"))
                .testEquals();

        // Differences
        Instant instantA = Instant.parse("2015-01-01T00:00:00Z");
        Instant instantB = Instant.parse("2015-01-01T01:00:00Z");
        assertNotEquals(new StringPoint(0, instantA, "a"), new StringPoint(0, instantA, "b"));
        assertNotEquals(new StringPoint(0, instantA, "a"), new StringPoint(1, instantA, "a"));
        assertNotEquals(new StringPoint(0, instantA, "a"), new StringPoint(0, instantB, "a"));
    }
}
