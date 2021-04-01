/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class Report {

    public static final String REPORT_SEVERITY_KEY = "reportSeverity";

    private final String reportKey;
    private final String defaultMessage;
    private final Map<String, TypedValue> values;

    public Report(String reportKey, String defaultMessage, Map<String, TypedValue> values) {
        this.reportKey = Objects.requireNonNull(reportKey);
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

    public String getReportKey() {
        return reportKey;
    }

    public TypedValue getValue(String valueKey) {
        return values.get(valueKey);
    }

    public Map<String, TypedValue> getValues() {
        return Collections.unmodifiableMap(values);
    }

    public static Report parseJsonNode(JsonNode jsonNode, Map<String, String> dictionary, ObjectMapper mapper) {
        String reportKey = mapper.convertValue(jsonNode.get("reportKey"), String.class);

        JsonNode taskValuesNode = jsonNode.get("values");
        Map<String, TypedValue> taskValues = taskValuesNode == null ? Collections.emptyMap() : mapper.convertValue(taskValuesNode, new TypeReference<HashMap<String, TypedValue>>() {
        });

        String defaultMessage = dictionary.getOrDefault(reportKey, "(missing report key in dictionary)");

        return new Report(reportKey, defaultMessage, taskValues);
    }

}
