/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import java.nio.DoubleBuffer;
import java.util.Arrays;
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

    @Override
    protected DoubleDataChunk mergeToUncompressed(int offset, int length, List<DoubleDataChunk> chunks) {
        double[] values = new double[length];
        Arrays.fill(values, Double.NaN);
        DoubleBuffer buffer = DoubleBuffer.wrap(values);
        chunks.forEach(piece -> piece.fillBuffer(buffer, -offset));
        return new UncompressedDoubleDataChunk(offset, values);
    }

    @Override
    protected DoubleDataChunk mergeCompressed(List<DoubleDataChunk> chunks) {
        if (!chunks.stream().allMatch(CompressedDoubleDataChunk.class::isInstance)) {
            return super.mergeCompressed(chunks);
        }
        int stepCount = chunks.stream().mapToInt(piece -> ((CompressedDoubleDataChunk) piece).getStepLengths().length).sum();
        double[] stepValues = new double[stepCount];
        int[] stepLengths = new int[stepCount];
        int n = 0;
        for (DoubleDataChunk piece : chunks) {
            CompressedDoubleDataChunk chunk = (CompressedDoubleDataChunk) piece;
            double[] values = chunk.getStepValues();
            int[] lengths = chunk.getStepLengths();
            // as in append, the first step is merged with the previous one when they have the same value
            int firstStep = 0;
            if (n > 0 && stepValues[n - 1] == values[0]) {
                stepLengths[n - 1] += lengths[0];
                firstStep = 1;
            }
            System.arraycopy(values, firstStep, stepValues, n, values.length - firstStep);
            System.arraycopy(lengths, firstStep, stepLengths, n, lengths.length - firstStep);
            n += values.length - firstStep;
        }
        int offset = chunks.getFirst().getOffset();
        DoubleDataChunk last = chunks.getLast();
        return new CompressedDoubleDataChunk(offset, last.getOffset() + last.getLength() - offset,
                Arrays.copyOf(stepValues, n), Arrays.copyOf(stepLengths, n));
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
