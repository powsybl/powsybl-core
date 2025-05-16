/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProviders;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public final class JsonUtil {

    private static final String UNEXPECTED_TOKEN = "Unexpected token ";

    enum ContextType {
        OBJECT,
        ARRAY
    }

    static final class Context {
        private final ContextType type;
        private String fieldName;

        Context(ContextType type, String fieldName) {
            this.type = Objects.requireNonNull(type);
            this.fieldName = fieldName;
        }

        ContextType getType() {
            return type;
        }

        String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }
    }

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class));

    private JsonUtil() {
    }

    public static ObjectMapper createObjectMapper() {
        return JsonMapper.builder()
                .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .disable(JsonWriteFeature.WRITE_NAN_AS_STRINGS)
                .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
            .build();
    }

    public static void writeJson(Path jsonFile, Object object, ObjectMapper objectMapper) {
        Objects.requireNonNull(jsonFile);
        Objects.requireNonNull(object);
        Objects.requireNonNull(objectMapper);
        try (Writer writer = Files.newBufferedWriter(jsonFile, StandardCharsets.UTF_8)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, object);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T readJson(Path jsonFile, Class<T> clazz, ObjectMapper objectMapper) {
        Objects.requireNonNull(jsonFile);
        Objects.requireNonNull(objectMapper);
        try (Reader reader = Files.newBufferedReader(jsonFile, StandardCharsets.UTF_8)) {
            return objectMapper.readValue(reader, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T readJsonAndUpdate(InputStream is, T object, ObjectMapper objectMapper) {
        Objects.requireNonNull(is);
        Objects.requireNonNull(object);
        Objects.requireNonNull(objectMapper);
        try {
            return objectMapper.readerForUpdating(object).readValue(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T readJsonAndUpdate(Path jsonFile, T object, ObjectMapper objectMapper) {
        Objects.requireNonNull(jsonFile);
        Objects.requireNonNull(object);
        Objects.requireNonNull(objectMapper);
        try (Reader reader = Files.newBufferedReader(jsonFile, StandardCharsets.UTF_8)) {
            return objectMapper.readerForUpdating(object).readValue(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static JsonFactory createJsonFactory() {
        return new JsonFactoryBuilder()
            .disable(JsonWriteFeature.WRITE_NAN_AS_STRINGS)
            .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
            .build();
    }

    public static void writeJson(Writer writer, Consumer<JsonGenerator> consumer) {
        Objects.requireNonNull(writer);
        Objects.requireNonNull(consumer);
        JsonFactory factory = createJsonFactory();
        try (JsonGenerator generator = factory.createGenerator(writer)) {
            generator.useDefaultPrettyPrinter();
            consumer.accept(generator);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String toJson(Consumer<JsonGenerator> consumer) {
        try (StringWriter writer = new StringWriter()) {
            writeJson(writer, consumer);
            return writer.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void writeJson(Path file, Consumer<JsonGenerator> consumer) {
        Objects.requireNonNull(file);
        Objects.requireNonNull(consumer);
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writeJson(writer, consumer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T parseJson(Path file, Function<JsonParser, T> function) {
        Objects.requireNonNull(file);
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return parseJson(reader, function);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T parseJson(String json, Function<JsonParser, T> function) {
        Objects.requireNonNull(json);
        try (StringReader reader = new StringReader(json)) {
            return parseJson(reader, function);
        }
    }

    public static <T> T parseJson(Reader reader, Function<JsonParser, T> function) {
        Objects.requireNonNull(reader);
        Objects.requireNonNull(function);
        JsonFactory factory = createJsonFactory();
        try (JsonParser parser = factory.createParser(reader)) {
            return function.apply(parser);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void writeOptionalStringField(JsonGenerator jsonGenerator, String fieldName, String value) throws IOException {
        Objects.requireNonNull(jsonGenerator);
        Objects.requireNonNull(fieldName);

        if (!Strings.isNullOrEmpty(value)) {
            jsonGenerator.writeStringField(fieldName, value);
        }
    }

    public static void writeOptionalEnumField(JsonGenerator jsonGenerator, String fieldName, Enum<?> value) throws IOException {
        Objects.requireNonNull(jsonGenerator);
        Objects.requireNonNull(fieldName);

        if (value != null) {
            jsonGenerator.writeStringField(fieldName, value.name());
        }
    }

    public static void writeOptionalBooleanField(JsonGenerator jsonGenerator, String fieldName, boolean value, boolean defaultValue) throws IOException {
        Objects.requireNonNull(jsonGenerator);
        Objects.requireNonNull(fieldName);

        if (value != defaultValue) {
            jsonGenerator.writeBooleanField(fieldName, value);
        }
    }

    public static void writeOptionalFloatField(JsonGenerator jsonGenerator, String fieldName, float value) throws IOException {
        Objects.requireNonNull(jsonGenerator);
        Objects.requireNonNull(fieldName);

        if (!Float.isNaN(value)) {
            jsonGenerator.writeNumberField(fieldName, value);
        }
    }

    public static void writeOptionalDoubleField(JsonGenerator jsonGenerator, String fieldName, double value) throws IOException {
        Objects.requireNonNull(jsonGenerator);
        Objects.requireNonNull(fieldName);

        if (!Double.isNaN(value)) {
            jsonGenerator.writeNumberField(fieldName, value);
        }
    }

    public static void writeOptionalDoubleField(JsonGenerator jsonGenerator, String fieldName, double value, double defaultValue) throws IOException {
        Objects.requireNonNull(jsonGenerator);
        Objects.requireNonNull(fieldName);

        if (!Double.isNaN(value) && value != defaultValue) {
            jsonGenerator.writeNumberField(fieldName, value);
        }
    }

    public static void writeOptionalIntegerField(JsonGenerator jsonGenerator, String fieldName, int value) throws IOException {
        Objects.requireNonNull(jsonGenerator);
        Objects.requireNonNull(fieldName);

        if (value != Integer.MAX_VALUE) {
            jsonGenerator.writeNumberField(fieldName, value);
        }
    }

    public static <T> Set<String> writeExtensions(Extendable<T> extendable, JsonGenerator jsonGenerator,
                                                  SerializerProvider serializerProvider) throws IOException {
        return writeExtensions(extendable, jsonGenerator, serializerProvider, SUPPLIER.get());
    }

    public static <T> Set<String> writeExtensions(Extendable<T> extendable, JsonGenerator jsonGenerator,
                                                  SerializerProvider serializerProvider,
                                                  ExtensionProviders<? extends ExtensionJsonSerializer> supplier) throws IOException {
        return writeExtensions(extendable, jsonGenerator, true, serializerProvider, supplier);
    }

    public static <T> Set<String> writeExtensions(Extendable<T> extendable, JsonGenerator jsonGenerator,
                                                  SerializerProvider serializerProvider,
                                                  SerializerSupplier supplier) throws IOException {
        return writeExtensions(extendable, jsonGenerator, true, serializerProvider, supplier);
    }

    public static <T> Set<String> writeExtensions(Extendable<T> extendable, JsonGenerator jsonGenerator,
                                                  boolean headerWanted, SerializerProvider serializerProvider,
                                                  ExtensionProviders<? extends ExtensionJsonSerializer> supplier) throws IOException {
        return writeExtensions(extendable, jsonGenerator, headerWanted, serializerProvider, supplier::findProvider);
    }

    public interface SerializerSupplier {
        ExtensionJsonSerializer getSerializer(String name);
    }

    public static <T> Set<String> writeExtensions(Extendable<T> extendable, JsonGenerator jsonGenerator,
                                                  boolean headerWanted, SerializerProvider serializerProvider,
                                                  SerializerSupplier supplier) throws IOException {
        Objects.requireNonNull(extendable);
        Objects.requireNonNull(jsonGenerator);
        Objects.requireNonNull(serializerProvider);
        Objects.requireNonNull(supplier);

        boolean headerDone = false;
        Set<String> notFound = new HashSet<>();

        if (!extendable.getExtensions().isEmpty()) {
            for (Extension<T> extension : extendable.getExtensions()) {
                ExtensionJsonSerializer serializer = supplier.getSerializer(extension.getName());
                if (serializer != null) {
                    if (!headerDone && headerWanted) {
                        jsonGenerator.writeFieldName("extensions");
                        jsonGenerator.writeStartObject();
                        headerDone = true;
                    }
                    jsonGenerator.writeFieldName(extension.getName());
                    serializer.serialize(extension, jsonGenerator, serializerProvider);
                } else {
                    notFound.add(extension.getName());
                }
            }
            if (headerDone) {
                jsonGenerator.writeEndObject();
            }
        }
        return notFound;
    }

    /**
     * Updates the extensions of the provided extendable with possibly partial definition read from JSON.
     *
     * <p>Note that in order for this to work correctly, extension providers need to implement {@link ExtensionJsonSerializer#deserializeAndUpdate}.
     */
    public static <T extends Extendable> List<Extension<T>> updateExtensions(JsonParser parser, DeserializationContext context, T extendable) throws IOException {
        return updateExtensions(parser, context, SUPPLIER.get(), null, extendable);
    }

    /**
     * Updates the extensions of the provided extendable with possibly partial definition read from JSON.
     *
     * <p>Note that in order for this to work correctly, extension providers need to implement {@link ExtensionJsonSerializer#deserializeAndUpdate}.
     */
    public static <T extends Extendable> List<Extension<T>> updateExtensions(JsonParser parser, DeserializationContext context,
                                                                             ExtensionProviders<? extends ExtensionJsonSerializer> supplier, T extendable) throws IOException {
        return updateExtensions(parser, context, supplier, null, extendable);
    }

    public static <T extends Extendable> List<Extension<T>> updateExtensions(JsonParser parser, DeserializationContext context,
                                                                             SerializerSupplier supplier, T extendable) throws IOException {
        return updateExtensions(parser, context, supplier, null, extendable);
    }

    public static <T extends Extendable> List<Extension<T>> updateExtensions(JsonParser parser, DeserializationContext context,
                                                                             ExtensionProviders<? extends ExtensionJsonSerializer> supplier, Set<String> extensionsNotFound, T extendable) throws IOException {
        return updateExtensions(parser, context, supplier::findProvider, extensionsNotFound, extendable);
    }

    /**
     * Updates the extensions of the provided extendable with possibly partial definition read from JSON.
     *
     * <p>Note that in order for this to work correctly, extension providers need to implement {@link ExtensionJsonSerializer#deserializeAndUpdate}.
     */
    public static <T extends Extendable> List<Extension<T>> updateExtensions(JsonParser parser, DeserializationContext context, SerializerSupplier supplier, Set<String> extensionsNotFound, T extendable) throws IOException {
        Objects.requireNonNull(parser);
        Objects.requireNonNull(context);
        Objects.requireNonNull(supplier);

        List<Extension<T>> extensions = new ArrayList<>();
        if (parser.currentToken() != com.fasterxml.jackson.core.JsonToken.START_OBJECT) {
            throw new PowsyblException("Error updating extensions, \"extensions\" field expected START_OBJECT, got "
                    + parser.currentToken());
        }
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            Extension<T> extension = updateExtension(parser, context, supplier, extensionsNotFound, extendable);
            if (extension != null) {
                extensions.add(extension);
            }
        }
        return extensions;
    }

    private static <T extends Extendable, E extends Extension<T>> E updateExtension(JsonParser parser, DeserializationContext context,
                                                                                    SerializerSupplier supplier, Set<String> extensionsNotFound, T extendable) throws IOException {
        String extensionName = parser.currentName();
        ExtensionJsonSerializer<T, E> extensionJsonSerializer = supplier.getSerializer(extensionName);
        if (extensionJsonSerializer != null) {
            parser.nextToken();
            if (extendable != null && extendable.getExtensionByName(extensionName) != null) {
                return extensionJsonSerializer.deserializeAndUpdate(parser, context, (E) extendable.getExtensionByName(extensionName));
            } else {
                return extensionJsonSerializer.deserialize(parser, context);
            }
        } else {
            if (extensionsNotFound != null) {
                extensionsNotFound.add(extensionName);
            }
            skip(parser);
            return null;
        }
    }

    public static <T extends Extendable> List<Extension<T>> readExtensions(JsonParser parser, DeserializationContext context) throws IOException {
        return readExtensions(parser, context, SUPPLIER.get());
    }

    public static <T extends Extendable> List<Extension<T>> readExtensions(JsonParser parser, DeserializationContext context,
                                                                           ExtensionProviders<? extends ExtensionJsonSerializer> supplier) throws IOException {
        return readExtensions(parser, context, supplier, null);
    }

    public static <T extends Extendable> List<Extension<T>> readExtensions(JsonParser parser, DeserializationContext context,
                                                                           ExtensionProviders<? extends ExtensionJsonSerializer> supplier, Set<String> extensionsNotFound) throws IOException {
        Objects.requireNonNull(parser);
        Objects.requireNonNull(context);
        Objects.requireNonNull(supplier);
        List<Extension<T>> extensions = new ArrayList<>();
        if (parser.currentToken() != JsonToken.START_OBJECT) {
            throw new PowsyblException("Error reading extensions, \"extensions\" field expected START_OBJECT, got "
                    + parser.currentToken());
        }
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            Extension<T> extension = readExtension(parser, context, supplier, extensionsNotFound);
            if (extension != null) {
                extensions.add(extension);
            }
        }
        return extensions;
    }

    public static <T extends Extendable> Extension<T> readExtension(JsonParser parser, DeserializationContext context,
                                                                    ExtensionProviders<? extends ExtensionJsonSerializer> supplier, Set<String> extensionsNotFound) throws IOException {
        Objects.requireNonNull(parser);
        Objects.requireNonNull(context);
        Objects.requireNonNull(supplier);
        return updateExtension(parser, context, supplier::findProvider, extensionsNotFound, null);
    }

    /**
     * Skip a part of a JSON document
     */
    public static void skip(JsonParser parser) throws IOException {
        parser.nextToken();
        parser.skipChildren();
    }

    public static void assertSupportedVersion(String contextName, String version, String maxSupportedVersion) {
        Objects.requireNonNull(version);
        if (version.compareTo(maxSupportedVersion) > 0) {
            String exception = String.format(
                    "%s. Unsupported version %s. Version should be <= %s %n",
                    contextName, version, maxSupportedVersion);
            throw new PowsyblException(exception);
        }
    }

    public static void assertLessThanOrEqualToReferenceVersion(String contextName, String elementName, String version, String referenceVersion) {
        Objects.requireNonNull(version);
        if (version.compareTo(referenceVersion) > 0) {
            String exception = String.format(
                    "%s. %s is not valid for version %s. Version should be <= %s %n",
                    contextName, elementName, version, referenceVersion);
            throw new PowsyblException(exception);
        }
    }

    public static void assertGreaterThanReferenceVersion(String contextName, String elementName, String version, String referenceVersion) {
        Objects.requireNonNull(version);
        if (version.compareTo(referenceVersion) <= 0) {
            String exception = String.format(
                    "%s. %s is not valid for version %s. Version should be > %s %n",
                    contextName, elementName, version, referenceVersion);
            throw new PowsyblException(exception);
        }
    }

    public static void assertGreaterOrEqualThanReferenceVersion(String contextName, String elementName, String version, String referenceVersion) {
        Objects.requireNonNull(version);
        if (version.compareTo(referenceVersion) < 0) {
            String exception = String.format(
                    "%s. %s is not valid for version %s. Version should be >= %s %n",
                    contextName, elementName, version, referenceVersion);
            throw new PowsyblException(exception);
        }
    }

    public static void assertLessThanReferenceVersion(String contextName, String elementName, String version, String referenceVersion) {
        Objects.requireNonNull(version);
        if (version.compareTo(referenceVersion) >= 0) {
            String exception = String.format(
                    "%s. %s is not valid for version %s. Version should be < %s %n",
                    contextName, elementName, version, referenceVersion);
            throw new PowsyblException(exception);
        }
    }

    /**
     * Called by variants of {@link #parseObject} on each encountered field.
     * Should return false if an unexpected field was encountered.
     */
    @FunctionalInterface
    public interface FieldHandler {
        boolean onField(String name) throws IOException;
    }

    /**
     * Parses an object from the current parser position, using the provided field handler.
     * The parsing will expect the starting position to be START_OBJECT.
     */
    public static void parseObject(JsonParser parser, FieldHandler fieldHandler) {
        parseObject(parser, false, fieldHandler);
    }

    /**
     * Parses an object from the current parser position, using the provided field handler.
     * The parsing will accept the starting position to be either a START_OBJECT or a FIELD_NAME,
     * see contract for {@link JsonDeserializer#deserialize(JsonParser, DeserializationContext)}.
     */
    public static void parsePolymorphicObject(JsonParser parser, FieldHandler fieldHandler) {
        parseObject(parser, true, fieldHandler);
    }

    /**
     * Parses an object from the current parser position, using the provided field handler.
     * If {@code polymorphic} is {@code true}, the parsing will accept the starting position
     * to be either a START_OBJECT or a FIELD_NAME, see contract for {@link JsonDeserializer#deserialize(JsonParser, DeserializationContext)}.
     */
    public static void parseObject(JsonParser parser, boolean polymorphic, FieldHandler fieldHandler) {
        Objects.requireNonNull(parser);
        Objects.requireNonNull(fieldHandler);
        try {
            com.fasterxml.jackson.core.JsonToken token = parser.currentToken();
            if (!polymorphic && token != JsonToken.START_OBJECT) {
                throw new PowsyblException("Start object token was expected instead got: " + token);
            }
            if (token == JsonToken.START_OBJECT) {
                token = parser.nextToken();
            }
            while (token != null) {
                if (token == JsonToken.FIELD_NAME) {
                    String fieldName = parser.currentName();
                    boolean found = fieldHandler.onField(fieldName);
                    if (!found) {
                        throw new PowsyblException("Unexpected field " + fieldName);
                    }
                } else if (token == JsonToken.END_OBJECT) {
                    break;
                } else {
                    throw new PowsyblException(UNEXPECTED_TOKEN + token);
                }
                token = parser.nextToken();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> void parseObjectArray(JsonParser parser, Consumer<T> objectAdder, Function<JsonParser, T> objectParser) {
        Objects.requireNonNull(parser);
        Objects.requireNonNull(objectAdder);
        Objects.requireNonNull(objectParser);
        try {
            JsonToken token = parser.nextToken();
            if (token != JsonToken.START_ARRAY) {
                throw new PowsyblException("Start array token was expected");
            }
            boolean continueLoop = true;
            while (continueLoop && (token = parser.nextToken()) != null) {
                switch (token) {
                    case START_OBJECT -> objectAdder.accept(objectParser.apply(parser));
                    case END_ARRAY -> continueLoop = false;
                    default -> throw new PowsyblException(UNEXPECTED_TOKEN + token);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @FunctionalInterface
    interface ValueParser<T> {

        T parse(JsonParser parser) throws IOException;
    }

    private static <T> List<T> parseValueArray(JsonParser parser, JsonToken valueToken, ValueParser<T> valueParser) {
        Objects.requireNonNull(parser);
        List<T> values = new ArrayList<>();
        try {
            JsonToken token = parser.nextToken();
            if (token != JsonToken.START_ARRAY) {
                throw new PowsyblException("Start array token was expected");
            }
            while ((token = parser.nextToken()) != null) {
                if (token == valueToken) {
                    values.add(valueParser.parse(parser));
                } else if (token == JsonToken.END_ARRAY) {
                    break;
                } else {
                    throw new PowsyblException(UNEXPECTED_TOKEN + token);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return values;
    }

    public static List<Integer> parseIntegerArray(JsonParser parser) {
        return parseValueArray(parser, JsonToken.VALUE_NUMBER_INT, JsonParser::getIntValue);
    }

    public static List<Long> parseLongArray(JsonParser parser) {
        return parseValueArray(parser, JsonToken.VALUE_NUMBER_INT, JsonParser::getLongValue);
    }

    public static List<Float> parseFloatArray(JsonParser parser) {
        return parseValueArray(parser, JsonToken.VALUE_NUMBER_FLOAT, JsonParser::getFloatValue);
    }

    public static List<Double> parseDoubleArray(JsonParser parser) {
        return parseValueArray(parser, JsonToken.VALUE_NUMBER_FLOAT, JsonParser::getDoubleValue);
    }

    public static List<String> parseStringArray(JsonParser parser) {
        return parseValueArray(parser, JsonToken.VALUE_STRING, JsonParser::getText);
    }

    /**
     * Saves the provided version into the context (typically a {@link DeserializationContext}),
     * for later retrieval.
     */
    public static void setSourceVersion(DatabindContext context, String version, String sourceVersionAttributeKey) {
        context.setAttribute(sourceVersionAttributeKey, version);
    }

    /**
     * Reads the version from the context (typically a {@link DeserializationContext}) where it has been
     * previously stored.
     */
    public static String getSourceVersion(DatabindContext context, String sourceVersionAttributeKey) {
        return context.getAttribute(sourceVersionAttributeKey) != null ? (String) context.getAttribute(sourceVersionAttributeKey) : null;
    }

    /**
     * Reads a value using the given deserialization context (instead of only using the parser reading method that
     * recreates a context every time).
     * Also handles reading {@code null} values.
     */
    public static <T> T readValue(DeserializationContext context, JsonParser parser, Class<?> type) {
        try {
            if (parser.currentToken() != JsonToken.VALUE_NULL) {
                JavaType jType = context.getTypeFactory()
                        .constructType(type);
                return context.readValue(parser, jType);
            }
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> List<T> readList(DeserializationContext context, JsonParser parser, Class<?> type) {
        JavaType listType = context.getTypeFactory()
                .constructCollectionType(List.class, type);
        try {
            return context.readValue(parser, listType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> Set<T> readSet(DeserializationContext context, JsonParser parser, Class<?> type) {
        JavaType setType = context.getTypeFactory()
                .constructCollectionType(Set.class, type);
        try {
            return context.readValue(parser, setType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T extends Enum> void writeOptionalEnum(JsonGenerator jsonGenerator, String field, Optional<T> optional) throws IOException {
        if (optional.isPresent()) {
            jsonGenerator.writeStringField(field, optional.get().toString());
        }
    }

    public static void writeOptionalDouble(JsonGenerator jsonGenerator, String field, OptionalDouble optional) throws IOException {
        if (optional.isPresent()) {
            jsonGenerator.writeNumberField(field, optional.getAsDouble());
        }
    }

    public static void writeOptionalBoolean(JsonGenerator jsonGenerator, String field, Optional<Boolean> optional) throws IOException {
        if (optional.isPresent()) {
            jsonGenerator.writeBooleanField(field, optional.get());
        }
    }
}
