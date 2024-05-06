/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Sets;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.timeseries.ast.*;
import com.powsybl.timeseries.json.TimeSeriesJsonModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class CalculatedTimeSeriesTest {

    private CalculatedTimeSeries timeSeries;

    @BeforeEach
    void setUp() {
        timeSeries = new CalculatedTimeSeries("ts1", new IntegerNodeCalc(1));
    }

    @Test
    void errorTest() {
        TimeSeriesException e = assertThrows(TimeSeriesException.class, () -> timeSeries.toArray());
        assertTrue(e.getMessage().contains("Impossible to fill buffer because calculated time series has not been synchronized on a finite time index"));
    }

    @Test
    void syncTest() {
        timeSeries.synchronize(RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-07-20T00:00:00Z"), Duration.ofDays(100)));
        assertArrayEquals(new double[] {1d, 1d, 1d}, timeSeries.toArray(), 0d);
    }

    @Test
    void splitSmallChunkTest() {
        timeSeries.synchronize(new RegularTimeSeriesIndex(0, 99, 1));
        List<List<DoubleTimeSeries>> list = TimeSeries.split(Collections.singletonList(timeSeries), 50);
        assertEquals(2, list.size());
    }

    @Test
    void splitBigChunkTest() {
        timeSeries.synchronize(new RegularTimeSeriesIndex(0, 99, 1));
        List<List<DoubleTimeSeries>> list = TimeSeries.split(Collections.singletonList(timeSeries), 2);
        assertEquals(50, list.size());
    }

    @Test
    void jsonTest() throws IOException {
        TimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-07-20T00:00:00Z"), Duration.ofDays(200));
        DoubleTimeSeries ts = TimeSeries.createDouble("ts", index, 1d, 2d);
        DoubleTimeSeries foo = TimeSeries.createDouble("foo", index, 0d, 3d);
        TimeSeriesNameResolver resolver = new TimeSeriesNameResolver() {

            @Override
            public List<TimeSeriesMetadata> getTimeSeriesMetadata(Set<String> timeSeriesNames) {
                List<TimeSeriesMetadata> metadataList = new ArrayList<>(2);
                if (timeSeriesNames.contains("ts")) {
                    metadataList.add(ts.getMetadata());
                }
                if (timeSeriesNames.contains("foo")) {
                    metadataList.add(foo.getMetadata());
                }
                return metadataList;
            }

            @Override
            public Set<Integer> getTimeSeriesDataVersions(String timeSeriesName) {
                return Collections.singleton(1);
            }

            @Override
            public List<DoubleTimeSeries> getDoubleTimeSeries(Set<String> timeSeriesNames) {
                List<DoubleTimeSeries> timeSeriesList = new ArrayList<>(2);
                if (timeSeriesNames.contains("ts")) {
                    timeSeriesList.add(ts);
                }
                if (timeSeriesNames.contains("foo")) {
                    timeSeriesList.add(foo);
                }
                return timeSeriesList;
            }
        };
        CalculatedTimeSeries tsCalc = new CalculatedTimeSeries("ts_calc", BinaryOperation.plus(
            new TimeSeriesNameNodeCalc("ts"),
            new IntegerNodeCalc(1)));
        CalculatedTimeSeries tsCalcMin = new CalculatedTimeSeries("ts_calc_min", new BinaryMinCalc(
            new TimeSeriesNameNodeCalc("ts"),
            new TimeSeriesNameNodeCalc("foo")));
        CalculatedTimeSeries tsCalcMax = new CalculatedTimeSeries("ts_calc_max", new BinaryMaxCalc(
            new TimeSeriesNameNodeCalc("ts"),
            new TimeSeriesNameNodeCalc("foo")));

        // check versions of the data available for the calculated time series
        tsCalc.setTimeSeriesNameResolver(resolver);
        tsCalcMin.setTimeSeriesNameResolver(resolver);
        tsCalcMax.setTimeSeriesNameResolver(resolver);
        assertEquals(Sets.newHashSet(1), tsCalc.getVersions());

        List<DoubleTimeSeries> tsLs = Arrays.asList(ts, tsCalc, tsCalcMin, tsCalcMax);
        String json = TimeSeries.toJson(tsLs);
        String jsonRef = String.join(System.lineSeparator(),
                "[ {",
                "  \"metadata\" : {",
                "    \"name\" : \"ts\",",
                "    \"dataType\" : \"DOUBLE\",",
                "    \"tags\" : [ ],",
                "    \"regularIndex\" : {",
                "      \"startTime\" : 1420070400000,",
                "      \"endTime\" : 1437350400000,",
                "      \"spacing\" : 17280000000",
                "    }",
                "  },",
                "  \"chunks\" : [ {",
                "    \"offset\" : 0,",
                "    \"values\" : [ 1.0, 2.0 ]",
                "  } ]",
                "}, {",
                "  \"name\" : \"ts_calc\",",
                "  \"expr\" : {",
                "    \"binaryOp\" : {",
                "      \"op\" : \"PLUS\",",
                "      \"timeSeriesName\" : \"ts\",",
                "      \"integer\" : 1",
                "    }",
                "  }",
                "}, {",
                "  \"name\" : \"ts_calc_min\",",
                "  \"expr\" : {",
                "    \"binaryMin\" : {",
                "      \"timeSeriesName\" : \"ts\",",
                "      \"timeSeriesName\" : \"foo\"",
                "    }",
                "  }",
                "}, {",
                "  \"name\" : \"ts_calc_max\",",
                "  \"expr\" : {",
                "    \"binaryMax\" : {",
                "      \"timeSeriesName\" : \"ts\",",
                "      \"timeSeriesName\" : \"foo\"",
                "    }",
                "  }",
                "} ]");
        assertEquals(jsonRef, json);

        List<TimeSeries> timeSeriesList = TimeSeries.parseJson(json);
        for (TimeSeries timeSeries : timeSeriesList) {
            timeSeries.setTimeSeriesNameResolver(resolver);
        }

        assertEquals(4, timeSeriesList.size());
        assertInstanceOf(StoredDoubleTimeSeries.class, timeSeriesList.get(0));
        assertInstanceOf(CalculatedTimeSeries.class, timeSeriesList.get(1));
        assertInstanceOf(CalculatedTimeSeries.class, timeSeriesList.get(2));
        assertInstanceOf(CalculatedTimeSeries.class, timeSeriesList.get(3));
        assertArrayEquals(new double[] {1d, 2d}, ((DoubleTimeSeries) timeSeriesList.get(0)).toArray(), 0d);
        assertArrayEquals(new double[] {2d, 3d}, ((DoubleTimeSeries) timeSeriesList.get(1)).toArray(), 0d);
        assertArrayEquals(new double[] {0d, 2d}, ((DoubleTimeSeries) timeSeriesList.get(2)).toArray(), 0d);
        assertArrayEquals(new double[] {1d, 3d}, ((DoubleTimeSeries) timeSeriesList.get(3)).toArray(), 0d);

        // automatic jackson serialization
        ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                .registerModule(new TimeSeriesJsonModule());
        List<TimeSeries> tsLs2 = objectMapper.readValue(objectMapper.writeValueAsString(tsLs),
                                                        TypeFactory.defaultInstance().constructCollectionType(List.class, TimeSeries.class));
        assertEquals(tsLs, tsLs2);
    }

    @Test
    void jsonErrorBinaryMinMaxTests() {

        // Initialisation
        TimeSeriesException e0;

        // Both parameters (left, right) are missing
        final String jsonNoParam = String.join(System.lineSeparator(),
            "[ {",
            "  \"metadata\" : {",
            "    \"name\" : \"ts\",",
            "    \"dataType\" : \"DOUBLE\",",
            "    \"tags\" : [ ],",
            "    \"regularIndex\" : {",
            "      \"startTime\" : 1420070400000,",
            "      \"endTime\" : 1437350400000,",
            "      \"spacing\" : 17280000000",
            "    }",
            "  },",
            "  \"chunks\" : [ {",
            "    \"offset\" : 0,",
            "    \"values\" : [ 1.0, 2.0 ]",
            "  } ]",
            "}, {",
            "  \"name\" : \"ts_calc_min\",",
            "  \"expr\" : {",
            "    \"binaryMin\" : {",
            "    }",
            "  }",
            "} ]");
        e0 = assertThrows(TimeSeriesException.class, () -> TimeSeries.parseJson(jsonNoParam));
        assertEquals("Invalid binary min/max node calc JSON", e0.getMessage());

        // One parameter is missing
        final String jsonOneParam = String.join(System.lineSeparator(),
            "[ {",
            "  \"metadata\" : {",
            "    \"name\" : \"ts\",",
            "    \"dataType\" : \"DOUBLE\",",
            "    \"tags\" : [ ],",
            "    \"regularIndex\" : {",
            "      \"startTime\" : 1420070400000,",
            "      \"endTime\" : 1437350400000,",
            "      \"spacing\" : 17280000000",
            "    }",
            "  },",
            "  \"chunks\" : [ {",
            "    \"offset\" : 0,",
            "    \"values\" : [ 1.0, 2.0 ]",
            "  } ]",
            "}, {",
            "  \"name\" : \"ts_calc_min\",",
            "  \"expr\" : {",
            "    \"binaryMin\" : {",
            "      \"timeSeriesName\" : \"foo\"",
            "    }",
            "  }",
            "} ]");
        e0 = assertThrows(TimeSeriesException.class, () -> TimeSeries.parseJson(jsonOneParam));
        assertEquals("Invalid binary min/max node calc JSON", e0.getMessage());

        // One parameter is missing
        final String jsonThreeParam = String.join(System.lineSeparator(),
            "[ {",
            "  \"metadata\" : {",
            "    \"name\" : \"ts\",",
            "    \"dataType\" : \"DOUBLE\",",
            "    \"tags\" : [ ],",
            "    \"regularIndex\" : {",
            "      \"startTime\" : 1420070400000,",
            "      \"endTime\" : 1437350400000,",
            "      \"spacing\" : 17280000000",
            "    }",
            "  },",
            "  \"chunks\" : [ {",
            "    \"offset\" : 0,",
            "    \"values\" : [ 1.0, 2.0 ]",
            "  } ]",
            "}, {",
            "  \"name\" : \"ts_calc_min\",",
            "  \"expr\" : {",
            "    \"binaryMin\" : {",
            "      \"timeSeriesName\" : \"ts\",",
            "      \"timeSeriesName\" : \"foo\",",
            "      \"timeSeriesName\" : \"bar\"",
            "    }",
            "  }",
            "} ]");
        e0 = assertThrows(TimeSeriesException.class, () -> TimeSeries.parseJson(jsonThreeParam));
        assertEquals("2 operands expected for a binary min/max comparison", e0.getMessage());

        // One parameter is missing
        final String jsonValueNull = String.join(System.lineSeparator(),
            "[ {",
            "  \"metadata\" : {",
            "    \"name\" : \"ts\",",
            "    \"dataType\" : \"DOUBLE\",",
            "    \"tags\" : [ ],",
            "    \"regularIndex\" : {",
            "      \"startTime\" : 1420070400000,",
            "      \"endTime\" : 1437350400000,",
            "      \"spacing\" : 17280000000",
            "    }",
            "  },",
            "  \"chunks\" : [ {",
            "    \"offset\" : 0,",
            "    \"values\" : [ 1.0, 2.0 ]",
            "  } ]",
            "}, {",
            "  \"name\" : \"ts_calc_min\",",
            "  \"expr\" : {",
            "    \"binaryMin\" : null",
            "  }",
            "} ]");
        e0 = assertThrows(TimeSeriesException.class, () -> TimeSeries.parseJson(jsonValueNull));
        assertEquals("Unexpected JSON token: VALUE_NULL", e0.getMessage());
    }

    @Test
    void jsonErrorMinMaxTests() {

        // Initialisation
        TimeSeriesException e0;

        // Both parameters (left, right) are missing
        final String jsonNoParam = String.join(System.lineSeparator(),
            "[ {",
            "  \"metadata\" : {",
            "    \"name\" : \"ts\",",
            "    \"dataType\" : \"DOUBLE\",",
            "    \"tags\" : [ ],",
            "    \"regularIndex\" : {",
            "      \"startTime\" : 1420070400000,",
            "      \"endTime\" : 1437350400000,",
            "      \"spacing\" : 17280000000",
            "    }",
            "  },",
            "  \"chunks\" : [ {",
            "    \"offset\" : 0,",
            "    \"values\" : [ 1.0, 2.0 ]",
            "  } ]",
            "}, {",
            "  \"name\" : \"ts_calc_min\",",
            "  \"expr\" : {",
            "    \"min\" : {",
            "    }",
            "  }",
            "} ]");
        e0 = assertThrows(TimeSeriesException.class, () -> TimeSeries.parseJson(jsonNoParam));
        assertEquals("Invalid min/max node calc JSON", e0.getMessage());

        // One parameter is missing
        final String jsonOneParam = String.join(System.lineSeparator(),
            "[ {",
            "  \"metadata\" : {",
            "    \"name\" : \"ts\",",
            "    \"dataType\" : \"DOUBLE\",",
            "    \"tags\" : [ ],",
            "    \"regularIndex\" : {",
            "      \"startTime\" : 1420070400000,",
            "      \"endTime\" : 1437350400000,",
            "      \"spacing\" : 17280000000",
            "    }",
            "  },",
            "  \"chunks\" : [ {",
            "    \"offset\" : 0,",
            "    \"values\" : [ 1.0, 2.0 ]",
            "  } ]",
            "}, {",
            "  \"name\" : \"ts_calc_min\",",
            "  \"expr\" : {",
            "    \"min\" : {",
            "      \"timeSeriesName\" : \"foo\"",
            "    }",
            "  }",
            "} ]");
        e0 = assertThrows(TimeSeriesException.class, () -> TimeSeries.parseJson(jsonOneParam));
        assertEquals("Invalid min/max node calc JSON", e0.getMessage());

        // One parameter is missing
        final String jsonThreeParam = String.join(System.lineSeparator(),
            "[ {",
            "  \"metadata\" : {",
            "    \"name\" : \"ts\",",
            "    \"dataType\" : \"DOUBLE\",",
            "    \"tags\" : [ ],",
            "    \"regularIndex\" : {",
            "      \"startTime\" : 1420070400000,",
            "      \"endTime\" : 1437350400000,",
            "      \"spacing\" : 17280000000",
            "    }",
            "  },",
            "  \"chunks\" : [ {",
            "    \"offset\" : 0,",
            "    \"values\" : [ 1.0, 2.0 ]",
            "  } ]",
            "}, {",
            "  \"name\" : \"ts_calc_min\",",
            "  \"expr\" : {",
            "    \"min\" : {",
            "      \"timeSeriesName\" : \"ts\",",
            "      \"integer\" : 1,",
            "      \"timeSeriesName\" : \"bar\"",
            "    }",
            "  }",
            "} ]");
        e0 = assertThrows(TimeSeriesException.class, () -> TimeSeries.parseJson(jsonThreeParam));
        assertEquals("Only 1 operand expected for a min/max", e0.getMessage());

        // One parameter is missing
        final String jsonValueNull = String.join(System.lineSeparator(),
            "[ {",
            "  \"metadata\" : {",
            "    \"name\" : \"ts\",",
            "    \"dataType\" : \"DOUBLE\",",
            "    \"tags\" : [ ],",
            "    \"regularIndex\" : {",
            "      \"startTime\" : 1420070400000,",
            "      \"endTime\" : 1437350400000,",
            "      \"spacing\" : 17280000000",
            "    }",
            "  },",
            "  \"chunks\" : [ {",
            "    \"offset\" : 0,",
            "    \"values\" : [ 1.0, 2.0 ]",
            "  } ]",
            "}, {",
            "  \"name\" : \"ts_calc_min\",",
            "  \"expr\" : {",
            "    \"min\" : null",
            "  }",
            "} ]");
        e0 = assertThrows(TimeSeriesException.class, () -> TimeSeries.parseJson(jsonValueNull));
        assertEquals("Unexpected JSON token: VALUE_NULL", e0.getMessage());
    }
}
