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
public class TreeReporterDeserializer extends StdDeserializer<TreeReporter> {

    TreeReporterDeserializer() {
        super(TreeReporter.class);
    }

    @Override
    public TreeReporter deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        return TreeReporter.parseJson(parser);
    }

    public static TreeReporter read(Path jsonFile) {
        Objects.requireNonNull(jsonFile);
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static TreeReporter read(InputStream is) throws IOException {
        Objects.requireNonNull(is);
        return getObjectMapper().readValue(is, TreeReporter.class);
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(TreeReporter.class, new TreeReporterDeserializer());
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
