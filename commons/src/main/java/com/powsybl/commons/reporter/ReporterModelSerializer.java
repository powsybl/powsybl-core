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
public class ReporterModelSerializer extends StdSerializer<ReporterModel> {

    private static final String VERSION = "1.0";

    ReporterModelSerializer() {
        super(ReporterModel.class);
    }

    @Override
    public void serialize(ReporterModel reporter, JsonGenerator generator, SerializerProvider serializerProvider) throws IOException {
        Map<String, String> dictionary = new HashMap<>();
        generator.writeStartObject();
        generator.writeStringField("version", VERSION);
        generator.writeFieldName("reportTree");
        writeReporterModel(reporter, generator, dictionary);
        writeDictionaryEntries(generator, dictionary);
        generator.writeEndObject();
    }

    private void writeDictionaryEntries(JsonGenerator generator, Map<String, String> dictionary) throws IOException {
        generator.writeFieldName("dics");
        generator.writeStartObject();
        generator.writeObjectField("default", dictionary);
        generator.writeEndObject();
    }

    private void writeReporterModel(ReporterModel reporter, JsonGenerator generator, Map<String, String> dictionary) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("reporterKey", reporter.getReporterKey());
        if (!reporter.getReporterValues().isEmpty()) {
            generator.writeObjectField("reporterValues", reporter.getReporterValues());
        }
        if (!reporter.getReportMessages().isEmpty()) {
            generator.writeFieldName("reportMessages");
            generator.writeStartArray();
            for (ReportMessage reportMessage : reporter.getReportMessages()) {
                writeReport(reportMessage, generator, dictionary);
            }
            generator.writeEndArray();
        }
        if (!reporter.getSubReporters().isEmpty()) {
            generator.writeFieldName("subReporters");
            generator.writeStartArray();
            for (ReporterModel subReporter : reporter.getSubReporters()) {
                writeReporterModel(subReporter, generator, dictionary);
            }
            generator.writeEndArray();
        }
        generator.writeEndObject();

        dictionary.put(reporter.getReporterKey(), reporter.getDefaultName());
    }

    private void writeReport(ReportMessage reportMessage, JsonGenerator generator, Map<String, String> dictionary) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("messageKey", reportMessage.getMessageKey());
        if (!reportMessage.getValues().isEmpty()) {
            generator.writeObjectField("values", reportMessage.getValues());
        }
        generator.writeEndObject();
        dictionary.put(reportMessage.getMessageKey(), reportMessage.getDefaultMessage());
    }

    public static void write(ReporterModel reporter, Path jsonFile) {
        Objects.requireNonNull(reporter);
        Objects.requireNonNull(jsonFile);
        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            createObjectMapper().writerWithDefaultPrettyPrinter().writeValue(os, reporter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        return new ObjectMapper().registerModule(new ReporterModelJsonModule());
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
