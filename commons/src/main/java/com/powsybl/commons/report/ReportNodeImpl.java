/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.ref.RefChain;
import com.powsybl.commons.ref.RefObj;
import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static com.powsybl.commons.report.ReportNodeDeserializer.checkToken;

/**
 * An in-memory implementation of {@link ReportNode}.
 *
 * <p>Being an implementation of {@link ReportNode}, instances of <code>ReportNodeImpl</code> are not thread-safe.
 * As such, a <code>ReportNodeImpl</code> is not meant to be shared with other threads.
 * Therefore, it should not be saved as a class parameter of an object which could be used by separate threads.
 * In those cases it should instead be passed on in methods through their arguments.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class ReportNodeImpl implements ReportNode {

    private final String messageKey;
    private final List<ReportNodeImpl> children = new ArrayList<>();
    private final Collection<Map<String, TypedValue>> inheritedValuesMaps;
    private final Map<String, TypedValue> values;
    private final RefChain<TreeContextImpl> treeContext;
    private boolean isRoot;
    private Collection<Map<String, TypedValue>> valuesMapsInheritance;

    static ReportNodeImpl createChildReportNode(String messageKey, String messageTemplate, Map<String, TypedValue> values, ReportNodeImpl parent) {
        return createReportNode(messageKey, messageTemplate, values, parent.getValuesMapsInheritance(), parent.getTreeContextRef(), false);
    }

    static ReportNodeImpl createRootReportNode(String messageKey, String messageTemplate, Map<String, TypedValue> values, boolean timestamps, DateTimeFormatter timestampPattern) {
        RefChain<TreeContextImpl> treeContext = new RefChain<>(new RefObj<>(new TreeContextImpl(timestamps, timestampPattern)));
        return createReportNode(messageKey, messageTemplate, values, Collections.emptyList(), treeContext, true);
    }

    private static ReportNodeImpl createReportNode(String messageKey, String messageTemplate, Map<String, TypedValue> values,
                                                   Collection<Map<String, TypedValue>> inheritedValuesMaps, RefChain<TreeContextImpl> treeContextRef,
                                                   boolean isRoot) {
        TreeContextImpl treeContext = treeContextRef.get();
        treeContext.addDictionaryEntry(Objects.requireNonNull(messageKey), Objects.requireNonNull(messageTemplate));
        if (treeContext.isTimestampAdded()) {
            values.put(ReportConstants.TIMESTAMP_KEY, TypedValue.getTimestamp(treeContext.getTimestampFormatter()));
        }
        return new ReportNodeImpl(messageKey, values, inheritedValuesMaps, treeContextRef, isRoot);
    }

    /**
     * ReportNodeImpl constructor, with no associated values.
     *
     * @param messageKey          the key identifying the corresponding task
     * @param values              a map of {@link TypedValue} indexed by their key, which may be referred to within the messageTemplate
     *                            or within any descendants of the created {@link ReportNode}.
     *                            Be aware that any value in this map might, in all descendants, override a value of one of
     *                            {@link ReportNode} ancestors.
     * @param inheritedValuesMaps a {@link Collection} of inherited values maps
     * @param treeContext         the {@link TreeContextImpl} of the root of corresponding report tree
     */
    private ReportNodeImpl(String messageKey, Map<String, TypedValue> values, Collection<Map<String, TypedValue>> inheritedValuesMaps, RefChain<TreeContextImpl> treeContext, boolean isRoot) {
        this.messageKey = Objects.requireNonNull(messageKey);
        checkMap(values);
        Objects.requireNonNull(inheritedValuesMaps).forEach(ReportNodeImpl::checkMap);
        this.values = values;
        this.inheritedValuesMaps = inheritedValuesMaps;
        this.treeContext = Objects.requireNonNull(treeContext);
        this.isRoot = isRoot;
    }

    private static void checkMap(Map<String, TypedValue> values) {
        Objects.requireNonNull(values).forEach((k, v) -> {
            Objects.requireNonNull(k);
            Objects.requireNonNull(v);
        });
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public String getMessageTemplate() {
        return getTreeContext().getDictionary().get(messageKey);
    }

    @Override
    public Map<String, TypedValue> getValues() {
        return Collections.unmodifiableMap(values);
    }

    @Override
    public String getMessage(ReportFormatter formatter) {
        return Optional.ofNullable(getTreeContext().getDictionary().get(messageKey))
                .map(messageTemplate -> new StringSubstitutor(vk -> getValueAsString(vk, formatter).orElse(null)).replace(messageTemplate))
                .orElse("(missing message key in dictionary)");
    }

    public Optional<String> getValueAsString(String valueKey, ReportFormatter formatter) {
        return getValue(valueKey).map(formatter::format);
    }

    @Override
    public TreeContextImpl getTreeContext() {
        return getTreeContextRef().get();
    }

    RefChain<TreeContextImpl> getTreeContextRef() {
        return treeContext;
    }

    private Collection<Map<String, TypedValue>> getValuesMapsInheritance() {
        if (valuesMapsInheritance == null) {
            valuesMapsInheritance = new ArrayList<>(1 + inheritedValuesMaps.size());
            valuesMapsInheritance.add(values);
            valuesMapsInheritance.addAll(inheritedValuesMaps);
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
    public ReportNodeAdder newReportNode() {
        return new ReportNodeChildAdderImpl(this);
    }

    @Override
    public void include(ReportNode reportNode) {
        if (!(reportNode instanceof ReportNodeImpl reportNodeImpl)) {
            throw new PowsyblException("Cannot mix implementations of ReportNode, included reportNode should be/extend ReportNodeImpl");
        }
        if (!reportNodeImpl.isRoot) {
            throw new PowsyblException("Cannot include non-root reportNode");
        }
        if (reportNode == this) {
            throw new PowsyblException("Cannot add a reportNode in itself");
        }

        reportNodeImpl.unroot();
        children.add(reportNodeImpl);

        getTreeContext().merge(reportNodeImpl.getTreeContext());
        reportNodeImpl.treeContext.setRef(treeContext);
    }

    private void unroot() {
        this.isRoot = false;
    }

    void addChild(ReportNodeImpl reportNode) {
        children.add(reportNode);
    }

    @Override
    public List<ReportNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public ReportNode addTypedValue(String key, String value, String type) {
        values.put(key, TypedValue.of(value, type));
        return this;
    }

    @Override
    public ReportNode addUntypedValue(String key, String value) {
        values.put(key, TypedValue.untyped(value));
        return this;
    }

    @Override
    public ReportNode addTypedValue(String key, double value, String type) {
        values.put(key, TypedValue.of(value, type));
        return this;
    }

    @Override
    public ReportNode addUntypedValue(String key, double value) {
        values.put(key, TypedValue.untyped(value));
        return this;
    }

    @Override
    public ReportNode addTypedValue(String key, float value, String type) {
        values.put(key, TypedValue.of(value, type));
        return this;
    }

    @Override
    public ReportNode addUntypedValue(String key, float value) {
        values.put(key, TypedValue.untyped(value));
        return this;
    }

    @Override
    public ReportNode addTypedValue(String key, int value, String type) {
        values.put(key, TypedValue.of(value, type));
        return this;
    }

    @Override
    public ReportNode addUntypedValue(String key, int value) {
        values.put(key, TypedValue.untyped(value));
        return this;
    }

    @Override
    public ReportNode addTypedValue(String key, long value, String type) {
        values.put(key, TypedValue.of(value, type));
        return this;
    }

    @Override
    public ReportNode addUntypedValue(String key, long value) {
        values.put(key, TypedValue.untyped(value));
        return this;
    }

    @Override
    public ReportNode addTypedValue(String key, boolean value, String type) {
        values.put(key, TypedValue.of(value, type));
        return this;
    }

    @Override
    public ReportNode addUntypedValue(String key, boolean value) {
        values.put(key, TypedValue.untyped(value));
        return this;
    }

    @Override
    public ReportNode addSeverity(TypedValue severity) {
        TypedValue.checkSeverityType(severity);
        values.put(ReportConstants.SEVERITY_KEY, severity);
        return this;
    }

    @Override
    public ReportNode addSeverity(String severity) {
        values.put(ReportConstants.SEVERITY_KEY, TypedValue.of(severity, TypedValue.SEVERITY));
        return this;
    }

    @Override
    public void print(Writer writer, ReportFormatter formatter) throws IOException {
        print(writer, "", formatter);
    }

    private void print(Writer writer, String indentationStart, ReportFormatter formatter) throws IOException {
        if (children.isEmpty()) {
            print(writer, indentationStart, "", formatter);
        } else {
            print(writer, indentationStart, "+ ", formatter);
            String childrenIndent = indentationStart + "   ";
            for (ReportNodeImpl child : children) {
                child.print(writer, childrenIndent, formatter);
            }
        }
    }

    private void print(Writer writer, String indent, String prefix, ReportFormatter formatter) throws IOException {
        writer.append(indent).append(prefix).append(getMessage(formatter)).append(System.lineSeparator());
    }

    public static ReportNodeImpl parseJsonNode(JsonParser parser, ObjectMapper objectMapper, TreeContextImpl treeContext, ReportNodeVersion version) throws IOException {
        Objects.requireNonNull(version, "ReportNode version is missing (null)");
        Objects.requireNonNull(treeContext);
        return switch (version) {
            case V_1_0, V_2_0 -> throw new PowsyblException("No backward compatibility of version " + version);
            case V_2_1 -> parseJsonNode(parser, objectMapper, treeContext);
        };
    }

    private static ReportNodeImpl parseJsonNode(JsonParser parser, ObjectMapper objectMapper, TreeContextImpl treeContext) throws IOException {
        checkToken(parser, JsonToken.START_OBJECT); // remove start object token to read the ReportNode itself
        return parseJsonNode(parser, objectMapper, new RefChain<>(new RefObj<>(treeContext)), Collections.emptyList(), true);
    }

    private static ReportNodeImpl parseJsonNode(JsonParser p, ObjectMapper objectMapper, RefChain<TreeContextImpl> treeContext,
                                                Collection<Map<String, TypedValue>> inheritedValuesMaps, boolean rootReportNode) throws IOException {
        ReportNodeImpl reportNode = null;
        var parsingContext = new Object() {
            String messageKey;
            Map<String, TypedValue> values = Collections.emptyMap();
        };

        while (p.nextToken() != JsonToken.END_OBJECT) {
            switch (p.currentName()) {
                case "messageKey" -> parsingContext.messageKey = p.nextTextValue();
                case "values" -> {
                    checkToken(p, JsonToken.START_OBJECT); // Remove start object token to read the underlying map
                    parsingContext.values = objectMapper.readValue(p, new TypeReference<HashMap<String, TypedValue>>() {
                    });
                }
                case "children" -> {
                    // create the current reportNode to add the children to it
                    reportNode = new ReportNodeImpl(parsingContext.messageKey, parsingContext.values, inheritedValuesMaps, treeContext, rootReportNode);

                    // Remove start array token to read each child
                    checkToken(p, JsonToken.START_ARRAY);

                    while (p.nextToken() != JsonToken.END_ARRAY) {
                        reportNode.addChild(parseJsonNode(p, objectMapper, treeContext, reportNode.getValuesMapsInheritance(), false));
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + p.currentName());
            }
        }

        if (reportNode == null) {
            reportNode = new ReportNodeImpl(parsingContext.messageKey, parsingContext.values, inheritedValuesMaps, treeContext, rootReportNode);
        }

        return reportNode;
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStringField("messageKey", getMessageKey());
        if (!values.isEmpty()) {
            generator.writeObjectField("values", values);
        }
        if (!children.isEmpty()) {
            generator.writeFieldName("children");
            generator.writeStartArray();
            for (ReportNodeImpl messageNode : children) {
                generator.writeStartObject();
                messageNode.writeJson(generator);
                generator.writeEndObject();
            }
            generator.writeEndArray();
        }
    }
}
