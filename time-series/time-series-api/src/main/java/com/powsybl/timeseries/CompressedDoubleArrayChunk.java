/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CompressedDoubleArrayChunk extends AbstractCompressedArrayChunk implements DoubleArrayChunk {

    private final double[] stepValues;

    public CompressedDoubleArrayChunk(int offset, int uncompressedLength, double[] stepValues, int[] stepLengths) {
        super(offset, uncompressedLength, stepLengths);
        check(offset, uncompressedLength, stepValues.length, stepLengths.length);
        this.stepValues = Objects.requireNonNull(stepValues);
    }

    public double[] getStepValues() {
        return stepValues;
    }

    static int getEstimatedSize(int stepValuesLength, int stepLengthsLength) {
        return Double.BYTES * stepValuesLength + Integer.BYTES * stepLengthsLength;
    }

    @Override
    public int getEstimatedSize() {
        return getEstimatedSize(stepValues.length, stepLengths.length);
    }

    @Override
    protected int getUncompressedEstimatedSize() {
        return Double.BYTES * uncompressedLength;
    }

    @Override
    public TimeSeriesDataType getDataType() {
        return TimeSeriesDataType.DOUBLE;
    }

    @Override
    public void fillBuffer(DoubleBuffer buffer, int timeSeriesOffset) {
        Objects.requireNonNull(buffer);
        int k = 0;
        for (int i = 0; i < stepValues.length; i++) {
            double value = stepValues[i];
            for (int j = 0; j < stepLengths[i]; j++) {
                buffer.put(timeSeriesOffset + offset + k++, value);
            }
        }
    }

    @Override
    public Iterator<DoublePoint> iterator(TimeSeriesIndex index) {
        Objects.requireNonNull(index);
        return new Iterator<DoublePoint>() {

            private int i = offset;
            private int step = 0;

            @Override
            public boolean hasNext() {
                return i < offset + uncompressedLength;
            }

            @Override
            public DoublePoint next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                DoublePoint point = new DoublePoint(i, index.getTimeAt(i), stepValues[step]);
                i += stepLengths[step];
                step++;
                return point;
            }
        };
    }

    @Override
    public Stream<DoublePoint> stream(TimeSeriesIndex index) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iterator(index),
                Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
    }

    @Override
    public DoubleArrayChunk tryToCompress() {
        return this;
    }

    @Override
    public Split<DoublePoint, DoubleArrayChunk> splitAt(int splitIndex) {
        // split at offset is not allowed because it will result to a null left chunk
        if (splitIndex <= offset || splitIndex > (offset + uncompressedLength - 1)) {
            throw new IllegalArgumentException("Split index " + splitIndex + " out of chunk range ]" + offset
                    + ", " + (offset + uncompressedLength - 1) + "]");
        }
        int index = offset;
        for (int step = 0; step < stepLengths.length; step++) {
            if (index + stepLengths[step] > splitIndex) {
                // first chunk
                int[] stepLengths1 = new int[step + 1];
                double[] stepValues1 = new double[stepLengths1.length];
                System.arraycopy(stepLengths, 0, stepLengths1, 0, stepLengths1.length);
                System.arraycopy(stepValues, 0, stepValues1, 0, stepValues1.length);
                stepLengths1[step] = splitIndex - index;
                CompressedDoubleArrayChunk chunk1 = new CompressedDoubleArrayChunk(offset, splitIndex - offset, stepValues1, stepLengths1);

                // second chunk
                int[] stepLengths2 = new int[stepLengths.length - step];
                double[] stepValues2 = new double[stepLengths2.length];
                System.arraycopy(stepLengths, step, stepLengths2, 0, stepLengths2.length);
                System.arraycopy(stepValues, step, stepValues2, 0, stepValues2.length);
                stepLengths2[0] = stepLengths[step] - stepLengths1[step];
                CompressedDoubleArrayChunk chunk2 = new CompressedDoubleArrayChunk(splitIndex, uncompressedLength - chunk1.uncompressedLength, stepValues2, stepLengths2);

                return new Split<>(chunk1, chunk2);
            }
            index += stepLengths[step];
        }
        throw new AssertionError("Should not happen");
    }

    @Override
    protected void writeStepValuesJson(JsonGenerator generator) throws IOException {
        generator.writeArray(stepValues, 0, stepValues.length);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, uncompressedLength, stepLengths, stepValues);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CompressedDoubleArrayChunk) {
            CompressedDoubleArrayChunk other = (CompressedDoubleArrayChunk) obj;
            return offset == other.offset &&
                    uncompressedLength == other.uncompressedLength &&
                    Arrays.equals(stepLengths, other.stepLengths) &&
                    Arrays.equals(stepValues, other.stepValues);
        }
        return false;
    }
}
