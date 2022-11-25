/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Sets;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.timeseries.ast.BinaryOperation;
import com.powsybl.timeseries.ast.IntegerNodeCalc;
import com.powsybl.timeseries.ast.TimeSeriesNameNodeCalc;
import com.powsybl.timeseries.json.TimeSeriesJsonModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CalculatedTimeSeriesTest {

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    private CalculatedTimeSeries timeSeries;

    @Before
    public void setUp() {
        timeSeries = new CalculatedTimeSeries("ts1", new IntegerNodeCalc(1));
    }

    @Test
    public void errorTest() {
        exceptions.expect(TimeSeriesException.class);
        exceptions.expectMessage("Impossible to fill buffer because calculated time series has not been synchronized on a finite time index");
        timeSeries.toArray();
    }

    @Test
    public void syncTest() {
        timeSeries.synchronize(RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-07-20T00:00:00Z"), Duration.ofDays(100)));
        assertArrayEquals(new double[] {1d, 1d, 1d}, timeSeries.toArray(), 0d);
    }

    @Test
    public void splitSmallChunkTest() {
        timeSeries.synchronize(new RegularTimeSeriesIndex(0, 99, 1));
        List<List<DoubleTimeSeries>> list = TimeSeries.split(Arrays.asList(timeSeries), 50);
        assertEquals(2, list.size());
    }

    @Test
    public void splitBigChunkTest() {
        timeSeries.synchronize(new RegularTimeSeriesIndex(0, 99, 1));
        List<List<DoubleTimeSeries>> list = TimeSeries.split(Arrays.asList(timeSeries), 2);
        assertEquals(50, list.size());
    }

    @Test
    public void jsonTest() throws IOException {
        TimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-07-20T00:00:00Z"), Duration.ofDays(200));
        DoubleTimeSeries ts = TimeSeries.createDouble("ts", index, 1d, 2d);
        TimeSeriesNameResolver resolver = new TimeSeriesNameResolver() {

            @Override
            public List<TimeSeriesMetadata> getTimeSeriesMetadata(Set<String> timeSeriesNames) {
                List<TimeSeriesMetadata> metadataList = new ArrayList<>(1);
                if (timeSeriesNames.contains("ts")) {
                    metadataList.add(ts.getMetadata());
                }
                return metadataList;
            }

            @Override
            public Set<Integer> getTimeSeriesDataVersions(String timeSeriesName) {
                return Collections.singleton(1);
            }

            @Override
            public List<DoubleTimeSeries> getDoubleTimeSeries(Set<String> timeSeriesNames) {
                List<DoubleTimeSeries> timeSeriesList = new ArrayList<>(1);
                if (timeSeriesNames.contains("ts")) {
                    timeSeriesList.add(ts);
                }
                return timeSeriesList;
            }
        };
        CalculatedTimeSeries tsCalc = new CalculatedTimeSeries("ts_calc", BinaryOperation.plus(new TimeSeriesNameNodeCalc("ts"),
                                                                                               new IntegerNodeCalc(1)));

        // check versions of the data available for the calculated time series
        tsCalc.setTimeSeriesNameResolver(resolver);
        assertEquals(Sets.newHashSet(1), tsCalc.getVersions());

        List<DoubleTimeSeries> tsLs = Arrays.asList(ts, tsCalc);
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
                "} ]");
        assertEquals(jsonRef, json);

        List<TimeSeries> timeSeriesList = TimeSeries.parseJson(json);
        for (TimeSeries timeSeries : timeSeriesList) {
            timeSeries.setTimeSeriesNameResolver(resolver);
        }

        assertEquals(2, timeSeriesList.size());
        assertTrue(timeSeriesList.get(0) instanceof StoredDoubleTimeSeries);
        assertTrue(timeSeriesList.get(1) instanceof CalculatedTimeSeries);
        assertArrayEquals(new double[] {1d, 2d}, ((DoubleTimeSeries) timeSeriesList.get(0)).toArray(), 0d);
        assertArrayEquals(new double[] {2d, 3d}, ((DoubleTimeSeries) timeSeriesList.get(1)).toArray(), 0d);

        // automatic jackson serialization
        ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                .registerModule(new TimeSeriesJsonModule());
        List<TimeSeries> tsLs2 = objectMapper.readValue(objectMapper.writeValueAsString(tsLs),
                                                        TypeFactory.defaultInstance().constructCollectionType(List.class, TimeSeries.class));
        assertEquals(tsLs, tsLs2);
    }
}
