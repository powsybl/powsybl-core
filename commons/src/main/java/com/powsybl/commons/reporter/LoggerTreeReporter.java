/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class LoggerTreeReporter extends LoggerReporter implements ReportSeeker {

    private final List<ReportSeeker> childReporters = new ArrayList<>();
    private final List<Report> reports = new ArrayList<>();

    public LoggerTreeReporter() {
        super();
    }

    public LoggerTreeReporter(String rootTaskKey, String rootDefaultName, Map<String, Object> taskValues) {
        super(rootTaskKey, rootDefaultName, taskValues);
    }

    @Override
    public LoggerTreeReporter createChild(String taskKey, String defaultName, Map<String, Object> values) {
        LoggerTreeReporter childReporter = new LoggerTreeReporter(taskKey, defaultName, values);
        childReporters.add(childReporter);
        return childReporter;
    }

    @Override
    public void report(Report report) {
        super.report(report);
        reports.add(report);
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
        writer.append(prefix).append("+ ").append(reportSeeker.toString()).append('\n');
        for (Report report : reportSeeker.getReports()) {
            writer.append(prefix).append("   ").append(formatReportMessage(report, reportSeeker.getTaskValues())).append('\n');
        }
        for (ReportSeeker childReporter : reportSeeker.getChildReporters()) {
            printTaskReport(childReporter, writer, prefix + "  ");
        }
    }

}
