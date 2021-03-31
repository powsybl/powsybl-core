/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

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
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class ReporterModelDeserializer extends StdDeserializer<ReporterModel> {

    public static final String DICTIONARY_VALUE_ID = "dictionary";

    ReporterModelDeserializer() {
        super(ReporterModel.class);
    }

    @Override
    public ReporterModel deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        JsonNode root = p.getCodec().readTree(p);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> dictionary = Collections.emptyMap();
        JsonNode dicsNode = root.get("dics");
        if (dicsNode != null) {
            JsonNode dicNode = dicsNode.get(getDicName(ctx));
            if (dicNode != null) {
                dictionary = mapper.convertValue(dicNode, new TypeReference<HashMap<String, String>>() {
                });
            }
        }
        SimpleModule module = new SimpleModule();
        module.addDeserializer(TypedValue.class, new TypedValueDeserializer());
        mapper.registerModule(module);
        return ReporterModel.parseJsonNode(root.get("reportTree"), dictionary, mapper);
    }

    private String getDicName(DeserializationContext ctx) throws JsonMappingException {
        Object dicNameInjected = ctx.findInjectableValue(DICTIONARY_VALUE_ID, null, null);
        return dicNameInjected instanceof String ? (String) dicNameInjected : "default";
    }

    public static ReporterModel read(Path jsonFile) {
        return read(jsonFile, "default");
    }

    public static ReporterModel read(Path jsonFile, String dictionary) {
        Objects.requireNonNull(jsonFile);
        Objects.requireNonNull(dictionary);
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return getReporterModelObjectReader(dictionary).readValue(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ObjectReader getReporterModelObjectReader(String dictionary) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ReporterModel.class, new ReporterModelDeserializer());
        mapper.registerModule(module);
        mapper.setInjectableValues(new InjectableValues.Std().addValue(DICTIONARY_VALUE_ID, dictionary));
        return mapper.readerFor(ReporterModel.class).withAttribute(DICTIONARY_VALUE_ID, dictionary);
    }

    private static final class TypedValueDeserializer extends StdDeserializer<TypedValue> {

        private TypedValueDeserializer() {
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
