/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.xml;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class JsonWriter implements TreeDataWriter {

    private final JsonGenerator jsonGenerator;

    public JsonWriter(JsonGenerator jsonGenerator) {
        this.jsonGenerator = Objects.requireNonNull(jsonGenerator);
    }

    @Override
    public void writeStartNode(String ns, String name) {
        try {
            jsonGenerator.writeStartObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeEmptyNode(String ns, String name) {
        try {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeEndNode() {
        try {
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeNodeContent(String value) {

    }

    @Override
    public void writeStringAttribute(String name, String value) {
        try {
            if (value != null) {
                jsonGenerator.writeStringField(name, value);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeFloatAttribute(String name, float value) {
        try {
            if (!Float.isNaN(value)) {
                jsonGenerator.writeNumberField(name, value);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeDoubleAttribute(String name, double value) {
        try {
            if (!Double.isNaN(value)) {
                jsonGenerator.writeNumberField(name, value);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeDoubleAttribute(String name, double value, double absentValue) {
        if (value != absentValue) {
            writeDoubleAttribute(name, value);
        }
    }

    @Override
    public void writeIntAttribute(String name, int value) {
        try {
            jsonGenerator.writeNumberField(name, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeIntAttribute(String name, int value, int absentValue) {
        if (value != absentValue) {
            writeIntAttribute(name, value);
        }
    }

    @Override
    public <E extends Enum<E>> void writeEnumAttribute(String name, E value) {

    }

    @Override
    public void writeBooleanAttribute(String name, boolean value) {
        try {
            jsonGenerator.writeBooleanField(name, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeBooleanAttribute(String name, boolean value, boolean absentValue) {
        if (value != absentValue) {
            writeBooleanAttribute(name, value);
        }
    }

    @Override
    public void close() {
        try {
            jsonGenerator.writeEndObject();
            jsonGenerator.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
