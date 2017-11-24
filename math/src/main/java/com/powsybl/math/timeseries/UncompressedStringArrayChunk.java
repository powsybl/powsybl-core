/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

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
            estimatedSize += value.length() * Character.BYTES;
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
    public void fillArray(String[] array) {
        Objects.requireNonNull(array);
        if ((offset + values.length) > array.length) {
            throw new IllegalArgumentException("Incorrect array length");
        }
        System.arraycopy(values, 0, array, offset, values.length);
    }

    public StringArrayChunk tryToCompress() {
        List<String> stepValues = new ArrayList<>();
        TIntArrayList stepLengths = new TIntArrayList();
        for (String value : values) {
            if (stepValues.isEmpty()) {
                stepValues.add(value);
                stepLengths.add(1);
            } else {
                int previousIndex = stepValues.size() - 1;
                String previousValue = stepValues.get(previousIndex);
                if (Objects.equals(previousValue, value)) {
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
        return new CompressedStringArrayChunk(offset, values.length, stepValues.toArray(new String[stepValues.size()]),
                                              stepLengths.toArray());
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
