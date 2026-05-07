/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.*;
import tools.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportNodeDeserializer extends StdDeserializer<ReportNode> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportNodeDeserializer.class);
    public static final String DICTIONARY_VALUE_ID = "dictionary";
    public static final String DICTIONARY_DEFAULT_NAME = "default";

    ReportNodeDeserializer() {
        super(ReportNode.class);
    }

    @Override
    public ReportNode deserialize(JsonParser p, DeserializationContext ctx) throws JacksonException {
        ReportNodeImpl reportNode = null;
        JsonMapper jsonMapper = createJsonMapperBuilder().build();
        ReportNodeVersion version = ReportConstants.CURRENT_VERSION;
        TreeContext treeContext = new TreeContextImpl();
        while (p.nextToken() != JsonToken.END_OBJECT) {
            switch (p.currentName()) {
                case "version" -> version = ReportNodeVersion.of(p.nextStringValue());
                case "dictionaries" -> readDictionary(p, jsonMapper, treeContext, getDictionaryName(ctx));
                case "reportRoot" -> reportNode = ReportNodeImpl.parseJsonNode(p, jsonMapper, treeContext, version);
                default -> throw new IllegalStateException("Unexpected value: " + p.currentName());
            }
        }
        return reportNode;
    }

    private void readDictionary(JsonParser p, JsonMapper jsonMapper, TreeContext treeContext, String dictionaryName) throws JacksonException {
        checkToken(p, JsonToken.START_OBJECT); // remove start object token to read the underlying map
        TypeReference<HashMap<String, HashMap<String, String>>> dictionariesTypeRef = new TypeReference<>() {
        };
        // We could avoid constructing the full HashMap, at the cost of code readability (the case "dictionaryName not found" makes it tricky)
        HashMap<String, HashMap<String, String>> dictionaries = jsonMapper.readValue(p, dictionariesTypeRef);
        Map<String, String> dictionary = dictionaries.get(dictionaryName);
        if (dictionary == null) {
            var dictionaryEntry = dictionaries.entrySet().stream().findFirst().orElse(null);
            if (dictionaryEntry == null) {
                LOGGER.warn("No dictionary found! `dictionaries` root entry is empty");
                return;
            } else {
                LOGGER.warn("Cannot find `{}` dictionary, taking first entry (`{}`)", dictionaryName, dictionaryEntry.getKey());
                dictionary = dictionaryEntry.getValue();
            }
        }
        dictionary.forEach((k, message) -> treeContext.addDictionaryEntry(k, (key, locale) -> message));
    }

    private String getDictionaryName(DeserializationContext ctx) {
        try {
            BeanProperty bp = new BeanProperty.Std(new PropertyName("Language for dictionary"), null, null, null, null);
            Object dicNameInjected = ctx.findInjectableValue(DICTIONARY_VALUE_ID, bp, null, null, null);
            return dicNameInjected instanceof String name ? name : DICTIONARY_DEFAULT_NAME;
        } catch (DatabindException | IllegalArgumentException e) {
            LOGGER.info("No injectable value found for id `{}` in DeserializationContext, therefore taking `{}` dictionary",
                    DICTIONARY_VALUE_ID, DICTIONARY_DEFAULT_NAME);
            return DICTIONARY_DEFAULT_NAME;
        }
    }

    public static ReportNode read(Path jsonFile) {
        return read(jsonFile, DICTIONARY_DEFAULT_NAME);
    }

    public static ReportNode read(InputStream jsonIs) {
        return read(jsonIs, DICTIONARY_DEFAULT_NAME);
    }

    public static ReportNode read(Path jsonFile, String dictionary) {
        Objects.requireNonNull(jsonFile);
        Objects.requireNonNull(dictionary);
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return read(is, dictionary);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ReportNode read(InputStream jsonIs, String dictionary) {
        Objects.requireNonNull(jsonIs);
        Objects.requireNonNull(dictionary);
        return createJsonMapper(dictionary).readValue(jsonIs, ReportNode.class);
    }

    private static JsonMapper.Builder createJsonMapperBuilder() {
        return JsonMapper.builder()
            .addModule(new ReportNodeJsonModule())
            .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    }

    private static JsonMapper createJsonMapper(String dictionary) {
        return createJsonMapperBuilder()
            .injectableValues(new InjectableValues.Std().addValue(DICTIONARY_VALUE_ID, dictionary))
            .build();
    }

    static void checkToken(JsonParser p, JsonToken startObject) throws JacksonException {
        if (p.nextToken() != startObject) {
            throw new IllegalStateException("Unexpected token " + p.currentToken());
        }
    }

    protected static final class TypedValueDeserializer extends StdDeserializer<TypedValue> {

        TypedValueDeserializer() {
            super(TypedValue.class);
        }

        @Override
        public TypedValue deserialize(JsonParser p, DeserializationContext deserializationContext) throws JacksonException {
            Object value = null;
            String type = TypedValue.UNTYPED_TYPE;

            while (p.nextToken() != JsonToken.END_OBJECT) {
                switch (p.currentName()) {
                    case "value":
                        p.nextToken();
                        value = p.readValueAs(Object.class);
                        break;

                    case "type":
                        type = p.nextStringValue();
                        break;

                    default:
                        throw new IllegalStateException("Unexpected field: " + p.currentName());
                }
            }

            return new TypedValue(value, type);
        }
    }
}
