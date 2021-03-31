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
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class TreeReporterDeserializer extends StdDeserializer<TreeReporter> {

    private final Map<String, String> dictionary;
    private boolean rootReporter;

    TreeReporterDeserializer(boolean rootReporter, Map<String, String> dictionary) {
        super(TreeReporter.class);
        this.dictionary = Objects.requireNonNull(dictionary);
        this.rootReporter = rootReporter;
    }

    @Override
    public TreeReporter deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        if (rootReporter) {
            rootReporter = false;
            return deserializeRootReporter(p);
        } else {
            return TreeReporter.parseJson(p, dictionary);
        }
    }

    private TreeReporter deserializeRootReporter(JsonParser p) throws IOException {
        while (p.nextToken() != JsonToken.END_OBJECT) {
            switch (p.getCurrentName()) {
                case "version":
                    p.nextToken();
                    break;

                case "reportTree":
                    p.nextValue();
                    return TreeReporter.parseJson(p, dictionary);

                case "dics":
                    p.nextValue();
                    p.skipChildren();
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + p.getCurrentName());
            }
        }

        return null;
    }

    public static TreeReporter read(Path jsonFile) {
        return read(jsonFile, "default");
    }

    public static TreeReporter read(Path jsonFile, String dictionary) {
        Objects.requireNonNull(jsonFile);
        Objects.requireNonNull(dictionary);
        TreeReporterHeader trh;
        try (InputStream is = Files.newInputStream(jsonFile)) {
            trh = readTreeReporterHeader(is, dictionary);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return readTreeReporter(is, trh.dictionary);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static TreeReporterHeader readTreeReporterHeader(InputStream is, String dictionary) throws IOException {
        Objects.requireNonNull(is);
        return getTreeReporterHeaderObjectMapper(dictionary).readValue(is, TreeReporterHeader.class);
    }

    public static TreeReporter readTreeReporter(InputStream is, Map<String, String> dictionary) throws IOException {
        Objects.requireNonNull(is);
        return getTreeReporterObjectMapper(dictionary).readValue(is, TreeReporter.class);
    }

    private static ObjectMapper getTreeReporterHeaderObjectMapper(String dictionaryName) {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(TreeReporterHeader.class, new TreeReporterHeaderDeserializer(dictionaryName));
        objectMapper.registerModule(module);
        return objectMapper;
    }

    private static ObjectMapper getTreeReporterObjectMapper(Map<String, String> dictionary) {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(TreeReporter.class, new TreeReporterDeserializer(true, dictionary));
        module.addDeserializer(Report.class, new ReportDeserializer(dictionary));
        module.addDeserializer(TypedValue.class, new TypedValueDeserializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

    private static final class ReportDeserializer extends StdDeserializer<Report> {
        private final Map<String, String> dictionary;

        public ReportDeserializer(Map<String, String> dictionary) {
            super(Report.class);
            this.dictionary = Objects.requireNonNull(dictionary);
        }

        @Override
        public Report deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String reportKey = null;
            Map<String, TypedValue> values = new HashMap<>();

            while (p.nextToken() != JsonToken.END_OBJECT) {
                switch (p.getCurrentName()) {
                    case "reportKey":
                        reportKey = p.nextTextValue();
                        break;

                    case "values":
                        p.nextToken();
                        values = p.readValueAs(new TypeReference<HashMap<String, TypedValue>>() {
                        });
                        break;

                    default:
                        throw new AssertionError("Unexpected field: " + p.getCurrentName());
                }
            }

            String defaultMessage = dictionary.getOrDefault(reportKey, "(missing report key in dictionary)");
            return new Report(reportKey, defaultMessage, values);
        }
    }

    private static final class TreeReporterHeader {
        private final String version;
        private final Map<String, String> dictionary;

        private TreeReporterHeader(String version, Map<String, String> dictionary) {
            this.version = Objects.requireNonNull(version);
            this.dictionary = Objects.requireNonNull(dictionary);
        }
    }

    private static final class TreeReporterHeaderDeserializer extends StdDeserializer<TreeReporterHeader> {
        private final String dictionaryName;

        private TreeReporterHeaderDeserializer(String dictionaryName) {
            super(TreeReporterHeader.class);
            this.dictionaryName = Objects.requireNonNull(dictionaryName);
        }

        @Override
        public TreeReporterHeader deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException {
            String version = "unknown";
            Map<String, Map<String, String>> dictionaries = new HashMap<>();

            while (p.nextToken() != JsonToken.END_OBJECT) {
                switch (p.getCurrentName()) {
                    case "version":
                        version = p.nextTextValue();
                        break;

                    case "reportTree":
                        // skip the whole object as only the header (version + dictionary) is extracted
                        p.nextValue();
                        p.skipChildren();
                        break;

                    case "dics":
                        p.nextToken();
                        dictionaries = p.readValueAs(new TypeReference<HashMap<String, HashMap<String, String>>>() {
                        });
                        break;

                    default:
                        throw new AssertionError("Unexpected field: " + p.getCurrentName());
                }
            }

            Map<String, String> dictionary = dictionaries.getOrDefault(dictionaryName, Collections.emptyMap());
            return new TreeReporterHeader(version, dictionary);
        }

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
