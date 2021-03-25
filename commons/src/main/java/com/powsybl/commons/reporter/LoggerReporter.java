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
import java.io.UncheckedIOException;
import java.io.Writer;
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
        Report.SEVERITY_TRACE, LOGGER::trace,
        Report.SEVERITY_DEBUG, LOGGER::debug,
        Report.SEVERITY_INFO, LOGGER::info,
        Report.SEVERITY_WARN, LOGGER::warn,
        Report.SEVERITY_ERROR, LOGGER::error
    );

    private final String taskKey;
    private final String defaultName;
    private final List<ReportSeeker> childReporters = new ArrayList<>();
    private final Map<String, Object> taskValues;
    private final List<Report> reports = new ArrayList<>();

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
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            export(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void export(Writer writer) {
        try {
            printTaskReport(this, writer, "");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void printTaskReport(ReportSeeker reportSeeker, Writer writer, String prefix) throws IOException {
        writer.append(prefix).append("+ ").append(formatTaskName(reportSeeker)).append('\n');
        for (Report report : reportSeeker.getReports()) {
            writer.append(prefix).append("   ").append(formatReportLog(report, reportSeeker.getTaskValues())).append('\n');
        }
        for (ReportSeeker childReporter : reportSeeker.getChildReporters()) {
            printTaskReport(childReporter, writer, prefix + "  ");
        }
    }

    protected String formatTaskName(ReportSeeker reportSeeker) {
        return new StringSubstitutor(reportSeeker.getTaskValues()).replace(reportSeeker.getDefaultName());
    }

    protected String formatReportLog(Report report, Map<String, Object> taskValues) {
        return new StringSubstitutor(taskValues).replace(new StringSubstitutor(report.getValues()).replace(report.getDefaultLog()));
    }

    @Override
    public void addTaskValue(String key, Object value) {
        taskValues.put(key, value);
    }

    @Override
    public void report(Report report) {
        reports.add(report);
        getLogConsumer(report).accept(formatReportLog(report, taskValues));
    }

    protected Consumer<String> getLogConsumer(Report report) {
        Object logLevelValue = report.getValue(Report.REPORT_SEVERITY_KEY);
        return logLevelValue instanceof String ? LOG_CONSUMER_MAP.get(logLevelValue) : getDefaultLogConsumer();
    }

    protected Consumer<String> getDefaultLogConsumer() {
        return LOGGER::info;
    }

    @Override
    public Collection<Report> getReports() {
        return Collections.unmodifiableCollection(reports);
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public String getTaskKey() {
        return taskKey;
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
