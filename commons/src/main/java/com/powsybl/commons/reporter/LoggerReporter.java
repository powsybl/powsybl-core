/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class LoggerReporter implements Reporter, ReportSeeker {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerReporter.class);

    private final Map<String, TaskReport> taskReports = new HashMap<>();
    private TaskReport ongoingTaskReport;

    public LoggerReporter() {
        this("Root", "Root task", Collections.emptyMap());
    }

    public LoggerReporter(String rootTaskKey, String rootDefaultName, Map<String, Object> values) {
        this.ongoingTaskReport = new TaskReport(rootTaskKey, rootDefaultName, values, null);
    }

    @Override
    public void startTask(String taskKey, String defaultName, Map<String, Object> values) {
        TaskReport childTaskReport = new TaskReport(taskKey, defaultName, values, ongoingTaskReport);
        ongoingTaskReport.addChildTaskReport(childTaskReport);
        ongoingTaskReport = childTaskReport;
        taskReports.put(taskKey, ongoingTaskReport);
    }

    void export(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addTaskValue(String key, Object value) {
        ongoingTaskReport.addTaskValue(key, value);
    }

    @Override
    public void endTask() {
        ongoingTaskReport = ongoingTaskReport.getParentTaskReport();
    }

    @Override
    public void report(String reportKey, String defaultLog, Map<String, Object> values) {
        report(reportKey, defaultLog, values, MarkerImpl.DEFAULT);
    }

    @Override
    public void report(String reportKey, String defaultLog, Map<String, Object> values, Marker marker) {
        String logFormatted = MessageFormatter.format(defaultLog, values, ongoingTaskReport.getTaskValues());
        getLogConsumer(marker.getLogLevel()).accept(logFormatted);
        ongoingTaskReport.addReport(new Report(reportKey, defaultLog, values, marker));
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
    public List<Report> getReports() {
        return getReportStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Report> getReportStream() {
        return taskReports.values().stream().flatMap(taskReport -> taskReport.getReports().stream());
    }

    @Override
    public Stream<TaskReport> getTaskReportStream() {
        return taskReports.values().stream();
    }

    @Override
    public TaskReport getTaskReport(String taskKey) {
        return taskReports.get(taskKey);
    }

    @Override
    public List<Report> getReport(String reportKey) {
        return getReportStream().filter(report -> report.getReportKey().equals(reportKey)).collect(Collectors.toList());
    }

    @Override
    public Report getReport(String taskKey, String reportKey) {
        return taskReports.get(taskKey).getReport(reportKey);
    }
}
