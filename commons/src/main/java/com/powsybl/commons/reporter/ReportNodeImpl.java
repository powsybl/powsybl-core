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
import java.io.Writer;
import java.util.*;

/**
 * An in-memory implementation of {@link ReportNode}.
 *
 * <p>Being an implementation of {@link ReportNode}, instances of <code>ReportNodeImpl</code> are not thread-safe.
 * A <code>ReporterNode</code> is not meant to be shared with other threads.
 * Therefore, it should not be saved as a class parameter of an object which could be used by separate threads.
 * In those cases it should instead be passed on in methods through their arguments.
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
     * ReportNodeImpl constructor, with no associated values and no inherited values.
     * @param key the key identifying the corresponding task
     * @param defaultMessage the name or message describing the corresponding task
     */
    public ReportNodeImpl(String key, String defaultMessage) {
        this(key, defaultMessage, Collections.emptyMap());
    }

    /**
     * ReportNodeImpl constructor, with no inherited values.
     * @param key the key identifying the corresponding task
     * @param defaultMessage the name or message describing the corresponding task, which may contain references to the
     *                    provided values
     * @param values a map of {@link TypedValue} indexed by their key, which may be referred to within the
     *                   defaultMessage or within the reports message of created ReporterModel
     */
    public ReportNodeImpl(String key, String defaultMessage, Map<String, TypedValue> values) {
        super(key, defaultMessage, values, new ArrayDeque<>());
    }

    /**
     * ReportNodeImpl constructor, with no associated values.
     * @param key the key identifying the corresponding task
     * @param defaultMessage the name or message describing the corresponding task, which may contain references to the
     *                    provided values
     * @param values a map of {@link TypedValue} indexed by their key, which may be referred to within the
     *                   defaultMessage or within the reports message of created ReporterModel
     */
    public ReportNodeImpl(String key, String defaultMessage, Map<String, TypedValue> values, Deque<Map<String, TypedValue>> inheritedValuesDeque) {
        super(key, defaultMessage, values, inheritedValuesDeque);
    }

    @Override
    public ReportNodeImpl report(String key, String messageTemplate, Map<String, TypedValue> values) {
        ReportNodeImpl child = new ReportNodeImpl(key, messageTemplate, values, getValuesDeque());
        children.add(child);
        return child;
    }

    @Override
    public ReportNodeAdder newReportNode() {
        return new ReportNodeImplAdder(this);
    }

    @Override
    public void addChild(ReportNode reportNode) {
        children.add(reportNode);
    }

    @Override
    public Collection<ReportNode> getChildren() {
        return Collections.unmodifiableCollection(children);
    }

    public static ReportNodeImpl parseJsonNode(JsonNode reportTree, Map<String, String> dictionary, ObjectCodec codec, ReporterVersion version) throws IOException {
        return switch (version) {
            case V_1_0 -> throw new PowsyblException("No backward compatibility of version " + version);
            case V_2_0 -> parseJsonNode(reportTree, dictionary, codec);
        };
    }

    public static ReportNodeImpl parseJsonNode(JsonNode reportTree, Map<String, String> dictionary, ObjectCodec codec) throws IOException {
        return parseJsonNode(reportTree, dictionary, codec, new ArrayDeque<>());
    }

    private static ReportNodeImpl parseJsonNode(JsonNode reportTree, Map<String, String> dictionary, ObjectCodec codec, Deque<Map<String, TypedValue>> inheritedValuesDeque) throws IOException {
        JsonNode keyNode = reportTree.get("key");
        String key = codec.readValue(keyNode.traverse(), String.class);

        JsonNode valuesNode = reportTree.get("values");
        Map<String, TypedValue> values = valuesNode == null ? Collections.emptyMap() : codec.readValue(valuesNode.traverse(codec), new TypeReference<HashMap<String, TypedValue>>() {
        });

        String defaultTitle = dictionary.getOrDefault(key, "(missing task key in dictionary)");
        ReportNodeImpl reportNode = new ReportNodeImpl(key, defaultTitle, values, inheritedValuesDeque);

        JsonNode reportsNode = reportTree.get("children");
        if (reportsNode != null) {
            for (JsonNode jsonNode : reportsNode) {
                reportNode.addChild(ReportNodeImpl.parseJsonNode(jsonNode, dictionary, codec, reportNode.getValuesDeque()));
            }
        }

        return reportNode;
    }

    @Override
    public void writeJson(JsonGenerator generator, Map<String, String> dictionary) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("key", getKey());
        Map<String, TypedValue> values = getValuesDeque().getFirst();
        if (!values.isEmpty()) {
            generator.writeObjectField("values", values);
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

        dictionary.put(getKey(), getMessage());
    }

    @Override
    public void print(Writer writer, String indentationStart) throws IOException {
        if (children.isEmpty()) {
            printDefaultText(writer, indentationStart, "");
        } else {
            printDefaultText(writer, indentationStart, "+ ");
            String childrenIndent = indentationStart + "   ";
            for (ReportNode child : children) {
                child.print(writer, childrenIndent);
            }
        }
    }
}
