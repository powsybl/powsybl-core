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
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
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
    private final RefChain<TreeContext> treeContext;
    private final MessageTemplateProvider messageTemplateProvider;
    private boolean isRoot;
    private Collection<Map<String, TypedValue>> valuesMapsInheritance;

    static ReportNodeImpl createChildReportNode(String messageKey, Map<String, TypedValue> values, ReportNodeImpl parent,
                                                MessageTemplateProvider messageTemplateProvider) {
        ReportNodeImpl child = new ReportNodeImpl(messageKey, values, parent.getValuesMapsInheritance(), parent.getTreeContextRef(), false, messageTemplateProvider);
        parent.addChild(child);
        return child;
    }

    static ReportNodeImpl createRootReportNode(String messageKey, Map<String, TypedValue> values, TreeContext treeContext,
                                               MessageTemplateProvider messageTemplateProvider) {
        RefChain<TreeContext> treeContextRef = new RefChain<>(new RefObj<>(treeContext));
        return new ReportNodeImpl(messageKey, values, Collections.emptyList(), treeContextRef, true, messageTemplateProvider);
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
    private ReportNodeImpl(String messageKey, Map<String, TypedValue> values, Collection<Map<String, TypedValue>> inheritedValuesMaps,
                           RefChain<TreeContext> treeContext, boolean isRoot, MessageTemplateProvider messageTemplateProvider) {
        this.messageKey = Objects.requireNonNull(messageKey);
        checkMap(values);
        Objects.requireNonNull(inheritedValuesMaps).forEach(ReportNodeImpl::checkMap);
        this.values = values;
        this.inheritedValuesMaps = inheritedValuesMaps;
        this.treeContext = Objects.requireNonNull(treeContext);
        this.messageTemplateProvider = Objects.requireNonNull(messageTemplateProvider);
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
    public TreeContext getTreeContext() {
        return getTreeContextRef().get();
    }

    RefChain<TreeContext> getTreeContextRef() {
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
        return new ReportNodeChildAdderImpl(this, messageTemplateProvider);
    }

    @Override
    public void include(ReportNode reportRoot) {
        if (!(reportRoot instanceof ReportNodeImpl reportNodeImpl)) {
            throw new PowsyblException("Cannot mix implementations of ReportNode, included reportNode should be/extend ReportNodeImpl");
        }
        if (!reportNodeImpl.isRoot) {
            throw new PowsyblException("Cannot include non-root reportNode");
        }
        if (getTreeContext() == reportNodeImpl.getTreeContext()) {
            throw new PowsyblException("The given reportNode cannot be included as it is the root of the reportNode");
        }

        reportNodeImpl.unroot();
        children.add(reportNodeImpl);

        getTreeContext().merge(reportNodeImpl.getTreeContext());
        reportNodeImpl.treeContext.setRef(treeContext);
    }

    @Override
    public void addCopy(ReportNode reportNode) {
        var om = new ObjectMapper().registerModule(new ReportNodeJsonModule());
        var sw = new StringWriter();

        try {
            om.writeValue(sw, reportNode);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        ReportNodeImpl copiedReportNode = (ReportNodeImpl) ReportNodeDeserializer.read(IOUtils.toInputStream(sw.toString(), StandardCharsets.UTF_8));
        children.add(copiedReportNode);

        getTreeContext().merge(copiedReportNode.getTreeContext());
        copiedReportNode.treeContext.setRef(treeContext);
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
    public ReportNodeImpl addTypedValue(String key, String value, String type) {
        values.put(key, TypedValue.of(value, type));
        return this;
    }

    @Override
    public ReportNodeImpl addUntypedValue(String key, String value) {
        values.put(key, TypedValue.untyped(value));
        return this;
    }

    @Override
    public ReportNodeImpl addTypedValue(String key, double value, String type) {
        values.put(key, TypedValue.of(value, type));
        return this;
    }

    @Override
    public ReportNodeImpl addUntypedValue(String key, double value) {
        values.put(key, TypedValue.untyped(value));
        return this;
    }

    @Override
    public ReportNodeImpl addTypedValue(String key, float value, String type) {
        values.put(key, TypedValue.of(value, type));
        return this;
    }

    @Override
    public ReportNodeImpl addUntypedValue(String key, float value) {
        values.put(key, TypedValue.untyped(value));
        return this;
    }

    @Override
    public ReportNodeImpl addTypedValue(String key, int value, String type) {
        values.put(key, TypedValue.of(value, type));
        return this;
    }

    @Override
    public ReportNodeImpl addUntypedValue(String key, int value) {
        values.put(key, TypedValue.untyped(value));
        return this;
    }

    @Override
    public ReportNodeImpl addTypedValue(String key, long value, String type) {
        values.put(key, TypedValue.of(value, type));
        return this;
    }

    @Override
    public ReportNodeImpl addUntypedValue(String key, long value) {
        values.put(key, TypedValue.untyped(value));
        return this;
    }

    @Override
    public ReportNodeImpl addTypedValue(String key, boolean value, String type) {
        values.put(key, TypedValue.of(value, type));
        return this;
    }

    @Override
    public ReportNodeImpl addUntypedValue(String key, boolean value) {
        values.put(key, TypedValue.untyped(value));
        return this;
    }

    @Override
    public ReportNodeImpl addSeverity(TypedValue severity) {
        TypedValue.checkSeverityType(severity);
        values.put(ReportConstants.SEVERITY_KEY, severity);
        return this;
    }

    @Override
    public ReportNodeImpl addSeverity(String severity) {
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

    public static ReportNodeImpl parseJsonNode(JsonParser parser, ObjectMapper objectMapper, TreeContext treeContext, ReportNodeVersion version) throws IOException {
        Objects.requireNonNull(version, "ReportNode version is missing (null)");
        Objects.requireNonNull(treeContext);
        return switch (version) {
            case V_1_0, V_2_0 -> throw new PowsyblException("No backward compatibility of version " + version);
            case V_2_1, V_3_0 -> parseJsonNode(parser, objectMapper, treeContext);
        };
    }

    private static ReportNodeImpl parseJsonNode(JsonParser parser, ObjectMapper objectMapper, TreeContext treeContext) throws IOException {
        checkToken(parser, JsonToken.START_OBJECT); // remove start object token to read the ReportNode itself
        return parseJsonNode(parser, objectMapper, new RefChain<>(new RefObj<>(treeContext)), Collections.emptyList(), true);
    }

    private static ReportNodeImpl parseJsonNode(JsonParser p, ObjectMapper objectMapper, RefChain<TreeContext> treeContext,
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
                    reportNode = new ReportNodeImpl(parsingContext.messageKey, parsingContext.values, inheritedValuesMaps, treeContext, rootReportNode, MessageTemplateProvider.EMPTY);

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
            reportNode = new ReportNodeImpl(parsingContext.messageKey, parsingContext.values, inheritedValuesMaps, treeContext, rootReportNode, MessageTemplateProvider.EMPTY);
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
