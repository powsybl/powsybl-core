/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CompressedStringArrayChunk extends AbstractCompressedArrayChunk implements StringArrayChunk {

    private final String[] stepValues;

    private int estimatedSize;

    private int uncompressedEstimatedSize;

    public CompressedStringArrayChunk(int offset, int uncompressedLength, String[] stepValues, int[] stepLengths) {
        super(offset, uncompressedLength, stepLengths);
        check(offset, uncompressedLength, stepValues.length, stepLengths.length);
        this.stepValues = Objects.requireNonNull(stepValues);
        updateEstimatedSize();
    }

    static int getStepEstimatedSize(String value) {
        int estimatedSize = Integer.BYTES;
        if (value != null) {
            estimatedSize += value.length() * Character.BYTES;
        }
        return estimatedSize;
    }

    private void updateEstimatedSize() {
        estimatedSize = 0;
        uncompressedEstimatedSize = 0;
        for (int i = 0; i < stepValues.length; i++) {
            String stepValue = stepValues[i];
            if (stepValue != null) {
                int stepLength = stepLengths[i];
                uncompressedEstimatedSize += stepValue.length() * Character.BYTES * stepLength;
            }
            estimatedSize += getStepEstimatedSize(stepValue);
        }
    }

    public String[] getStepValues() {
        return stepValues;
    }

    @Override
    public int getEstimatedSize() {
        return estimatedSize;
    }

    @Override
    public int getUncompressedEstimatedSize() {
        return uncompressedEstimatedSize;
    }

    @Override
    public TimeSeriesDataType getDataType() {
        return TimeSeriesDataType.STRING;
    }

    @Override
    public void fillBuffer(CompactStringBuffer buffer, int timeSeriesOffset) {
        Objects.requireNonNull(buffer);
        int k = 0;
        for (int i = 0; i < stepValues.length; i++) {
            String value = stepValues[i];
            for (int j = 0; j < stepLengths[i]; j++) {
                buffer.putString(timeSeriesOffset + offset + k++, value);
            }
        }
    }

    @Override
    public Iterator<StringPoint> iterator(TimeSeriesIndex index) {
        Objects.requireNonNull(index);
        return new Iterator<StringPoint>() {

            private int i = offset;
            private int step = 0;

            @Override
            public boolean hasNext() {
                return i < offset + uncompressedLength;
            }

            @Override
            public StringPoint next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                StringPoint point = new StringPoint(i, index.getTimeAt(i), stepValues[step]);
                i += stepLengths[step];
                step++;
                return point;
            }
        };
    }

    @Override
    public Stream<StringPoint> stream(TimeSeriesIndex index) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iterator(index),
                Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
    }

    @Override
    public StringArrayChunk tryToCompress() {
        return this;
    }

    @Override
    public Split<StringPoint, StringArrayChunk> splitAt(int splitIndex) {
        throw new UnsupportedOperationException("TODO"); // TODO
    }

    @Override
    protected void writeStepValuesJson(JsonGenerator generator) throws IOException {
        generator.writeStartArray();
        for (String stepValue : stepValues) {
            generator.writeString(stepValue);
        }
        generator.writeEndArray();
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, uncompressedLength, stepLengths, stepValues);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CompressedStringArrayChunk) {
            CompressedStringArrayChunk other = (CompressedStringArrayChunk) obj;
            return offset == other.offset &&
                    uncompressedLength == other.uncompressedLength &&
                    Arrays.equals(stepLengths, other.stepLengths) &&
                    Arrays.equals(stepValues, other.stepValues);
        }
        return false;
    }
}
