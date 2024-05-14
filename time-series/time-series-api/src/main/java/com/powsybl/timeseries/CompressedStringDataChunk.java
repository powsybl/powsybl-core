/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.*;
import java.util.function.ObjIntConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * RLE (Run-Length encoding) compressed string data chunk.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class CompressedStringDataChunk extends AbstractCompressedDataChunk implements StringDataChunk {

    private final String[] stepValues;

    private int estimatedSize;

    private int uncompressedEstimatedSize;

    public CompressedStringDataChunk(int offset, int uncompressedLength, String[] stepValues, int[] stepLengths) {
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

    private void forEachMaterializedValueIndex(ObjIntConsumer<String> consumer) {
        int k = 0;
        for (int i = 0; i < stepValues.length; i++) {
            String value = stepValues[i];
            for (int j = 0; j < stepLengths[i]; j++) {
                consumer.accept(value, offset + k++);
            }
        }
    }

    @Override
    public void fillBuffer(CompactStringBuffer buffer, int timeSeriesOffset) {
        Objects.requireNonNull(buffer);
        forEachMaterializedValueIndex((v, i) -> buffer.putString(timeSeriesOffset + i, v));
    }

    @Override
    public void fillBuffer(BigStringBuffer buffer, long timeSeriesOffset) {
        Objects.requireNonNull(buffer);
        forEachMaterializedValueIndex((v, i) -> buffer.putString(timeSeriesOffset + i, v));
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
    public StringDataChunk tryToCompress() {
        return this;
    }

    @Override
    public Split<StringPoint, StringDataChunk> splitAt(int splitIndex) {
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
                String[] stepValues1 = new String[stepLengths1.length];
                System.arraycopy(stepLengths, 0, stepLengths1, 0, stepLengths1.length);
                System.arraycopy(stepValues, 0, stepValues1, 0, stepValues1.length);
                stepLengths1[step] = splitIndex - index;
                CompressedStringDataChunk chunk1 = new CompressedStringDataChunk(offset, splitIndex - offset, stepValues1, stepLengths1);

                // second chunk
                int[] stepLengths2 = new int[stepLengths.length - step];
                String[] stepValues2 = new String[stepLengths2.length];
                System.arraycopy(stepLengths, step, stepLengths2, 0, stepLengths2.length);
                System.arraycopy(stepValues, step, stepValues2, 0, stepValues2.length);
                stepLengths2[0] = stepLengths[step] - stepLengths1[step];
                CompressedStringDataChunk chunk2 = new CompressedStringDataChunk(splitIndex, uncompressedLength - chunk1.uncompressedLength, stepValues2, stepLengths2);

                return new Split<>(chunk1, chunk2);
            }
            index += stepLengths[step];
        }
        throw new IllegalStateException("Should not happen");
    }

    @Override
    public StringDataChunk append(final StringDataChunk otherChunk) {
        if (getOffset() + getLength() != otherChunk.getOffset()) {
            throw new IllegalArgumentException("Chunks are not successive. First offset is " + getOffset()
                                               + " and first size is " + getLength() + "; second offset should be " +
                                               (getOffset() + getLength()) + "but is " + otherChunk.getOffset());
        }
        if (!(otherChunk instanceof CompressedStringDataChunk)) {
            throw new IllegalArgumentException("The chunks to merge have to have the same implementation. One of them is " + this.getClass()
                                               + ", the other one is " + otherChunk.getClass());
        }
        CompressedStringDataChunk chunk = (CompressedStringDataChunk) otherChunk;
        int[] newStepLengths;
        String[] newStepValues;

        if (stepValues[stepValues.length - 1].equals(chunk.getStepValues()[0])) {
            //The last value of the first chunk is equals to the first value of the second one
            // -> the first step of the second chunk needs to be erased

            //Step lengths
            newStepLengths = new int[stepLengths.length + chunk.getStepLengths().length - 1];
            System.arraycopy(stepLengths, 0, newStepLengths, 0, stepLengths.length);
            newStepLengths[stepLengths.length - 1] = stepLengths[stepLengths.length - 1] + newStepLengths[0];
            System.arraycopy(chunk.getStepLengths(), 1, newStepLengths, stepLengths.length, chunk.getStepLengths().length - 1);

            //Step values
            newStepValues = new String[newStepLengths.length];
            System.arraycopy(stepValues, 0, newStepValues, 0, stepValues.length);
            System.arraycopy(chunk.getStepValues(), 1, newStepValues, stepValues.length, chunk.getStepValues().length - 1);
        } else {
            //The last value of the first chunk is different from to the first value of the second one
            // -> both chunks have to be copied completely

            //Step lengths
            newStepLengths = new int[stepLengths.length + chunk.getStepLengths().length];
            System.arraycopy(stepLengths, 0, newStepLengths, 0, stepLengths.length);
            System.arraycopy(chunk.getStepLengths(), 0, newStepLengths, stepLengths.length, chunk.getStepLengths().length);

            //Step values
            newStepValues = new String[newStepLengths.length];
            System.arraycopy(stepValues, 0, newStepValues, 0, stepValues.length);
            System.arraycopy(chunk.getStepValues(), 0, newStepValues, stepValues.length, chunk.getStepValues().length);

        }

        return new CompressedStringDataChunk(offset, uncompressedLength + chunk.getUncompressedLength(), newStepValues, newStepLengths);
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
        if (obj instanceof CompressedStringDataChunk other) {
            return offset == other.offset &&
                    uncompressedLength == other.uncompressedLength &&
                    Arrays.equals(stepLengths, other.stepLengths) &&
                    Arrays.equals(stepValues, other.stepValues);
        }
        return false;
    }
}
