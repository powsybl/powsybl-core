/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.powsybl.timeseries.ast.NodeCalc;
import org.junit.Ignore;
import org.junit.Test;
import org.threeten.extra.Interval;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TimeSeriesCalcStackOverflowErrorIssue {

    @Ignore
    @Test
    public void stackOverflowExceptionTest() {
        TimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-07-20T00:00:00Z"), Duration.ofDays(200));
        int n = 10000;
        List<DoubleTimeSeries> timeSeriesList = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            timeSeriesList.add(StoredDoubleTimeSeries.create("ts" + i, index, new double[] {1d, 1d}));
        }
        ReadOnlyTimeSeriesStore store = new ReadOnlyTimeSeriesStoreCache(timeSeriesList);
        String script = String.join(System.lineSeparator(),
                "ts['sum'] = 0",
                "for (i in 0..<" + n  + ") {",
                "    ts['sum'] = ts['sum'] + ts['ts' + i]",
                "}");
        Map<String, NodeCalc> nodes = new CalculatedTimeSeriesDslLoader(script)
                .load(store);
        NodeCalc node = nodes.get("sum");
        assertNotNull(node);
        CalculatedTimeSeries sum = new CalculatedTimeSeries("sum", node, store, 0);
        assertArrayEquals(new double[] {n, n}, sum.toArray(), 0d);
    }
}
