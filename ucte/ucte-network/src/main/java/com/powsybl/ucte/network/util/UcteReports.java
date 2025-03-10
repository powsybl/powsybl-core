/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network.util;

import com.powsybl.commons.report.ReportBundleBaseName;
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
                .withLocaleMessageTemplate("core-ucte-fixUcteTransformer", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .add();
    }

    public static ReportNode fixUcteRegulations(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-ucte-fixUcteRegulations", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .add();
    }

    public static void negativeLineResistance(UcteLine line, ReportNode reportNode, String lineId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-ucte-negativeLineResistance", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(LINE_ID_KEY, lineId)
                .withTypedValue("resistance", line.getResistance(), TypedValue.RESISTANCE)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void undefinedActivePower(ReportNode reportNode, String code) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-ucte-activePowerUndefined", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("node", code.toString())
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void switchVoltageLevelTypeCOdeToPQ(ReportNode reportNode, String code, double voltageReference) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-ucte-PvUndefinedVoltage", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("node", code)
                .withUntypedValue("voltageReference", voltageReference)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void nullifyVoltageLevelReactivePower(ReportNode reportNode, String code) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-ucte-PqUndefinedReactivePower", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("node", code)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static ReportNode fixUcteNodes(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-ucte-fixUcteNodes", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .add();
    }

    public static ReportNode fixUcteLines(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-ucte-fixUcteLines", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .add();
    }

    public static void invalidateRealLineReactance(ReportNode reportNode, String lineId, double reactance) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-ucte-epsilonLineReactance", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(LINE_ID_KEY, lineId)
                .withTypedValue("reactance", reactance, TypedValue.REACTANCE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidateBusbarCouplerResistance(ReportNode reportNode, String lineId, double resistance) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-ucte-nonZeroBusbarCouplerResistance", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(LINE_ID_KEY, lineId)
                .withTypedValue("resistance", resistance, TypedValue.RESISTANCE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidateBusbarCouplerReactance(ReportNode reportNode, String lineId, double reactance) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-ucte-nonZeroBusbarCouplerReactance", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(LINE_ID_KEY, lineId)
                .withTypedValue("reactance", reactance, TypedValue.REACTANCE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidateBusbarCouplerSusceptance(ReportNode reportNode, String lineId, double susceptance) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-ucte-nonZeroBusbarCouplerSusceptance", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(LINE_ID_KEY, lineId)
                .withTypedValue("susceptance", susceptance, TypedValue.SUSCEPTANCE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidateTransformerNominalPowerValue(ReportNode reportNode, String transformerId, double nominalPower) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-ucte-epsilonTransformerNominalPower", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("transformerId", transformerId)
                .withUntypedValue("nominalPower", nominalPower)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void invalidateLtcTransformerPhaseRegulationValue(ReportNode reportNode, String transfoId, double uctePhaseRegulationDu) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-ucte-wrongPhaseRegulationDu", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("transfoId", transfoId)
                .withUntypedValue("du", uctePhaseRegulationDu)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidateLtcTransformerAngleRegulationValue(ReportNode reportNode, String transfoId, double uctePhaseRegulationDu) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-ucte-wrongAngleRegulationDu", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("transfoId", transfoId)
                .withUntypedValue("du", uctePhaseRegulationDu)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void unsupportedTtBlock(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-ucte-UnsupportedTTBlock", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .add();
    }

    public static ReportNode readUcteNetworkFile(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-ucte-UcteReading", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .add();
    }
}
