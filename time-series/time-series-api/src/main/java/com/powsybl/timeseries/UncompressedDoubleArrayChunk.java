/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.util.trove.TDoubleArrayListHack;
import com.powsybl.commons.util.trove.TIntArrayListHack;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UncompressedDoubleArrayChunk extends AbstractUncompressedArrayChunk implements DoubleArrayChunk {

    private final double[] values;

    public UncompressedDoubleArrayChunk(int offset, double[] values) {
        super(offset);
        this.values = Objects.requireNonNull(values);
    }

    public double[] getValues() {
        return values;
    }

    @Override
    public int getLength() {
        return values.length;
    }

    @Override
    public int getEstimatedSize() {
        return Double.BYTES * values.length;
    }

    @Override
    public TimeSeriesDataType getDataType() {
        return TimeSeriesDataType.DOUBLE;
    }

    @Override
    public void fillBuffer(DoubleBuffer buffer, int timeSeriesOffset) {
        Objects.requireNonNull(buffer);
        for (int i = 0; i < values.length; i++) {
            buffer.put(timeSeriesOffset + offset + i, values[i]);
        }
    }

    @Override
    public DoubleArrayChunk tryToCompress() {
        TDoubleArrayListHack stepValues = new TDoubleArrayListHack();
        TIntArrayListHack stepLengths = new TIntArrayListHack();
        int estimatedSize = getEstimatedSize();
        for (double value : values) {
            if (stepValues.isEmpty()) {
                stepValues.add(value);
                stepLengths.add(1);
            } else {
                int previousIndex = stepValues.size() - 1;
                double previousValue = stepValues.getQuick(previousIndex);
                if (previousValue == value) {
                    stepLengths.set(previousIndex, stepLengths.getQuick(previousIndex) + 1);
                } else {
                    stepValues.add(value);
                    stepLengths.add(1);
                }
            }
            if (CompressedDoubleArrayChunk.getEstimatedSize(stepValues.size(), stepLengths.size()) >= estimatedSize) {
                // compression is inefficient
                return this;
            }
        }
        return new CompressedDoubleArrayChunk(offset, values.length, stepValues.toArray(), stepLengths.toArray());
    }

    @Override
    public Split<DoublePoint, DoubleArrayChunk> splitAt(int splitIndex) {
        // split at offset is not allowed because it will result to a null left chunk
        if (splitIndex <= offset || splitIndex > (offset + values.length - 1)) {
            throw new IllegalArgumentException("Split index " + splitIndex + " out of chunk range ]" + offset
                    + ", " + (offset + values.length - 1) + "]");
        }
        double[] values1 = new double[splitIndex - offset];
        double[] values2 = new double[values.length - values1.length];
        System.arraycopy(values, 0, values1, 0, values1.length);
        System.arraycopy(values, values1.length, values2, 0, values2.length);
        return new Split<>(new UncompressedDoubleArrayChunk(offset, values1),
                           new UncompressedDoubleArrayChunk(splitIndex, values2));
    }

    @Override
    public Stream<DoublePoint> stream(TimeSeriesIndex index) {
        Objects.requireNonNull(index);
        return IntStream.range(0, values.length).mapToObj(i -> new DoublePoint(offset + i, index.getTimeAt(offset + i), values[i]));
    }

    @Override
    public Iterator<DoublePoint> iterator(TimeSeriesIndex index) {
        Objects.requireNonNull(index);
        return new Iterator<DoublePoint>() {

            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < values.length;
            }

            @Override
            public DoublePoint next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                DoublePoint point = new DoublePoint(offset + i, index.getTimeAt(offset + i), values[i]);
                i++;
                return point;
            }
        };
    }

    @Override
    protected void writeValuesJson(JsonGenerator generator) throws IOException {
        generator.writeArray(values, 0, values.length);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, values);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UncompressedDoubleArrayChunk) {
            UncompressedDoubleArrayChunk other = (UncompressedDoubleArrayChunk) obj;
            return offset == other.offset &&
                    Arrays.equals(values, other.values);
        }
        return false;
    }
}
