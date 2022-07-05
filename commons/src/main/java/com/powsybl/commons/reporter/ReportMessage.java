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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A class representing a functional log, consisting of a key identifying the report, a map of {@link TypedValue} indexed
 * by their keys, and a default report message string, which may contain references to those values or to the values
 * of corresponding {@link Reporter}.
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportMessage extends AbstractReportNode {

    public static final String REPORT_SEVERITY_KEY = "reportSeverity";

    /**
     * Constructor
     * @param key a key identifying the current report
     * @param defaultMessage the default report message, which may contain references to the provided values or to the
     *                       values of corresponding {@link Reporter}.
     * @param values a map of {@link TypedValue} indexed by their key, which may be referred to within the
     *               defaultMessage provided
     */
    public ReportMessage(String key, String defaultMessage, Map<String, TypedValue> values) {
        super(key, defaultMessage, values);
    }

    public static ReportBuilder builder() {
        return new ReportBuilder();
    }

    public static ReportMessage parseJsonNode(JsonNode jsonNode, Map<String, String> dictionary, ObjectCodec codec) throws IOException {
        JsonNode msgKeyNode = jsonNode.get("key");
        String key = codec.readValue(msgKeyNode.traverse(), String.class);

        JsonNode reportValuesNode = jsonNode.get("values");
        Map<String, TypedValue> values = reportValuesNode == null ? Collections.emptyMap() : codec.readValue(reportValuesNode.traverse(codec), new TypeReference<HashMap<String, TypedValue>>() {
        });

        String defaultMessage = dictionary.getOrDefault(key, "(missing report key in dictionary)");

        return new ReportMessage(key, defaultMessage, values);
    }

    public void writeJson(JsonGenerator generator, Map<String, String> dictionary) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("nodeType", REPORT_MESSAGE_NODE_TYPE);
        generator.writeStringField("key", getKey());
        if (!getValues().isEmpty()) {
            generator.writeObjectField("values", getValues());
        }
        generator.writeEndObject();
        dictionary.put(getKey(), getDefaultText());
    }
}
