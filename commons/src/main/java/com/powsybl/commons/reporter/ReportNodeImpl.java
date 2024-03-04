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
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public final class ReportNodeImpl implements ReportNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportNodeImpl.class);

    private final String key;
    private final List<ReportNodeImpl> children = new ArrayList<>();
    private final Collection<Map<String, TypedValue>> inheritedValuesMaps;
    private final Map<String, TypedValue> values;
    private final RootContext rootContext;
    private final boolean isRoot;
    private Collection<Map<String, TypedValue>> valuesMapsInheritance;

    static ReportNodeImpl createChildReportNode(String msgKey, String message, Map<String, TypedValue> values, Collection<Map<String, TypedValue>> inheritedValuesDeque, RootContext rootContext) {
        return new ReportNodeImpl(msgKey, message, values, inheritedValuesDeque, rootContext, false);
    }

    static ReportNodeImpl createRootReportNode(String msgKey, String message, Map<String, TypedValue> values, RootContext rootContext) {
        return new ReportNodeImpl(msgKey, message, values, Collections.emptyList(), rootContext, true);
    }

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
     * @param rootContext         the {@link RootContext} of the root of corresponding report tree
     */
    private ReportNodeImpl(String key, String messageTemplate, Map<String, TypedValue> values, Collection<Map<String, TypedValue>> inheritedValuesMaps, RootContext rootContext, boolean isRoot) {
        this.key = Objects.requireNonNull(key);
        checkMap(values);
        Objects.requireNonNull(inheritedValuesMaps).forEach(ReportNodeImpl::checkMap);
        this.values = Collections.unmodifiableMap(values);
        this.inheritedValuesMaps = Collections.unmodifiableCollection(inheritedValuesMaps);
        this.rootContext = rootContext;
        this.isRoot = isRoot;
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

        children.addAll(reportNodeImpl.children);
        rootContext.merge(reportNodeImpl.rootContext);
    }

    void addChild(ReportNodeImpl reportNode) {
        children.add(reportNode);
    }

    @Override
    public Collection<ReportNode> getChildren() {
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

    private void print(Writer writer, String indent, String prefix) throws IOException {
        writer.append(indent).append(prefix).append(getMessage()).append(System.lineSeparator());
    }

    public static ReportNodeImpl parseJsonNode(JsonNode reportTree, ObjectCodec codec, ReporterVersion version, String dictionaryName) throws IOException {
        return switch (version) {
            case V_1_0 -> throw new PowsyblException("No backward compatibility of version " + version);
            case V_2_0 -> parseJsonNode(reportTree, codec, dictionaryName);
        };
    }

    private static ReportNodeImpl parseJsonNode(JsonNode jsonNode, ObjectCodec codec, String dictionaryName) throws IOException {
        RootContext rootContext = new RootContext();
        readDictionary(jsonNode, codec, dictionaryName, rootContext);
        return parseJsonNode(jsonNode, codec, rootContext, Collections.emptyList(), true);
    }

    private static ReportNodeImpl parseJsonNode(JsonNode jsonNode, ObjectCodec codec, RootContext rootContext, Collection<Map<String, TypedValue>> inheritedValuesDeque, boolean rootReportNode) throws IOException {
        JsonNode keyNode = jsonNode.get("messageKey");
        String key = codec.readValue(keyNode.traverse(), String.class);

        JsonNode valuesNode = jsonNode.get("values");
        Map<String, TypedValue> values = valuesNode == null ? Collections.emptyMap() : codec.readValue(valuesNode.traverse(codec), new TypeReference<HashMap<String, TypedValue>>() {
        });

        String message = rootContext.getDictionary().getOrDefault(key, "(missing task key in dictionary)");

        ReportNodeImpl reportNode = rootReportNode
                ? createRootReportNode(key, message, values, rootContext)
                : createChildReportNode(key, message, values, inheritedValuesDeque, rootContext);

        JsonNode reportsNode = jsonNode.get("children");
        if (reportsNode != null) {
            for (JsonNode jsonChildNode : reportsNode) {
                reportNode.addChild(parseJsonNode(jsonChildNode, codec, rootContext, reportNode.getValuesMapsInheritance(), false));
            }
        }

        return reportNode;
    }

    private static void readDictionary(JsonNode root, ObjectCodec codec, String dictionaryName, RootContext rootContext) throws IOException {
        JsonNode dicsNode = root.get("dictionaries");
        if (dicsNode != null) {
            JsonNode dicNode = dicsNode.get(dictionaryName);
            if (dicNode == null && dicsNode.fields().next() != null) {
                Map.Entry<String, JsonNode> firstDictionary = dicsNode.fields().next();
                dicNode = firstDictionary.getValue();
                LOGGER.warn("Cannot find `{}` dictionary, taking first entry (`{}`)", dictionaryName, firstDictionary.getKey());
            }
            if (dicNode != null) {
                for (Iterator<Map.Entry<String, JsonNode>> it = dicNode.fields(); it.hasNext(); ) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    String value = codec.readValue(entry.getValue().traverse(), String.class);
                    rootContext.addDictionaryEntry(entry.getKey(), value);
                }
            } else {
                LOGGER.warn("No dictionary found! `dictionaries` root entry is empty");
            }
        } else {
            LOGGER.warn("No dictionary found! `dictionaries` root entry is missing");
        }
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        writeReportNodeParametersJson(generator);
        writeDictionaryEntries(generator, rootContext.getDictionary());
        generator.writeEndObject();
    }

    private void writeReportNodeParametersJson(JsonGenerator generator) throws IOException {
        generator.writeStringField("messageKey", getKey());
        if (!values.isEmpty()) {
            generator.writeObjectField("values", values);
        }
        if (!children.isEmpty()) {
            generator.writeFieldName("children");
            generator.writeStartArray();
            for (ReportNodeImpl messageNode : children) {
                generator.writeStartObject();
                messageNode.writeReportNodeParametersJson(generator);
                generator.writeEndObject();
            }
            generator.writeEndArray();
        }
    }

    private void writeDictionaryEntries(JsonGenerator generator, Map<String, String> dictionary) throws IOException {
        generator.writeFieldName("dictionaries");
        generator.writeStartObject();
        generator.writeObjectField("default", dictionary);
        generator.writeEndObject();
    }
}
