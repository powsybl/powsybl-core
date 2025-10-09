/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.powsybl.timeseries.TimeSeries.TimeFormat;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TimeSeriesTableTest {

    private TimeSeriesTable createTimeSeriesTable(TimeSeriesIndex index) {
        TimeSeriesTable table = getTimeSeriesTable(index);

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

        return table;
    }

    private static TimeSeriesTable getTimeSeriesTable(TimeSeriesIndex index) {
        TimeSeriesMetadata metadata1 = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index);
        TimeSeriesMetadata metadata2 = new TimeSeriesMetadata("ts2", TimeSeriesDataType.DOUBLE, index);
        TimeSeriesMetadata metadata3 = new TimeSeriesMetadata("ts3", TimeSeriesDataType.STRING, index);
        DoubleTimeSeries ts1 = new StoredDoubleTimeSeries(metadata1, new UncompressedDoubleDataChunk(0, new double[] {1, 2, 3, 4}));
        DoubleTimeSeries ts2 = new StoredDoubleTimeSeries(metadata2, new UncompressedDoubleDataChunk(0, new double[] {5, 6, 7, 8}));
        StringTimeSeries ts3 = new StringTimeSeries(metadata3, new UncompressedStringDataChunk(1, new String[] {"a", "b", "c"}));

        // load time series in the table
        TimeSeriesTable table = new TimeSeriesTable(1, 1, index);

        table.load(1, List.of(ts1, ts2, ts3));
        return table;
    }

    @Test
    void testVersionedCSV() {
        TimeSeriesIndex index = new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(3), Duration.ofMillis(1));
        TimeSeriesTable table = createTimeSeriesTable(index);
        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(ZoneId.of("UTC"));

        // test CSV export
        assertEquals(String.join(System.lineSeparator(),
                                 "Time;Version;ts1;ts2;ts3",
                                 "1970-01-01T00:00:00Z;1;1.0;5.0;",
                                 "1970-01-01T00:00:00.001Z;1;2.0;6.0;a",
                                 "1970-01-01T00:00:00.002Z;1;3.0;7.0;b",
                                 "1970-01-01T00:00:00.003Z;1;4.0;8.0;c") + System.lineSeparator(),
                     table.toCsvString(timeSeriesCsvConfig));

    }

    @Test
    void testVersionedCSVNano() {
        TimeSeriesIndex index = new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(0).plus(Duration.ofNanos(3)), Duration.ofNanos(1));
        TimeSeriesTable table = createTimeSeriesTable(index);
        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(ZoneId.of("UTC"));

        // test CSV export
        assertEquals(String.join(System.lineSeparator(),
            "Time;Version;ts1;ts2;ts3",
            "1970-01-01T00:00:00Z;1;1.0;5.0;",
            "1970-01-01T00:00:00.000000001Z;1;2.0;6.0;a",
            "1970-01-01T00:00:00.000000002Z;1;3.0;7.0;b",
            "1970-01-01T00:00:00.000000003Z;1;4.0;8.0;c") + System.lineSeparator(),
            table.toCsvString(timeSeriesCsvConfig));

    }

    @Test
    void testUnversionedCSV() {
        TimeSeriesIndex index = new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(3), Duration.ofMillis(1));
        TimeSeriesTable table = createTimeSeriesTable(index);
        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(ZoneId.of("UTC"), ';', false, TimeFormat.FRACTIONS_OF_SECOND);

        // test CSV export
        assertEquals(String.join(System.lineSeparator(),
                                 "Time;ts1;ts2;ts3",
                                 "0.0;1.0;5.0;",
                                 "0.001;2.0;6.0;a",
                                 "0.002;3.0;7.0;b",
                                 "0.003;4.0;8.0;c") + System.lineSeparator(),
                     table.toCsvString(timeSeriesCsvConfig));
    }

    @Test
    void testMillisCSV() {
        TimeSeriesIndex index = new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(3), Duration.ofMillis(1));
        TimeSeriesTable table = createTimeSeriesTable(index);
        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(ZoneId.of("UTC"), ';', false, TimeFormat.MILLIS, false);

        // test CSV export
        assertEquals(String.join(System.lineSeparator(),
                                 "Time;ts1;ts2;ts3",
                                 "0;1.0;5.0;",
                                 "1;2.0;6.0;a",
                                 "2;3.0;7.0;b",
                                 "3;4.0;8.0;c") + System.lineSeparator(),
                     table.toCsvString(timeSeriesCsvConfig));
    }

    @Test
    void testMicrosCSV() {
        TimeSeriesIndex index = new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(0).plus(Duration.ofMillis(3)), Duration.ofMillis(1));
        TimeSeriesTable table = createTimeSeriesTable(index);
        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(ZoneId.of("UTC"), ';', false, TimeFormat.MICROS, false);

        // test CSV export
        assertEquals(String.join(System.lineSeparator(),
            "Time;ts1;ts2;ts3",
            "0;1.0;5.0;",
            "1000;2.0;6.0;a",
            "2000;3.0;7.0;b",
            "3000;4.0;8.0;c") + System.lineSeparator(),
            table.toCsvString(timeSeriesCsvConfig));
    }

    @Test
    void testNanosCSV() {
        TimeSeriesIndex index = new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(0).plus(Duration.ofNanos(3)), Duration.ofNanos(1));
        TimeSeriesTable table = createTimeSeriesTable(index);
        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(ZoneId.of("UTC"), ';', false, TimeFormat.NANOS, false);

        // test CSV export
        assertEquals(String.join(System.lineSeparator(),
            "Time;ts1;ts2;ts3",
            "0;1.0;5.0;",
            "1;2.0;6.0;a",
            "2;3.0;7.0;b",
            "3;4.0;8.0;c") + System.lineSeparator(),
            table.toCsvString(timeSeriesCsvConfig));
    }

    @Test
    void testEmptyTable() {
        // test empty table CSV export
        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(ZoneId.of("UTC"));
        String emptyCsv = new TimeSeriesTable(0, 0, InfiniteTimeSeriesIndex.INSTANCE).toCsvString(timeSeriesCsvConfig);
        assertEquals("Time;Version" + System.lineSeparator(), emptyCsv);
    }

    @Test
    void testConcurrent() throws Exception {
        int threadCount = 16;
        int padLeftCount = (int) Math.floor(Math.log10(threadCount)) + 1;
        int timeSeriesLength = 100000;
        TimeSeriesIndex index = new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(timeSeriesLength - 1), Duration.ofMillis(1));
        TimeSeriesTable table = new TimeSeriesTable(1, 1, index);
        List<TimeSeries<?, ?>> list = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            TimeSeries<?, ?> ts;
            String name = "ts" + String.format("%0" + padLeftCount + "d", i);
            if (i < threadCount / 2) {
                TimeSeriesMetadata metadata = new TimeSeriesMetadata(name, TimeSeriesDataType.DOUBLE, index);
                ts = new StoredDoubleTimeSeries(metadata);
            } else {
                TimeSeriesMetadata metadata = new TimeSeriesMetadata(name, TimeSeriesDataType.STRING, index);
                ts = new StringTimeSeries(metadata);
            }
            list.add(ts);
        }
        table.load(1, list); // init time series without data in the table

        CountDownLatch cdl = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) { // load data concurrently
            final int ii = i;
            new Thread() {
                public void run() {
                    try {
                        TimeSeries<?, ?> ts;
                        String name = "ts" + String.format("%0" + padLeftCount + "d", ii);
                        if (ii < threadCount / 2) {
                            TimeSeriesMetadata metadata = new TimeSeriesMetadata(name, TimeSeriesDataType.DOUBLE, index);
                            double[] values = new double[timeSeriesLength];
                            for (int j = 0; j < timeSeriesLength; j++) {
                                values[j] = j + ii;
                            }
                            ts = new StoredDoubleTimeSeries(metadata, new UncompressedDoubleDataChunk(0, values));
                        } else {
                            TimeSeriesMetadata metadata = new TimeSeriesMetadata(name, TimeSeriesDataType.DOUBLE, index);
                            String[] values = new String[timeSeriesLength];
                            for (int j = 0; j < timeSeriesLength; j++) {
                                values[j] = Integer.toString(j + ii);
                            }
                            ts = new StringTimeSeries(metadata, new UncompressedStringDataChunk(0, values));
                        }
                        table.load(1, List.of(ts));
                    } finally {
                        cdl.countDown();
                    }
                }
            }.start();
        }
        cdl.await();
        String csvString = table.toCsvString();
        List<List<String>> actual = new BufferedReader(new StringReader(csvString))
            .lines()
            .skip(1) // header
            .map(l ->
                Stream.of(l.split(";"))
                .skip(2) // date;version
                .collect(Collectors.toList()))
            .toList();
        List<List<String>> expected = new ArrayList<>(timeSeriesLength);
        for (int j = 0; j < timeSeriesLength; j++) {
            List<String> line = new ArrayList<>(threadCount);
            for (int i = 0; i < threadCount; i++) {
                String expectedValue;
                if (i < threadCount / 2) {
                    expectedValue = Double.toString(j + i);
                } else {
                    expectedValue = Integer.toString(j + i);
                }
                line.add(expectedValue);
            }
            expected.add(line);
        }
        assertEquals(expected.size(), actual.size(), "Number of lines");
        for (int i = 0; i < actual.size(); i++) {
            assertEquals(expected.get(i), actual.get(i), "Line " + i);
        }
    }

    @Test
    void testVersionError() {
        TimeSeriesIndex index = new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(3), Duration.ofMillis(1));
        // load time series in the table
        TimeSeriesException e = assertThrows(TimeSeriesException.class, () -> new TimeSeriesTable(1, 0, index));
        assertTrue(e.getMessage().contains("toVersion (0) is expected to be greater than fromVersion (1)"));
    }
}
