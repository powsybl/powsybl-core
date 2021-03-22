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

    private static final String DEFAULT_ROOT_TASK_KEY = "rootTaskKey";

    private final Map<String, TaskReport> taskReports = new HashMap<>();
    private final String rootTaskKey;
    private TaskReport ongoingTaskReport;

    public LoggerReporter() {
        this(DEFAULT_ROOT_TASK_KEY, "Root task", Collections.emptyMap());
    }

    public LoggerReporter(String rootTaskKey, String rootDefaultName, Map<String, Object> values) {
        this.rootTaskKey = rootTaskKey;
        this.ongoingTaskReport = createTaskReport(rootTaskKey, rootDefaultName, values, null);
    }

    private TaskReport createTaskReport(String taskKey, String defaultName, Map<String, Object> values, TaskReport parentTask) {
        TaskReport newTaskReport = new TaskReport(taskKey, defaultName, values, parentTask);
        TaskReport previousValue = taskReports.put(taskKey, newTaskReport);
        if (previousValue != null) {
            LOGGER.warn("Task key {} already exists in current task! replacing previous value", taskKey);
        }
        return newTaskReport;
    }

    @Override
    public void startTask(String taskKey, String defaultName, Map<String, Object> values) {
        TaskReport childTaskReport = createTaskReport(taskKey, defaultName, values, ongoingTaskReport);
        ongoingTaskReport.addChildTaskReport(childTaskReport);
        ongoingTaskReport = childTaskReport;
    }

    public void export(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            TaskReport taskReport = taskReports.get(rootTaskKey);
            printTaskReport(taskReport, writer, "");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void printTaskReport(TaskReport taskReport, PrintWriter writer, String prefix) {
        writer.println(prefix + "+ " + formatTaskReportName(taskReport.getDefaultName(), taskReport.getTaskValues()));
        taskReport.getReports().forEach(report -> writer.println(prefix + "   " + formatReportLog(report, taskReport.getTaskValues())));
        taskReport.getChildTaskReports().forEach(child -> printTaskReport(child, writer, prefix + "  "));
    }

    private static String formatTaskReportName(String msgPattern, Map<String, Object> taskValues) {
        return new StringSubstitutor(taskValues).replace(msgPattern);
    }

    private static String formatReportLog(Report report, Map<String, Object> taskValues) {
        return new StringSubstitutor(taskValues).replace(new StringSubstitutor(report.getValues()).replace(report.getDefaultLog()));
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
        Report report = new Report(reportKey, defaultLog, values, marker);
        ongoingTaskReport.addReport(report);
        getLogConsumer(marker.getLogLevel()).accept(formatReportLog(report, ongoingTaskReport.getTaskValues()));
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
