/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.extensions.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class JsonUtil {

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class));

    private JsonUtil() {
    }

    public static ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .disable(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS)
                .enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
    }

    public static JsonFactory createJsonFactory() {
        return new JsonFactory()
                .disable(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS)
                .enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
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

    public static <T> void writeExtensions(Extendable<T> extendable, JsonGenerator jsonGenerator,
                                           SerializerProvider serializerProvider) throws IOException {
        writeExtensions(extendable, jsonGenerator, serializerProvider, SUPPLIER.get());
    }

    public static <T> void writeExtensions(Extendable<T> extendable, JsonGenerator jsonGenerator,
                                           SerializerProvider serializerProvider,
                                           ExtensionProviders<? extends ExtensionJsonSerializer> supplier) throws IOException {
        writeExtensions(extendable, jsonGenerator, true, serializerProvider, supplier);
    }

    public static <T> void writeExtensions(Extendable<T> extendable, JsonGenerator jsonGenerator,
                                           boolean headerWanted, SerializerProvider serializerProvider,
                                           ExtensionProviders<? extends ExtensionJsonSerializer> supplier) throws IOException {
        Objects.requireNonNull(extendable);
        Objects.requireNonNull(jsonGenerator);
        Objects.requireNonNull(serializerProvider);
        Objects.requireNonNull(supplier);

        boolean headerDone = false;

        if (!extendable.getExtensions().isEmpty()) {
            for (Extension<T> extension : extendable.getExtensions()) {
                ExtensionJsonSerializer serializer = supplier.findProvider(extension.getName());
                if (serializer != null) {
                    if (!headerDone && headerWanted) {
                        jsonGenerator.writeFieldName("extensions");
                        jsonGenerator.writeStartObject();
                        headerDone = true;
                    }
                    jsonGenerator.writeFieldName(extension.getName());
                    serializer.serialize(extension, jsonGenerator, serializerProvider);
                }
            }
            if (headerDone) {
                jsonGenerator.writeEndObject();
            }
        }
    }

    public static <T> List<Extension<T>> readExtensions(JsonParser parser, DeserializationContext context) throws IOException {
        return readExtensions(parser, context, SUPPLIER.get());
    }

    public static <T> List<Extension<T>> readExtensions(JsonParser parser, DeserializationContext context,
                                                        ExtensionProviders<? extends ExtensionJsonSerializer> supplier) throws IOException {
        return readExtensions(parser, context, supplier, null);
    }

    public static <T> List<Extension<T>> readExtensions(JsonParser parser, DeserializationContext context,
                                                        ExtensionProviders<? extends ExtensionJsonSerializer> supplier, Set<String> extensionsNotFound) throws IOException {
        Objects.requireNonNull(parser);
        Objects.requireNonNull(context);
        Objects.requireNonNull(supplier);

        List<Extension<T>> extensions = new ArrayList<>();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            Extension<T> extension = readExtension(parser, context, supplier, extensionsNotFound);
            if (extension != null) {
                extensions.add(extension);
            }
        }

        return extensions;
    }

    public static <T> Extension<T> readExtension(JsonParser parser, DeserializationContext context,
                                                 ExtensionProviders<? extends ExtensionJsonSerializer> supplier, Set<String> extensionsNotFound) throws IOException {
        String extensionName = parser.getCurrentName();
        ExtensionJsonSerializer extensionJsonSerializer = supplier.findProvider(extensionName);
        if (extensionJsonSerializer != null) {
            parser.nextToken();
            return extensionJsonSerializer.deserialize(parser, context);
        } else {
            if (extensionsNotFound != null) {
                extensionsNotFound.add(extensionName);
            }
            skip(parser);
            return null;
        }
    }

    /**
     * Skip a part of a JSON document
     */
    public static void skip(JsonParser parser) throws IOException {
        int objectCount = 0;
        do {
            JsonToken token = parser.nextToken();
            if (token == JsonToken.START_OBJECT) {
                objectCount++;
            } else if (token == JsonToken.END_OBJECT) {
                objectCount--;
            }
        } while (objectCount != 0);
    }
}
