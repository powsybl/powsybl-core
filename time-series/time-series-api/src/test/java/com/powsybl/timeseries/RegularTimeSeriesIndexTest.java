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

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class RegularTimeSeriesIndexTest {

    @Test
    void test() {
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:00:00Z"),
            Duration.ofMinutes(15));

        // test getters
        assertEquals("2015-01-01T00:00:00Z", index.getStartInstant().toString());
        assertEquals("2015-01-01T01:00:00Z", index.getEndInstant().toString());
        assertEquals(15 * 60 * 1_000_000_000L, index.getDeltaT().toNanos());
        assertEquals(5, index.getPointCount());
        Instant secondInstant = index.getStartInstant().plus(index.getDeltaT());
        assertEquals(secondInstant, index.getInstantAt(1));
        assertEquals("2015-01-01T00:15:00Z", index.getInstantAt(1).toString());

        // test iterator and stream
        List<Instant> instants = Arrays.asList(Instant.parse("2015-01-01T00:00:00Z"),
            Instant.parse("2015-01-01T00:15:00Z"),
            Instant.parse("2015-01-01T00:30:00Z"),
            Instant.parse("2015-01-01T00:45:00Z"),
            Instant.parse("2015-01-01T01:00:00Z"));
        assertEquals(instants, index.stream().toList());
        assertEquals(instants, Lists.newArrayList(index.iterator()));

        // test to string
        assertEquals("RegularTimeSeriesIndex(startInstant=2015-01-01T00:00:00Z, endInstant=2015-01-01T01:00:00Z, deltaT=PT15M)",
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
        Interval interval = Interval.parse("2015-01-01T00:00:00Z/2015-01-01T00:10:00Z");
        Duration duration = Duration.ofMinutes(15);
        assertThrows(IllegalArgumentException.class, () -> RegularTimeSeriesIndex.create(interval, duration));
    }

    @Test
    void testContructorErrorPointCount() {
        Interval interval = Interval.parse("2000-01-01T00:00:00Z/2100-01-01T00:10:00Z");
        Duration duration = Duration.ofSeconds(1);
        assertThrows(IllegalArgumentException.class, () -> RegularTimeSeriesIndex.create(interval, duration));
    }

    @Test
    void testPointCountSimple() {
        //2 data points at 0 and 10
        assertEquals(2, new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(10), Duration.ofMillis(10)).getPointCount());
    }

    @Test
    void testPointCountRounded() {
        //We allow some imprecision to simplify calendar dates
        assertEquals(3, new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(19), Duration.ofMillis(10)).getPointCount());
        assertEquals(3, new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(20), Duration.ofMillis(10)).getPointCount());
        assertEquals(3, new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(21), Duration.ofMillis(10)).getPointCount());
        //Concrete example:
        // 1 data every year for 10 years (rounding hides the year length differences):
        // millisInYear is not exact because of leap years, but even when taking leap years into account,
        // the number of seconds in a year in not predictable because of leap seconds.
        // Still it's a good enough approximation give the correct result.
        long millisInYear = 365L * 86400 * 1000;
        assertEquals(10, new RegularTimeSeriesIndex(
            Instant.parse("2000-01-01T00:00:00Z"),
            Instant.parse("2009-01-01T00:00:00Z"),
            Duration.ofMillis(millisInYear)).getPointCount());
    }

    @Test
    void testPointCountHuge() {
        // 1 data every 30 seconds for ~30years, ~30years+30s, ~30years+60s
        assertEquals(30 * 365 * 24 * 120 + 1, new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(30L * 365 * 24 * 60 * 60 * 1000), Duration.ofMillis(30 * 1000)).getPointCount());
        assertEquals(30 * 365 * 24 * 120 + 2, new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(30L * 365 * 24 * 60 * 60 * 1000 + 30 * 1000), Duration.ofMillis(30 * 1000)).getPointCount());
        assertEquals(30 * 365 * 24 * 120 + 3, new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(30L * 365 * 24 * 60 * 60 * 1000 + 2 * 30 * 1000), Duration.ofMillis(30 * 1000)).getPointCount());
    }
}
