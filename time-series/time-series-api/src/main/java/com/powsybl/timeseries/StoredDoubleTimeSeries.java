/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import java.nio.DoubleBuffer;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class StoredDoubleTimeSeries extends AbstractTimeSeries<DoublePoint, DoubleDataChunk, DoubleTimeSeries> implements DoubleTimeSeries {

    private static final double[] NAN_ARRAY = new double[]{Double.NaN};

    public StoredDoubleTimeSeries(TimeSeriesMetadata metadata, DoubleDataChunk... chunks) {
        super(metadata, chunks);
    }

    public StoredDoubleTimeSeries(TimeSeriesMetadata metadata, List<DoubleDataChunk> chunks) {
        super(metadata, chunks);
    }

    protected CompressedDoubleDataChunk createGapFillingChunk(int i, int length) {
        return new CompressedDoubleDataChunk(i, length, NAN_ARRAY, new int[] {length});
    }

    @Override
    protected DoubleTimeSeries createTimeSeries(DoubleDataChunk chunk) {
        return new StoredDoubleTimeSeries(metadata, chunk);
    }

    private void forEachChunk(Consumer<DoubleDataChunk> consumer) {
        chunks.forEach(consumer);
    }

    @Override
    public void fillBuffer(DoubleBuffer buffer, int timeSeriesOffset) {
        forEachChunk(chunk -> chunk.fillBuffer(buffer, timeSeriesOffset));
    }

    @Override
    public void fillBuffer(BigDoubleBuffer buffer, long timeSeriesOffset) {
        forEachChunk(chunk -> chunk.fillBuffer(buffer, timeSeriesOffset));
    }

    @Override
    public double[] toArray() {
        DoubleBuffer buffer = DoubleBuffer.allocate(metadata.getIndex().getPointCount());
        for (int i = 0; i < metadata.getIndex().getPointCount(); i++) {
            buffer.put(i, Double.NaN);
        }
        fillBuffer(buffer, 0);
        return buffer.array();
    }
}
