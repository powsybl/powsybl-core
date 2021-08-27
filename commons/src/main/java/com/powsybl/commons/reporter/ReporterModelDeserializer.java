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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <i>WARNING:</i> <code>Reporter</code> <i>is still a beta feature, structural changes might occur in the future releases</i>
 *
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class ReporterModelDeserializer extends StdDeserializer<ReporterModel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReporterModelDeserializer.class);

    public static final String DICTIONARY_VALUE_ID = "dictionary";
    public static final String DICTIONARY_DEFAULT_NAME = "default";

    ReporterModelDeserializer() {
        super(ReporterModel.class);
    }

    @Override
    public ReporterModel deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode root = codec.readTree(p);
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
        return ReporterModel.parseJsonNode(root.get("reportTree"), dictionary, codec);
    }

    private String getDictionaryName(DeserializationContext ctx) {
        try {
            BeanProperty bp = new BeanProperty.Std(new PropertyName("Language for dictionary"), null, null, null, null);
            Object dicNameInjected = ctx.findInjectableValue(DICTIONARY_VALUE_ID, bp, null);
            return dicNameInjected instanceof String ? (String) dicNameInjected : DICTIONARY_DEFAULT_NAME;
        } catch (JsonMappingException | IllegalArgumentException e) {
            LOGGER.info("No injectable value found for id `{}` in DeserializationContext, therefore taking `{}` dictionary",
                DICTIONARY_VALUE_ID, DICTIONARY_DEFAULT_NAME);
            return DICTIONARY_DEFAULT_NAME;
        }
    }

    public static ReporterModel read(Path jsonFile) {
        return read(jsonFile, DICTIONARY_DEFAULT_NAME);
    }

    public static ReporterModel read(InputStream jsonIs) {
        return read(jsonIs, DICTIONARY_DEFAULT_NAME);
    }

    public static ReporterModel read(Path jsonFile, String dictionary) {
        Objects.requireNonNull(jsonFile);
        Objects.requireNonNull(dictionary);
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return read(is, dictionary);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ReporterModel read(InputStream jsonIs, String dictionary) {
        Objects.requireNonNull(jsonIs);
        Objects.requireNonNull(dictionary);
        try {
            return getReporterModelObjectMapper(dictionary).readValue(jsonIs, ReporterModel.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ObjectMapper getReporterModelObjectMapper(String dictionary) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ReporterModelJsonModule());
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
                        throw new AssertionError("Unexpected field: " + p.getCurrentName());
                }
            }

            return new TypedValue(value, type);
        }
    }
}
