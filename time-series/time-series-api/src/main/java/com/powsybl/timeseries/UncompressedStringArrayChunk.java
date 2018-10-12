/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UncompressedStringArrayChunk extends AbstractUncompressedArrayChunk implements StringArrayChunk {

    private final String[] values;

    private final int estimatedSize;

    public UncompressedStringArrayChunk(int offset, String[] values) {
        super(offset);
        this.values = Objects.requireNonNull(values);
        estimatedSize = computeEstimatedSize(values);
    }

    private static int computeEstimatedSize(String[] values) {
        int estimatedSize = 0;
        for (String value : values) {
            if (value != null) {
                estimatedSize += value.length() * Character.BYTES;
            }
        }
        return estimatedSize;
    }

    public String[] getValues() {
        return values;
    }

    @Override
    public int getLength() {
        return values.length;
    }

    @Override
    public int getEstimatedSize() {
        return estimatedSize;
    }

    @Override
    public TimeSeriesDataType getDataType() {
        return TimeSeriesDataType.STRING;
    }

    @Override
    public void fillBuffer(CompactStringBuffer buffer, int timeSeriesOffset) {
        Objects.requireNonNull(buffer);
        for (int i = 0; i < values.length; i++) {
            buffer.putString(timeSeriesOffset + offset + i, values[i]);
        }
    }

    @Override
    public StringArrayChunk tryToCompress() {
        List<String> stepValues = new ArrayList<>();
        TIntArrayList stepLengths = new TIntArrayList();
        int compressedEstimatedSize = 0;
        for (String value : values) {
            if (stepValues.isEmpty()) {
                // create first step
                stepValues.add(value);
                stepLengths.add(1);
                compressedEstimatedSize += CompressedStringArrayChunk.getStepEstimatedSize(value);
            } else {
                int previousIndex = stepValues.size() - 1;
                String previousValue = stepValues.get(previousIndex);
                if (Objects.equals(previousValue, value)) {
                    stepLengths.set(previousIndex, stepLengths.getQuick(previousIndex) + 1);
                } else {
                    // create a new step
                    stepValues.add(value);
                    stepLengths.add(1);
                    compressedEstimatedSize += CompressedStringArrayChunk.getStepEstimatedSize(value);
                }
            }
            if (compressedEstimatedSize > estimatedSize) {
                // compression is inefficient
                return this;
            }
        }
        return new CompressedStringArrayChunk(offset, values.length, stepValues.toArray(new String[stepValues.size()]),
                                              stepLengths.toArray());
    }

    @Override
    public Split<StringPoint, StringArrayChunk> splitAt(int splitIndex) {
        // split at offset is not allowed because it will result to a null left chunk
        if (splitIndex <= offset || splitIndex > (offset + values.length - 1)) {
            throw new IllegalArgumentException("Split index " + splitIndex + " out of chunk range ]" + offset
                    + ", " + (offset + values.length - 1) + "]");
        }
        String[] values1 = new String[splitIndex - offset];
        String[] values2 = new String[values.length - values1.length];
        System.arraycopy(values, 0, values1, 0, values1.length);
        System.arraycopy(values, values1.length, values2, 0, values2.length);
        return new Split<>(new UncompressedStringArrayChunk(offset, values1),
                           new UncompressedStringArrayChunk(splitIndex, values2));
    }

    @Override
    public Stream<StringPoint> stream(TimeSeriesIndex index) {
        Objects.requireNonNull(index);
        return IntStream.range(0, values.length).mapToObj(i -> new StringPoint(offset + i, index.getTimeAt(offset + i), values[i]));
    }

    @Override
    public Iterator<StringPoint> iterator(TimeSeriesIndex index) {
        Objects.requireNonNull(index);
        return new Iterator<StringPoint>() {

            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < values.length;
            }

            @Override
            public StringPoint next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                StringPoint point = new StringPoint(offset + i, index.getTimeAt(offset + i), values[i]);
                i++;
                return point;
            }
        };
    }

    @Override
    protected void writeValuesJson(JsonGenerator generator) throws IOException {
        generator.writeStartArray();
        for (String value : values) {
            generator.writeString(value);
        }
        generator.writeEndArray();
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, values);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UncompressedStringArrayChunk) {
            UncompressedStringArrayChunk other = (UncompressedStringArrayChunk) obj;
            return offset == other.offset &&
                    Arrays.equals(values, other.values);
        }
        return false;
    }
}
