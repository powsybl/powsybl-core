/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.nio.DoubleBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Arrays.copyOfRange;

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

    @Override
    protected DoubleTimeSeries createTimeSeries(List<DoubleDataChunk> chunks) {
        return new StoredDoubleTimeSeries(metadata, chunks);
    }

    @Override
    protected DoubleDataChunk toCompactChunk() {
        if (chunks.size() == 1 && chunks.getFirst() instanceof UncompressedDoubleDataChunk uncompressed) {
            return uncompressed; // already compact, no copy
        }
        return new UncompressedDoubleDataChunk(getMinOffset(), toCompactArray());
    }

    //    protected DoubleDataChunk sliceNonBlank(DoubleDataChunk compactChunk, int from, int to) {
    //        double[] values = ((UncompressedDoubleDataChunk) compactChunk).getValues();
    //        for (int i = from; i < to; i++) {
    //            if (!Double.isNaN(values[i])) {
    //                return from == 0 && to == values.length ? compactChunk : new UncompressedDoubleDataChunk(compactChunk.getOffset() + from, copyOfRange(values, from, to));
    //            }
    //        }
    //        return null;
    //    }

    @Override
    protected DoubleDataChunk sliceNonBlank(DoubleDataChunk chunk, int from, int to) {
        int offset = chunk.getOffset();
        boolean whole = from == offset && to == offset + chunk.getLength();
        if (chunk instanceof UncompressedDoubleDataChunk uncompressed) {
            return sliceNonBlankUncompressed(uncompressed, from, to, offset, whole);
        }
        CompressedDoubleDataChunk compressed = (CompressedDoubleDataChunk) chunk;
        return sliceNonBlankCompressed(compressed, from, to, offset, whole);
    }

    private DoubleDataChunk sliceNonBlankUncompressed(UncompressedDoubleDataChunk uncompressed, int from, int to, int offset, boolean whole) {
        double[] values = uncompressed.getValues();
        for (int i = from - offset; i < to - offset; i++) {
            if (!Double.isNaN(values[i])) {
                return whole ? uncompressed : new UncompressedDoubleDataChunk(from, copyOfRange(values, from - offset, to - offset));
            }
        }
        return null;
    }

    private DoubleDataChunk sliceNonBlankCompressed(CompressedDoubleDataChunk compressed, int from, int to, int offset, boolean whole) {
        double[] stepValues = compressed.getStepValues();
        int[] stepLengths = compressed.getStepLengths();
        TDoubleArrayList newValues = new TDoubleArrayList();
        TIntArrayList newLengths = new TIntArrayList();
        boolean nonBlank = false;
        int stepStart = offset;
        for (int step = 0; step < stepValues.length && stepStart < to; step++) {
            int length = Math.min(stepStart + stepLengths[step], to) - Math.max(stepStart, from);
            if (length > 0) {
                newValues.add(stepValues[step]);
                newLengths.add(length);
                nonBlank |= !Double.isNaN(stepValues[step]);
            }
            stepStart += stepLengths[step];
        }
        if (!nonBlank) {
            return null;
        }
        return whole ? compressed : new CompressedDoubleDataChunk(from, to - from, newValues.toArray(), newLengths.toArray());
    }

    @Override
    protected DoubleDataChunk mergeRun(List<DoubleDataChunk> successiveChunks) {
        if (successiveChunks.getFirst().isCompressed()) {
            return successiveChunks.stream().reduce(DoubleDataChunk::append).orElseThrow();
        }
        int length = successiveChunks.getLast().getOffset() + successiveChunks.getLast().getLength() - successiveChunks.getFirst().getOffset();
        double[] values = new double[length];
        int position = 0;
        for (DoubleDataChunk chunk : successiveChunks) {
            double[] chunkValues = ((UncompressedDoubleDataChunk) chunk).getValues();
            System.arraycopy(chunkValues, 0, values, position, chunkValues.length);
            position += chunkValues.length;
        }
        return new UncompressedDoubleDataChunk(successiveChunks.getFirst().getOffset(), values);
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

    @Override
    public double[] toCompactArray() {
        if (chunks.isEmpty()) {
            return new double[0];
        }
        int minOffset = getMinOffset();
        int compactLength = getMaxChunkEnd() - minOffset;
        double[] array = new double[compactLength];
        Arrays.fill(array, Double.NaN);
        DoubleBuffer buffer = DoubleBuffer.wrap(array);
        chunks.forEach(chunk -> chunk.fillBuffer(buffer, -minOffset));
        return array;
    }

    @Override
    public DoubleTimeSeriesValues getDoubleTimeSeriesValues() {
        return new DoubleTimeSeriesValues(toCompactArray(), getMinOffset());
    }

}
