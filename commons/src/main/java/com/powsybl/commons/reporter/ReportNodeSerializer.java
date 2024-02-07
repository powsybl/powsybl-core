/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportNodeSerializer extends StdSerializer<ReportNodeImpl> {

    ReportNodeSerializer() {
        super(ReportNodeImpl.class);
    }

    @Override
    public void serialize(ReportNodeImpl reporter, JsonGenerator generator, SerializerProvider serializerProvider) throws IOException {
        Map<String, String> dictionary = new HashMap<>();
        generator.writeStartObject();
        generator.writeStringField("version", ReportConstants.CURRENT_VERSION.toString());
        generator.writeFieldName("reportTree");
        reporter.writeJson(generator, dictionary);
        writeDictionaryEntries(generator, dictionary);
        generator.writeEndObject();
    }

    private void writeDictionaryEntries(JsonGenerator generator, Map<String, String> dictionary) throws IOException {
        generator.writeFieldName("dics");
        generator.writeStartObject();
        generator.writeObjectField("default", dictionary);
        generator.writeEndObject();
    }

    public static void write(ReportNodeImpl reporter, Path jsonFile) {
        Objects.requireNonNull(reporter);
        Objects.requireNonNull(jsonFile);
        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            createObjectMapper().writerWithDefaultPrettyPrinter().writeValue(os, reporter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        return new ObjectMapper().registerModule(new ReportNodeJsonModule());
    }

    protected static final class TypedValueSerializer extends StdSerializer<TypedValue> {
        protected TypedValueSerializer() {
            super(TypedValue.class);
        }

        @Override
        public void serialize(TypedValue typedValue, JsonGenerator generator, SerializerProvider serializerProvider) throws IOException {
            generator.writeStartObject();
            serializerProvider.defaultSerializeField("value", typedValue.getValue(), generator);
            if (!TypedValue.UNTYPED.equals(typedValue.getType())) {
                generator.writeStringField("type", typedValue.getType());
            }
            generator.writeEndObject();
        }
    }
}
