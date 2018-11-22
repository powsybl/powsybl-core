/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.buffer;

import com.powsybl.timeseries.InfiniteTimeSeriesIndex;
import com.powsybl.timeseries.TimeSeriesDataType;
import com.powsybl.timeseries.TimeSeriesMetadata;
import com.powsybl.timeseries.UncompressedDoubleDataChunk;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StorageChangeBufferTest {

    @Test
    public void testMaximumChange() {
        boolean[] flushed = new boolean[1];
        flushed[0] = false;
        StorageChangeBuffer buffer = new StorageChangeBuffer(changeSet -> {
            assertEquals(2, changeSet.getChanges().size());
            flushed[0] = true;
        }, 2, Integer.MAX_VALUE);
        assertTrue(buffer.isEmpty());
        buffer.createTimeSeries("a", new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, InfiniteTimeSeriesIndex.INSTANCE));
        assertFalse(buffer.isEmpty());
        assertFalse(flushed[0]);
        buffer.createTimeSeries("b", new TimeSeriesMetadata("ts2", TimeSeriesDataType.DOUBLE, InfiniteTimeSeriesIndex.INSTANCE));
        assertTrue(buffer.isEmpty());
        assertTrue(flushed[0]);
    }

    @Test
    public void testMaximumSize() {
        boolean[] flushed = new boolean[1];
        flushed[0] = false;
        StorageChangeBuffer buffer = new StorageChangeBuffer(changeSet -> {
            assertTrue(changeSet.getEstimatedSize() > 50);
            flushed[0] = true;
        }, Integer.MAX_VALUE, 50);
        assertTrue(buffer.isEmpty());
        buffer.createTimeSeries("a", new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, InfiniteTimeSeriesIndex.INSTANCE));
        assertFalse(buffer.isEmpty());
        assertFalse(flushed[0]);
        buffer.addDoubleTimeSeriesData("a", 1, "ts1", Collections.singletonList(new UncompressedDoubleDataChunk(0, new double[] {0, 0, 0, 0})));
        assertFalse(buffer.isEmpty());
        assertFalse(flushed[0]);
        buffer.addDoubleTimeSeriesData("a", 1, "ts1", Collections.singletonList(new UncompressedDoubleDataChunk(4, new double[] {0, 0, 0, 0})));
        assertTrue(buffer.isEmpty());
        assertTrue(flushed[0]);
    }
}
