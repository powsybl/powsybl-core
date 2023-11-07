/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class JsonReader implements TreeDataReader {

    public static final String VERSION_NAME = "version";
    public static final String EXTENSION_VERSIONS_NAME = "extensionVersions";
    private static final String EXTENSION_NAME = "extensionName";
    private final JsonParser parser;
    private JsonToken currentJsonToken;
    private final Deque<Node> nodeChain = new ArrayDeque<>();

    public JsonReader(JsonParser parser, String rootName) throws IOException {
        Objects.requireNonNull(parser);
        this.parser = parser;
        this.nodeChain.add(new Node(rootName, JsonNodeType.OBJECT));
    }

    @Override
    public String readRootVersion() {
        return readStringAttribute(VERSION_NAME, true);
    }

    @Override
    public Map<String, String> readVersions() {
        try {
            if (!(getNextToken() == JsonToken.FIELD_NAME && EXTENSION_VERSIONS_NAME.equals(parser.currentName()))) {
                return Collections.emptyMap();
            }
            currentJsonToken = null;
            Map<String, String> versions = new HashMap<>();
            JsonUtil.parseObjectArray(parser, ve -> versions.put(ve.name(), ve.version()), this::parseVersionedExtension);
            return versions;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private record VersionedExtension(String name, String version) {
    }

    private VersionedExtension parseVersionedExtension(JsonParser parser) {
        try {
            String extensionName = readStringAttribute(EXTENSION_NAME, true);
            String version = readStringAttribute(VERSION_NAME, true);
            parser.nextToken();
            return new VersionedExtension(extensionName, version);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private PowsyblException createUnexpectedNameException(String expected, String actual) {
        return new PowsyblException("Unexpected name: '" + expected + "' expected but found " + actual);
    }

    @Override
    public double readDoubleAttribute(String name) {
        return readDoubleAttribute(name, Double.NaN);
    }

    @Override
    public double readDoubleAttribute(String name, double defaultValue) {
        return Objects.requireNonNull(name).equals(getFieldName()) ? getDoubleValue() : defaultValue;
    }

    @Override
    public float readFloatAttribute(String name) {
        return readFloatAttribute(name, Float.NaN);
    }

    @Override
    public float readFloatAttribute(String name, float defaultValue) {
        return Objects.requireNonNull(name).equals(getFieldName()) ? getFloatValue() : defaultValue;
    }

    @Override
    public String readStringAttribute(String name) {
        return readStringAttribute(name, false);
    }

    private String readStringAttribute(String name, boolean throwException) {
        Objects.requireNonNull(name);
        try {
            String fieldName = getFieldName();
            if (!name.equals(fieldName)) {
                if (throwException) {
                    throw createUnexpectedNameException(name, fieldName);
                }
                return null;
            } else {
                currentJsonToken = null;
                return parser.nextTextValue();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Integer readIntAttribute(String name) {
        return Objects.requireNonNull(name).equals(getFieldName()) ? getIntValue() : null;
    }

    @Override
    public int readIntAttribute(String name, int defaultValue) {
        return Objects.requireNonNull(name).equals(getFieldName()) ? getIntValue() : defaultValue;
    }

    @Override
    public Boolean readBooleanAttribute(String name) {
        return Objects.requireNonNull(name).equals(getFieldName()) ? getBooleanValue() : null;
    }

    @Override
    public boolean readBooleanAttribute(String name, boolean defaultValue) {
        return Objects.requireNonNull(name).equals(getFieldName()) ? getBooleanValue() : defaultValue;
    }

    private double getDoubleValue() {
        try {
            currentJsonToken = null;
            parser.nextToken();
            return parser.getDoubleValue();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private float getFloatValue() {
        try {
            currentJsonToken = null;
            parser.nextToken();
            return parser.getFloatValue();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private int getIntValue() {
        try {
            currentJsonToken = null;
            parser.nextToken();
            return parser.getIntValue();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean getBooleanValue() {
        try {
            currentJsonToken = null;
            parser.nextToken();
            return parser.getBooleanValue();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz) {
        return readEnumAttribute(name, clazz, null);
    }

    @Override
    public <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz, T defaultValue) {
        String attributeValue = readStringAttribute(name);
        return attributeValue != null ? Enum.valueOf(clazz, attributeValue) : defaultValue;
    }

    public String getFieldName() {
        try {
            return getNextToken() == JsonToken.FIELD_NAME ? parser.currentName() : null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getNodeName() {
        return Optional.ofNullable(nodeChain.peekLast())
                .map(Node::name)
                .orElseThrow(() -> new PowsyblException("JSON reader reached EOF"));
    }

    @Override
    public String readContent() {
        return readStringAttribute("content");
    }

    @Override
    public String readText(String endNodeName) {
        return readUntilEndNode(endNodeName, () -> { });
    }

    @Override
    public List<Integer> readIntArrayAttribute(String name) {
        Objects.requireNonNull(name);
        String fieldName = getFieldName();
        currentJsonToken = null;
        if (!name.equals(fieldName)) {
            throw createUnexpectedNameException(name, fieldName);
        }
        return JsonUtil.parseIntegerArray(parser);
    }

    @Override
    public String readUntilEndNode(String endElementName, EventHandler eventHandler) {
        return readUntilEndNodeWithDepth(endElementName, elementDepth -> {
            if (eventHandler != null) {
                eventHandler.onStartElement();
            }
        });
    }

    @Override
    public String readUntilEndNodeWithDepth(String endElementName, EventHandlerWithDepth eventHandler) {
        Objects.requireNonNull(endElementName);
        int depth = 0;
        String text = null;
        try {
            while (!(getNextToken() == JsonToken.END_OBJECT && depth == 0)) {
                currentJsonToken = null; // the token will be consumed in all cases below
                switch (parser.currentToken()) {
                    case FIELD_NAME -> nodeChain.add(new Node(parser.currentName(), JsonNodeType.NULL));
                    case START_ARRAY -> {
                        Node arrayNode = Optional.ofNullable(nodeChain.pollLast())
                                .filter(n -> n.jsonNodeType() == JsonNodeType.NULL)
                                .orElseThrow(() -> new PowsyblException("JSON parsing: array started without a preceding field"));
                        nodeChain.add(new Node(arrayNode.name(), JsonNodeType.ARRAY));
                    }
                    case START_OBJECT -> {
                        Node arrayNode = Optional.ofNullable(nodeChain.peekLast())
                                .filter(n -> n.jsonNodeType() == JsonNodeType.NULL || n.jsonNodeType() == JsonNodeType.ARRAY)
                                .orElseThrow(() -> new PowsyblException("JSON parsing: object started without a preceding field"));
                        if (arrayNode.jsonNodeType() == JsonNodeType.NULL) {
                            nodeChain.removeLast();
                            nodeChain.add(new Node(arrayNode.name(), JsonNodeType.OBJECT));
                        }
                        if (eventHandler != null) {
                            eventHandler.onStartElement(depth);
                        }
                        depth++;
                    }
                    case END_OBJECT -> {
                        depth--;
                        Node arrayNode = Optional.ofNullable(nodeChain.peekLast())
                                .orElseThrow(() -> new PowsyblException("JSON parsing: object end reached without a preceding start"));
                        if (arrayNode.jsonNodeType() == JsonNodeType.OBJECT) {
                            nodeChain.removeLast();
                        }
                    }
                    case END_ARRAY -> {
                        Node arrayNode = nodeChain.removeLast();
                        if (arrayNode.jsonNodeType() != JsonNodeType.ARRAY) {
                            throw new PowsyblException("JSON parsing: array end reached without a preceding start");
                        }
                    }
                    case VALUE_STRING -> text = parser.getText();
                }
            }
            // Exiting the while means currentJsonToken == JsonToken.END_OBJECT && depth == 0
            // As it could be nested within another readUntilEndNodeWithDepth, the token should not be consumed,
            // for the depth to be decreased in the calling readUntilEndNodeWithDepth

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return text;
    }

    private JsonToken getNextToken() throws IOException {
        if (currentJsonToken == null) {
            currentJsonToken = parser.nextToken();
        }
        return currentJsonToken;
    }

    @Override
    public void close() {
        try {
            parser.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private record Node(String name, JsonNodeType jsonNodeType) {
    }
}
