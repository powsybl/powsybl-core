/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.dsl;

import com.google.common.collect.ImmutableMap;
import com.powsybl.timeseries.*;
import com.powsybl.timeseries.ast.NodeCalc;
import com.powsybl.timeseries.ast.NodeCalcEvaluator;
import com.powsybl.timeseries.ast.NodeCalcResolver;
import com.powsybl.timeseries.ast.NodeCalcVisitors;
import org.junit.jupiter.api.Test;
import org.threeten.extra.Interval;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class CalculatedTimeSeriesGroovyDslTest {

    private void evaluate1(String expr, double[] expectedValue) {
        // create time series space mock
        TimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-07-20T00:00:00Z"), Duration.ofDays(200));

        String[] timeSeriesNames = {"foo", "bar", "baz"};
        double[] fooValues = new double[] {3d, 3d};
        double[] barValues = new double[] {2d, 2d};
        double[] bazValues = new double[] {-1d, 4d};

        ReadOnlyTimeSeriesStore store = new ReadOnlyTimeSeriesStoreCache(
                TimeSeries.createDouble(timeSeriesNames[0], index, fooValues),
                TimeSeries.createDouble(timeSeriesNames[1], index, barValues),
                TimeSeries.createDouble(timeSeriesNames[2], index, bazValues)
        );

        // evaluate calculated time series
        String name = "test";
        String script = "timeSeries['" + name + "'] = " + expr + System.lineSeparator();
        Map<String, NodeCalc> nodes = CalculatedTimeSeriesDslLoader.find().load(script, store);

        // assertions
        NodeCalc node = nodes.get(name);
        assertNotNull(node);
        NodeCalc resolvedNode = NodeCalcResolver.resolve(node, ImmutableMap.of(timeSeriesNames[0], 0,
                timeSeriesNames[1], 1,
                timeSeriesNames[2], 2));

        // Compute the value for each point corresponding to the expected value
        double[] calculatedValue = new double[expectedValue.length];
        for (int point = 0; point < expectedValue.length; point++) {
            int finalPoint = point;
            calculatedValue[point] = NodeCalcEvaluator.eval(resolvedNode, new DoubleMultiPoint() {
                @Override
                public int getIndex() {
                    return finalPoint;
                }

                @Override
                public long getTime() {
                    return index.getTimeAt(finalPoint);
                }

                @Override
                public double getValue(int timeSeriesNum) {
                    return switch (timeSeriesNum) {
                        case 0 -> fooValues[finalPoint];
                        case 1 -> barValues[finalPoint];
                        case 2 -> bazValues[finalPoint];
                        default -> throw new IllegalStateException();
                    };
                }
            });
        }
        assertArrayEquals(expectedValue, calculatedValue, 0d);
    }

    //We want to test the expression directly as standalone tree, and in addition we want to test
    //this expression when evaluated as a child of a tree at depth around where
    //the traversal switches from recursive to iterative, so we construct a
    //dummy tree of (expr)+0+0...+0. Due to the way we parse things, this new
    //expression is (somewhat counterintuitively) translated to a tree that is
    //fully unbalanced on the left, with the left most element at the bottom
    //and the right most element at the root, as shown in the following diagram:
    //
    //          +     <- root
    //         + 0    <- right most element
    //        + 0
    //      ...
    //      +
    //  expr 0        <- left most elements
    private void evaluate(String expr, double[] expectedValue) {
        evaluate1(expr, expectedValue);
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(expr);
        sb.append(")");
        int range = 2;
        sb.append("+0".repeat(Math.max(0, NodeCalcVisitors.RECURSION_THRESHOLD - range)));
        for (int i = NodeCalcVisitors.RECURSION_THRESHOLD - range; i <= NodeCalcVisitors.RECURSION_THRESHOLD + range; i++) {
            sb.append("+0");
            evaluate1(sb.toString(), expectedValue);
        }
    }

    private void evaluate(String expr, double expectedValue) {
        evaluate(expr, new double[]{expectedValue});
    }

    @Test
    void evalTest() {
        evaluate("1", 1);
        evaluate("1f", 1f);
        evaluate("1d", 1d);
        evaluate("(-1f).abs()", 1f);
        evaluate("timeSeries['baz'].abs()", 1f);
        evaluate("-(-2f)", 2f);
        evaluate("+(-2f)", -2f);
        evaluate("timeSeries['foo'].time()", ZonedDateTime.parse("2015-01-01T00:00:00Z").toInstant().toEpochMilli());
        evaluate("timeSeries['foo'].time() == time('2015-01-01T00:00:00Z')", 1);
        evaluate("timeSeries['foo'].time() <= time('2015-01-01T00:15:00Z')", 1);
        evaluate("timeSeries['foo'].time() < time('2014-01-01T00:00:00Z')", 0);
        evaluate("timeSeries['foo'] = 1", 1);
        evaluate("timeSeries['foo'] = 1.5d", 1.5d);
        evaluate("timeSeries['foo'] = 1.5", 1.5d);
    }

    @Test
    void evalTestAddition() {
        evaluate("1f + timeSeries['foo']", 4);
        evaluate("1 + timeSeries['foo']", 4);
        evaluate("1.1 + timeSeries['foo']", 4.1);
        evaluate("1d + timeSeries['foo']", 4);
        evaluate("timeSeries['foo'] + 1f", 4);
        evaluate("timeSeries['foo'] + 1", 4);
        evaluate("timeSeries['foo'] + 1.1", 4.1);
        evaluate("timeSeries['foo'] + 1d", 4);
        evaluate("timeSeries['foo'] + timeSeries['bar']", 5f);
    }

    @Test
    void evalTestSubtraction() {
        evaluate("1f - timeSeries['foo']", -2);
        evaluate("1 - timeSeries['foo']", -2);
        evaluate("1.1 - timeSeries['foo']", -1.9);
        evaluate("1d - timeSeries['foo']", -2);
        evaluate("timeSeries['foo'] - 1f", 2);
        evaluate("timeSeries['foo'] - 1", 2);
        evaluate("timeSeries['foo'] - 1.1", 1.9);
        evaluate("timeSeries['foo'] - 1d", 2);
        evaluate("timeSeries['foo'] - timeSeries['bar']", 1);
    }

    @Test
    void evalTestMultiplication() {
        evaluate("1f * timeSeries['foo']", 3);
        evaluate("1 * timeSeries['foo']", 3);
        evaluate("1d * timeSeries['foo']", 3);
        evaluate("1.1 * timeSeries['foo']", 3.3000000000000003);
        evaluate("timeSeries['foo'] * 1f", 3);
        evaluate("timeSeries['foo'] * 1", 3);
        evaluate("timeSeries['foo'] * 1.1", 3.3000000000000003);
        evaluate("timeSeries['foo'] * 1d", 3);
        evaluate("timeSeries['foo'] * timeSeries['bar']", 6);
    }

    @Test
    void evalTestDivision() {
        evaluate("timeSeries['foo'] / 2f", 1.5);
        evaluate("timeSeries['foo'] / 2", 1.5);
        evaluate("timeSeries['foo'] / 2.1", 1.4285714285714286);
        evaluate("timeSeries['foo'] / 2d", 1.5);
        evaluate("2f / timeSeries['foo']", 0.6666666666666666);
        evaluate("2 / timeSeries['foo']", 0.6666666666666666);
        evaluate("2.1 / timeSeries['foo']", 0.7000000000000001);
        evaluate("2d / timeSeries['foo']", 0.6666666666666666);
        evaluate("timeSeries['foo'] / timeSeries['bar']", 1.5);
    }

    @Test
    void evalTestComparison() {
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
    }

    @Test
    void evalTestMinMax() {
        evaluate("timeSeries['foo'].min(1)", 1);
        evaluate("timeSeries['foo'].min(5)", 3);
        evaluate("timeSeries['foo'].max(1)", 3);
        evaluate("timeSeries['foo'].max(5)", 5);
        evaluate("min(timeSeries['foo'], timeSeries['bar'])", 2f);
        evaluate("min(timeSeries['foo'], timeSeries['baz'])", new double[]{-1f, 3f});
        evaluate("max(timeSeries['foo'], timeSeries['bar'])", 3f);
        evaluate("max(timeSeries['foo'], timeSeries['baz'])", new double[]{3f, 4f});
    }

    @Test
    void builderTest() {
        DoubleTimeSeries a = TimeSeries.createDouble("a", IrregularTimeSeriesIndex.create(Instant.now()), 5d);
        DoubleTimeSeries b = DoubleTimeSeries.fromTimeSeries(a)
                .build("ts['b'] = ts['a'] + 1")
                .get(0);
        assertArrayEquals(new double[] {6d}, b.toArray(), 0d);
    }

    @Test
    void splitWithCalcTest() {
        TimeSeriesIndex index = new RegularTimeSeriesIndex(10000, 10002, 1);
        DoubleTimeSeries a = TimeSeries.createDouble("a", index, 1d, 2d, 3d);
        DoubleTimeSeries b = DoubleTimeSeries.fromTimeSeries(a).build("ts['b'] = ts['a'] + 1").get(0);
        List<DoubleTimeSeries> timeSeriesList = Arrays.asList(a, b);
        List<List<DoubleTimeSeries>> split = TimeSeries.split(timeSeriesList, 2);
        assertEquals(2, split.size());
        assertEquals(2, split.get(0).size());
        assertEquals(2, split.get(1).size());
        assertInstanceOf(StoredDoubleTimeSeries.class, split.get(0).get(0));
        assertInstanceOf(StoredDoubleTimeSeries.class, split.get(1).get(0));
        assertInstanceOf(CalculatedTimeSeries.class, split.get(0).get(1));
        assertInstanceOf(CalculatedTimeSeries.class, split.get(1).get(1));
        assertArrayEquals(new double[]{1d, 2d, Double.NaN}, split.get(0).get(0).toArray(), 0d);
        assertArrayEquals(new double[]{Double.NaN, Double.NaN, 3d}, split.get(1).get(0).toArray(), 0d);
        // next check could surprising but it is because of calculated time series with infinite indexes which
        // are not really splitted
        assertArrayEquals(new double[]{2d, 3d, 4d}, split.get(0).get(1).toArray(), 0d);
        assertArrayEquals(new double[]{2d, 3d, 4d}, split.get(1).get(1).toArray(), 0d);
    }

    @Test
    void splitWithOnlyCalcTest() {
        List<DoubleTimeSeries> timeSeriesList = DoubleTimeSeries.build("ts['a'] = 1", "ts['b'] = 2");
        assertThrows(IllegalArgumentException.class, () -> TimeSeries.split(timeSeriesList, 2));
    }
}
