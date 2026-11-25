/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
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

    @Override
    protected StringDataChunk mergeToUncompressed(int offset, int length, List<StringDataChunk> chunks) {
        CompactStringBuffer buffer = new CompactStringBuffer(ByteBuffer::allocate, length);
        chunks.forEach(piece -> piece.fillBuffer(buffer, -offset));
        return new UncompressedStringDataChunk(offset, buffer.toArray());
    }

    @Override
    protected StringDataChunk mergeCompressed(List<StringDataChunk> chunks) {
        if (!chunks.stream().allMatch(CompressedStringDataChunk.class::isInstance)) {
            return super.mergeCompressed(chunks);
        }
        int stepCount = chunks.stream().mapToInt(piece -> ((CompressedStringDataChunk) piece).getStepLengths().length).sum();
        String[] stepValues = new String[stepCount];
        int[] stepLengths = new int[stepCount];
        // number of steps already written in stepValues and stepLengths
        int writtenSteps = 0;
        for (StringDataChunk piece : chunks) {
            CompressedStringDataChunk chunk = (CompressedStringDataChunk) piece;
            String[] values = chunk.getStepValues();
            int[] lengths = chunk.getStepLengths();
            // as in append, the first step is merged with the previous one when they have the same value
            int firstStep = 0;
            if (writtenSteps > 0 && Objects.equals(stepValues[writtenSteps - 1], values[0])) {
                stepLengths[writtenSteps - 1] += lengths[0];
                firstStep = 1;
            }
            System.arraycopy(values, firstStep, stepValues, writtenSteps, values.length - firstStep);
            System.arraycopy(lengths, firstStep, stepLengths, writtenSteps, lengths.length - firstStep);
            writtenSteps += values.length - firstStep;
        }
        int offset = chunks.getFirst().getOffset();
        StringDataChunk last = chunks.getLast();
        return new CompressedStringDataChunk(offset, last.getOffset() + last.getLength() - offset,
                Arrays.copyOf(stepValues, writtenSteps), Arrays.copyOf(stepLengths, writtenSteps));
    }

    private void forEachChunk(Consumer<StringDataChunk> consumer) {
        chunks.forEach(consumer);
    }

    public void fillBuffer(CompactStringBuffer buffer, int timeSeriesOffset) {
        forEachChunk(chunk -> chunk.fillBuffer(buffer, timeSeriesOffset));
    }

    public void fillBuffer(BigStringBuffer buffer, long timeSeriesOffset) {
        forEachChunk(chunk -> chunk.fillBuffer(buffer, timeSeriesOffset));
    }

    public String[] toArray() {
        CompactStringBuffer buffer = new CompactStringBuffer(ByteBuffer::allocate, metadata.getIndex().getPointCount());
        chunks.forEach(chunk -> chunk.fillBuffer(buffer, 0));
        return buffer.toArray();
    }

    public String[] toCompactArray() {
        if (chunks.isEmpty()) {
            return new String[0];
        }
        int minOffset = getMinOffset();
        int compactLength = getMaxChunkEnd() - minOffset;
        CompactStringBuffer buffer = new CompactStringBuffer(ByteBuffer::allocate, compactLength);
        chunks.forEach(chunk -> chunk.fillBuffer(buffer, -minOffset));
        return buffer.toArray();
    }

    public String get(int index) {
        return getStringTimeSeriesValues().get(index);
    }

    public StringTimeSeriesValues getStringTimeSeriesValues() {
        return new StringTimeSeriesValues(toCompactArray(), getMinOffset());
    }

}
