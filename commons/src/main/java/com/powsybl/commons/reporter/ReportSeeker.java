/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public interface ReportSeeker {

    List<Report> getReports();

    Stream<Report> getReportStream();

    Stream<TaskReport> getTaskReportStream();

    TaskReport getTaskReport(String taskKey);

    List<Report> getReport(String reportKey);

    Report getReport(String taskKey, String reportKey);
}
