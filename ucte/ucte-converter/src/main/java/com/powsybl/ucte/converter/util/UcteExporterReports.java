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

    public static void tapChangerModelDeviation(ReportNode reportNode, String equipmentId, String tapChangerType) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.tapChangerModelDeviation")
                .withUntypedValue("equipmentId", equipmentId)
                .withUntypedValue("tapChangerType", tapChangerType)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void tapPositionRangeExtended(ReportNode reportNode,
                                                String equipmentId,
                                                String tapChangerType,
                                                int lowSpan,
                                                int highSpan) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.tapPositionRangeExtended")
                .withUntypedValue("equipmentId", equipmentId)
                .withUntypedValue("tapChangerType", tapChangerType)
                .withUntypedValue("lowSpan", lowSpan)
                .withUntypedValue("highSpan", highSpan)
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
}
