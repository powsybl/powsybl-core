/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.dsl;

import com.powsybl.computation.CompletableFutureTask;
import com.powsybl.timeseries.*;
import com.powsybl.timeseries.ast.NodeCalc;
import com.powsybl.timeseries.ast.NodeCalcVisitors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.threeten.extra.Interval;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class CalculatedTimeSeriesGroovyDslInterruptionsTest {
    ReadOnlyTimeSeriesStore store;
    TimeSeriesNameResolver resolver;

    String[] timeSeriesNames = {"foo", "bar", "baz", "toCache"};
    double[] fooValues = new double[] {3d, 5d};
    TimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-07-20T00:00:00Z"), Duration.ofDays(200));

    // Counters
    CountDownLatch waitForStart;
    CountDownLatch waitForFinish;
    CountDownLatch waitForInterruption;

    // Booleans
    AtomicBoolean config;
    AtomicBoolean interrupted;


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
        // Counters
        waitForStart = new CountDownLatch(1);
        waitForFinish = new CountDownLatch(1);
        waitForInterruption = new CountDownLatch(1);

        // Booleans
        config = new AtomicBoolean(false);
        interrupted = new AtomicBoolean(false);

        // Time Series Store
        store = new ReadOnlyTimeSeriesStoreCache(
            TimeSeries.createDouble(timeSeriesNames[0], index, fooValues)
        );

        // TimeSeries name resolver
        resolver = new FromStoreTimeSeriesNameResolver(store, 0);
    }

    private void assertions(CompletableFuture<Object> task) throws InterruptedException {

        // This line is used to check that the task has already started
        waitForStart.await();

        // The task should not be done at that point
        assertFalse(task.isDone());

        // Cancel the task
        boolean cancelled = task.cancel(true);

        // Check that the task is cancelled
        assertTrue(cancelled);
        assertTrue(task.isCancelled());

        // Boolean stays at false if the task is cancelled
        assertFalse(config.get());

        // This should throw an exception since the task is cancelled
        assertThrows(CancellationException.class, () -> {
            task.get();
            fail("Should not happen: task has been cancelled");
        });

        // This line should return immediately since the task has been cancelled
        waitForInterruption.await();

        // This boolean is true if the task has been interrupted
        assertTrue(interrupted.get());

        // Second call to cancel should return false
        cancelled = task.cancel(true);
        assertFalse(cancelled);
    }

    @ParameterizedTest
    @Timeout(10)
    @Order(2)
    @ValueSource(booleans = {false, true})
    void testCancelGroovyLongScript(boolean isDelayed) throws Exception {
        Map<String, NodeCalc>[] nodes = new HashMap[1];
        CompletableFuture<Object> task = CompletableFutureTask.runAsync(() -> {
            waitForStart.countDown();
            try {
                nodes[0] = CalculatedTimeSeriesDslLoader.find().load(script, store);
                config.set(true);
                waitForFinish.countDown();
            } catch (Exception e) { // Thread interrupted => good
                interrupted.set(true);
                waitForInterruption.countDown();
            }
            return null;
        }, Executors.newSingleThreadExecutor());

        // Is asked, wait a bit to simulate interruption by a user
        if (isDelayed) {
            Thread.sleep(800);
        }

        assertions(task);
    }
}
