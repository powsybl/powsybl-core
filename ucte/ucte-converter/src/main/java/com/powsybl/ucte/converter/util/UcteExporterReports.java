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
import com.powsybl.iidm.network.IdentifiableType;

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

    public static ReportNode postProcessor(ReportNode reportNode, String postProcessorName) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.postProcessor")
                .withUntypedValue("postProcessorName", postProcessorName)
                .add();
    }

    public static void fileWritten(ReportNode reportNode, String fileName) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.fileWritten")
                .withUntypedValue("fileName", fileName)
                .add();
    }

    public static void switchCurrentLimitMissing(ReportNode reportNode, String switchId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.switchCurrentLimitMissing")
                .withUntypedValue("switchId", switchId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void noLoadFlowResults(ReportNode reportNode) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.noLoadFlowResults")
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static ReportNode deenergizedElements(ReportNode reportNode, int count) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.deenergizedElements")
                .withUntypedValue("count", count)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void deenergizedElement(ReportNode reportNode, IdentifiableType elementType, String ucteElementId) {
        reportNode.newReportNode()
                .withMessageTemplate(getDeenergizedElementKey(elementType))
                .withUntypedValue("ucteElementId", ucteElementId)
                .withSeverity(TypedValue.DETAIL_SEVERITY)
                .add();
    }

    /**
     * Get the message key reporting a de-energized element of the given type.
     *
     * @param elementType the type of the de-energized element
     * @return the message key for this type of element
     * @throws IllegalArgumentException if the type is not one of the types of elements set out of operation
     */
    private static String getDeenergizedElementKey(IdentifiableType elementType) {
        return switch (elementType) {
            case BOUNDARY_LINE -> "core.ucte.export.deenergizedBoundaryLine";
            case LINE -> "core.ucte.export.deenergizedLine";
            case SWITCH -> "core.ucte.export.deenergizedSwitch";
            case TIE_LINE -> "core.ucte.export.deenergizedTieLine";
            case TWO_WINDINGS_TRANSFORMER -> "core.ucte.export.deenergizedTwoWindingsTransformer";
            default -> throw new IllegalArgumentException("Unexpected element type: " + elementType);
        };
    }
}
