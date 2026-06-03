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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
class DoubleTimeSeriesValuesTest {

    @Test
    void testGet() {
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:45:00Z"), Duration.ofMinutes(15));
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index);
        UncompressedDoubleDataChunk chunk1 = new UncompressedDoubleDataChunk(0, new double[]{1d, 2d, 3d, 4d, 5d, 6d});
        UncompressedDoubleDataChunk chunk2 = new UncompressedDoubleDataChunk(6, new double[]{7d, 8d});
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata, chunk1, chunk2);
        List<DoubleTimeSeries> splitTimeSeries = timeSeries.split(4);
        DoubleTimeSeries timeSeries1 = splitTimeSeries.get(0);
        DoubleTimeSeries timeSeries2 = splitTimeSeries.get(1);
        assertArrayEquals(new double[]{1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d}, timeSeries.toArray(), 0d);
        assertArrayEquals(new double[]{1d, 2d, 3d, 4d, NaN, NaN, NaN, NaN}, timeSeries1.toArray(), 0d);
        assertArrayEquals(new double[]{NaN, NaN, NaN, NaN, 5d, 6d, 7d, 8d}, timeSeries2.toArray(), 0d);
        // Original time series
        assertTimeSerie(timeSeries, 2d, 6d, 7d);
        // First split time series
        assertTimeSerie(timeSeries1, 2d, NaN, NaN);
        // Second split time series
        assertTimeSerie(timeSeries2, NaN, 6d, 7d);
    }

    @Test
    void testDefaultGetDoubleTimeSeries() {
        DoubleTimeSeries timeSeries = new FixedArrayDoubleTimeSeries(1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d);
        DoubleTimeSeries timeSeries1 = new FixedArrayDoubleTimeSeries(1d, 2d, 3d, 4d, NaN, NaN, NaN, NaN);
        DoubleTimeSeries timeSeries2 = new FixedArrayDoubleTimeSeries(NaN, NaN, NaN, NaN, 5d, 6d, 7d, 8d);
        // Original time series
        assertTimeSerie(timeSeries, 2d, 6d, 7d);
        // First split time series
        assertTimeSerie(timeSeries1, 2d, NaN, NaN);
        // Second split time series
        assertTimeSerie(timeSeries2, NaN, 6d, 7d);
    }

    private static void assertTimeSerie(DoubleTimeSeries timeSeries, double expectedAtIndex1, double expectedAtIndex5, double expectedAtIndex6) {
        assertEquals(expectedAtIndex1, timeSeries.get(1), 0d);
        assertEquals(expectedAtIndex5, timeSeries.get(5), 0d);
        assertEquals(expectedAtIndex6, timeSeries.get(6), 0d);
    }

    @Test
    void testEqualsAndToString() {
        DoubleTimeSeriesValues timeSeriesValues1 = new DoubleTimeSeriesValues(new double[] {1d, 2d}, 3);
        DoubleTimeSeriesValues timeSeriesValues2 = new DoubleTimeSeriesValues(new double[] {1d, 2d}, 3);
        assertEquals(timeSeriesValues1, timeSeriesValues2);
        assertEquals(timeSeriesValues1.hashCode(), timeSeriesValues2.hashCode());
        assertEquals("DoubleTimeSeriesValues{values=[1.0, 2.0], offset=3}", timeSeriesValues2.toString());
    }
}
