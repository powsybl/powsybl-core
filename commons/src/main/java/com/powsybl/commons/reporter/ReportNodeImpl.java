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
import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Stream;

/**
 * An in-memory implementation of {@link ReportNode}.
 *
 * <p>Being an implementation of {@link ReportNode}, instances of <code>ReportNodeImpl</code> are not thread-safe.
 * A <code>ReporterNode</code> is not meant to be shared with other threads.
 * Therefore, it should not be saved as a class parameter of an object which could be used by separate threads.
 * In those cases it should instead be passed on in methods through their arguments.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportNodeImpl implements ReportNode {

    private final String key;
    private final List<ReportNode> children = new ArrayList<>();
    private final Collection<Map<String, TypedValue>> inheritedValuesMaps;
    private final Map<String, TypedValue> values;
    private final RootContext rootContext;
    private Collection<Map<String, TypedValue>> valuesMapsInheritance;

    /**
     * ReportNodeImpl constructor, with no associated values.
     *
     * @param key                 the key identifying the corresponding task
     * @param messageTemplate     functional log, which may contain references to the given value or to values of any
     *                            <code>ReporterNode</code> ancestor of the created <code>ReporterNode</code>, using the
     *                            <code>${key}</code> syntax
     * @param values              a map of {@link TypedValue} indexed by their key, which may be referred to within the messageTemplate
     *                            or within any descendants of the created <code>ReporterNode</code>.
     *                            Be aware that any value in this map might, in all descendants, override a value of one of
     *                            <code>ReporterNode</code> ancestors.
     * @param inheritedValuesMaps a {@link Deque} of inherited values maps
     * @param rootContext         the {@link RootContext} of the {@link ReportRoot} at the root of corresponding report tree
     */
    ReportNodeImpl(String key, String messageTemplate, Map<String, TypedValue> values, Collection<Map<String, TypedValue>> inheritedValuesMaps, RootContext rootContext) {
        this.key = Objects.requireNonNull(key);
        checkMap(values);
        Objects.requireNonNull(inheritedValuesMaps).forEach(ReportNodeImpl::checkMap);
        this.values = Collections.unmodifiableMap(values);
        this.inheritedValuesMaps = Collections.unmodifiableCollection(inheritedValuesMaps);
        this.rootContext = rootContext;
        rootContext.addDictionaryEntry(key, messageTemplate);
    }

    private static void checkMap(Map<String, TypedValue> values) {
        Objects.requireNonNull(values).forEach((k, v) -> {
            Objects.requireNonNull(k);
            Objects.requireNonNull(v);
        });
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getMessage() {
        String messageTemplate = rootContext.getDictionary().get(key);
        return new StringSubstitutor(vk -> getValueAsString(vk).orElse(null)).replace(messageTemplate);
    }

    public Optional<String> getValueAsString(String valueKey) {
        return getValue(valueKey).map(TypedValue::getValue).map(Object::toString);
    }

    RootContext getRootContext() {
        return rootContext;
    }

    @Override
    public Collection<Map<String, TypedValue>> getValuesMapsInheritance() {
        if (valuesMapsInheritance == null) {
            List<Map<String, TypedValue>> v = new ArrayList<>(1 + inheritedValuesMaps.size());
            v.add(values);
            v.addAll(inheritedValuesMaps);
            valuesMapsInheritance = Collections.unmodifiableCollection(v);
        }
        return valuesMapsInheritance;
    }

    @Override
    public Optional<TypedValue> getValue(String valueKey) {
        return Stream.concat(Stream.of(values), inheritedValuesMaps.stream())
                .map(m -> m.get(valueKey))
                .filter(Objects::nonNull)
                .findFirst();
    }

    @Override
    public ReportNodeChildAdder newReportNode() {
        return new ReportNodeChildAdderImpl(this);
    }

    @Override
    public void include(ReportRoot reportRoot) {
        children.addAll(reportRoot.getReportNodes());
        rootContext.merge(reportRoot.getContext());
    }

    void addChild(ReportNode reportNode) {
        children.add(reportNode);
    }

    @Override
    public Collection<ReportNode> getReportNodes() {
        return Collections.unmodifiableCollection(children);
    }

    @Override
    public void print(Writer writer, String indentationStart) throws IOException {
        if (children.isEmpty()) {
            print(writer, indentationStart, "");
        } else {
            print(writer, indentationStart, "+ ");
            String childrenIndent = indentationStart + "   ";
            for (ReportNode child : children) {
                child.print(writer, childrenIndent);
            }
        }
    }

    protected void print(Writer writer, String indent, String prefix) throws IOException {
        writer.append(indent).append(prefix).append(getMessage()).append(System.lineSeparator());
    }

    static ReportNodeImpl parseJsonNode(JsonNode reportTree, RootContext rootContext, ObjectCodec codec, Collection<Map<String, TypedValue>> inheritedValuesDeque) throws IOException {
        JsonNode keyNode = reportTree.get("key");
        String key = codec.readValue(keyNode.traverse(), String.class);

        JsonNode valuesNode = reportTree.get("values");
        Map<String, TypedValue> values = valuesNode == null ? Collections.emptyMap() : codec.readValue(valuesNode.traverse(codec), new TypeReference<HashMap<String, TypedValue>>() {
        });

        String message = rootContext.getDictionary().getOrDefault(key, "(missing task key in dictionary)");
        ReportNodeImpl reportNode = new ReportNodeImpl(key, message, values, inheritedValuesDeque, rootContext);

        JsonNode reportsNode = reportTree.get("children");
        if (reportsNode != null) {
            for (JsonNode jsonNode : reportsNode) {
                reportNode.addChild(ReportNodeImpl.parseJsonNode(jsonNode, rootContext, codec, reportNode.getValuesMapsInheritance()));
            }
        }

        return reportNode;
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("key", getKey());
        if (!values.isEmpty()) {
            generator.writeObjectField("values", values);
        }
        if (!children.isEmpty()) {
            generator.writeFieldName("children");
            generator.writeStartArray();
            for (ReportNode messageNode : children) {
                messageNode.writeJson(generator);
            }
            generator.writeEndArray();
        }
        generator.writeEndObject();
    }
}
