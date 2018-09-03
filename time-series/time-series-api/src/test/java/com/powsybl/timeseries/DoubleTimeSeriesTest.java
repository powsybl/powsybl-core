/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DoubleTimeSeriesTest {

    @Test
    public void test() {
        // ts1 1 1 1 1 1 1 1 1
        // ts2 2 2 2 3 3 3 2 2
        // ts3 4 4 5 5 5 4 4 4
        //     |   | |   | |
        //     0   2 3   5 6
        TimeSeriesIndex index = new TestTimeSeriesIndex(10000, 8);
        DoubleTimeSeries ts1 = new StoredDoubleTimeSeries(new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index),
                                                          new CompressedDoubleArrayChunk(0, 8, new double[] {1}, new int[] {8}));
        DoubleTimeSeries ts2 = new StoredDoubleTimeSeries(new TimeSeriesMetadata("ts2", TimeSeriesDataType.DOUBLE, index),
                                                          new CompressedDoubleArrayChunk(0, 8, new double[] {2, 3, 2}, new int[] {3, 3, 2}));
        DoubleTimeSeries ts3 = new StoredDoubleTimeSeries(new TimeSeriesMetadata("ts3", TimeSeriesDataType.DOUBLE, index),
                                                          new CompressedDoubleArrayChunk(0, 8, new double[] {4, 5, 4}, new int[] {2, 3, 3}));

        Iterator<DoubleMultiPoint> it = DoubleTimeSeries.iterator(ImmutableList.of(ts1, ts2, ts3));

        assertTrue(it.hasNext());
        DoubleMultiPoint point0 = it.next();
        assertEquals(1d, point0.getValue(0), 0d);
        assertEquals(2d, point0.getValue(1), 0d);
        assertEquals(4d, point0.getValue(2), 0d);

        assertTrue(it.hasNext());
        DoubleMultiPoint point2 = it.next();
        assertEquals(1d, point2.getValue(0), 0d);
        assertEquals(2d, point2.getValue(1), 0d);
        assertEquals(5d, point2.getValue(2), 0d);

        assertTrue(it.hasNext());
        DoubleMultiPoint point3 = it.next();
        assertEquals(1d, point3.getValue(0), 0d);
        assertEquals(3d, point3.getValue(1), 0d);
        assertEquals(5d, point3.getValue(2), 0d);

        assertTrue(it.hasNext());
        DoubleMultiPoint point5 = it.next();
        assertEquals(1d, point5.getValue(0), 0d);
        assertEquals(3d, point5.getValue(1), 0d);
        assertEquals(4d, point5.getValue(2), 0d);

        assertTrue(it.hasNext());
        DoubleMultiPoint point6 = it.next();
        assertEquals(1d, point6.getValue(0), 0d);
        assertEquals(2d, point6.getValue(1), 0d);
        assertEquals(4d, point6.getValue(2), 0d);

        assertFalse(it.hasNext());
    }
}
