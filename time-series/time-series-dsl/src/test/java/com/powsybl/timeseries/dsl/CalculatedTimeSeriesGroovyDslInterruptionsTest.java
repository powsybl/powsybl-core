/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.dsl;

import com.powsybl.computation.AbstractTaskInterruptionTest;
import com.powsybl.timeseries.*;
import com.powsybl.timeseries.ast.NodeCalcVisitors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.threeten.extra.Interval;

import java.time.Duration;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class CalculatedTimeSeriesGroovyDslInterruptionsTest extends AbstractTaskInterruptionTest {
    ReadOnlyTimeSeriesStore store;
    TimeSeriesNameResolver resolver;

    String[] timeSeriesNames = {"foo", "bar", "baz", "toCache"};
    double[] fooValues = new double[] {3d, 5d};
    TimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-07-20T00:00:00Z"), Duration.ofDays(200));


    // Script to evaluate
    String script = "ts['toCache'] = (timeSeries['foo'] < 4.0)" +
        "+0".repeat(Math.max(0, NodeCalcVisitors.RECURSION_THRESHOLD + 1)) +
        "\n" +
        """
            for (int i = 0; i < 10; i++) {
                sleep(500)
            }
            """ +
        "ts['test'] = (timeSeries['toCache'] + timeSeries['toCache'] * 2.0)" +
        "\n" +
        "ts['testIterative'] = ts['test']" +
        "+0".repeat(Math.max(0, NodeCalcVisitors.RECURSION_THRESHOLD + 1));

    @BeforeEach
    void setUp() {
        // Time Series Store
        store = new ReadOnlyTimeSeriesStoreCache(
            TimeSeries.createDouble(timeSeriesNames[0], index, fooValues)
        );

        // TimeSeries name resolver
        resolver = new FromStoreTimeSeriesNameResolver(store, 0);
    }

    @ParameterizedTest
    @Timeout(10)
    @Order(2)
    @ValueSource(booleans = {false, true})
    void test(boolean isDelayed) throws Exception {
        testCancelLongTask(isDelayed, () -> CalculatedTimeSeriesDslLoader.find().load(script, store));
    }
}
