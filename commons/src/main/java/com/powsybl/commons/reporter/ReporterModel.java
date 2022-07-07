/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * An in-memory implementation of {@link Reporter}.
 *
 * <p>Being an implementation of {@link Reporter}, instances of <code>ReporterModel</code> are not thread-safe.
 * A reporterModel is not meant to be shared with other threads nor to be saved as a class parameter, but should instead
 * be passed on in methods through their arguments. Respecting this ensures that
 * <ol>
 *   <li>sub-reporters always are in the same order</li>
 *   <li>reports always are in the same order</li>
 * </ol>
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReporterModel extends AbstractReporter {

    private final List<MessageNode> children = new ArrayList<>();

    /**
     * ReporterModel constructor, with no associated values.
     * @param key the key identifying the corresponding task
     * @param defaultTitle the name or message describing the corresponding task
     */
    public ReporterModel(String key, String defaultTitle) {
        this(key, defaultTitle, Collections.emptyMap());
    }

    /**
     * ReporterModel constructor, with no associated values.
     * @param key the key identifying the corresponding task
     * @param defaultTitle the name or message describing the corresponding task, which may contain references to the
     *                    provided values
     * @param values a map of {@link TypedValue} indexed by their key, which may be referred to within the
     *                   defaultTitle or within the reports message of created ReporterModel
     */
    public ReporterModel(String key, String defaultTitle, Map<String, TypedValue> values) {
        super(key, defaultTitle, values);
    }

    @Override
    public ReporterModel createSubReporter(String reporterKey, String defaultTitle, Map<String, TypedValue> values) {
        ReporterModel subReporter = new ReporterModel(reporterKey, defaultTitle, values);
        addSubReporter(subReporter);
        return subReporter;
    }

    /**
     * Add a reporterModel to the sub-reporters of current reporterModel.
     * @param reporterModel the reporterModel to add
     */
    public void addSubReporter(ReporterModel reporterModel) {
        children.add(reporterModel);
    }

    @Override
    public void report(ReportMessage reportMessage) {
        children.add(reportMessage);
    }

    public Collection<MessageNode> getChildren() {
        return Collections.unmodifiableCollection(children);
    }

    public void export(Path path) {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            export(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void export(Writer writer) {
        try {
            print(writer, "", new HashMap<>());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void print(Writer writer, String indent, Map<String, TypedValue> inheritedValueMap) throws IOException {
        Map<String, TypedValue> valueMap = new HashMap<>(inheritedValueMap);
        valueMap.putAll(getValues());
        printDefaultText(writer, indent, "+ ", valueMap);
        for (MessageNode child : children) {
            child.print(writer, indent + "  ", valueMap);
        }
    }

    public static ReporterModel parseJsonNode(JsonNode reportTree, Map<String, String> dictionary, ObjectCodec codec) throws IOException {
        JsonNode keyNode = reportTree.get("key");
        String key = codec.readValue(keyNode.traverse(), String.class);

        JsonNode valuesNode = reportTree.get("values");
        Map<String, TypedValue> values = valuesNode == null ? Collections.emptyMap() : codec.readValue(valuesNode.traverse(codec), new TypeReference<HashMap<String, TypedValue>>() {
        });

        String defaultTitle = dictionary.getOrDefault(key, "(missing task key in dictionary)");
        ReporterModel reporter = new ReporterModel(key, defaultTitle, values);

        JsonNode reportsNode = reportTree.get("children");
        if (reportsNode != null) {
            for (JsonNode jsonNode : reportsNode) {
                JsonNode nodeTypeNode = jsonNode.get("nodeType");
                String nodeType = codec.readValue(nodeTypeNode.traverse(), String.class);
                if (nodeType.equals(REPORT_MESSAGE_NODE_TYPE)) {
                    reporter.children.add(ReportMessage.parseJsonNode(jsonNode, dictionary, codec));
                } else if (nodeType.equals(REPORTER_NODE_TYPE)) {
                    reporter.addSubReporter(ReporterModel.parseJsonNode(jsonNode, dictionary, codec));
                }
            }
        }

        return reporter;
    }

    public void writeJson(JsonGenerator generator, Map<String, String> dictionary) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("nodeType", REPORTER_NODE_TYPE);
        generator.writeStringField("key", getKey());
        if (!getValues().isEmpty()) {
            generator.writeObjectField("values", getValues());
        }
        if (!children.isEmpty()) {
            generator.writeFieldName("children");
            generator.writeStartArray();
            for (MessageNode messageNode : children) {
                messageNode.writeJson(generator, dictionary);
            }
            generator.writeEndArray();
        }
        generator.writeEndObject();

        dictionary.put(getKey(), getDefaultText());
    }
}
