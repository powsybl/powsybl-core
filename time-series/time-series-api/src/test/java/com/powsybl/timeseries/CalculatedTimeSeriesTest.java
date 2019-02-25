/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.timeseries.ast.*;
import com.powsybl.timeseries.json.TimeSeriesJsonModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
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

    private void evaluate(String expr, double expectedValue) {
        // create time series space mock
        TimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-07-20T00:00:00Z"), Duration.ofDays(200));

        String[] timeSeriesNames = {"foo", "bar", "baz"};
        double[] fooValues = new double[] {3d, 3d};
        double[] barValues = new double[] {2d, 2d};
        double[] bazValues = new double[] {-1d, -1d};

        ReadOnlyTimeSeriesStore store = new ReadOnlyTimeSeriesStoreCache(
                TimeSeries.createDouble(timeSeriesNames[0], index, fooValues),
                TimeSeries.createDouble(timeSeriesNames[1], index, barValues),
                TimeSeries.createDouble(timeSeriesNames[2], index, bazValues)
        );

        // evaluate calculated time series
        String name = "test";
        String script = "timeSeries['" + name + "'] = " + expr + System.lineSeparator();
        Map<String, NodeCalc> nodes = new CalculatedTimeSeriesDslLoader(script).load(store);

        // assertions
        NodeCalc node = nodes.get(name);
        assertNotNull(node);
        NodeCalc resolvedNode = NodeCalcResolver.resolve(node, ImmutableMap.of(timeSeriesNames[0], 0,
                                                                               timeSeriesNames[1], 1,
                                                                               timeSeriesNames[2], 2));
        int point = 0;
        double calculatedValue = NodeCalcEvaluator.eval(resolvedNode, new DoubleMultiPoint() {
            @Override
            public int getIndex() {
                return point;
            }

            @Override
            public long getTime() {
                return index.getTimeAt(point);
            }

            @Override
            public double getValue(int timeSeriesNum) {
                switch (timeSeriesNum) {
                    case 0:
                        return fooValues[point];
                    case 1:
                        return barValues[point];
                    case 2:
                        return bazValues[point];
                    default:
                        throw new AssertionError();
                }
            }
        });
        assertEquals(expectedValue, calculatedValue, 0d);
    }

    @Test
    public void evalTest() {
        evaluate("1", 1);
        evaluate("1f", 1f);
        evaluate("1d", 1d);
        evaluate("1f + timeSeries['foo']", 4);
        evaluate("1 + timeSeries['foo']", 4);
        evaluate("1.1 + timeSeries['foo']", 4.1);
        evaluate("1d + timeSeries['foo']", 4);
        evaluate("timeSeries['foo'] + 1f", 4);
        evaluate("timeSeries['foo'] + 1", 4);
        evaluate("timeSeries['foo'] + 1.1", 4.1);
        evaluate("timeSeries['foo'] + 1d", 4);
        evaluate("timeSeries['foo'] + timeSeries['bar']", 5f);
        evaluate("1f - timeSeries['foo']", -2);
        evaluate("1 - timeSeries['foo']", -2);
        evaluate("1.1 - timeSeries['foo']", -1.9);
        evaluate("1d - timeSeries['foo']", -2);
        evaluate("timeSeries['foo'] - 1f", 2);
        evaluate("timeSeries['foo'] - 1", 2);
        evaluate("timeSeries['foo'] - 1.1", 1.9);
        evaluate("timeSeries['foo'] - 1d", 2);
        evaluate("timeSeries['foo'] - timeSeries['bar']", 1);
        evaluate("1f * timeSeries['foo']", 3);
        evaluate("1 * timeSeries['foo']", 3);
        evaluate("1d * timeSeries['foo']", 3);
        evaluate("1.1 * timeSeries['foo']", 3.3000000000000003);
        evaluate("timeSeries['foo'] * 1f", 3);
        evaluate("timeSeries['foo'] * 1", 3);
        evaluate("timeSeries['foo'] * 1.1", 3.3000000000000003);
        evaluate("timeSeries['foo'] * 1d", 3);
        evaluate("timeSeries['foo'] * timeSeries['bar']", 6);
        evaluate("timeSeries['foo'] / 2f", 1.5);
        evaluate("timeSeries['foo'] / 2", 1.5);
        evaluate("timeSeries['foo'] / 2.1", 1.4285714285714286);
        evaluate("timeSeries['foo'] / 2d", 1.5);
        evaluate("2f / timeSeries['foo']", 0.6666666666666666);
        evaluate("2 / timeSeries['foo']", 0.6666666666666666);
        evaluate("2.1 / timeSeries['foo']", 0.7000000000000001);
        evaluate("2d / timeSeries['foo']", 0.6666666666666666);
        evaluate("timeSeries['foo'] / timeSeries['bar']", 1.5);
        evaluate("(-1f).abs()", 1f);
        evaluate("timeSeries['baz'].abs()", 1f);
        evaluate("-(-2f)", 2f);
        evaluate("+(-2f)", -2f);
        evaluate("timeSeries['foo'].min(1)", 1);
        evaluate("timeSeries['foo'].min(5)", 3);
        evaluate("timeSeries['foo'].max(1)", 3);
        evaluate("timeSeries['foo'].max(5)", 5);
        evaluate("1 < timeSeries['foo']", 1);
        evaluate("1f < timeSeries['foo']", 1);
        evaluate("1.0 < timeSeries['foo']", 1);
        evaluate("timeSeries['foo'] > 1", 1);
        evaluate("timeSeries['foo'] > 1f", 1);
        evaluate("timeSeries['foo'] > 1.0", 1);
        evaluate("timeSeries['foo'] >= 1", 1);
        evaluate("timeSeries['foo'] >= 1f", 1);
        evaluate("timeSeries['foo'] >= 1.0", 1);
        evaluate("timeSeries['foo'] != 3", 0);
        evaluate("timeSeries['foo'] != 3f", 0);
        evaluate("timeSeries['foo'] != 3.0", 0);
        evaluate("timeSeries['foo'] == 3", 1);
        evaluate("timeSeries['foo'] == 3f", 1);
        evaluate("timeSeries['foo'] == 3.0", 1);
        evaluate("timeSeries['foo'] < 3", 0);
        evaluate("timeSeries['foo'] < 3f", 0);
        evaluate("timeSeries['foo'] < 3.0", 0);
        evaluate("timeSeries['foo'] <= 3", 1);
        evaluate("timeSeries['foo'] <= 3f", 1);
        evaluate("timeSeries['foo'] <= 3.0", 1);
        evaluate("timeSeries['foo'] < timeSeries['bar']", 0);
        evaluate("timeSeries['foo'] >= timeSeries['bar']", 1);
        evaluate("timeSeries['foo'].time()", ZonedDateTime.parse("2015-01-01T00:00:00Z").toInstant().toEpochMilli());
        evaluate("timeSeries['foo'].time() == time('2015-01-01T00:00:00Z')", 1);
        evaluate("timeSeries['foo'].time() <= time('2015-01-01T00:15:00Z')", 1);
        evaluate("timeSeries['foo'].time() < time('2014-01-01T00:00:00Z')", 0);
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
