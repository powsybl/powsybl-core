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
    public static final String BUNDLE_BASE_NAME = "com.powsybl.commons.reports";

    private UcteReports() {
    }

    public static ReportNode fixUcteTransformers(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("fixUcteTransformer", BUNDLE_BASE_NAME)
                .add();
    }

    public static ReportNode fixUcteRegulations(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("fixUcteRegulations", BUNDLE_BASE_NAME)
                .add();
    }

    public static void negativeLineResistance(UcteLine line, ReportNode reportNode, String lineId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("negativeLineResistance", BUNDLE_BASE_NAME)
                .withUntypedValue(LINE_ID_KEY, lineId)
                .withTypedValue("resistance", line.getResistance(), TypedValue.RESISTANCE)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void undefinedActivePower(ReportNode reportNode, String code) {
        reportNode.newReportNode()
                .withMessageTemplate("activePowerUndefined", "Node ${node}: active power is undefined, set value to 0")
                .withUntypedValue("node", code.toString())
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void switchVoltageLevelTypeCOdeToPQ(ReportNode reportNode, String code, double voltageReference) {
        reportNode.newReportNode()
                .withMessageTemplate("PvUndefinedVoltage", "Node ${node}: voltage is regulated, but voltage setpoint is null (${voltageReference}), switch type code to PQ")
                .withUntypedValue("node", code)
                .withUntypedValue("voltageReference", voltageReference)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void nullifyVoltageLevelReactivePower(ReportNode reportNode, String code) {
        reportNode.newReportNode()
                .withMessageTemplate("PqUndefinedReactivePower", "Node ${node}: voltage is not regulated but reactive power is undefined, set value to 0")
                .withUntypedValue("node", code)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static ReportNode fixUcteNodes(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("fixUcteNodes", "Fix UCTE nodes")
                .add();
    }

    public static ReportNode fixUcteLines(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("fixUcteLines", "Fix UCTE lines")
                .add();
    }

    public static void invalidateRealLineReactance(ReportNode reportNode, String lineId, double reactance) {
        reportNode.newReportNode()
                .withMessageTemplate("epsilonLineReactance", "${lineId} - Real line reactance must be larger than 0.05 ohm (${reactance} ohm)")
                .withUntypedValue(LINE_ID_KEY, lineId)
                .withTypedValue("reactance", reactance, TypedValue.REACTANCE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidateBusbarCouplerResistance(ReportNode reportNode, String lineId, double resistance) {
        reportNode.newReportNode()
                .withMessageTemplate("nonZeroBusbarCouplerResistance", "${lineId} - Busbar coupler resistance must be zero (${resistance} ohm)")
                .withUntypedValue(LINE_ID_KEY, lineId)
                .withTypedValue("resistance", resistance, TypedValue.RESISTANCE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidateBusbarCouplerReactance(ReportNode reportNode, String lineId, double reactance) {
        reportNode.newReportNode()
                .withMessageTemplate("nonZeroBusbarCouplerReactance", "${lineId} - Busbar coupler reactance must be zero (${reactance} ohm)")
                .withUntypedValue(LINE_ID_KEY, lineId)
                .withTypedValue("reactance", reactance, TypedValue.REACTANCE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidateBusbarCouplerSusceptance(ReportNode reportNode, String lineId, double susceptance) {
        reportNode.newReportNode()
                .withMessageTemplate("nonZeroBusbarCouplerSusceptance", "${lineId} - Busbar coupler susceptance must be zero (${susceptance} ohm)")
                .withUntypedValue(LINE_ID_KEY, lineId)
                .withTypedValue("susceptance", susceptance, TypedValue.SUSCEPTANCE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidateTransformerNominalPowerValue(ReportNode reportNode, String transformerId, double nominalPower) {
        reportNode.newReportNode()
                .withMessageTemplate("epsilonTransformerNominalPower", "${transformerId} - Value must be positive, blank and zero is not allowed (${nominalPower} ohm)")
                .withUntypedValue("transformerId", transformerId)
                .withUntypedValue("nominalPower", nominalPower)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void invalidateLtcTransformerPhaseRegulationValue(ReportNode reportNode, String transfoId, double uctePhaseRegulationDu) {
        reportNode.newReportNode()
                .withMessageTemplate("wrongPhaseRegulationDu", "${transfoId} - For LTCs, transformer phase regulation voltage per tap should not be zero. Its absolute value should not be above 6 % (${du} %)")
                .withUntypedValue("transfoId", transfoId)
                .withUntypedValue("du", uctePhaseRegulationDu)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidateLtcTransformerAngleRegulationValue(ReportNode reportNode, String transfoId, double uctePhaseRegulationDu) {
        reportNode.newReportNode()
                .withMessageTemplate("wrongAngleRegulationDu", "${transfoId} - For LTCs, transformer angle regulation voltage per tap should not be zero. Its absolute value should not be above 6 % (${du} %)")
                .withUntypedValue("transfoId", transfoId)
                .withUntypedValue("du", uctePhaseRegulationDu)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void unsupportedTtBlock(ReportNode reportNode) {
        reportNode.newReportNode()
                .withMessageTemplate("UnsupportedTTBlock", "TT block not supported")
                .add();
    }

    public static ReportNode readUcteNetworkFile(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("UcteReading", "Reading UCTE network file")
                .add();
    }
}
