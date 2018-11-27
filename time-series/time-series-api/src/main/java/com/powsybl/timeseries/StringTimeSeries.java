/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StringTimeSeries extends AbstractTimeSeries<StringPoint, StringDataChunk, StringTimeSeries> implements TimeSeries<StringPoint, StringTimeSeries> {

    private static final String[] NULL_ARRAY = new String[] {null};

    public StringTimeSeries(TimeSeriesMetadata metadata, StringDataChunk... chunks) {
        super(metadata, chunks);
    }

    public StringTimeSeries(TimeSeriesMetadata metadata, List<StringDataChunk> chunks) {
        super(metadata, chunks);
    }

    protected CompressedStringDataChunk createGapFillingChunk(int i, int length) {
        return new CompressedStringDataChunk(i, length, NULL_ARRAY, new int[] {length});
    }

    @Override
    protected StringTimeSeries createTimeSeries(StringDataChunk chunk) {
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
