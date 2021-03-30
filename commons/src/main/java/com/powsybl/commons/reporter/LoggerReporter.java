/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class LoggerReporter extends AbstractReporter {

    private final Logger logger;
    private final Map<String, Consumer<String>> slf4jLoggerMap;
    private final String taskKeysStack;

    public LoggerReporter() {
        this(DEFAULT_ROOT_TASK_KEY, DEFAULT_ROOT_NAME, Collections.emptyMap(), "");
    }

    public LoggerReporter(String taskKey, String defaultName, Map<String, Object> taskValues, String parentKeys) {
        super(taskKey, defaultName, taskValues);
        this.taskKeysStack = parentKeys + "." + taskKey;
        this.logger = LoggerFactory.getLogger(LoggerReporter.class.getName() + this.taskKeysStack);
        this.slf4jLoggerMap = Map.of(
            Report.SEVERITY_TRACE, logger::trace,
            Report.SEVERITY_DEBUG, logger::debug,
            Report.SEVERITY_INFO, logger::info,
            Report.SEVERITY_WARN, logger::warn,
            Report.SEVERITY_ERROR, logger::error
        );
    }

    @Override
    public LoggerReporter createChild(String taskKey, String defaultName, Map<String, Object> values) {
        return new LoggerReporter(taskKey, defaultName, values, taskKeysStack);
    }

    @Override
    public void report(Report report) {
        getLogConsumer(report).accept(formatReportMessage(report, taskValues));
    }

    @Override
    public String toString() {
        return formatMessage(defaultName, taskValues);
    }

    protected Consumer<String> getLogConsumer(Report report) {
        Object logLevelValue = report.getValue(Report.REPORT_SEVERITY_KEY);
        return logLevelValue instanceof String ? slf4jLoggerMap.getOrDefault(logLevelValue, getDefaultLogConsumer()) : getDefaultLogConsumer();
    }

    protected Consumer<String> getDefaultLogConsumer() {
        return logger::info;
    }

}
