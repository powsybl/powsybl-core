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
    private final RefChain<RootContext> rootContext;
    private boolean isRoot;
    private Collection<Map<String, TypedValue>> valuesMapsInheritance;

    static ReportNodeImpl createChildReportNode(String messageKey, String messageTemplate, Map<String, TypedValue> values, ReportNodeImpl parent) {
        return createReportNode(messageKey, messageTemplate, values, parent.getValuesMapsInheritance(), parent.getRootContext(), false);
    }

    static ReportNodeImpl createRootReportNode(String messageKey, String messageTemplate, Map<String, TypedValue> values, String timestampPattern) {
        RefChain<RootContext> rootContext = new RefChain<>(new RefObj<>(new RootContext(timestampPattern)));
        return createReportNode(messageKey, messageTemplate, values, Collections.emptyList(), rootContext, true);
    }

    private static ReportNodeImpl createReportNode(String messageKey, String messageTemplate, Map<String, TypedValue> values,
                                                   Collection<Map<String, TypedValue>> inheritedValuesMaps, RefChain<RootContext> rootContextRef,
                                                   boolean isRoot) {
        RootContext rootContext = rootContextRef.get();
        rootContext.addDictionaryEntry(Objects.requireNonNull(messageKey), Objects.requireNonNull(messageTemplate));
        if (rootContext.isTimestampAdded()) {
            values.put(ReportConstants.TIMESTAMP_KEY, rootContext.getTimestamp());
        }
        return new ReportNodeImpl(messageKey, values, inheritedValuesMaps, rootContextRef, isRoot);
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
     * @param rootContext         the {@link RootContext} of the root of corresponding report tree
     */
    private ReportNodeImpl(String messageKey, Map<String, TypedValue> values, Collection<Map<String, TypedValue>> inheritedValuesMaps, RefChain<RootContext> rootContext, boolean isRoot) {
        this.messageKey = Objects.requireNonNull(messageKey);
        checkMap(values);
        Objects.requireNonNull(inheritedValuesMaps).forEach(ReportNodeImpl::checkMap);
        this.values = Collections.unmodifiableMap(values);
        this.inheritedValuesMaps = inheritedValuesMaps;
        this.rootContext = Objects.requireNonNull(rootContext);
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
        return rootContext.get().getDictionary().get(messageKey);
    }

    @Override
    public Map<String, TypedValue> getValues() {
        return values;
    }

    @Override
    public String getMessage() {
        return Optional.ofNullable(rootContext.get().getDictionary().get(messageKey))
                .map(messageTemplate -> new StringSubstitutor(vk -> getValueAsString(vk).orElse(null)).replace(messageTemplate))
                .orElse("(missing message key in dictionary)");
    }

    public Optional<String> getValueAsString(String valueKey) {
        return getValue(valueKey).map(TypedValue::getValue).map(Object::toString);
    }

    RefChain<RootContext> getRootContext() {
        return rootContext;
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

        rootContext.get().merge(reportNodeImpl.rootContext.get());
        reportNodeImpl.rootContext.setRef(rootContext);
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
    public void print(Writer writer) throws IOException {
        print(writer, "");
    }

    private void print(Writer writer, String indentationStart) throws IOException {
        if (children.isEmpty()) {
            print(writer, indentationStart, "");
        } else {
            print(writer, indentationStart, "+ ");
            String childrenIndent = indentationStart + "   ";
            for (ReportNodeImpl child : children) {
                child.print(writer, childrenIndent);
            }
        }
    }

    private void print(Writer writer, String indent, String prefix) throws IOException {
        writer.append(indent).append(prefix).append(getMessage()).append(System.lineSeparator());
    }

    public static ReportNodeImpl parseJsonNode(JsonParser parser, ObjectMapper objectMapper, RootContext rootContext, ReportNodeVersion version) throws IOException {
        Objects.requireNonNull(version, "ReportNode version is missing (null)");
        Objects.requireNonNull(rootContext);
        return switch (version) {
            case V_1_0, V_2_0 -> throw new PowsyblException("No backward compatibility of version " + version);
            case V_2_1 -> parseJsonNode(parser, objectMapper, rootContext);
        };
    }

    private static ReportNodeImpl parseJsonNode(JsonParser parser, ObjectMapper objectMapper, RootContext rootContext) throws IOException {
        checkToken(parser, JsonToken.START_OBJECT); // remove start object token to read the ReportNode itself
        return parseJsonNode(parser, objectMapper, new RefChain<>(new RefObj<>(rootContext)), Collections.emptyList(), true);
    }

    private static ReportNodeImpl parseJsonNode(JsonParser p, ObjectMapper objectMapper, RefChain<RootContext> rootContext,
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
                    reportNode = new ReportNodeImpl(parsingContext.messageKey, parsingContext.values, inheritedValuesMaps, rootContext, rootReportNode);

                    // Remove start array token to read each child
                    checkToken(p, JsonToken.START_ARRAY);

                    while (p.nextToken() != JsonToken.END_ARRAY) {
                        reportNode.addChild(parseJsonNode(p, objectMapper, rootContext, reportNode.getValuesMapsInheritance(), false));
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + p.currentName());
            }
        }

        if (reportNode == null) {
            reportNode = new ReportNodeImpl(parsingContext.messageKey, parsingContext.values, inheritedValuesMaps, rootContext, rootReportNode);
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
