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
public class DoublePointTest {

    @Test
    public void testGetters() {
        DoublePoint point = new DoublePoint(0, Instant.parse("2015-01-01T00:00:00Z").toEpochMilli(), 10d);
        assertEquals(0, point.getIndex());
        assertEquals(Instant.parse("2015-01-01T00:00:00Z").toEpochMilli(), point.getTime());
        assertEquals(10d, point.getValue(), 0d);
        assertEquals("DoublePoint(index=0, time=2015-01-01T00:00:00Z, value=10.0)", point.toString());
    }

    @Test
    public void testEquals() {
        new EqualsTester()
                .addEqualityGroup(new DoublePoint(0, Instant.parse("2015-01-01T00:00:00Z").toEpochMilli(), 10d),
                        new DoublePoint(0, Instant.parse("2015-01-01T00:00:00Z").toEpochMilli(), 10d))
                .addEqualityGroup(new DoublePoint(1, Instant.parse("2015-01-01T00:15:00Z").toEpochMilli(), 8d),
                        new DoublePoint(1, Instant.parse("2015-01-01T00:15:00Z").toEpochMilli(), 8d))
                .testEquals();
    }
}
