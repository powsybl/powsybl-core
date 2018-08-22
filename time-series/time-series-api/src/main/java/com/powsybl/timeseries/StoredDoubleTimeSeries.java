/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import java.nio.DoubleBuffer;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StoredDoubleTimeSeries extends AbstractTimeSeries<DoublePoint, DoubleArrayChunk, DoubleTimeSeries> implements DoubleTimeSeries {

    private static final double[] NAN_ARRAY = new double[] {Double.NaN};

    public static StoredDoubleTimeSeries create(String name, TimeSeriesIndex index, double[] values) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(index);
        Objects.requireNonNull(values);
        if (index.getPointCount() != values.length) {
            throw new IllegalArgumentException("Bad number of values " + values.length + ", expected " + index.getPointCount());
        }
        return new StoredDoubleTimeSeries(new TimeSeriesMetadata(name, TimeSeriesDataType.DOUBLE, index),
                                          new UncompressedDoubleArrayChunk(0, values));
    }

    public StoredDoubleTimeSeries(TimeSeriesMetadata metadata, DoubleArrayChunk... chunks) {
        super(metadata, chunks);
    }

    public StoredDoubleTimeSeries(TimeSeriesMetadata metadata, List<DoubleArrayChunk> chunks) {
        super(metadata, chunks);
    }

    protected CompressedDoubleArrayChunk createGapFillingChunk(int i, int length) {
        return new CompressedDoubleArrayChunk(i, length, NAN_ARRAY, new int[] {length});
    }

    @Override
    protected DoubleTimeSeries createTimeSeries(DoubleArrayChunk chunk) {
        return new StoredDoubleTimeSeries(metadata, chunk);
    }

    @Override
    public void fillBuffer(DoubleBuffer buffer, int timeSeriesOffset) {
        chunks.forEach(chunk -> chunk.fillBuffer(buffer, timeSeriesOffset));
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
