/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
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

    public static ReportNode exportUcteNetwork(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.UcteExport")
                .add();
    }

    public static void ignoredYNode(ReportNode reportNode, String busId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.ignoredYNode")
                .withUntypedValue("busId", busId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void ignoredBoundaryLineAtYNode(ReportNode reportNode, String boundaryLineId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.ignoredBoundaryLineAtYNode")
                .withUntypedValue("boundaryLineId", boundaryLineId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void mergedPropertySide1Empty(ReportNode reportNode, String key, String side2Value) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.mergedPropertySide1Empty")
                .withUntypedValue("key", key)
                .withUntypedValue("side2Value", side2Value)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .add();
    }

    public static void mergedPropertySide2Empty(ReportNode reportNode, String key, String side1Value) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.mergedPropertySide2Empty")
                .withUntypedValue("key", key)
                .withUntypedValue("side1Value", side1Value)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .add();
    }

    public static void mergedPropertyInconsistent(ReportNode reportNode, String key, String side1Value, String side2Value) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.mergedPropertyInconsistent")
                .withUntypedValue("key", key)
                .withUntypedValue("side1Value", side1Value)
                .withUntypedValue("side2Value", side2Value)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .add();
    }

    public static void transformerAtBoundaryExported(ReportNode reportNode, String transformerId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.transformerAtBoundaryExported")
                .withUntypedValue("transformerId", transformerId)
                .withSeverity(TypedValue.INFO_SEVERITY)
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
