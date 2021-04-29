/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import org.apache.commons.text.StringSubstitutor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An abstract class providing some default method implementations for {@link Reporter} implementations.
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractReporter implements Reporter {

    protected final String taskKey;
    protected final String defaultName;
    protected final Map<String, TypedValue> taskValues;

    public AbstractReporter(String taskKey, String defaultName, Map<String, TypedValue> taskValues) {
        this.taskKey = Objects.requireNonNull(taskKey);
        this.defaultName = defaultName;
        this.taskValues = new HashMap<>();
        Objects.requireNonNull(taskValues).forEach(this::addTaskValue);
    }

    private void addTaskValue(String key, TypedValue typedValue) {
        Objects.requireNonNull(typedValue);
        taskValues.put(key, typedValue);
    }

    @Override
    public Reporter createSubReporter(String taskKey, String defaultName) {
        return createSubReporter(taskKey, defaultName, Collections.emptyMap());
    }

    @Override
    public Reporter createSubReporter(String taskKey, String defaultName, String key, Object value) {
        return createSubReporter(taskKey, defaultName, key, value, TypedValue.UNTYPED);
    }

    @Override
    public Reporter createSubReporter(String taskKey, String defaultName, String key, Object value, String type) {
        return createSubReporter(taskKey, defaultName, Map.of(key, new TypedValue(value, type)));
    }

    @Override
    public void report(String reportKey, String defaultMessage, Map<String, TypedValue> values) {
        report(new Report(reportKey, defaultMessage, values));
    }

    @Override
    public void report(String reportKey, String defaultMessage) {
        report(reportKey, defaultMessage, Collections.emptyMap());
    }

    @Override
    public void report(String reportKey, String defaultMessage, String valueKey, Object value) {
        report(reportKey, defaultMessage, valueKey, value, TypedValue.UNTYPED);
    }

    @Override
    public void report(String reportKey, String defaultMessage, String valueKey, Object value, String type) {
        report(reportKey, defaultMessage, Map.of(valueKey, new TypedValue(value, type)));
    }

    /**
     * Format default message of given report by replacing value references by the corresponding values.
     * The values in the report default message have to be referred to with their corresponding key, using the <code>${key}</code> syntax.
     * The values are first searched in the report key-value map, then in or the given key-value map.
     * {@link org.apache.commons.text.StringSubstitutor} is used for the string replacements.
     * @param report the report whose default message needs to be formatted
     * @param taskValues the key-value map used if any value reference is not found among the report values
     * @return the resulting formatted string
     */
    protected static String formatReportMessage(Report report, Map<String, TypedValue> taskValues) {
        return new StringSubstitutor(taskValues).replace(new StringSubstitutor(report.getValues()).replace(report.getDefaultMessage()));
    }

    /**
     * Format given message by replacing value references by the corresponding values.
     * The values in the given message have to be referred to with their corresponding key, using the <code>${key}</code> syntax.
     * {@link org.apache.commons.text.StringSubstitutor} is used for the string replacements.
     * @param message the message to be formatted
     * @param values the key-value map used to look for the values
     * @return the resulting formatted string
     */
    protected static String formatMessage(String message, Map<String, TypedValue> values) {
        return new StringSubstitutor(values).replace(message);
    }

}
