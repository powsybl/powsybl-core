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

import static java.lang.Double.NaN;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DoubleTimeSeriesValuesTest {

    @Test
    void testGet() {
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:45:00Z"), Duration.ofMinutes(15));
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index);
        UncompressedDoubleDataChunk chunk1 = new UncompressedDoubleDataChunk(0, new double[]{1d, 2d, 3d, 4d, 5d, 6d});
        UncompressedDoubleDataChunk chunk2 = new UncompressedDoubleDataChunk(6, new double[]{7d, 8d});
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata, chunk1, chunk2);
        List<DoubleTimeSeries> splitTimeSeries = timeSeries.split(4);
        DoubleTimeSeries ts1 = splitTimeSeries.get(0);
        DoubleTimeSeries ts2 = splitTimeSeries.get(1);
        // Original time series
        assertEquals(2d, timeSeries.get(1), 0d);
        assertEquals(6d, timeSeries.get(5), 0d);
        assertEquals(7d, timeSeries.get(6), 0d);

        // First split time series
        assertEquals(2d, ts1.get(1), 0d);
        assertEquals(NaN, ts1.get(5), 0d);
        assertEquals(NaN, ts1.get(6), 0d);

        // Second split time series
        assertEquals(6d, ts2.get(1), 0d);
        assertEquals(NaN, ts2.get(5), 0d);
        assertEquals(NaN, ts2.get(6), 0d);
    }
}