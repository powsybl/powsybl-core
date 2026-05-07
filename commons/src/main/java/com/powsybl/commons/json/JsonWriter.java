/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.json;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.AbstractTreeDataWriter;
import com.powsybl.commons.json.JsonUtil.Context;
import com.powsybl.commons.json.JsonUtil.ContextType;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonEncoding;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.ObjectWriteContext;

import java.io.OutputStream;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class JsonWriter extends AbstractTreeDataWriter {

    public static final String VERSION = "version";
    private static final String EXTENSION_VERSIONS = "extensionVersions";
    private final JsonGenerator jsonGenerator;
    private final String rootVersion;
    private final Map<String, String> singleNameToArrayNameMap;
    private Map<String, String> extensionVersions;

    private final Deque<Context> contextQueue = new ArrayDeque<>();

    public JsonWriter(OutputStream os, boolean indent, String rootVersion, Map<String, String> singleNameToArrayNameMap) throws JacksonException {
        ObjectWriteContext ppContext = indent ?
            JsonUtil.getObjectWriteContextWithDefaultPrettyPrinter() :
            ObjectWriteContext.empty();
        this.jsonGenerator = JsonUtil.createJsonFactory().createGenerator(ppContext, os, JsonEncoding.UTF8);
        this.rootVersion = Objects.requireNonNull(rootVersion);
        this.singleNameToArrayNameMap = Objects.requireNonNull(singleNameToArrayNameMap);
    }

    @Override
    public void setVersions(Map<String, String> extensionVersions) {
        this.extensionVersions = Objects.requireNonNull(extensionVersions);
    }

    @Override
    public void writeStartNodes() {
        contextQueue.push(new Context(ContextType.ARRAY, null));
    }

    @Override
    public void writeEndNodes() {
        Context context = Objects.requireNonNull(contextQueue.pop());
        if (context.getType() != ContextType.ARRAY) {
            throw new IllegalStateException();
        }
        if (context.getFieldName() != null) {
            jsonGenerator.writeEndArray();
        }
    }

    @Override
    public void writeStartNode(String namespace, String name) {
        Context context = contextQueue.peekFirst();
        if (context != null) {
            if (context.getType() == ContextType.ARRAY) {
                if (context.getFieldName() == null) {
                    String arrayFieldName = singleNameToArrayNameMap.get(name);
                    if (arrayFieldName == null) {
                        throw new PowsyblException(String.format("Cannot write start node %s with unknown corresponding array name", name));
                    }
                    context.setFieldName(arrayFieldName);
                    jsonGenerator.writeName(arrayFieldName);
                    jsonGenerator.writeStartArray();
                }
            } else if (context.getType() == ContextType.OBJECT) {
                jsonGenerator.writeName(name);
            }
            jsonGenerator.writeStartObject();
        } else {
            jsonGenerator.writeStartObject();
            writeStringAttribute(VERSION, rootVersion);
            writeExtensionVersions();
        }
        contextQueue.push(new Context(ContextType.OBJECT, name));
    }

    private void writeExtensionVersions() throws JacksonException {
        if (!extensionVersions.isEmpty()) {
            jsonGenerator.writeName(EXTENSION_VERSIONS);
            jsonGenerator.writeStartArray();
            extensionVersions.forEach((extensionName, version) -> {
                jsonGenerator.writeStartObject();
                writeStringAttribute("extensionName", extensionName);
                writeStringAttribute(VERSION, version);
                jsonGenerator.writeEndObject();
            });
            jsonGenerator.writeEndArray();
        }
    }

    @Override
    public void writeEndNode() {
        jsonGenerator.writeEndObject();
        contextQueue.pop();
    }

    @Override
    public void writeNamespace(String prefix, String namespace) {
        // nothing to do
    }

    @Override
    public void writeNodeContent(String value) {
        writeStringAttribute("content", value);
    }

    @Override
    public void writeStringAttribute(String name, String value) {
        if (value != null) {
            jsonGenerator.writeStringProperty(name, value);
        }
    }

    @Override
    public void writeFloatAttribute(String name, float value) {
        if (!Float.isNaN(value)) {
            jsonGenerator.writeNumberProperty(name, value);
        }
    }

    @Override
    public void writeDoubleAttribute(String name, double value) {
        if (!Double.isNaN(value)) {
            jsonGenerator.writeNumberProperty(name, value);
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
        jsonGenerator.writeNumberProperty(name, value);
    }

    @Override
    public void writeIntAttribute(String name, int value, int absentValue) {
        if (value != absentValue) {
            writeIntAttribute(name, value);
        }
    }

    @Override
    public void writeIntArrayAttribute(String name, Collection<Integer> values) {
        if (!values.isEmpty()) {
            jsonGenerator.writeName(name);
            jsonGenerator.writeStartArray();
            for (int value : values) {
                jsonGenerator.writeNumber(value);
            }
            jsonGenerator.writeEndArray();
        }
    }

    @Override
    public void writeStringArrayAttribute(String name, Collection<String> values) {
        if (!values.isEmpty()) {
            jsonGenerator.writeName(name);
            jsonGenerator.writeStartArray();
            for (String value : values) {
                jsonGenerator.writeString(value);
            }
            jsonGenerator.writeEndArray();
        }
    }

    @Override
    public <E extends Enum<E>> void writeEnumAttribute(String name, E value) {
        if (value != null) {
            writeStringAttribute(name, value.name());
        }
    }

    @Override
    public void writeBooleanAttribute(String name, boolean value) {
        jsonGenerator.writeBooleanProperty(name, value);
    }

    @Override
    public void writeBooleanAttribute(String name, boolean value, boolean absentValue) {
        if (value != absentValue) {
            writeBooleanAttribute(name, value);
        }
    }

    @Override
    public void close() {
        jsonGenerator.close();
    }
}
