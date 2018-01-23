/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.afs.storage.NodeGenericMetadata;

import java.io.IOException;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeGenericMetadataJsonSerializer extends StdSerializer<NodeGenericMetadata> {

    static final String VALUE = "value";
    static final String NAME = "name";
    static final String TYPE = "type";
    static final String STRING = "string";
    static final String DOUBLE = "double";
    static final String INT = "int";
    static final String BOOLEAN = "boolean";

    public NodeGenericMetadataJsonSerializer() {
        super(NodeGenericMetadata.class);
    }

    @Override
    public void serialize(NodeGenericMetadata nodeMetadata, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();
        for (Map.Entry<String, String> e : nodeMetadata.getStrings().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(TYPE, STRING);
            jsonGenerator.writeStringField(NAME, e.getKey());
            jsonGenerator.writeStringField(VALUE, e.getValue());
            jsonGenerator.writeEndObject();
        }
        for (Map.Entry<String, Double> e : nodeMetadata.getDoubles().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(TYPE, DOUBLE);
            jsonGenerator.writeStringField(NAME, e.getKey());
            jsonGenerator.writeNumberField(VALUE, e.getValue());
            jsonGenerator.writeEndObject();
        }
        for (Map.Entry<String, Integer> e : nodeMetadata.getInts().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(TYPE, INT);
            jsonGenerator.writeStringField(NAME, e.getKey());
            jsonGenerator.writeNumberField(VALUE, e.getValue());
            jsonGenerator.writeEndObject();
        }
        for (Map.Entry<String, Boolean> e : nodeMetadata.getBooleans().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(TYPE, BOOLEAN);
            jsonGenerator.writeStringField(NAME, e.getKey());
            jsonGenerator.writeBooleanField(VALUE, e.getValue());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
    }
}
