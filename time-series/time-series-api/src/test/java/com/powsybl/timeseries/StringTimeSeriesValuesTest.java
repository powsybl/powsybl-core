/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import org.junit.jupiter.api.Test;
import org.threeten.extra.Interval;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringTimeSeriesValuesTest {

    @Test
    void testGet() {
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:45:00Z"), Duration.ofMinutes(15));
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index);
        UncompressedStringDataChunk chunk1 = new UncompressedStringDataChunk(0, new String[]{"a", "b", "c", "d", "e", "f"});
        UncompressedStringDataChunk chunk2 = new UncompressedStringDataChunk(6, new String[]{"g", "h"});
        StringTimeSeries timeSeries = new StringTimeSeries(metadata, chunk1, chunk2);
        List<StringTimeSeries> splitTimeSeries = timeSeries.split(4);
        StringTimeSeries ts1 = splitTimeSeries.get(0);
        StringTimeSeries ts2 = splitTimeSeries.get(1);
        // Original time series
        assertEquals("b", timeSeries.get(1));
        assertEquals("f", timeSeries.get(5));
        assertEquals("g", timeSeries.get(6));

        // First split time series
        assertEquals("b", ts1.get(1));
        assertEquals(null, ts1.get(5));
        assertEquals(null, ts1.get(6));

        // Second split time series
        assertEquals("f", ts2.get(1));
        assertEquals(null, ts2.get(5));
        assertEquals(null, ts2.get(6));
    }
}