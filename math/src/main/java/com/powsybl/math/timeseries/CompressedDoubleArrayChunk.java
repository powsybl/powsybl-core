/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
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

    @Override
    public int getEstimatedSize() {
        return Double.BYTES * stepValues.length + Integer.BYTES * stepLengths.length;
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
    public void fillArray(double[] array) {
        Objects.requireNonNull(array);
        int k = 0;
        for (int i = 0; i < stepValues.length; i++) {
            double value = stepValues[i];
            for (int j = 0; j < stepLengths[i]; j++) {
                array[offset + k++] = value;
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
    protected void writeStepValuesJson(JsonGenerator generator) throws IOException {
        generator.writeArray(stepValues, 0, stepValues.length);
    }
}
