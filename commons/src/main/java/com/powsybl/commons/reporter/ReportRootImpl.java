/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.reporter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.commons.PowsyblException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportRootImpl implements ReportRoot {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportRootImpl.class);

    private final RootContext context = new RootContext();
    private final List<ReportNode> children = new ArrayList<>();

    @Override
    public Collection<ReportNode> getChildren() {
        return children;
    }

    @Override
    public RootContext getContext() {
        return context;
    }

    @Override
    public ReportNodeChildAdder newReportNode() {
        return new ReportRootChildAdderImpl(this);
    }

    void addChild(ReportNode node) {
        children.add(node);
    }

    public static ReportRootImpl parseJsonNode(JsonNode reportTree, ObjectCodec codec, ReporterVersion version, String dictionaryName) throws IOException {
        return switch (version) {
            case V_1_0 -> throw new PowsyblException("No backward compatibility of version " + version);
            case V_2_0 -> parseJsonNode(reportTree, codec, dictionaryName);
        };
    }

    private static ReportRootImpl parseJsonNode(JsonNode root, ObjectCodec codec, String dictionaryName) throws IOException {
        Map<String, String> dictionary = readDictionary(root, codec, dictionaryName);
        ReportRootImpl reportRoot = new ReportRootImpl();
        RootContext rootContext = reportRoot.getContext();
        dictionary.forEach(rootContext::addDictionaryEntry);

        JsonNode reportsNode = root.get("children");
        if (reportsNode != null) {
            for (JsonNode jsonNode : reportsNode) {
                reportRoot.addChild(ReportNodeImpl.parseJsonNode(jsonNode, rootContext, codec, Collections.emptyList()));
            }
        }

        return reportRoot;
    }

    private static Map<String, String> readDictionary(JsonNode root, ObjectCodec codec, String dictionaryName) throws IOException {
        Map<String, String> dictionary = Collections.emptyMap();
        JsonNode dicsNode = root.get("dictionaries");
        if (dicsNode != null) {
            JsonNode dicNode = dicsNode.get(dictionaryName);
            if (dicNode == null && dicsNode.fields().next() != null) {
                Map.Entry<String, JsonNode> firstDictionary = dicsNode.fields().next();
                dicNode = firstDictionary.getValue();
                LOGGER.warn("Cannot find `{}` dictionary, taking first entry (`{}`)", dictionaryName, firstDictionary.getKey());
            }
            if (dicNode != null) {
                dictionary = codec.readValue(dicNode.traverse(), new TypeReference<LinkedHashMap<String, String>>() {
                });
            } else {
                LOGGER.warn("No dictionary found! `dictionaries` root entry is empty");
            }
        } else {
            LOGGER.warn("No dictionary found! `dictionaries` root entry is missing");
        }
        return dictionary;
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        if (!children.isEmpty()) {
            generator.writeFieldName("children");
            generator.writeStartArray();
            for (ReportNode messageNode : children) {
                messageNode.writeJson(generator);
            }
            generator.writeEndArray();
        }
        writeDictionaryEntries(generator, context.getDictionary());
        generator.writeEndObject();
    }

    private void writeDictionaryEntries(JsonGenerator generator, Map<String, String> dictionary) throws IOException {
        generator.writeFieldName("dictionaries");
        generator.writeStartObject();
        generator.writeObjectField("default", dictionary);
        generator.writeEndObject();
    }
}
