/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StringTimeSeries extends AbstractTimeSeries<StringPoint, StringArrayChunk, StringTimeSeries> implements TimeSeries<StringPoint, StringTimeSeries> {

    private static final String[] NULL_ARRAY = new String[] {null};

    public static StringTimeSeries create(String name, TimeSeriesIndex index, String[] values) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(index);
        Objects.requireNonNull(values);
        if (index.getPointCount() != values.length) {
            throw new IllegalArgumentException("Bad number of values " + values.length + ", expected " + index.getPointCount());
        }
        return new StringTimeSeries(new TimeSeriesMetadata(name, TimeSeriesDataType.STRING, index),
                new UncompressedStringArrayChunk(0, values));
    }

    public StringTimeSeries(TimeSeriesMetadata metadata, StringArrayChunk... chunks) {
        super(metadata, chunks);
    }

    public StringTimeSeries(TimeSeriesMetadata metadata, List<StringArrayChunk> chunks) {
        super(metadata, chunks);
    }

    protected CompressedStringArrayChunk createGapFillingChunk(int i, int length) {
        return new CompressedStringArrayChunk(i, length, NULL_ARRAY, new int[] {length});
    }

    @Override
    protected StringTimeSeries createTimeSeries(StringArrayChunk chunk) {
        return new StringTimeSeries(metadata, chunk);
    }

    public void fillBuffer(CompactStringBuffer buffer, int timeSeriesOffset) {
        chunks.forEach(chunk -> chunk.fillBuffer(buffer, timeSeriesOffset));
    }

    public String[] toArray() {
        CompactStringBuffer buffer = new CompactStringBuffer(ByteBuffer::allocate, metadata.getIndex().getPointCount());
        chunks.forEach(chunk -> chunk.fillBuffer(buffer, 0));
        return buffer.toArray();
    }
}
