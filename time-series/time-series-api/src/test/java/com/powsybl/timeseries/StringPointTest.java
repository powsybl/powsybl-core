/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StringPointTest {

    @Test
    public void testGetters() {
        StringPoint point = new StringPoint(0, Instant.parse("2015-01-01T00:00:00Z").toEpochMilli(), "a");
        assertEquals(0, point.getIndex());
        assertEquals(Instant.parse("2015-01-01T00:00:00Z").toEpochMilli(), point.getTime());
        assertEquals("a", point.getValue());
        assertEquals("StringPoint(index=0, time=2015-01-01T00:00:00Z, value=a)", point.toString());
    }

    @Test
    public void testEquals() {
        new EqualsTester()
                .addEqualityGroup(new StringPoint(0, Instant.parse("2015-01-01T00:00:00Z").toEpochMilli(), "a"),
                                  new StringPoint(0, Instant.parse("2015-01-01T00:00:00Z").toEpochMilli(), "a"))
                .addEqualityGroup(new StringPoint(1, Instant.parse("2015-01-01T00:15:00Z").toEpochMilli(), "b"),
                                  new StringPoint(1, Instant.parse("2015-01-01T00:15:00Z").toEpochMilli(), "b"))
                .testEquals();
    }
}
