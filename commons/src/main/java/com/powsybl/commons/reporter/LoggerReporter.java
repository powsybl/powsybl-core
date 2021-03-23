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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class LoggerReporter implements Reporter, ReportSeeker {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerReporter.class);

    private static final String DEFAULT_ROOT_TASK_KEY = "rootTaskKey";

    private final String taskKey;
    private final String defaultName;
    private final LoggerReporter parentReporter;
    private final List<ReportSeeker> childReporters = new ArrayList<>();
    private final Map<String, Object> taskValues;
    private final Map<String, Report> reports = new LinkedHashMap<>();

    public LoggerReporter() {
        this(DEFAULT_ROOT_TASK_KEY, "Root task", Collections.emptyMap(), null);
    }

    public LoggerReporter(String rootTaskKey, String rootDefaultName, Map<String, Object> taskValues, LoggerReporter parent) {
        this.taskKey = rootTaskKey;
        this.defaultName = rootDefaultName;
        this.taskValues = taskValues;
        this.parentReporter = parent;
    }

    @Override
    public LoggerReporter createChild(String taskKey, String defaultName, Map<String, Object> values) {
        LoggerReporter childReporter = new LoggerReporter(taskKey, defaultName, values, this);
        childReporters.add(childReporter);
        return childReporter;
    }

    public void export(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            printTaskReport(this, writer, "");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void printTaskReport(ReportSeeker loggerReporter, PrintWriter writer, String prefix) {
        writer.println(prefix + "+ " + formatTaskReportName(loggerReporter.getDefaultName(), loggerReporter.getTaskValues()));
        loggerReporter.getReports().forEach(report -> writer.println(prefix + "   " + formatReportLog(report, loggerReporter.getTaskValues())));
        loggerReporter.getChildReporters().forEach(child -> printTaskReport(child, writer, prefix + "  "));
    }

    private static String formatTaskReportName(String msgPattern, Map<String, Object> taskValues) {
        return new StringSubstitutor(taskValues).replace(msgPattern);
    }

    private static String formatReportLog(Report report, Map<String, Object> taskValues) {
        return new StringSubstitutor(taskValues).replace(new StringSubstitutor(report.getValues()).replace(report.getDefaultLog()));
    }

    @Override
    public void addTaskValue(String key, Object value) {
        taskValues.put(key, value);
    }

    @Override
    public void report(String reportKey, String defaultLog, Map<String, Object> values) {
        report(reportKey, defaultLog, values, MarkerImpl.DEFAULT);
    }

    @Override
    public void report(String reportKey, String defaultLog, Map<String, Object> values, Marker marker) {
        Report report = new Report(reportKey, defaultLog, values, marker);
        Report previousValue = reports.put(report.getReportKey(), report);
        if (previousValue != null) {
            LOGGER.warn("Report key {} already exists in current task! replacing previous value", taskKey);
        }
        getLogConsumer(marker.getLogLevel()).accept(formatReportLog(report, taskValues));
    }

    private Consumer<String> getLogConsumer(Marker.LogLevel logLevel) {
        switch (logLevel) {
            case TRACE: return LOGGER::trace;
            case DEBUG: return LOGGER::debug;
            case INFO: return LOGGER::info;
            case WARN: return LOGGER::warn;
            case ERROR: return LOGGER::error;
        }
        return s -> { };
    }

    @Override
    public Collection<Report> getReports() {
        return reports.values();
    }

    @Override
    public Report getReport(String reportKey) {
        return reports.get(reportKey);
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public Map<String, Object> getTaskValues() {
        return taskValues;
    }

    @Override
    public List<ReportSeeker> getChildReporters() {
        return childReporters;
    }

}
