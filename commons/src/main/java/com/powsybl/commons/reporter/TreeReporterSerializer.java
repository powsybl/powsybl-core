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
import com.fasterxml.jackson.databind.module.SimpleModule;
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
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TreeReporterSerializer extends StdSerializer<TreeReporter> {

    private static final String VERSION = "1.0";
    private final Map<String, String> dictionary;
    private boolean rootReporter;

    TreeReporterSerializer(boolean rootReporter, Map<String, String> dictionary) {
        super(TreeReporter.class);
        this.rootReporter = rootReporter;
        this.dictionary = dictionary;
    }

    @Override
    public void serialize(TreeReporter reporter, JsonGenerator generator, SerializerProvider serializerProvider) throws IOException {
        if (rootReporter) {
            rootReporter = false;
            writeRootEntry(reporter, generator);
        } else {
            writeTreeReporter(reporter, generator);
            dictionary.put(reporter.getTaskKey(), reporter.getDefaultName());
        }
    }

    private void writeRootEntry(TreeReporter reporter, JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("version", VERSION);
        generator.writeObjectField("reportTree", reporter);
        writeDictionaryEntries(generator);
        generator.writeEndObject();
    }

    private void writeDictionaryEntries(JsonGenerator generator) throws IOException {
        generator.writeFieldName("dics");
        generator.writeStartObject();
        generator.writeObjectField("default", dictionary);
        generator.writeEndObject();
    }

    private void writeTreeReporter(TreeReporter reporter, JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("taskKey", reporter.getTaskKey());
        if (!reporter.getTaskValues().isEmpty()) {
            generator.writeObjectField("taskValues", reporter.getTaskValues());
        }
        if (!reporter.getReports().isEmpty()) {
            generator.writeObjectField("reports", reporter.getReports());
        }
        if (!reporter.getChildReporters().isEmpty()) {
            generator.writeObjectField("childReporters", reporter.getChildReporters());
        }
        generator.writeEndObject();
    }

    public static void write(TreeReporter reporter, Path jsonFile) {
        Objects.requireNonNull(reporter);
        Objects.requireNonNull(jsonFile);
        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            createObjectMapper().writerWithDefaultPrettyPrinter().writeValue(os, reporter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        Map<String, String> dictionary = new HashMap<>();
        module.addSerializer(TreeReporter.class, new TreeReporterSerializer(true, dictionary));
        module.addSerializer(Report.class, new ReportSerializer(dictionary));
        objectMapper.registerModule(module);
        return objectMapper;
    }

    private static class ReportSerializer extends StdSerializer<Report> {
        private final Map<String, String> dictionary;

        public ReportSerializer(Map<String, String> dictionary) {
            super(Report.class);
            this.dictionary = dictionary;
        }

        @Override
        public void serialize(Report report, JsonGenerator generator, SerializerProvider serializerProvider) throws IOException {
            generator.writeStartObject();
            generator.writeStringField("reportKey", report.getReportKey());
            generator.writeObjectField("values", report.getValues());
            generator.writeEndObject();
            dictionary.put(report.getReportKey(), report.getDefaultMessage());
        }
    }
}
