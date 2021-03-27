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
import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class LoggerTreeReporterSerializer extends StdSerializer<LoggerTreeReporter> {

    private static final String VERSION = "1.0";
    private boolean rootReporter;

    LoggerTreeReporterSerializer(boolean rootReporter) {
        super(LoggerTreeReporter.class);
        this.rootReporter = rootReporter;
    }

    @Override
    public void serialize(LoggerTreeReporter reporter, JsonGenerator generator, SerializerProvider serializerProvider) throws IOException {
        generator.writeStartObject();
        if (rootReporter) {
            rootReporter = false;
            generator.writeStringField("version", VERSION);
        }
        generator.writeStringField("taskKey", reporter.getTaskKey());
        generator.writeStringField("defaultName", reporter.getDefaultName());
        generator.writeObjectField("taskValues", reporter.getTaskValues());
        generator.writeObjectField("reports", reporter.getReports());
        generator.writeObjectField("childReporters", reporter.getChildReporters());
        generator.writeEndObject();
    }

    public static void write(LoggerTreeReporter reporter, Path jsonFile) {
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
        module.addSerializer(LoggerTreeReporter.class, new LoggerTreeReporterSerializer(true));
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
