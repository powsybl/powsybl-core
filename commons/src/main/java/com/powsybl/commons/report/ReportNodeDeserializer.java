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
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    public ReportNode deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode root = codec.readTree(p);

        JsonNode versionNode = root.get("version");
        String versionStr = codec.readValue(versionNode.traverse(), String.class);
        ReportNodeVersion version = ReportNodeVersion.of(versionStr);

        String dictionaryName = getDictionaryName(ctx);
        return ReportNodeImpl.parseJsonNode(root.get("reportRoot"), codec, version, dictionaryName);
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
            return getReportNodeModelObjectMapper(dictionary).readValue(jsonIs, ReportNode.class);
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

    protected static final class TypedValueDeserializer extends StdDeserializer<TypedValue> {

        TypedValueDeserializer() {
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
