/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.time.ZoneId;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TimeSeriesTableTest {

    @Test
    public void test() {
        TimeSeriesIndex index = new TestTimeSeriesIndex(0, 4);
        TimeSeriesMetadata metadata1 = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index);
        TimeSeriesMetadata metadata2 = new TimeSeriesMetadata("ts2", TimeSeriesDataType.DOUBLE, index);
        TimeSeriesMetadata metadata3 = new TimeSeriesMetadata("ts3", TimeSeriesDataType.STRING, index);
        DoubleTimeSeries ts1 = new StoredDoubleTimeSeries(metadata1, new UncompressedDoubleArrayChunk(0, new double[] {1, 2, 3, 4}));
        DoubleTimeSeries ts2 = new StoredDoubleTimeSeries(metadata2, new UncompressedDoubleArrayChunk(0, new double[] {5, 6, 7, 8}));
        StringTimeSeries ts3 = new StringTimeSeries(metadata3, new UncompressedStringArrayChunk(1, new String[] {"a", "b", "c"}));

        // load time series in the table
        TimeSeriesTable table = new TimeSeriesTable(1, 1, index);

        table.load(1, ImmutableList.of(ts1, ts2, ts3));

        assertEquals(index, table.getTableIndex());

        // test indexes
        assertEquals(0, table.getDoubleTimeSeriesIndex("ts1"));
        assertEquals(1, table.getDoubleTimeSeriesIndex("ts2"));
        assertEquals(0, table.getStringTimeSeriesIndex("ts3"));

        // test values
        assertEquals(1, table.getDoubleValue(1, 0, 0), 0);
        assertEquals(2, table.getDoubleValue(1, 0, 1), 0);
        assertEquals(3, table.getDoubleValue(1, 0, 2), 0);
        assertEquals(4, table.getDoubleValue(1, 0, 3), 0);
        assertEquals(5, table.getDoubleValue(1, 1, 0), 0);
        assertEquals(6, table.getDoubleValue(1, 1, 1), 0);
        assertEquals(7, table.getDoubleValue(1, 1, 2), 0);
        assertEquals(8, table.getDoubleValue(1, 1, 3), 0);
        assertNull(table.getStringValue(1, 2, 0));
        assertEquals("a", table.getStringValue(1, 2, 1));
        assertEquals("b", table.getStringValue(1, 2, 2));
        assertEquals("c", table.getStringValue(1, 2, 3));

        // test statistics
        assertEquals(2.5, table.getMean(1, 0), 0);
        assertEquals(1.2909944487358056, table.getStdDev(1, 0), Math.pow(10, -15));
        List<TimeSeriesTable.Correlation> corrs = table.findMostCorrelatedTimeSeries("ts1", 1);
        assertEquals(1, corrs.size());
        assertEquals("ts2", corrs.get(0).getTimeSeriesName2());
        assertEquals(1, corrs.get(0).getCoefficient(), 0);

        // test CSV export
        assertEquals(String.join(System.lineSeparator(),
                                 "Time;Version;ts1;ts2;ts3",
                                 "1970-01-01T00:00:00Z;1;1.0;5.0;",
                                 "1970-01-01T00:00:00.001Z;1;2.0;6.0;a",
                                 "1970-01-01T00:00:00.002Z;1;3.0;7.0;b",
                                 "1970-01-01T00:00:00.003Z;1;4.0;8.0;c") + System.lineSeparator(),
                     table.toCsvString(';', ZoneId.of("UTC")));

        // test empty table CSV export
        String emptyCsv = new TimeSeriesTable(0, 0, InfiniteTimeSeriesIndex.INSTANCE).toCsvString(';', ZoneId.of("UTC"));
        assertEquals("Time;Version" + System.lineSeparator(), emptyCsv);
    }
}
