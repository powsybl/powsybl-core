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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class LoggerReporter extends AbstractReporter implements ReportSeeker {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerReporter.class);

    private static final String DEFAULT_ROOT_TASK_KEY = "rootTaskKey";
    private static final Map<String, Consumer<String>> LOG_CONSUMER_MAP = Map.of(
        "TRACE", LOGGER::trace,
        "DEBUG", LOGGER::debug,
        "INFO", LOGGER::info,
        "WARN", LOGGER::warn,
        "ERROR", LOGGER::error,
        "PERFORMANCE", LOGGER::info
    );

    private final String taskKey;
    private final String defaultName;
    private final List<ReportSeeker> childReporters = new ArrayList<>();
    private final Map<String, Object> taskValues;
    private final Map<String, Report> reports = new LinkedHashMap<>();

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
        LoggerReporter childReporter = new LoggerReporter(taskKey, defaultName, values);
        childReporters.add(childReporter);
        return childReporter;
    }

    public void export(Path path) {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            printTaskReport(this, writer, "");
        } catch (IOException e) {
            LOGGER.error("IO exception while exporting logs to path {}", path, e);
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
        Report report = new Report(reportKey, defaultLog, values);
        Report previousValue = reports.put(report.getReportKey(), report);
        if (previousValue != null) {
            LOGGER.warn("Report key {} already exists in current task! replacing previous value", taskKey);
        }
        getLogConsumer(report).accept(formatReportLog(report, taskValues));
    }

    protected Consumer<String> getLogConsumer(Report report) {
        Object logLevelValue = report.getValue(REPORT_GRAVITY);
        return logLevelValue instanceof String ? LOG_CONSUMER_MAP.get(logLevelValue) : getDefaultLogConsumer();
    }

    protected Consumer<String> getDefaultLogConsumer() {
        return LOGGER::info;
    }

    @Override
    public Collection<Report> getReports() {
        return Collections.unmodifiableCollection(reports.values());
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
        return Collections.unmodifiableMap(taskValues);
    }

    @Override
    public List<ReportSeeker> getChildReporters() {
        return Collections.unmodifiableList(childReporters);
    }

}
