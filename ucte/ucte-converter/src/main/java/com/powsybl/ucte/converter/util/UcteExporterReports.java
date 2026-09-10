/**
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter.util;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;

/**
 * Message-templated {@link ReportNode} entries reported by {@link com.powsybl.ucte.converter.UcteExporter}.
 *
 * @author Arthur Michaut {@literal <arthur.michaut at artelys.com>}
 */
public final class UcteExporterReports {

    private UcteExporterReports() {
    }

    public static ReportNode networkCreation(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.networkCreation")
                .add();
    }

    public static ReportNode busesAndSwitches(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.busesAndSwitches")
                .add();
    }

    public static ReportNode boundaryLines(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.boundaryLines")
                .add();
    }

    public static ReportNode lines(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.lines")
                .add();
    }

    public static ReportNode tieLines(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.tieLines")
                .add();
    }

    public static ReportNode transformers(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.transformers")
                .add();
    }

    public static void fileWritten(ReportNode reportNode, String fileName) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.fileWritten")
                .withUntypedValue("fileName", fileName)
                .add();
    }

    public static void ignoredYNode(ReportNode reportNode, String busId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.ignoredYNode")
                .withUntypedValue("busId", busId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void switchCurrentLimitMissing(ReportNode reportNode, String switchId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.switchCurrentLimitMissing")
                .withUntypedValue("switchId", switchId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }
}
