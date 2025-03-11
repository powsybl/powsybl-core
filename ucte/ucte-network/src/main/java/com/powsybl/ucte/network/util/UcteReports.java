/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network.util;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.ucte.network.UcteLine;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class UcteReports {

    public static final String LINE_ID_KEY = "lineId";

    private UcteReports() {
    }

    public static ReportNode fixUcteTransformers(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.fixUcteTransformer")
                .add();
    }

    public static ReportNode fixUcteRegulations(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.fixUcteRegulations")
                .add();
    }

    public static void negativeLineResistance(UcteLine line, ReportNode reportNode, String lineId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.negativeLineResistance")
                .withUntypedValue(LINE_ID_KEY, lineId)
                .withTypedValue("resistance", line.getResistance(), TypedValue.RESISTANCE)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void undefinedActivePower(ReportNode reportNode, String code) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.activePowerUndefined")
                .withUntypedValue("node", code)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void switchVoltageLevelTypeCOdeToPQ(ReportNode reportNode, String code, double voltageReference) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.PvUndefinedVoltage")
                .withUntypedValue("node", code)
                .withUntypedValue("voltageReference", voltageReference)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void nullifyVoltageLevelReactivePower(ReportNode reportNode, String code) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.PqUndefinedReactivePower")
                .withUntypedValue("node", code)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static ReportNode fixUcteNodes(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.fixUcteNodes")
                .add();
    }

    public static ReportNode fixUcteLines(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.fixUcteLines")
                .add();
    }

    public static void invalidateRealLineReactance(ReportNode reportNode, String lineId, double reactance) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.epsilonLineReactance")
                .withUntypedValue(LINE_ID_KEY, lineId)
                .withTypedValue("reactance", reactance, TypedValue.REACTANCE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidateBusbarCouplerResistance(ReportNode reportNode, String lineId, double resistance) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.nonZeroBusbarCouplerResistance")
                .withUntypedValue(LINE_ID_KEY, lineId)
                .withTypedValue("resistance", resistance, TypedValue.RESISTANCE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidateBusbarCouplerReactance(ReportNode reportNode, String lineId, double reactance) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.nonZeroBusbarCouplerReactance")
                .withUntypedValue(LINE_ID_KEY, lineId)
                .withTypedValue("reactance", reactance, TypedValue.REACTANCE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidateBusbarCouplerSusceptance(ReportNode reportNode, String lineId, double susceptance) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.nonZeroBusbarCouplerSusceptance")
                .withUntypedValue(LINE_ID_KEY, lineId)
                .withTypedValue("susceptance", susceptance, TypedValue.SUSCEPTANCE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidateTransformerNominalPowerValue(ReportNode reportNode, String transformerId, double nominalPower) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.epsilonTransformerNominalPower")
                .withUntypedValue("transformerId", transformerId)
                .withUntypedValue("nominalPower", nominalPower)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void invalidateLtcTransformerPhaseRegulationValue(ReportNode reportNode, String transfoId, double uctePhaseRegulationDu) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.wrongPhaseRegulationDu")
                .withUntypedValue("transfoId", transfoId)
                .withUntypedValue("du", uctePhaseRegulationDu)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidateLtcTransformerAngleRegulationValue(ReportNode reportNode, String transfoId, double uctePhaseRegulationDu) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.wrongAngleRegulationDu")
                .withUntypedValue("transfoId", transfoId)
                .withUntypedValue("du", uctePhaseRegulationDu)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void unsupportedTtBlock(ReportNode reportNode) {
        reportNode.newReportNode()
                .withMessageTemplate("core.ucte.UnsupportedTTBlock")
                .add();
    }

    public static ReportNode readUcteNetworkFile(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.ucte.UcteReading")
                .add();
    }
}
