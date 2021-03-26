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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class LoggerTreeReporterDeserializer extends StdDeserializer<LoggerTreeReporter> {

    LoggerTreeReporterDeserializer() {
        super(LoggerTreeReporter.class);
    }

    @Override
    public LoggerTreeReporter deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        String taskKey = null;
        String defaultName = "";
        Map<String, Object> taskValues = new HashMap<>();
        List<Report> reports = new ArrayList<>();
        List<LoggerTreeReporter> childReporters = new ArrayList<>();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken(); // skip
                    break;

                case "taskKey":
                    taskKey = parser.nextTextValue();
                    break;

                case "defaultName":
                    defaultName = parser.nextTextValue();
                    break;

                case "taskValues":
                    parser.nextToken();
                    taskValues = parser.readValueAs(new TypeReference<HashMap<String, Object>>() {
                    });
                    break;

                case "reports":
                    parser.nextToken();
                    reports = parser.readValueAs(new TypeReference<ArrayList<Report>>() {
                    });
                    break;

                case "childReporters":
                    parser.nextToken();
                    childReporters = parser.readValueAs(new TypeReference<ArrayList<LoggerTreeReporter>>() {
                    });
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }

        LoggerTreeReporter rootReporter = new LoggerTreeReporter(taskKey, defaultName, taskValues);
        reports.forEach(rootReporter::report);
        childReporters.forEach(childReporter -> attachChildToParent(rootReporter, childReporter));

        return rootReporter;
    }

    private static void attachChildToParent(LoggerTreeReporter parent, LoggerTreeReporter childDetached) {
        LoggerTreeReporter childAttached = parent.createChild(childDetached.getTaskKey(), childDetached.getDefaultName(), childDetached.getTaskValues());
        childDetached.getReports().forEach(childAttached::report);
        for (ReportSeeker grandChildReporter : childDetached.getChildReporters()) {
            attachChildToParent(childAttached, (LoggerTreeReporter) grandChildReporter);
        }
    }

    public static LoggerTreeReporter read(Path jsonFile) {
        Objects.requireNonNull(jsonFile);
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static LoggerTreeReporter read(InputStream is) throws IOException {
        Objects.requireNonNull(is);
        return getObjectMapper().readValue(is, LoggerTreeReporter.class);
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LoggerTreeReporter.class, new LoggerTreeReporterDeserializer());
        module.addDeserializer(Report.class, new ReportDeserializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

    private static class ReportDeserializer extends JsonDeserializer<Report> {
        @Override
        public Report deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String reportKey = null;
            String defaultMessage = "";
            Map<String, Object> values = new HashMap<>();

            while (p.nextToken() != JsonToken.END_OBJECT) {
                switch (p.getCurrentName()) {
                    case "reportKey":
                        reportKey = p.nextTextValue();
                        break;

                    case "defaultMessage":
                        defaultMessage = p.nextTextValue();
                        break;

                    case "values":
                        p.nextToken();
                        values = p.readValueAs(new TypeReference<HashMap<String, Object>>() {
                        });
                        break;

                    default:
                        throw new AssertionError("Unexpected field: " + p.getCurrentName());
                }
            }

            return new Report(reportKey, defaultMessage, values);
        }
    }
}
