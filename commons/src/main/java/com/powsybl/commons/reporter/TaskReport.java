/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import java.util.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TaskReport {

    private final String taskKey;
    private final String defaultName;
    private final TaskReport parentTaskReport;
    private final List<TaskReport> childrenTaskReports = new ArrayList<>();
    private final Map<String, Object> values;
    private Map<String, Report> reports = new HashMap<>();

    public TaskReport(String taskKey, String defaultName, Map<String, Object> values, TaskReport parentTaskReport) {
        this.taskKey = taskKey;
        this.defaultName = defaultName;
        this.values = new HashMap<>(values);
        this.parentTaskReport = parentTaskReport;
    }

    public TaskReport getParentTaskReport() {
        return parentTaskReport;
    }

    public void addChildTaskReport(TaskReport childTaskReport) {
        childrenTaskReports.add(childTaskReport);
    }

    public void addReport(Report report) {
        reports.put(report.getReportKey(), report);
    }

    public Collection<Report> getReports() {
        return reports.values();
    }

    public Report getReport(String reportKey) {
        return reports.get(reportKey);
    }

    public Map<String, Object> getTaskValues() {
        return values;
    }

    public Object getTaskValue(String valueKey) {
        return values.get(valueKey);
    }

    public void addTaskValue(String key, Object value) {
        values.put(key, value);
    }
}
