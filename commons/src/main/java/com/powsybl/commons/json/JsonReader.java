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
import com.powsybl.commons.io.AbstractTreeDataReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class JsonReader extends AbstractTreeDataReader {

    public static final String VERSION_NAME = "version";
    public static final String EXTENSION_VERSIONS_NAME = "extensionVersions";
    private static final String EXTENSION_NAME = "extensionName";
    private final JsonParser parser;
    private JsonToken currentJsonToken;
    private final Deque<Node> nodeChain = new ArrayDeque<>();
    private final Map<String, String> arrayElementNameToSingleElementName;

    public JsonReader(JsonParser parser, String rootName, Map<String, String> arrayNameToSingleNameMap) throws IOException {
        this.parser = Objects.requireNonNull(parser);
        this.nodeChain.add(new Node(Objects.requireNonNull(rootName), JsonNodeType.OBJECT));
        this.arrayElementNameToSingleElementName = Objects.requireNonNull(arrayNameToSingleNameMap);
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
    public double readDoubleAttribute(String name, double defaultValue) {
        return Objects.requireNonNull(name).equals(getFieldName()) ? getDoubleValue() : defaultValue;
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

    public String getFieldName() {
        try {
            return getNextToken() == JsonToken.FIELD_NAME ? parser.currentName() : null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String readContent() {
        return readStringAttribute("content");
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
    public void readChildNodes(ChildNodeReader childNodeReader) {
        Objects.requireNonNull(childNodeReader);
        try {
            Node startNode = nodeChain.peekLast();
            while (!(getNextToken() == JsonToken.END_OBJECT && nodeChain.peekLast() == startNode)) {
                currentJsonToken = null; // the token will be consumed in all cases below
                switch (parser.currentToken()) {
                    case FIELD_NAME -> {
                        switch (parser.nextToken()) {
                            case START_ARRAY -> nodeChain.add(new Node(parser.currentName(), JsonNodeType.ARRAY));
                            case START_OBJECT -> {
                                nodeChain.add(new Node(parser.currentName(), JsonNodeType.OBJECT));
                                childNodeReader.onStartNode(nodeChain.getLast().name());
                            }
                        }
                    }
                    case START_OBJECT -> {
                        Node arrayNode = checkNodeChain(JsonNodeType.ARRAY);
                        nodeChain.add(new Node(parser.currentName(), JsonNodeType.OBJECT));
                        childNodeReader.onStartNode(arrayElementNameToSingleElementName.get(arrayNode.name()));
                    }
                    case END_ARRAY -> {
                        checkNodeChain(JsonNodeType.ARRAY);
                        nodeChain.removeLast();
                    }
                    case END_OBJECT -> throw new PowsyblException("JSON parsing: unexpected END_OBJECT");
                }
            }

            currentJsonToken = null;
            nodeChain.removeLast();

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Node checkNodeChain(JsonNodeType expectedNodeType) {
        return Optional.ofNullable(nodeChain.peekLast())
                .filter(n -> n.jsonNodeType == expectedNodeType)
                .orElseThrow(() -> new PowsyblException("JSON parsing: unexpected " + parser.currentToken()));
    }

    @Override
    public void readEndNode() {
        try {
            if (getNextToken() != JsonToken.END_OBJECT) {
                throw new PowsyblException("JSON parsing: unexpected end node");
            }
            checkNodeChain(JsonNodeType.OBJECT);
            nodeChain.removeLast();
            currentJsonToken = null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
