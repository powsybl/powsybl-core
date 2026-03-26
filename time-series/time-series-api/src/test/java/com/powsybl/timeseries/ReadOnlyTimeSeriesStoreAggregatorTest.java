/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ReadOnlyTimeSeriesStoreAggregatorTest {

    @Test
    void test() {
        TimeSeriesIndex index = new RegularTimeSeriesIndex(Instant.ofEpochMilli(10000), Instant.ofEpochMilli(10001), Duration.ofMillis(1));
        DoubleTimeSeries ts1 = TimeSeries.createDouble("ts1", index, 1d, 2d);
        DoubleTimeSeries ts2 = TimeSeries.createDouble("ts2", index, 3d, 4d);
        ReadOnlyTimeSeriesStore store1 = new ReadOnlyTimeSeriesStoreCache(ts1);
        ReadOnlyTimeSeriesStore store2 = new ReadOnlyTimeSeriesStoreCache(ts2);
        ReadOnlyTimeSeriesStore store12 = new ReadOnlyTimeSeriesStoreAggregator(store1, store2);
        assertEquals(Sets.newHashSet("ts2", "ts1"), store12.getTimeSeriesNames(null));
        assertTrue(store12.timeSeriesExists("ts1"));
        assertFalse(store12.timeSeriesExists("ts3"));
        assertEquals(new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index), store12.getTimeSeriesMetadata("ts1").orElseThrow(IllegalStateException::new));
        assertEquals(Arrays.asList(new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index),
                                   new TimeSeriesMetadata("ts2", TimeSeriesDataType.DOUBLE, index)),
                     store12.getTimeSeriesMetadata(Sets.newHashSet("ts1", "ts2")));
        assertEquals(Collections.emptySet(), store12.getTimeSeriesDataVersions());
        assertEquals(Collections.emptySet(), store12.getTimeSeriesDataVersions("ts1"));
        assertSame(ts1, store12.getDoubleTimeSeries("ts1", 1).orElseThrow(IllegalStateException::new));
        assertFalse(store12.getDoubleTimeSeries("ts3", 1).isPresent());
        assertEquals(Arrays.asList(ts1, ts2), store12.getDoubleTimeSeries(Sets.newHashSet("ts1", "ts2"), 1));
        assertEquals(Arrays.asList(ts1, ts2), store12.getDoubleTimeSeries(1));
        assertFalse(store12.getStringTimeSeries("ts3", 1).isPresent());
        assertTrue(store12.getStringTimeSeries(Collections.singleton("ts3"), 1).isEmpty());
    }
}
