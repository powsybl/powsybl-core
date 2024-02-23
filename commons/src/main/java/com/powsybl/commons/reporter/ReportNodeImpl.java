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
import com.powsybl.commons.PowsyblException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * An in-memory implementation of {@link ReportNode}.
 *
 * <p>Being an implementation of {@link ReportNode}, instances of <code>ReporterModel</code> are not thread-safe.
 * A reporterModel is not meant to be shared with other threads nor to be saved as a class parameter, but should instead
 * be passed on in methods through their arguments. Respecting this ensures that
 * <ol>
 *   <li>sub-reporters always are in the same order</li>
 *   <li>reports always are in the same order</li>
 * </ol>
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportNodeImpl extends AbstractReportNode {

    private final List<ReportNode> children = new ArrayList<>();

    /**
     * ReporterModel constructor, with no associated values.
     * @param key the key identifying the corresponding task
     * @param defaultMessage the name or message describing the corresponding task
     */
    public ReportNodeImpl(String key, String defaultMessage) {
        this(key, defaultMessage, Collections.emptyMap());
    }

    /**
     * ReporterModel constructor, with no associated values.
     * @param key the key identifying the corresponding task
     * @param defaultMessage the name or message describing the corresponding task, which may contain references to the
     *                    provided values
     * @param values a map of {@link TypedValue} indexed by their key, which may be referred to within the
     *                   defaultMessage or within the reports message of created ReporterModel
     */
    public ReportNodeImpl(String key, String defaultMessage, Map<String, TypedValue> values) {
        super(key, defaultMessage, values);
    }

    public static ReportNodeBuilder builder() {
        return new ReportNodeBuilder();
    }

    @Override
    public ReportNodeImpl report(String key, String defaultMessage, Map<String, TypedValue> values) {
        ReportNodeImpl subReporter = new ReportNodeImpl(key, defaultMessage, values);
        addChild(subReporter);
        return subReporter;
    }

    @Override
    public void addChild(ReportNode reportNode) {
        children.add(reportNode);
    }

    @Override
    public Collection<ReportNode> getChildren() {
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
            print(writer, "", new ArrayDeque<>());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ReportNodeImpl parseJsonNode(JsonNode reportTree, Map<String, String> dictionary, ObjectCodec codec, ReporterVersion version) throws IOException {
        return switch (version) {
            case V_1_0 -> throw new PowsyblException("No backward compatibility of version " + version);
            case V_2_0 -> parseJsonNode(reportTree, dictionary, codec);
        };
    }

    public static ReportNodeImpl parseJsonNode(JsonNode reportTree, Map<String, String> dictionary, ObjectCodec codec) throws IOException {
        JsonNode keyNode = reportTree.get("key");
        String key = codec.readValue(keyNode.traverse(), String.class);

        JsonNode valuesNode = reportTree.get("values");
        Map<String, TypedValue> values = valuesNode == null ? Collections.emptyMap() : codec.readValue(valuesNode.traverse(codec), new TypeReference<HashMap<String, TypedValue>>() {
        });

        String defaultTitle = dictionary.getOrDefault(key, "(missing task key in dictionary)");
        ReportNodeImpl reportNode = new ReportNodeImpl(key, defaultTitle, values);

        JsonNode reportsNode = reportTree.get("children");
        if (reportsNode != null) {
            for (JsonNode jsonNode : reportsNode) {
                reportNode.addChild(ReportNodeImpl.parseJsonNode(jsonNode, dictionary, codec));
            }
        }

        return reportNode;
    }

    public void writeJson(JsonGenerator generator, Map<String, String> dictionary) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("key", getKey());
        if (!getValues().isEmpty()) {
            generator.writeObjectField("values", getValues());
        }
        if (!children.isEmpty()) {
            generator.writeFieldName("children");
            generator.writeStartArray();
            for (ReportNode messageNode : children) {
                messageNode.writeJson(generator, dictionary);
            }
            generator.writeEndArray();
        }
        generator.writeEndObject();

        dictionary.put(getKey(), getDefaultText());
    }

    public void print(Writer writer, String indent, Deque<Map<String, TypedValue>> inheritedValueMaps) throws IOException {
        inheritedValueMaps.addFirst(getValues());
        if (children.isEmpty()) {
            printDefaultText(writer, indent, "", inheritedValueMaps);
        } else {
            printDefaultText(writer, indent, "+ ", inheritedValueMaps);
            String childrenIndent = indent + "   ";
            for (ReportNode child : children) {
                child.print(writer, childrenIndent, inheritedValueMaps);
            }
        }
    }
}
