/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.PowsyblCoreTestReportResourceBundle;
import com.powsybl.timeseries.TimeSeries.TimeFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.powsybl.timeseries.TimeSeries.writeInstantToString;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TimeSeriesTest {

    private void assertOnParsedTimeSeries(Map<Integer, List<TimeSeries>> timeSeriesPerVersion, Class<?> className) {
        assertEquals(2, timeSeriesPerVersion.size());
        assertEquals(2, timeSeriesPerVersion.get(1).size());
        assertEquals(2, timeSeriesPerVersion.get(2).size());

        TimeSeries<?, ?> ts1v1 = timeSeriesPerVersion.get(1).get(0);
        TimeSeries<?, ?> ts2v1 = timeSeriesPerVersion.get(1).get(1);
        TimeSeries<?, ?> ts1v2 = timeSeriesPerVersion.get(2).get(0);
        TimeSeries<?, ?> ts2v2 = timeSeriesPerVersion.get(2).get(1);

        assertEquals(className, ts1v1.getMetadata().getIndex().getClass());

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
    void testRegularTimeSeriesIndex() {
        String csv = """
                Time;Version;ts1;ts2
                1970-01-01T01:00:00.000+01:00;1;1.0;
                1970-01-01T02:00:00.000+01:00;1;;a
                1970-01-01T03:00:00.000+01:00;1;3.0;b
                1970-01-01T01:00:00.000+01:00;2;4.0;c
                1970-01-01T02:00:00.000+01:00;2;5.0;
                1970-01-01T03:00:00.000+01:00;2;6.0;d
                """.replaceAll("\n", System.lineSeparator());

        String csvWithQuotes = """
                "Time";"Version";"ts1";"ts2"
                "1970-01-01T01:00:00.000+01:00";"1";"1.0";
                "1970-01-01T02:00:00.000+01:00";"1";;"a"
                "1970-01-01T03:00:00.000+01:00";"1";"3.0";"b"
                "1970-01-01T01:00:00.000+01:00";"2";"4.0";"c"
                "1970-01-01T02:00:00.000+01:00";"2";"5.0";
                "1970-01-01T03:00:00.000+01:00";"2";"6.0";"d"
                """.replaceAll("\n", System.lineSeparator());

        String csvMicroseconds = """
            Time;Version;ts1;ts2
            1970-01-01T01:00:00.000000+01:00;1;1.0;
            1970-01-01T02:00:00.000000+01:00;1;;a
            1970-01-01T03:00:00.000000+01:00;1;3.0;b
            1970-01-01T01:00:00.000000+01:00;2;4.0;c
            1970-01-01T02:00:00.000000+01:00;2;5.0;
            1970-01-01T03:00:00.000000+01:00;2;6.0;d
            """.replaceAll("\n", System.lineSeparator());

        String csvNanoseconds = """
            Time;Version;ts1;ts2
            1970-01-01T01:00:00.000000000+01:00;1;1.0;
            1970-01-01T02:00:00.000000000+01:00;1;;a
            1970-01-01T03:00:00.000000000+01:00;1;3.0;b
            1970-01-01T01:00:00.000000000+01:00;2;4.0;c
            1970-01-01T02:00:00.000000000+01:00;2;5.0;
            1970-01-01T03:00:00.000000000+01:00;2;6.0;d
            """.replaceAll("\n", System.lineSeparator());

        Arrays.asList(csv, csvWithQuotes, csvMicroseconds, csvNanoseconds).forEach(data -> {
            Map<Integer, List<TimeSeries>> timeSeriesPerVersion = TimeSeries.parseCsv(data);

            assertOnParsedTimeSeries(timeSeriesPerVersion, RegularTimeSeriesIndex.class);
        });
    }

    @Test
    void testIrregularTimeSeriesIndex() {
        String csv = """
                Time;Version;ts1;ts2
                1970-01-01T01:00:00.000+01:00;1;1.0;
                1970-01-01T02:00:00.000+01:00;1;;a
                1970-01-01T04:00:00.000+01:00;1;3.0;b
                1970-01-01T01:00:00.000+01:00;2;4.0;c
                1970-01-01T02:00:00.000+01:00;2;5.0;
                1970-01-01T04:00:00.000+01:00;2;6.0;d
                """.replaceAll("\n", System.lineSeparator());

        Map<Integer, List<TimeSeries>> timeSeriesPerVersion = TimeSeries.parseCsv(csv);

        assertOnParsedTimeSeries(timeSeriesPerVersion, IrregularTimeSeriesIndex.class);
    }

    @Test
    void testTimeSeriesNameMissing() {
        String csv = """
            Time;Version;;ts2
            1970-01-01T01:00:00.000+01:00;1;1.0;
            1970-01-01T02:00:00.000+01:00;1;;a
            1970-01-01T04:00:00.000+01:00;1;3.0;b
            1970-01-01T01:00:00.000+01:00;2;4.0;c
            1970-01-01T02:00:00.000+01:00;2;5.0;
            1970-01-01T04:00:00.000+01:00;2;6.0;d
            """.replaceAll("\n", System.lineSeparator());

        Map<Integer, List<TimeSeries>> timeSeriesPerVersion = TimeSeries.parseCsv(csv);

        // Since the name if the first timeseries is missing, only the second is saved
        assertEquals(2, timeSeriesPerVersion.size());
        assertEquals(1, timeSeriesPerVersion.get(1).size());
        assertEquals(1, timeSeriesPerVersion.get(2).size());
    }

    @Test
    void testFractionsOfSecondsRegularTimeSeriesIndex() {
        String csv = """
                Time;Version;ts1;ts2
                0.000;1;1.0;
                0.001;1;;a
                0.002;1;3.0;b
                0.000;2;4.0;c
                0.001;2;5.0;
                0.002;2;6.0;d
                """.replaceAll("\n", System.lineSeparator());

        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(';', true, TimeFormat.FRACTIONS_OF_SECOND, true);
        Map<Integer, List<TimeSeries>> timeSeriesPerVersion = TimeSeries.parseCsv(csv, timeSeriesCsvConfig);

        assertOnParsedTimeSeries(timeSeriesPerVersion, RegularTimeSeriesIndex.class);
    }

    @Test
    void testFractionsOfSecondsRegularTimeSeriesIndexWithDuplicateTime() {
        String csv = """
                Time;Version;ts1;ts2
                0.000000000;1;1.0;
                0.000000001;1;;a
                0.0000000012;1;3.0;b
                0.000000000;2;4.0;c
                0.000000001;2;5.0;
                0.0000000012;2;6.0;d
                """.replaceAll("\n", System.lineSeparator());

        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(';', true, TimeFormat.FRACTIONS_OF_SECOND, true);
        Map<Integer, List<TimeSeries>> timeSeriesPerVersion = TimeSeries.parseCsv(csv, timeSeriesCsvConfig);

        assertOnParsedTimeSeries(timeSeriesPerVersion, IrregularTimeSeriesIndex.class);
    }

    @Test
    void testFractionsOfSecondsRegularTimeSeriesIndexWithSkippedDuplicateTime() {
        String csv = """
                Time;Version;ts1;ts2
                0.000000000;1;1.0;
                0.000000001;1;;a
                0.0000000015;1;;b
                0.000000002;1;3.0;b
                0.000000000;2;4.0;c
                0.0000000002;2;4.5;c
                0.000000001;2;5.0;
                0.000000002;2;6.0;d
                """.replaceAll("\n", System.lineSeparator());

        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(';', true,
                TimeFormat.FRACTIONS_OF_SECOND, true, true);
        Map<Integer, List<TimeSeries>> timeSeriesPerVersion = TimeSeries.parseCsv(csv, timeSeriesCsvConfig);

        assertOnParsedTimeSeries(timeSeriesPerVersion, RegularTimeSeriesIndex.class);
    }

    @Test
    void testParseCsvBuffered() {
        String csv = """
                Time;Version;ts1;ts2
                0.000;1;1.0;
                0.001;1;;a
                0.002;1;3.0;b
                0.000;2;4.0;c
                0.001;2;5.0;
                0.002;2;6.0;d
                """.replaceAll("\n", System.lineSeparator());

        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(';', true, TimeFormat.FRACTIONS_OF_SECOND, true);
        try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
            Map<Integer, List<TimeSeries>> timeSeriesPerVersion = TimeSeries.parseCsv(reader, timeSeriesCsvConfig);
            assertOnParsedTimeSeries(timeSeriesPerVersion, RegularTimeSeriesIndex.class);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void testMillisIrregularTimeSeriesIndex() {
        String csv = """
                Time;Version;ts1;ts2
                0;1;1.0;
                1;1;;a
                4;1;3.0;b
                0;2;4.0;c
                1;2;5.0;
                4;2;6.0;d
                """.replaceAll("\n", System.lineSeparator());

        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(';', true, TimeFormat.MILLIS);
        Map<Integer, List<TimeSeries>> timeSeriesPerVersion = TimeSeries.parseCsv(csv, timeSeriesCsvConfig);

        assertOnParsedTimeSeries(timeSeriesPerVersion, IrregularTimeSeriesIndex.class);
    }

    @Test
    void testMilliRegularTimeSeriesIndex() {
        String csv = """
            Time;Version;ts1;ts2
            1737377647003;1;1.0;
            1737377647004;1;;a
            1737377647005;1;3.0;b
            1737377647003;2;4.0;c
            1737377647004;2;5.0;
            1737377647005;2;6.0;d
            """.replaceAll("\n", System.lineSeparator());

        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(';', true, TimeFormat.MILLIS, true);
        Map<Integer, List<TimeSeries>> timeSeriesPerVersion = TimeSeries.parseCsv(csv, timeSeriesCsvConfig);

        assertOnParsedTimeSeries(timeSeriesPerVersion, RegularTimeSeriesIndex.class);
        RegularTimeSeriesIndex timeSeriesIndex = RegularTimeSeriesIndex.create(Instant.ofEpochMilli(1737377647003L),
            Instant.ofEpochMilli(1737377647005L),
            Duration.ofMillis(1));
        assertEquals(timeSeriesIndex, timeSeriesPerVersion.get(1).get(0).getMetadata().getIndex());
    }

    @Test
    void testMicroRegularTimeSeriesIndex() {
        String csv = """
            Time;Version;ts1;ts2
            1737377647000004;1;1.0;
            1737377647000005;1;;a
            1737377647000006;1;3.0;b
            1737377647000004;2;4.0;c
            1737377647000005;2;5.0;
            1737377647000006;2;6.0;d
            """.replaceAll("\n", System.lineSeparator());

        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(';', true, TimeFormat.MICROS, true);
        Map<Integer, List<TimeSeries>> timeSeriesPerVersion = TimeSeries.parseCsv(csv, timeSeriesCsvConfig);

        assertOnParsedTimeSeries(timeSeriesPerVersion, RegularTimeSeriesIndex.class);
        RegularTimeSeriesIndex timeSeriesIndex = RegularTimeSeriesIndex.create(Instant.ofEpochSecond(1737377647, 4000),
            Instant.ofEpochSecond(1737377647, 6000),
            Duration.ofNanos(1000));
        assertEquals(timeSeriesIndex, timeSeriesPerVersion.get(1).get(0).getMetadata().getIndex());
    }

    @Test
    void testNanoRegularTimeSeriesIndex() {
        String csv = """
            Time;Version;ts1;ts2
            1737377647000000001;1;1.0;
            1737377647000000002;1;;a
            1737377647000000003;1;3.0;b
            1737377647000000001;2;4.0;c
            1737377647000000002;2;5.0;
            1737377647000000003;2;6.0;d
            """.replaceAll("\n", System.lineSeparator());

        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(';', true, TimeFormat.NANOS, true);
        Map<Integer, List<TimeSeries>> timeSeriesPerVersion = TimeSeries.parseCsv(csv, timeSeriesCsvConfig);

        assertOnParsedTimeSeries(timeSeriesPerVersion, RegularTimeSeriesIndex.class);
        RegularTimeSeriesIndex timeSeriesIndex = RegularTimeSeriesIndex.create(Instant.ofEpochSecond(1737377647, 1),
            Instant.ofEpochSecond(1737377647, 3),
            Duration.ofNanos(1));
        assertEquals(timeSeriesIndex, timeSeriesPerVersion.get(1).get(0).getMetadata().getIndex());
    }

    @Test
    void testNoVersion() {
        String csv = """
            Time;ts1;ts2
            1970-01-01T01:00:00.000+01:00;1.0;
            1970-01-01T02:00:00.000+01:00;;a
            1970-01-01T03:00:00.000+01:00;3.0;b
            1970-01-01T04:00:00.000+01:00;4.0;c
            1970-01-01T05:00:00.000+01:00;5.0;
            1970-01-01T06:00:00.000+01:00;6.0;d
            """.replaceAll("\n", System.lineSeparator());

        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(';', false, TimeFormat.DATE_TIME);
        Map<Integer, List<TimeSeries>> timeSeriesPerVersion = TimeSeries.parseCsv(csv, timeSeriesCsvConfig);

        assertEquals(1, timeSeriesPerVersion.size());
        assertEquals(2, timeSeriesPerVersion.get(-1).size());

        TimeSeries ts1 = timeSeriesPerVersion.get(-1).get(0);
        TimeSeries ts2 = timeSeriesPerVersion.get(-1).get(1);

        assertEquals("ts1", ts1.getMetadata().getName());
        assertEquals(TimeSeriesDataType.DOUBLE, ts1.getMetadata().getDataType());
        assertArrayEquals(new double[] {1, Double.NaN, 3, 4, 5, 6}, ((DoubleTimeSeries) ts1).toArray(), 0);

        assertEquals("ts2", ts2.getMetadata().getName());
        assertEquals(TimeSeriesDataType.STRING, ts2.getMetadata().getDataType());
        assertArrayEquals(new String[] {null, "a", "b", "c", null, "d"}, ((StringTimeSeries) ts2).toArray());
    }

    @Test
    void testVersionedAtDefaultNumberNotStrictCSV() {
        String csv = """
            Time;Version;ts1;ts2
            0.000;-1;1.0;
            0.001;-1;;a
            0.002;-1;3.0;b
            0.000;2;4.0;c
            0.001;2;5.0;
            0.002;2;6.0;d
            """.replaceAll("\n", System.lineSeparator());

        // Reporter
        ReportNode reportNode = ReportNode.newRootReportNode(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestVersionedAtDefaultNumberNotStrictCSV")
                .build();

        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(ZoneId.of("UTC"), ';', true, TimeFormat.FRACTIONS_OF_SECOND, false);
        TimeSeries.parseCsv(csv, timeSeriesCsvConfig, reportNode);

        assertEquals(4, reportNode.getChildren().size());
        assertEquals("The version number for a versioned TimeSeries should not be equals to the default version number (-1) at line 0.000;-1;1.0;null",
            reportNode.getChildren().get(0).getMessage());
        assertEquals("The version number for a versioned TimeSeries should not be equals to the default version number (-1) at line 0.001;-1;null;a",
            reportNode.getChildren().get(1).getMessage());
        assertEquals("The version number for a versioned TimeSeries should not be equals to the default version number (-1) at line 0.002;-1;3.0;b",
            reportNode.getChildren().get(2).getMessage());
        assertTrue(Pattern.compile("4 time series loaded from CSV in .* ms").matcher(reportNode.getChildren().get(3).getMessage()).find());
    }

    @Test
    void testVersionedAtDefaultNumberStrictCSV() {
        String csv = """
                Time;Version;ts1;ts2
                0.000;-1;1.0;
                0.001;-1;;a
                0.002;-1;3.0;b
                0.000;2;4.0;c
                0.001;2;5.0;
                0.002;2;6.0;d
                """.replaceAll("\n", System.lineSeparator());

        // Reporter
        ReportNode reportNode = ReportNode.newRootReportNode(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestVersionedAtDefaultNumberNotStrictCSV")
                .build();

        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(ZoneId.of("UTC"), ';', true, TimeFormat.FRACTIONS_OF_SECOND, true);
        TimeSeriesException timeSeriesException = assertThrows(TimeSeriesException.class, () -> TimeSeries.parseCsv(csv, timeSeriesCsvConfig, reportNode));
        assertEquals("The version number for a versioned TimeSeries cannot be equals to the default version number (-1) at line \"0.000;-1;1.0;null\"",
            timeSeriesException.getMessage());
    }

    @Test
    void testImportByPath() throws URISyntaxException {
        Path path = Paths.get(Objects.requireNonNull(getClass().getResource("/timeseries.csv")).toURI());

        // Default case
        Map<Integer, List<TimeSeries>> timeSeriesPerVersion = TimeSeries.parseCsv(path);
        assertOnParsedTimeSeries(timeSeriesPerVersion, RegularTimeSeriesIndex.class);

        // Case with specific timeSeriesCsvConfig
        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(';', true, TimeFormat.DATE_TIME, true);
        timeSeriesPerVersion = TimeSeries.parseCsv(path, timeSeriesCsvConfig);
        assertOnParsedTimeSeries(timeSeriesPerVersion, RegularTimeSeriesIndex.class);
    }

    @Test
    void testErrors() {
        TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(';', false, TimeFormat.DATE_TIME);

        String emptyCsv = "";
        assertThatCode(() -> TimeSeries.parseCsv(emptyCsv)).hasMessage("CSV header is missing").isInstanceOf(TimeSeriesException.class);

        String badHeaderNoTime = """
            NoTime;ts1;ts2
            1970-01-01T01:00:00.000+01:00;1.0;
            1970-01-01T02:00:00.000+01:00;;a
            1970-01-01T03:00:00.000+01:00;3.0;b
            1970-01-01T04:00:00.000+01:00;4.0;c
            1970-01-01T05:00:00.000+01:00;5.0;
            1970-01-01T06:00:00.000+01:00;6.0;d
            """.replaceAll("\n", System.lineSeparator());
        assertThatCode(() -> TimeSeries.parseCsv(badHeaderNoTime, timeSeriesCsvConfig)).hasMessage("Bad CSV header, should be \ntime;...").isInstanceOf(TimeSeriesException.class);

        String badHeaderNoVersion = """
            Time;NoVersion;ts1;ts2
            1970-01-01T01:00:00.000+01:00;1;1.0;
            1970-01-01T02:00:00.000+01:00;1;;a
            1970-01-01T03:00:00.000+01:00;1;3.0;b
            1970-01-01T01:00:00.000+01:00;2;4.0;c
            1970-01-01T02:00:00.000+01:00;2;5.0;
            1970-01-01T03:00:00.000+01:00;2;6.0;d
            """.replaceAll("\n", System.lineSeparator());
        assertThatCode(() -> TimeSeries.parseCsv(badHeaderNoVersion)).hasMessage("Bad CSV header, should be \ntime;version;...").isInstanceOf(TimeSeriesException.class);

        String duplicates = """
            Time;Version;ts1;ts1
            1970-01-01T01:00:00.000+01:00;1;1.0;
            1970-01-01T02:00:00.000+01:00;1;;a
            1970-01-01T03:00:00.000+01:00;1;3.0;b
            1970-01-01T01:00:00.000+01:00;2;4.0;c
            1970-01-01T02:00:00.000+01:00;2;5.0;
            1970-01-01T03:00:00.000+01:00;2;6.0;d
            """.replaceAll("\n", System.lineSeparator());
        assertThatCode(() -> TimeSeries.parseCsv(duplicates)).hasMessageContaining("Bad CSV header, there are duplicates in time series names").isInstanceOf(TimeSeriesException.class);

        String noData = """
            Time;Version
            1970-01-01T01:00:00.000+01:00;1
            1970-01-01T02:00:00.000+01:00;1
            1970-01-01T03:00:00.000+01:00;1
            1970-01-01T01:00:00.000+01:00;2
            1970-01-01T02:00:00.000+01:00;2
            1970-01-01T03:00:00.000+01:00;2
            """.replaceAll("\n", System.lineSeparator());
        assertThatCode(() -> TimeSeries.parseCsv(noData)).hasMessageContaining("Bad CSV header, should be \ntime;version;...").isInstanceOf(TimeSeriesException.class);

        String onlyOneTime = """
            Time;ts1
            1970-01-01T03:00:00.000+01:00;2.0
            """.replaceAll("\n", System.lineSeparator());
        assertThatCode(() -> TimeSeries.parseCsv(onlyOneTime, timeSeriesCsvConfig)).hasMessageContaining("At least 2 rows are expected").isInstanceOf(TimeSeriesException.class);

        String unexpectedTokens = """
            Time;ts1;ts2
            1970-01-01T01:00:00.000+01:00;1.0;3.2
            1970-01-01T02:00:00.000+01:00;2.0
            1970-01-01T03:00:00.000+01:00;2.0;1.0
            """.replaceAll("\n", System.lineSeparator());
        assertThatCode(() -> TimeSeries.parseCsv(unexpectedTokens, timeSeriesCsvConfig)).hasMessageContaining("Columns of line 1 are inconsistent with header").isInstanceOf(TimeSeriesException.class);

        Path path = Path.of("wrongPath.csv");
        assertThrows(UncheckedIOException.class, () -> TimeSeries.parseCsv(path));
    }

    @Test
    void splitTest() {
        try {
            TimeSeries.split(Collections.<DoubleTimeSeries>emptyList(), 2);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        TimeSeriesIndex index = new RegularTimeSeriesIndex(Instant.ofEpochMilli(10000), Instant.ofEpochMilli(10002), Duration.ofMillis(1));
        List<DoubleTimeSeries> timeSeriesList = Arrays.asList(TimeSeries.createDouble("ts1", index, 1d, 2d, 3d),
                                                              TimeSeries.createDouble("ts1", index, 4d, 5d, 6d));
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

    private static Stream<Arguments> getArgumentsWriteInstantToString() {
        return Stream.of(
            Arguments.of(Instant.ofEpochSecond(123456, 7), "123456000000007", 9),
            Arguments.of(Instant.ofEpochSecond(123456, 7000), "123456000007000", 9),
            Arguments.of(Instant.ofEpochSecond(123456, 700000000), "123456700000000", 9),
            Arguments.of(Instant.ofEpochSecond(0, 7), "7", 9),
            Arguments.of(Instant.ofEpochSecond(0, 7000), "7000", 9),
            Arguments.of(Instant.ofEpochSecond(0, 700000000), "700000000", 9),
            Arguments.of(Instant.ofEpochSecond(123456, 7), "123456000000", 6),
            Arguments.of(Instant.ofEpochSecond(123456, 700), "123456000001", 6), // Case with rounding
            Arguments.of(Instant.ofEpochSecond(123456, 7000), "123456000007", 6),
            Arguments.of(Instant.ofEpochSecond(123456, 700000000), "123456700000", 6),
            Arguments.of(Instant.ofEpochSecond(0, 7), "0", 6),
            Arguments.of(Instant.ofEpochSecond(0, 700), "1", 6), // Case with rounding
            Arguments.of(Instant.ofEpochSecond(0, 7000), "7", 6),
            Arguments.of(Instant.ofEpochSecond(0, 700000000), "700000", 6),
            Arguments.of(Instant.ofEpochSecond(0, 999999999), "1000000", 6) // Rounding to the next second
        );
    }

    @ParameterizedTest
    @MethodSource("getArgumentsWriteInstantToString")
    void testWriteInstantToString(Instant instant, String expected, int precision) {
        assertEquals(expected, writeInstantToString(instant, precision));
    }
}
