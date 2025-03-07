/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;

/**
 * @author Alice Caron {@literal <alice.caron at rte-france.com>}
 */
public final class TimeseriesReports {

    public static final String BUNDLE_BASE_NAME = "com.powsybl.commons.reports";

    private TimeseriesReports() {
    }

    public static void warnsOnTimeseriesVersionNumber(ReportNode reportNode, int versionNumber, String line) {
        reportNode.newReportNode()
                .withMessageTemplate("invalidVersionNumber", "The version number for a versioned TimeSeries should not be equals to the default version number (${versionNumber}) at line \"${line}\"")
                .withSeverity(TypedValue.WARN_SEVERITY)
                .withUntypedValue("versionNumber", versionNumber)
                .withUntypedValue("line", line)
                .add();
    }

    public static void timeseriesLoadingTimeDuration(ReportNode reportNode, int tsNumber, long timing) {
        reportNode.newReportNode()
                .withMessageTemplate("timeseriesLoadingTime", "${tsNumber} time series loaded from CSV in ${timing} ms")
                .withUntypedValue("tsNumber", tsNumber)
                .withUntypedValue("timing", timing)
                .add();
    }
}
