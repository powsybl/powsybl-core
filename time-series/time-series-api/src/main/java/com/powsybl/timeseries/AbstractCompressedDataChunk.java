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
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractCompressedDataChunk {

    protected final int offset;

    protected final int uncompressedLength;

    protected final int[] stepLengths;

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
