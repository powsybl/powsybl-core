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
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.AbstractTreeDataReader;
import com.powsybl.commons.json.JsonUtil.Context;
import com.powsybl.commons.json.JsonUtil.ContextType;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Function;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class JsonReader extends AbstractTreeDataReader {

    public static final String VERSION_NAME = "version";
    public static final String EXTENSION_VERSIONS_NAME = "extensionVersions";
    private static final String EXTENSION_NAME = "extensionName";
    private final JsonParser parser;
    private boolean currentJsonTokenConsumed = false;
    private final Deque<Context> contextQueue = new ArrayDeque<>();
    private final Map<String, String> arrayElementNameToSingleElementName;

    public JsonReader(InputStream is, String rootName, Map<String, String> arrayNameToSingleNameMap) throws IOException {
        this.parser = JsonUtil.createJsonFactory().createParser(Objects.requireNonNull(is));
        this.parser.nextToken();
        if (parser.currentToken() == JsonToken.START_OBJECT) {
            currentJsonTokenConsumed = true;
        }
        this.contextQueue.add(new Context(ContextType.OBJECT, Objects.requireNonNull(rootName)));
        this.arrayElementNameToSingleElementName = Objects.requireNonNull(arrayNameToSingleNameMap);
    }

    @Override
    public String readRootVersion() {
        return readStringAttribute(VERSION_NAME, true);
    }

    @Override
    public Map<String, String> readExtensionVersions() {
        if (!(EXTENSION_VERSIONS_NAME.equals(getFieldName()))) {
            return Collections.emptyMap();
        }
        currentJsonTokenConsumed = true;
        Map<String, String> versions = new HashMap<>();
        JsonUtil.parseObjectArray(parser, ve -> versions.put(ve.name(), ve.version()), this::parseVersionedExtension);
        return versions;
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
    public OptionalDouble readOptionalDoubleAttribute(String name) {
        return Objects.requireNonNull(name).equals(getFieldName()) ? OptionalDouble.of(getDoubleValue()) : OptionalDouble.empty();
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
                currentJsonTokenConsumed = true;
                return parser.nextTextValue();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public int readIntAttribute(String name) {
        String fieldName = getFieldName();
        if (!Objects.requireNonNull(name).equals(fieldName)) {
            throw new PowsyblException("JSON parsing: expected '" + name + "' but got '" + fieldName + "'");
        }
        return getIntValue();
    }

    @Override
    public OptionalInt readOptionalIntAttribute(String name) {
        return Objects.requireNonNull(name).equals(getFieldName()) ? OptionalInt.of(getIntValue()) : OptionalInt.empty();
    }

    @Override
    public int readIntAttribute(String name, int defaultValue) {
        return Objects.requireNonNull(name).equals(getFieldName()) ? getIntValue() : defaultValue;
    }

    @Override
    public boolean readBooleanAttribute(String name) {
        String fieldName = getFieldName();
        if (!Objects.requireNonNull(name).equals(fieldName)) {
            throw new PowsyblException("JSON parsing: expected '" + name + "' but got '" + fieldName + "'");
        }
        return getBooleanValue();
    }

    @Override
    public boolean readBooleanAttribute(String name, boolean defaultValue) {
        return Objects.requireNonNull(name).equals(getFieldName()) ? getBooleanValue() : defaultValue;
    }

    @Override
    public Optional<Boolean> readOptionalBooleanAttribute(String name) {
        return Objects.requireNonNull(name).equals(getFieldName()) ? Optional.of(getBooleanValue()) : Optional.empty();
    }

    private double getDoubleValue() {
        try {
            currentJsonTokenConsumed = true;
            parser.nextToken();
            return parser.getDoubleValue();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private float getFloatValue() {
        try {
            currentJsonTokenConsumed = true;
            parser.nextToken();
            return parser.getFloatValue();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private int getIntValue() {
        try {
            currentJsonTokenConsumed = true;
            parser.nextToken();
            return parser.getIntValue();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean getBooleanValue() {
        try {
            currentJsonTokenConsumed = true;
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
        String content = readStringAttribute("content");
        readEndNode();
        return content;
    }

    @Override
    public List<Integer> readIntArrayAttribute(String name) {
        return readArrayAttribute(name, JsonUtil::parseIntegerArray);
    }

    @Override
    public List<String> readStringArrayAttribute(String name) {
        return readArrayAttribute(name, JsonUtil::parseStringArray);
    }

    private <T> List<T> readArrayAttribute(String name, Function<JsonParser, List<T>> arrayParser) {
        Objects.requireNonNull(name);
        String fieldName = getFieldName();
        if (!name.equals(fieldName)) {
            return Collections.emptyList();
        }
        currentJsonTokenConsumed = true;
        return arrayParser.apply(parser);
    }

    @Override
    public void readChildNodes(ChildNodeReader childNodeReader) {
        Objects.requireNonNull(childNodeReader);
        try {
            Context startContext = contextQueue.peekLast();
            while (!(getNextToken() == JsonToken.END_OBJECT && contextQueue.peekLast() == startContext)) {
                currentJsonTokenConsumed = true; // token consumed in all cases below
                switch (parser.currentToken()) {
                    case FIELD_NAME -> {
                        switch (parser.nextToken()) {
                            case START_ARRAY -> contextQueue.add(new Context(ContextType.ARRAY, parser.currentName()));
                            case START_OBJECT -> {
                                contextQueue.add(new Context(ContextType.OBJECT, parser.currentName()));
                                childNodeReader.onStartNode(contextQueue.getLast().getFieldName());
                            }
                            default -> throw newUnexpectedTokenException();
                        }
                    }
                    case START_OBJECT -> {
                        Context arrayContext = checkNodeChain(ContextType.ARRAY);
                        contextQueue.add(new Context(ContextType.OBJECT, arrayContext.getFieldName()));
                        childNodeReader.onStartNode(arrayElementNameToSingleElementName.get(arrayContext.getFieldName()));
                    }
                    case END_ARRAY -> {
                        checkNodeChain(ContextType.ARRAY);
                        contextQueue.removeLast();
                    }
                    case END_OBJECT -> throw new PowsyblException("JSON parsing: unexpected END_OBJECT");
                    default -> throw newUnexpectedTokenException();
                }
            }

            currentJsonTokenConsumed = true; // the END_OBJECT token is also consumed
            contextQueue.removeLast();

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Context checkNodeChain(ContextType expectedNodeType) {
        return Optional.ofNullable(contextQueue.peekLast())
                .filter(n -> n.getType() == expectedNodeType)
                .orElseThrow(this::newUnexpectedTokenException);
    }

    private PowsyblException newUnexpectedTokenException() {
        try {
            return new PowsyblException("JSON parsing: unexpected token '" + parser.currentToken() + "'" +
                    " (value = '" + parser.getValueAsString() + "')" +
                    " after field name '" + parser.currentName() + "'");
        } catch (IOException e) {
            return new PowsyblException("JSON parsing: unexpected " + parser.currentToken());
        }
    }

    @Override
    public void readEndNode() {
        try {
            if (getNextToken() != JsonToken.END_OBJECT) {
                throw newUnexpectedTokenException();
            }
            checkNodeChain(ContextType.OBJECT);
            contextQueue.removeLast();
            currentJsonTokenConsumed = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private JsonToken getNextToken() throws IOException {
        if (currentJsonTokenConsumed) {
            currentJsonTokenConsumed = false;
            return parser.nextToken();
        } else {
            return parser.currentToken();
        }
    }

    @Override
    public void close() {
        try {
            parser.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
