/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model.io;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;

import java.time.OffsetDateTime;

/**
 * Functional logs produced while reading NC profiles.
 *
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public final class NcModelReports {

    private NcModelReports() {
    }

    public static ReportNode readingNcProfilesReport(ReportNode reportNode, String dataSourceName) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.nc.model.readingNcProfiles")
                .withUntypedValue("dataSource", dataSourceName)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void ncProfileRead(ReportNode reportNode, String name, String keyword) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.model.ncProfileRead")
                .withTypedValue("instanceFile", name, TypedValue.FILENAME)
                .withUntypedValue("keyword", keyword)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void ncProfileWithoutKeyword(ReportNode reportNode, String name) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.model.ncProfileWithoutKeyword")
                .withTypedValue("instanceFile", name, TypedValue.FILENAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void ncProfileWithoutData(ReportNode reportNode, String name) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.model.ncProfileWithoutData")
                .withTypedValue("instanceFile", name, TypedValue.FILENAME)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void ncProfileNotApplicable(ReportNode reportNode, String name, OffsetDateTime timestamp) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.model.ncProfileNotApplicable")
                .withTypedValue("instanceFile", name, TypedValue.FILENAME)
                .withUntypedValue("timestamp", String.valueOf(timestamp))
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void unsupportedNcVersion(ReportNode reportNode, String name, String version) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.model.unsupportedNcVersion")
                .withTypedValue("instanceFile", name, TypedValue.FILENAME)
                .withUntypedValue("version", version)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }
}
