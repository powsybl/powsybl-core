/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class LoggerReporter extends AbstractReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerReporter.class);

    private static final String DEFAULT_ROOT_TASK_KEY = "rootTaskKey";
    private static final Map<String, Consumer<String>> LOG_CONSUMER_MAP = Map.of(
        Report.SEVERITY_TRACE, LOGGER::trace,
        Report.SEVERITY_DEBUG, LOGGER::debug,
        Report.SEVERITY_INFO, LOGGER::info,
        Report.SEVERITY_WARN, LOGGER::warn,
        Report.SEVERITY_ERROR, LOGGER::error
    );

    protected final String taskKey;
    protected final String defaultName;
    protected final Map<String, Object> taskValues;

    public LoggerReporter() {
        this(DEFAULT_ROOT_TASK_KEY, "Root task", Collections.emptyMap());
    }

    public LoggerReporter(String rootTaskKey, String rootDefaultName, Map<String, Object> taskValues) {
        this.taskKey = Objects.requireNonNull(rootTaskKey);
        this.defaultName = rootDefaultName;
        this.taskValues = new HashMap<>(Objects.requireNonNull(taskValues));
    }

    @Override
    public LoggerReporter createChild(String taskKey, String defaultName, Map<String, Object> values) {
        return new LoggerReporter(taskKey, defaultName, values);
    }

    @Override
    public void addTaskValue(String key, Object value) {
        taskValues.put(key, value);
    }

    @Override
    public void report(Report report) {
        getLogConsumer(report).accept(formatReportMessage(report, taskValues));
    }

    public String toString() {
        return new StringSubstitutor(taskValues).replace(defaultName);
    }

    protected String formatReportMessage(Report report, Map<String, Object> taskValues) {
        return new StringSubstitutor(taskValues).replace(new StringSubstitutor(report.getValues()).replace(report.getDefaultMessage()));
    }

    protected Consumer<String> getLogConsumer(Report report) {
        Object logLevelValue = report.getValue(Report.REPORT_SEVERITY_KEY);
        return logLevelValue instanceof String ? LOG_CONSUMER_MAP.get(logLevelValue) : getDefaultLogConsumer();
    }

    protected Consumer<String> getDefaultLogConsumer() {
        return LOGGER::info;
    }

}
