/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
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
    public void fillArray(double[] array) {
        System.arraycopy(values, 0, array, offset, values.length);
    }

    public DoubleArrayChunk tryToCompress() {
        TDoubleArrayList stepValues = new TDoubleArrayList();
        TIntArrayList stepLengths = new TIntArrayList();
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
                // compression is not really interesting...
                if (stepValues.size() > values.length * 0.60) {
                    return this;
                }
            }
        }
        return new CompressedDoubleArrayChunk(offset, values.length, stepValues.toArray(), stepLengths.toArray());
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
}
