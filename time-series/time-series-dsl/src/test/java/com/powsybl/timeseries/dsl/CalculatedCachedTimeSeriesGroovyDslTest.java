/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.dsl;

import com.powsybl.timeseries.*;
import com.powsybl.timeseries.ast.NodeCalc;
import com.powsybl.timeseries.ast.NodeCalcVisitors;
import org.junit.jupiter.api.Test;
import org.threeten.extra.Interval;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class CalculatedCachedTimeSeriesGroovyDslTest {

    ReadOnlyTimeSeriesStore store;
    TimeSeriesNameResolver resolver;

    String[] timeSeriesNames = {"foo", "bar", "baz", "toCache"};
    double[] fooValues = new double[] {3d, 5d};
    TimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-07-20T00:00:00Z"), Duration.ofDays(200));

    @Test
    void evalCached() {
        // Script to evaluate
        String script = "ts['toCache'] = (timeSeries['foo'] <= 3.0)" +
            "+0".repeat(Math.max(0, NodeCalcVisitors.RECURSION_THRESHOLD + 1)) +
            "\n" +
            "ts['test'] = (timeSeries['toCache'] + timeSeries['toCache'] * 2.0)" +
            "+0".repeat(Math.max(0, NodeCalcVisitors.RECURSION_THRESHOLD + 1));
        // create time series space mock
        TimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-07-20T00:00:00Z"), Duration.ofDays(200));

        // Time Series Store
        store = new ReadOnlyTimeSeriesStoreCache(
            TimeSeries.createDouble(timeSeriesNames[0], index, fooValues)
        );

        // TimeSeries name resolver
        resolver = new FromStoreTimeSeriesNameResolver(store, 0);

        // Nodes
        Map<String, NodeCalc> nodes = CalculatedTimeSeriesDslLoader.find().load(script, store);

        // NodeCalc
        NodeCalc testNodeCalc = nodes.get("test");

        // Calculated TimeSeries creation
        CalculatedTimeSeries tsCalc = new CalculatedTimeSeries("test_calc", testNodeCalc);

        // Add the resolver to the calculated time series
        tsCalc.setTimeSeriesNameResolver(resolver);

        assertArrayEquals(new double[] {3.0, 0.0}, tsCalc.toArray());

    }
}
