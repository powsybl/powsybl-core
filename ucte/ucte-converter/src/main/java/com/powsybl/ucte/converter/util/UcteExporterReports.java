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

    public static final String BUS_ID = "busId";
    public static final String GENERATOR_ID = "generatorId";
    public static final String TARGET_V = "targetV";

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

    public static void aggregatedPowerLimitOutOfBounds(ReportNode reportNode, String busId, String limitName, double value) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.aggregatedPowerLimitOutOfBounds")
                .withUntypedValue(BUS_ID, busId)
                .withUntypedValue("limitName", limitName)
                .withUntypedValue("value", value)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void mixedPowerPlantTypes(ReportNode reportNode, String busId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.mixedPowerPlantTypes")
                .withUntypedValue(BUS_ID, busId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void remoteVoltageTargetRescaled(ReportNode reportNode, String busId, String generatorId, String remoteBusId,
                                                   double remoteTargetV, double localTargetV) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.remoteVoltageTargetRescaled")
                .withUntypedValue(BUS_ID, busId)
                .withUntypedValue(GENERATOR_ID, generatorId)
                .withUntypedValue("remoteBusId", remoteBusId)
                .withUntypedValue("remoteTargetV", remoteTargetV)
                .withUntypedValue("localTargetV", localTargetV)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void voltageTargetIgnored(ReportNode reportNode, String busId, String generatorId, String regulatedEquipmentId,
                                            double targetV) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.voltageTargetIgnored")
                .withUntypedValue(BUS_ID, busId)
                .withUntypedValue(GENERATOR_ID, generatorId)
                .withUntypedValue("regulatedEquipmentId", regulatedEquipmentId)
                .withUntypedValue(TARGET_V, targetV)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void voltageTargetMissing(ReportNode reportNode, String busId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.voltageTargetMissing")
                .withUntypedValue(BUS_ID, busId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void voltageTargetNotFromRegulatingGenerator(ReportNode reportNode, String busId, String generatorId, double targetV) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.voltageTargetNotFromRegulatingGenerator")
                .withUntypedValue(BUS_ID, busId)
                .withUntypedValue(GENERATOR_ID, generatorId)
                .withUntypedValue(TARGET_V, targetV)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void voltageTargetConflict(ReportNode reportNode, String busId, String generatorId, double targetV) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.voltageTargetConflict")
                .withUntypedValue(BUS_ID, busId)
                .withUntypedValue(GENERATOR_ID, generatorId)
                .withUntypedValue(TARGET_V, targetV)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void nominalPowerMissing(ReportNode reportNode, String transformerId, double nominalPowerNovalue) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.export.nominalPowerMissing")
                .withUntypedValue("transformerId", transformerId)
                .withUntypedValue("nominalPowerNovalue", (int) nominalPowerNovalue)
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
