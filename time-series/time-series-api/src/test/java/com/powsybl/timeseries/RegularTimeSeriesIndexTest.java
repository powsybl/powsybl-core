/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import com.powsybl.commons.json.JsonUtil;
import org.junit.jupiter.api.Test;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class RegularTimeSeriesIndexTest {

    @Test
    void test() throws IOException {
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:00:00Z"),
                                                                     Duration.ofMinutes(15));

        // test getters
        assertEquals("2015-01-01T00:00:00Z", Instant.ofEpochMilli(index.getStartTime()).toString());
        assertEquals("2015-01-01T01:00:00Z", Instant.ofEpochMilli(index.getEndTime()).toString());
        assertEquals(15 * 60 * 1000, index.getSpacing());
        assertEquals(5, index.getPointCount());
        assertEquals(Instant.ofEpochMilli(index.getStartTime() + 15 * 60 * 1000).toEpochMilli(), index.getTimeAt(1));
        assertEquals("2015-01-01T00:15:00Z", index.getInstantAt(1).toString());

        // test iterator ans stream
        List<Instant> instants = Arrays.asList(Instant.parse("2015-01-01T00:00:00Z"),
                                               Instant.parse("2015-01-01T00:15:00Z"),
                                               Instant.parse("2015-01-01T00:30:00Z"),
                                               Instant.parse("2015-01-01T00:45:00Z"),
                                               Instant.parse("2015-01-01T01:00:00Z"));
        assertEquals(instants, index.stream().collect(Collectors.toList()));
        assertEquals(instants, Lists.newArrayList(index.iterator()));

        // test to string
        assertEquals("RegularTimeSeriesIndex(startTime=2015-01-01T00:00:00Z, endTime=2015-01-01T01:00:00Z, spacing=PT15M)",
                     index.toString());

        // test json
        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"startTime\" : 1420070400000,",
                "  \"endTime\" : 1420074000000,",
                "  \"spacing\" : 900000",
                "}");
        String json = index.toJson();
        assertEquals(jsonRef, json);
        RegularTimeSeriesIndex index2 = JsonUtil.parseJson(json, RegularTimeSeriesIndex::parseJson);
        assertNotNull(index2);
        assertEquals(index, index2);
    }

    @Test
    void testEquals() {
        new EqualsTester()
                .addEqualityGroup(RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:00:00Z"), Duration.ofMinutes(15)),
                                  RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:00:00Z"), Duration.ofMinutes(15)))
                .addEqualityGroup(RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:15:00Z"), Duration.ofMinutes(30)),
                                  RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:15:00Z"), Duration.ofMinutes(30)))
                .testEquals();
    }

    @Test
    void testContructorError() {
        assertThrows(IllegalArgumentException.class, () -> RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T00:10:00Z"),
                                      Duration.ofMinutes(15)));
    }

    @Test
    void testContructorErrorPointCount() {
        assertThrows(IllegalArgumentException.class, () -> RegularTimeSeriesIndex.create(Interval.parse("2000-01-01T00:00:00Z/2100-01-01T00:10:00Z"),
                                      Duration.ofSeconds(1)));
    }

    @Test
    void testPointCountSimple() {
        //2 data points at 0 and 10
        assertEquals(2, new RegularTimeSeriesIndex(0, 10, 10).getPointCount());
    }

    @Test
    void testPointCountRounded() {
        //We allow some imprecision to simplify calendar dates
        assertEquals(3, new RegularTimeSeriesIndex(0, 19, 10).getPointCount());
        assertEquals(3, new RegularTimeSeriesIndex(0, 20, 10).getPointCount());
        assertEquals(3, new RegularTimeSeriesIndex(0, 21, 10).getPointCount());
        //Concrete example:
        // 1 data every year for 10 years (rounding hides the year length differences):
        // millisInYear is not exact because of leap years, but even when taking leap years into account,
        // the number of seconds in a year in not predictable because of leap seconds.
        // Still it's a good enough approximation give the correct result.
        long millisInYear = 365L * 86400 * 1000;
        assertEquals(10, new RegularTimeSeriesIndex(
            Instant.parse("2000-01-01T00:00:00Z").toEpochMilli(),
            Instant.parse("2009-01-01T00:00:00Z").toEpochMilli(),
            millisInYear).getPointCount());
    }

    @Test
    void testPointCountHuge() {
        // 1 data every 30 seconds for ~30years, ~30years+30s, ~30years+60s
        assertEquals(30 * 365 * 24 * 120 + 1, new RegularTimeSeriesIndex(0, 30L * 365 * 24 * 60 * 60 * 1000 + 0 * 30 * 1000, 30 * 1000).getPointCount());
        assertEquals(30 * 365 * 24 * 120 + 2, new RegularTimeSeriesIndex(0, 30L * 365 * 24 * 60 * 60 * 1000 + 1 * 30 * 1000, 30 * 1000).getPointCount());
        assertEquals(30 * 365 * 24 * 120 + 3, new RegularTimeSeriesIndex(0, 30L * 365 * 24 * 60 * 60 * 1000 + 2 * 30 * 1000, 30 * 1000).getPointCount());
    }
}
