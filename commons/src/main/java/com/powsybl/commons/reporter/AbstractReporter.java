/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import com.powsybl.commons.PowsyblException;
import org.apache.commons.text.StringSubstitutor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractReporter implements Reporter {

    protected static final String DEFAULT_ROOT_TASK_KEY = "rootTaskKey";
    protected static final String DEFAULT_ROOT_NAME = "Root task";

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
        Object value = typedValue.getValue();
        if (!(value instanceof Float || value instanceof Double || value instanceof Integer || value instanceof Long || value instanceof Boolean || value instanceof String)) {
            throw new PowsyblException("Report expects only Float, Double, Integer, Long and String values (value is an instance of " + value.getClass() + ")");
        }
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

    protected static String formatReportMessage(Report report, Map<String, TypedValue> taskValues) {
        return new StringSubstitutor(taskValues).replace(new StringSubstitutor(report.getValues()).replace(report.getDefaultMessage()));
    }

    protected static String formatMessage(String message, Map<String, TypedValue> values) {
        return new StringSubstitutor(values).replace(message);
    }

}
