/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportNodeDeserializer extends StdDeserializer<ReportNodeImpl> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportNodeDeserializer.class);
    public static final String DICTIONARY_VALUE_ID = "dictionary";
    public static final String DICTIONARY_DEFAULT_NAME = "default";

    ReportNodeDeserializer() {
        super(ReportNodeImpl.class);
    }

    @Override
    public ReportNodeImpl deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ReportNodeImpl reportNode = null;
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new ReportNodeJsonModule());
        var parsingContext = new Object() {
            ReportNodeVersion version;
            Map<String, String> dictionary;
        };
        while (p.nextToken() != JsonToken.END_OBJECT) {
            switch (p.currentName()) {
                case "version" -> parsingContext.version = ReportNodeVersion.of(p.nextTextValue());
                case "dictionaries" -> parsingContext.dictionary = readDictionary(p, objectMapper, getDictionaryName(ctx));
                case "reportRoot" -> reportNode = ReportNodeImpl.parseJsonNode(p, objectMapper, parsingContext.version, parsingContext.dictionary);
                default -> throw new IllegalStateException("Unexpected value: " + p.currentName());
            }
        }
        return reportNode;
    }

    private Map<String, String> readDictionary(JsonParser p, ObjectMapper objectMapper, String dictionaryName) throws IOException {
        checkToken(p, JsonToken.START_OBJECT); // remove start object token to read the underlying map
        TypeReference<HashMap<String, HashMap<String, String>>> dictionariesTypeRef = new TypeReference<>() {
        };
        HashMap<String, HashMap<String, String>> dictionaries = objectMapper.readValue(p, dictionariesTypeRef);
        Map<String, String> dictionary = dictionaries.get(dictionaryName);
        if (dictionary == null) {
            var dictionaryEntry = dictionaries.entrySet().stream().findFirst().orElse(null);
            if (dictionaryEntry == null) {
                LOGGER.warn("No dictionary found! `dictionaries` root entry is empty");
                return Collections.emptyMap();
            } else {
                LOGGER.warn("Cannot find `{}` dictionary, taking first entry (`{}`)", dictionaryName, dictionaryEntry.getKey());
                dictionary = dictionaryEntry.getValue();
            }
        }
        return dictionary;
    }

    private String getDictionaryName(DeserializationContext ctx) {
        try {
            BeanProperty bp = new BeanProperty.Std(new PropertyName("Language for dictionary"), null, null, null, null);
            Object dicNameInjected = ctx.findInjectableValue(DICTIONARY_VALUE_ID, bp, null);
            return dicNameInjected instanceof String name ? name : DICTIONARY_DEFAULT_NAME;
        } catch (JsonMappingException | IllegalArgumentException e) {
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
        try {
            return getReportNodeModelObjectMapper(dictionary).readValue(jsonIs, ReportNodeImpl.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ObjectMapper getReportNodeModelObjectMapper(String dictionary) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ReportNodeJsonModule());
        mapper.setInjectableValues(new InjectableValues.Std().addValue(DICTIONARY_VALUE_ID, dictionary));
        return mapper;
    }

    static void checkToken(JsonParser p, JsonToken startObject) throws IOException {
        if (p.nextToken() != startObject) {
            throw new IllegalStateException("Unexpected token " + p.currentToken());
        }
    }

    protected static final class TypedValueDeserializer extends StdDeserializer<TypedValue> {

        TypedValueDeserializer() {
            super(TypedValue.class);
        }

        @Override
        public TypedValue deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException {
            Object value = null;
            String type = TypedValue.UNTYPED;

            while (p.nextToken() != JsonToken.END_OBJECT) {
                switch (p.currentName()) {
                    case "value":
                        p.nextToken();
                        value = p.readValueAs(Object.class);
                        break;

                    case "type":
                        type = p.nextTextValue();
                        break;

                    default:
                        throw new IllegalStateException("Unexpected field: " + p.currentName());
                }
            }

            return new TypedValue(value, type);
        }
    }
}
