/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TimeSeriesTest {

    @Test
    public void test() {
        String csv = String.join(System.lineSeparator(),
                "Time;Version;ts1;ts2",
                "1970-01-01T01:00:00.000+01:00;1;1.0;",
                "1970-01-01T02:00:00.000+01:00;1;;a",
                "1970-01-01T03:00:00.000+01:00;1;3.0;b",
                "1970-01-01T01:00:00.000+01:00;2;4.0;c",
                "1970-01-01T02:00:00.000+01:00;2;5.0;",
                "1970-01-01T03:00:00.000+01:00;2;6.0;d") + System.lineSeparator();

        Map<Integer, List<TimeSeries>> timeSeriesPerVersion = TimeSeries.parseCsv(csv, ';');

        assertEquals(2, timeSeriesPerVersion.size());
        assertEquals(2, timeSeriesPerVersion.get(1).size());
        assertEquals(2, timeSeriesPerVersion.get(2).size());

        TimeSeries ts1v1 = timeSeriesPerVersion.get(1).get(0);
        TimeSeries ts2v1 = timeSeriesPerVersion.get(1).get(1);
        TimeSeries ts1v2 = timeSeriesPerVersion.get(2).get(0);
        TimeSeries ts2v2 = timeSeriesPerVersion.get(2).get(1);

        assertEquals("ts1", ts1v1.getMetadata().getName());
        assertEquals(TimeSeriesDataType.DOUBLE, ts1v1.getMetadata().getDataType());
        assertArrayEquals(new double[] {1, Double.NaN, 3}, ((DoubleTimeSeries) ts1v1).toArray(), 0);

        assertEquals("ts2", ts2v1.getMetadata().getName());
        assertEquals(TimeSeriesDataType.STRING, ts2v1.getMetadata().getDataType());
        assertArrayEquals(new String[] {null, "a", "b"}, ((StringTimeSeries) ts2v1).toArray());

        assertEquals("ts1", ts1v2.getMetadata().getName());
        assertEquals(TimeSeriesDataType.DOUBLE, ts1v2.getMetadata().getDataType());
        assertArrayEquals(new double[] {4, 5, 6}, ((DoubleTimeSeries) ts1v2).toArray(), 0);

        assertEquals("ts2", ts2v2.getMetadata().getName());
        assertEquals(TimeSeriesDataType.STRING, ts2v2.getMetadata().getDataType());
        assertArrayEquals(new String[] {"c", null, "d"}, ((StringTimeSeries) ts2v2).toArray());
    }

    @Test
    public void splitTest() {
        try {
            TimeSeries.split(Collections.<DoubleTimeSeries>emptyList(), 2);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        TimeSeriesIndex index = new TestTimeSeriesIndex(10000, 3);
        List<DoubleTimeSeries> timeSeriesList = Arrays.asList(TimeSeries.create("ts1", index, 1d, 2d, 3d),
                                                              TimeSeries.create("ts1", index, 4d, 5d, 6d));
        try {
            TimeSeries.split(timeSeriesList, 4);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            TimeSeries.split(timeSeriesList, -1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        List<List<DoubleTimeSeries>> split = TimeSeries.split(timeSeriesList, 2);
        assertEquals(2, split.size());
        assertEquals(2, split.get(0).size());
        assertEquals(2, split.get(1).size());
        assertArrayEquals(new double[] {1d, 2d, Double.NaN}, split.get(0).get(0).toArray(), 0d);
        assertArrayEquals(new double[] {4d, 5d, Double.NaN}, split.get(0).get(1).toArray(), 0d);
        assertArrayEquals(new double[] {Double.NaN, Double.NaN, 3d}, split.get(1).get(0).toArray(), 0d);
        assertArrayEquals(new double[] {Double.NaN, Double.NaN, 6d}, split.get(1).get(1).toArray(), 0d);
    }
}
