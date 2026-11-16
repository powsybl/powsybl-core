/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import org.junit.jupiter.api.Test;
import org.threeten.extra.Interval;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.powsybl.commons.test.ComparisonUtils.assertIteratorsEquals;
import static java.lang.Double.NaN;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class DoubleTimeSeriesTest {

    @Test
    void test() {
        // ts1 1 1 1 1 1 1 1 1
        // ts2 2 2 2 3 3 3 2 2
        // ts3 4 4 5 5 5 4 4 4
        //     |   | |   | |
        //     0   2 3   5 6
        TimeSeriesIndex index = new RegularTimeSeriesIndex(Instant.ofEpochMilli(10000), Instant.ofEpochMilli(10007), Duration.ofMillis(1));
        DoubleTimeSeries ts1 = new StoredDoubleTimeSeries(new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index),
                                                          new CompressedDoubleDataChunk(0, 8, new double[] {1}, new int[] {8}));
        DoubleTimeSeries ts2 = new StoredDoubleTimeSeries(new TimeSeriesMetadata("ts2", TimeSeriesDataType.DOUBLE, index),
                                                          new CompressedDoubleDataChunk(0, 8, new double[] {2, 3, 2}, new int[] {3, 3, 2}));
        DoubleTimeSeries ts3 = new StoredDoubleTimeSeries(new TimeSeriesMetadata("ts3", TimeSeriesDataType.DOUBLE, index),
                                                          new CompressedDoubleDataChunk(0, 8, new double[] {4, 5, 4}, new int[] {2, 3, 3}));

        double[][] expectedValues = {
            {1d, 2d, 4d},
            {1d, 2d, 5d},
            {1d, 3d, 5d},
            {1d, 3d, 4d},
            {1d, 2d, 4d}
        };
        Iterator<Instant> expectedInstants = List.of(
            Instant.ofEpochMilli(10000),
            Instant.ofEpochMilli(10002),
            Instant.ofEpochMilli(10003),
            Instant.ofEpochMilli(10005),
            Instant.ofEpochMilli(10006)
        ).iterator();
        assertDoubleMultiPointsIteratorEquals(expectedValues, expectedInstants, DoubleTimeSeries.compressedIterator(List.of(ts1, ts2, ts3)));
    }

    @Test
    void testUncompressedIterator() {
        // ts1 1 1 1 1 1 1 1 1
        // ts2 2 2 2 3 3 3 2 2
        // ts3 4 4 5 5 5 4 4 4
        //     |   | |   | |
        //     0   2 3   5 6
        TimeSeriesIndex index = new RegularTimeSeriesIndex(Instant.ofEpochMilli(10000), Instant.ofEpochMilli(10007), Duration.ofMillis(1));
        DoubleTimeSeries ts1 = new StoredDoubleTimeSeries(new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index),
            new CompressedDoubleDataChunk(0, 8, new double[] {1}, new int[] {8}));
        DoubleTimeSeries ts2 = new StoredDoubleTimeSeries(new TimeSeriesMetadata("ts2", TimeSeriesDataType.DOUBLE, index),
            new CompressedDoubleDataChunk(0, 8, new double[] {2, 3, 2}, new int[] {3, 3, 2}));
        DoubleTimeSeries ts3 = new StoredDoubleTimeSeries(new TimeSeriesMetadata("ts3", TimeSeriesDataType.DOUBLE, index),
            new CompressedDoubleDataChunk(0, 8, new double[] {4, 5, 4}, new int[] {2, 3, 3}));

        double[][] expectedValues = {
            {1d, 2d, 4d},
            {1d, 2d, 4d},
            {1d, 2d, 5d},
            {1d, 3d, 5d},
            {1d, 3d, 5d},
            {1d, 3d, 4d},
            {1d, 2d, 4d},
            {1d, 2d, 4d}
        };
        assertDoubleMultiPointsIteratorEquals(expectedValues, index.iterator(), DoubleTimeSeries.uncompressedIterator(List.of(ts1, ts2, ts3)));
        assertDoubleMultiPointsIteratorEquals(expectedValues, index.iterator(), DoubleTimeSeries.iterator(List.of(ts1, ts2, ts3)));
    }

    @Test
    void testStream() {
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:45:00Z"),
            Duration.ofMinutes(15));
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), index);
        CompressedDoubleDataChunk chunk1 = new CompressedDoubleDataChunk(0, 5, new double[] {1d, 2d}, new int[] {3, 2});
        CompressedDoubleDataChunk chunk2 = new CompressedDoubleDataChunk(5, 3, new double[] {3d, 4d}, new int[] {1, 2});
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata, chunk1, chunk2);
        assertArrayEquals(new double[] {1d, 1d, 1d, 2d, 2d, 3d, 4d, 4d}, timeSeries.toArray(), 0d);

        // Uncompressed points
        DoublePoint[] uncompressedPointsRef = {
            new DoublePoint(0, Instant.parse("2015-01-01T00:00:00Z"), 1d),
            new DoublePoint(1, Instant.parse("2015-01-01T00:15:00Z"), 1d),
            new DoublePoint(2, Instant.parse("2015-01-01T00:30:00Z"), 1d),
            new DoublePoint(3, Instant.parse("2015-01-01T00:45:00Z"), 2d),
            new DoublePoint(4, Instant.parse("2015-01-01T01:00:00Z"), 2d),
            new DoublePoint(5, Instant.parse("2015-01-01T01:15:00Z"), 3d),
            new DoublePoint(6, Instant.parse("2015-01-01T01:30:00Z"), 4d),
            new DoublePoint(7, Instant.parse("2015-01-01T01:45:00Z"), 4d)
        };
        assertArrayEquals(uncompressedPointsRef, timeSeries.stream().toArray());
        assertArrayEquals(uncompressedPointsRef, timeSeries.uncompressedStream().toArray());
        assertIteratorsEquals(List.of(uncompressedPointsRef).iterator(), timeSeries.uncompressedIterator());

        // Compressed points
        DoublePoint[] compressedPointsRef = {
            new DoublePoint(0, Instant.parse("2015-01-01T00:00:00Z"), 1d),
            new DoublePoint(3, Instant.parse("2015-01-01T00:45:00Z"), 2d),
            new DoublePoint(5, Instant.parse("2015-01-01T01:15:00Z"), 3d),
            new DoublePoint(6, Instant.parse("2015-01-01T01:30:00Z"), 4d)
        };
        assertArrayEquals(compressedPointsRef, timeSeries.compressedStream().toArray());
        assertIteratorsEquals(List.of(compressedPointsRef).iterator(), timeSeries.compressedIterator());
    }

    @Test
    void testGetByIndex() {
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
    void testDefaultGetByIndexDoubleTimeSeries() {
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

    private static void assertDoubleMultiPointsIteratorEquals(double[][] expectedValues, Iterator<Instant> expectedInstants, Iterator<DoubleMultiPoint> it) {
        for (double[] expectedValuesRow : List.of(expectedValues)) {
            DoubleMultiPoint point = it.next();
            assertEquals(expectedValuesRow[0], point.getValue(0), 0d);
            assertEquals(expectedValuesRow[1], point.getValue(1), 0d);
            assertEquals(expectedValuesRow[2], point.getValue(2), 0d);
            assertEquals(expectedInstants.next(), point.getInstant());
        }
        assertFalse(it.hasNext());
    }
}
