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

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractUncompressedDataChunk {

    private static final double COMPRESSION_FACTOR = 1d;

    protected final int offset;

    public AbstractUncompressedDataChunk(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Bad offset value " + offset);
        }
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public boolean isCompressed() {
        return false;
    }

    public double getCompressionFactor() {
        return COMPRESSION_FACTOR;
    }

    protected abstract void writeValuesJson(JsonGenerator generator) throws IOException;

    public void writeJson(JsonGenerator generator) {
        Objects.requireNonNull(generator);
        try {
            generator.writeStartObject();
            generator.writeNumberField("offset", offset);
            generator.writeFieldName("values");
            writeValuesJson(generator);
            generator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toJson() {
        return JsonUtil.toJson(this::writeJson);
    }
}
