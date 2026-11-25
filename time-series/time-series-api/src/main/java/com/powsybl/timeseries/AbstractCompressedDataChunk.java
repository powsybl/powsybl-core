/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.json.JsonUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractCompressedDataChunk {

    protected final int offset;

    protected final int uncompressedLength;

    protected final int[] stepLengths;

    // Absolute index of the first point of each step, lazily computed to slice in O(log(steps) + slice steps)
    @SuppressWarnings("java:S3077") // stepStarts is lazily computed and immutable, so it is safe to be volatile
    private volatile int[] stepStarts;

    public AbstractCompressedDataChunk(int offset, int uncompressedLength, int[] stepLengths) {
        this.offset = offset;
        this.uncompressedLength = uncompressedLength;
        this.stepLengths = Objects.requireNonNull(stepLengths);
        check(uncompressedLength, stepLengths);
    }

    static void check(int offset, int uncompressedLength, int stepValuesLength, int stepLengthsLength) {
        if (offset < 0) {
            throw new IllegalArgumentException("Bad offset value " + offset);
        }
        if (uncompressedLength < 1) {
            throw new IllegalArgumentException("Bad uncompressed length value " + offset);
        }
        if (stepValuesLength != stepLengthsLength) {
            throw new IllegalArgumentException("Inconsistent step arrays size: "
                    + stepValuesLength + " != " + stepLengthsLength);
        }
        if (stepValuesLength < 1) {
            throw new IllegalArgumentException("Bad step arrays length " + stepValuesLength);
        }
    }

    static void check(int uncompressedLength, int[] stepLengths) {
        if (IntStream.of(stepLengths).sum() != uncompressedLength) {
            throw new IllegalArgumentException("Inconsistent uncompressedLength length: "
                + uncompressedLength + " with sum of step length");
        }
    }

    protected void checkSliceRange(int from, int to) {
        if (from < offset || to > offset + uncompressedLength || from >= to) {
            throw new IllegalArgumentException("Slice [" + from + ", " + to + "[ out of chunk range ["
                    + offset + ", " + (offset + uncompressedLength) + "[");
        }
    }

    private int[] getStepStarts() {
        int[] starts = stepStarts;
        if (starts == null) {
            starts = new int[stepLengths.length];
            int index = offset;
            for (int step = 0; step < stepLengths.length; step++) {
                starts[step] = index;
                index += stepLengths[step];
            }
            stepStarts = starts;
        }
        return starts;
    }

    /**
     * Get the step containing the point at the given absolute index, i.e. the last step starting at or before it
     * (which skips zero-length steps).
     */
    protected int findStep(int index) {
        int[] starts = getStepStarts();
        int low = 0;
        int high = starts.length;
        // find the first step starting after index
        while (low < high) {
            int mid = (low + high) >>> 1;
            if (starts[mid] <= index) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low - 1;
    }

    /**
     * Get the step lengths of the slice {@code [from, to[}, which spans the steps {@code firstStep} to {@code lastStep}.
     *
     * @param from      first absolute index of the slice (inclusive)
     * @param to        last absolute index of the slice (exclusive)
     * @param firstStep position in {@code stepLengths} of the step containing {@code from}
     * @param lastStep  position in {@code stepLengths} of the step containing {@code to - 1}
     */
    protected int[] sliceStepLengths(int from, int to, int firstStep, int lastStep) {
        int[] starts = getStepStarts();
        int[] lengths = Arrays.copyOfRange(stepLengths, firstStep, lastStep + 1);
        // the first and last steps may only partially lie in the slice: trim their lengths
        lengths[0] -= from - starts[firstStep];
        lengths[lengths.length - 1] -= starts[lastStep] + stepLengths[lastStep] - to;
        return lengths;
    }

    public int getOffset() {
        return offset;
    }

    public int[] getStepLengths() {
        return stepLengths;
    }

    public int getUncompressedLength() {
        return uncompressedLength;
    }

    public int getLength() {
        return uncompressedLength;
    }

    public boolean isCompressed() {
        return true;
    }

    protected abstract int getEstimatedSize();

    protected abstract int getUncompressedEstimatedSize();

    public double getCompressionFactor() {
        return ((double) getEstimatedSize()) / getUncompressedEstimatedSize();
    }

    protected abstract void writeStepValuesJson(JsonGenerator generator) throws IOException;

    public void writeJson(JsonGenerator generator) {
        Objects.requireNonNull(generator);
        try {
            generator.writeStartObject();
            generator.writeNumberField("offset", offset);
            generator.writeNumberField("uncompressedLength", uncompressedLength);
            generator.writeFieldName("stepValues");
            writeStepValuesJson(generator);
            generator.writeFieldName("stepLengths");
            generator.writeArray(stepLengths, 0, stepLengths.length);
            generator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toJson() {
        return JsonUtil.toJson(this::writeJson);
    }
}
