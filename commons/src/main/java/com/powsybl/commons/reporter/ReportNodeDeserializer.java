/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
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
import java.util.*;

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
        ObjectCodec codec = p.getCodec();
        JsonNode root = codec.readTree(p);

        JsonNode versionNode = root.get("version");
        String versionStr = codec.readValue(versionNode.traverse(), String.class);
        ReporterVersion version = ReporterVersion.of(versionStr);

        Map<String, String> dictionary = readDictionary(root, ctx, codec);

        return ReportNodeImpl.parseJsonNode(root.get("reportTree"), dictionary, codec, version);
    }

    private Map<String, String> readDictionary(JsonNode root, DeserializationContext ctx, ObjectCodec codec) throws IOException {
        Map<String, String> dictionary = Collections.emptyMap();
        JsonNode dicsNode = root.get("dics");
        if (dicsNode != null) {
            String dictionaryName = getDictionaryName(ctx);
            JsonNode dicNode = dicsNode.get(dictionaryName);
            if (dicNode == null && dicsNode.fields().next() != null) {
                Map.Entry<String, JsonNode> firstDictionary = dicsNode.fields().next();
                dicNode = firstDictionary.getValue();
                LOGGER.warn("Cannot find `{}` dictionary, taking first entry (`{}`)", dictionaryName, firstDictionary.getKey());
            }
            if (dicNode != null) {
                dictionary = codec.readValue(dicNode.traverse(), new TypeReference<HashMap<String, String>>() {
                });
            } else {
                LOGGER.warn("No dictionary found! `dics` root entry is empty");
            }
        } else {
            LOGGER.warn("No dictionary found! `dics` root entry is missing");
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

    public static ReportNodeImpl read(Path jsonFile) {
        return read(jsonFile, DICTIONARY_DEFAULT_NAME);
    }

    public static ReportNodeImpl read(InputStream jsonIs) {
        return read(jsonIs, DICTIONARY_DEFAULT_NAME);
    }

    public static ReportNodeImpl read(Path jsonFile, String dictionary) {
        Objects.requireNonNull(jsonFile);
        Objects.requireNonNull(dictionary);
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return read(is, dictionary);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ReportNodeImpl read(InputStream jsonIs, String dictionary) {
        Objects.requireNonNull(jsonIs);
        Objects.requireNonNull(dictionary);
        try {
            return getReporterModelObjectMapper(dictionary).readValue(jsonIs, ReportNodeImpl.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ObjectMapper getReporterModelObjectMapper(String dictionary) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ReportNodeJsonModule());
        mapper.setInjectableValues(new InjectableValues.Std().addValue(DICTIONARY_VALUE_ID, dictionary));
        return mapper;
    }

    protected static final class TypedValueDeserializer extends StdDeserializer<TypedValue> {

        protected TypedValueDeserializer() {
            super(TypedValue.class);
        }

        @Override
        public TypedValue deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException {
            Object value = null;
            String type = TypedValue.UNTYPED;

            while (p.nextToken() != JsonToken.END_OBJECT) {
                switch (p.getCurrentName()) {
                    case "value":
                        p.nextToken();
                        value = p.readValueAs(Object.class);
                        break;

                    case "type":
                        type = p.nextTextValue();
                        break;

                    default:
                        throw new IllegalStateException("Unexpected field: " + p.getCurrentName());
                }
            }

            return new TypedValue(value, type);
        }
    }
}
