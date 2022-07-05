/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A class representing a functional log, consisting of a key identifying the report, a map of {@link TypedValue} indexed
 * by their keys, and a default report message string, which may contain references to those values or to the values
 * of corresponding {@link Reporter}.
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportMessage {

    public static final String REPORT_SEVERITY_KEY = "reportSeverity";

    private final String messageKey;
    private final String defaultMessage;
    private final Map<String, TypedValue> values;

    /**
     * Constructor
     * @param messageKey a key identifying the current report
     * @param defaultMessage the default report message, which may contain references to the provided values or to the
     *                       values of corresponding {@link Reporter}.
     * @param values a map of {@link TypedValue} indexed by their key, which may be referred to within the
     *               defaultMessage provided
     */
    public ReportMessage(String messageKey, String defaultMessage, Map<String, TypedValue> values) {
        this.messageKey = Objects.requireNonNull(messageKey);
        this.defaultMessage = defaultMessage;
        this.values = new HashMap<>();
        Objects.requireNonNull(values).forEach(this::addTypedValue);
    }

    private void addTypedValue(String key, TypedValue typedValue) {
        Objects.requireNonNull(typedValue);
        values.put(key, typedValue);
    }

    public static ReportBuilder builder() {
        return new ReportBuilder();
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public TypedValue getValue(String valueKey) {
        return values.get(valueKey);
    }

    public Map<String, TypedValue> getValues() {
        return Collections.unmodifiableMap(values);
    }

    public static ReportMessage parseJsonNode(JsonNode jsonNode, Map<String, String> dictionary, ObjectCodec codec) throws IOException {
        JsonNode msgKeyNode = jsonNode.get("messageKey");
        String messageKey = codec.readValue(msgKeyNode.traverse(), String.class);

        JsonNode reportValuesNode = jsonNode.get("values");
        Map<String, TypedValue> values = reportValuesNode == null ? Collections.emptyMap() : codec.readValue(reportValuesNode.traverse(codec), new TypeReference<HashMap<String, TypedValue>>() {
        });

        String defaultMessage = dictionary.getOrDefault(messageKey, "(missing report key in dictionary)");

        return new ReportMessage(messageKey, defaultMessage, values);
    }

}
