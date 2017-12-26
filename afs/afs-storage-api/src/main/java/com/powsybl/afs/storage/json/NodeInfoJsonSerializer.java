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
import com.powsybl.afs.storage.NodeInfo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeInfoJsonSerializer extends StdSerializer<NodeInfo> {

    static final String VALUE = "value";
    static final String NAME = "name";
    static final String ID = "id";
    static final String DESCRIPTION = "description";
    static final String PSEUDO_CLASS = "pseudoClass";
    static final String CREATION_TIME = "creationTime";
    static final String MODIFICATION_TIME = "modificationTime";
    static final String VERSION = "version";
    static final String STRING_METADATA = "stringMetadata";
    static final String DOUBLE_METADATA = "doubleMetadata";
    static final String INT_METADATA = "intMetadata";
    static final String BOOLEAN_METADATA = "booleanMetadata";

    public NodeInfoJsonSerializer() {
        super(NodeInfo.class);
    }

    @Override
    public void serialize(NodeInfo nodeInfo, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
        try {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(ID, nodeInfo.getId().toString());
            jsonGenerator.writeStringField(NAME, nodeInfo.getName());
            jsonGenerator.writeStringField(PSEUDO_CLASS, nodeInfo.getPseudoClass());
            jsonGenerator.writeStringField(DESCRIPTION, nodeInfo.getDescription());
            jsonGenerator.writeNumberField(CREATION_TIME, nodeInfo.getCreationTime());
            jsonGenerator.writeNumberField(MODIFICATION_TIME, nodeInfo.getModificationTime());
            jsonGenerator.writeNumberField(VERSION, nodeInfo.getVersion());
            if (!nodeInfo.getStringMetadata().isEmpty()) {
                jsonGenerator.writeFieldName(STRING_METADATA);
                jsonGenerator.writeStartArray();
                for (Map.Entry<String, String> e : nodeInfo.getStringMetadata().entrySet()) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField(NAME, e.getKey());
                    jsonGenerator.writeStringField(VALUE, e.getValue());
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();
            }
            if (!nodeInfo.getStringMetadata().isEmpty()) {
                jsonGenerator.writeFieldName(DOUBLE_METADATA);
                jsonGenerator.writeStartArray();
                for (Map.Entry<String, Double> e : nodeInfo.getDoubleMetadata().entrySet()) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField(NAME, e.getKey());
                    jsonGenerator.writeNumberField(VALUE, e.getValue());
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();
            }
            if (!nodeInfo.getStringMetadata().isEmpty()) {
                jsonGenerator.writeFieldName(INT_METADATA);
                jsonGenerator.writeStartArray();
                for (Map.Entry<String, Integer> e : nodeInfo.getIntMetadata().entrySet()) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField(NAME, e.getKey());
                    jsonGenerator.writeNumberField(VALUE, e.getValue());
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();
            }
            if (!nodeInfo.getStringMetadata().isEmpty()) {
                jsonGenerator.writeFieldName(BOOLEAN_METADATA);
                jsonGenerator.writeStartArray();
                for (Map.Entry<String, Boolean> e : nodeInfo.getBooleanMetadata().entrySet()) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField(NAME, e.getKey());
                    jsonGenerator.writeBooleanField(VALUE, e.getValue());
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();
            }
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
