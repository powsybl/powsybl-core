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
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractReporter implements Reporter {

    protected final String key;
    protected final String defaultTitle;
    protected final Map<String, TypedValue> values;

    protected AbstractReporter(String key, String defaultTitle, Map<String, TypedValue> values) {
        this.key = Objects.requireNonNull(key);
        this.defaultTitle = defaultTitle;
        this.values = new HashMap<>();
        Objects.requireNonNull(values).forEach(this::addTaskValue);
    }

    private void addTaskValue(String key, TypedValue typedValue) {
        Objects.requireNonNull(typedValue);
        values.put(key, typedValue);
    }

    @Override
    public Reporter createSubReporter(String key, String defaultTitle) {
        return createSubReporter(key, defaultTitle, Collections.emptyMap());
    }

    @Override
    public Reporter createSubReporter(String reporterKey, String defaultTitle, String valueKey, Object value) {
        return createSubReporter(reporterKey, defaultTitle, valueKey, value, TypedValue.UNTYPED);
    }

    @Override
    public Reporter createSubReporter(String reporterKey, String defaultTitle, String valueKey, Object value, String type) {
        return createSubReporter(reporterKey, defaultTitle, Map.of(valueKey, new TypedValue(value, type)));
    }

    @Override
    public void report(String messageKey, String defaultMessage, Map<String, TypedValue> values) {
        report(new ReportMessage(messageKey, defaultMessage, values));
    }

    @Override
    public void report(String messageKey, String defaultMessage) {
        report(messageKey, defaultMessage, Collections.emptyMap());
    }

    @Override
    public void report(String messageKey, String defaultMessage, String valueKey, Object value) {
        report(messageKey, defaultMessage, valueKey, value, TypedValue.UNTYPED);
    }

    @Override
    public void report(String messageKey, String defaultMessage, String valueKey, Object value, String type) {
        report(messageKey, defaultMessage, Map.of(valueKey, new TypedValue(value, type)));
    }

    /**
     * Format default message of given report by replacing value references by the corresponding values.
     * The values in the report default message have to be referred to with their corresponding key, using the <code>${key}</code> syntax.
     * The values are first searched in the report key-value map, then in or the given key-value map.
     * {@link org.apache.commons.text.StringSubstitutor} is used for the string replacements.
     * @param reportMessage the report whose default message needs to be formatted
     * @param values the key-value map used if any value reference is not found among the report values
     * @return the resulting formatted string
     */
    protected static String formatReportMessage(ReportMessage reportMessage, Map<String, TypedValue> values) {
        return new StringSubstitutor(values).replace(new StringSubstitutor(reportMessage.getValues()).replace(reportMessage.getDefaultMessage()));
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
