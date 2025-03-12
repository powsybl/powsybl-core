/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;

import java.util.Objects;

import static com.powsybl.commons.report.ReportBundleBaseName.BUNDLE_BASE_NAME;

/**
 * @author Alice Caron {@literal <alice.caron at rte-france.com>}
 */
public final class NetworkReports {

    private static final String DANGLING_LINE_ID_1 = "danglingLineId1";
    private static final String DANGLING_LINE_ID_2 = "danglingLineId2";

    private NetworkReports() {
    }

    public static void alreadyConnectedIdentifiableTerminal(ReportNode reportNode, String identifiableId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.alreadyConnectedTerminal", BUNDLE_BASE_NAME)
                .withUntypedValue("identifiable", identifiableId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void alreadyDisconnectedIdentifiableTerminal(ReportNode reportNode, String identifiableId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.alreadyDisconnectedTerminal", BUNDLE_BASE_NAME)
                .withUntypedValue("identifiable", identifiableId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void parentHasBothRatioAndPhaseTapChanger(ReportNode reportNode, String parentMessage) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.validationWarningBothRatioPhase", BUNDLE_BASE_NAME)
                .withUntypedValue("parent", parentMessage)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void parentHasDuplicatePointForActivePower(ReportNode reportNode, String ownerMessage, Double p) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.validationWarningDuplicate", BUNDLE_BASE_NAME)
                .withUntypedValue("parent", ownerMessage)
                .withUntypedValue("p", p)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static ReportNode runIidmNetworkValidationCHecks(ReportNode reportNode, String networkId) {
        return Objects.requireNonNull(reportNode).newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.IIDMValidation", BUNDLE_BASE_NAME)
                .withUntypedValue("networkId", networkId)
                .add();
    }

    // DEBUG
    public static void inconsistentPropertyValues(ReportNode reportNode, String propertyName, String propertyValue1, String propertyValue2, String danglingLineId1, String danglingLineId2) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.InconsistentPropertyValues", BUNDLE_BASE_NAME)
                .withUntypedValue("propertyName", propertyName)
                .withUntypedValue(DANGLING_LINE_ID_1, danglingLineId1)
                .withUntypedValue("propertyValue1", propertyValue1)
                .withUntypedValue(DANGLING_LINE_ID_2, danglingLineId2)
                .withUntypedValue("propertyValue2", propertyValue2)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .add();
    }

    public static void moveCommonAliases(ReportNode reportNode, String alias, String danglingLineId1, String danglingLineId2) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.MoveCommonAlias", BUNDLE_BASE_NAME)
                .withUntypedValue("alias", alias)
                .withUntypedValue(DANGLING_LINE_ID_1, danglingLineId1)
                .withUntypedValue(DANGLING_LINE_ID_2, danglingLineId2)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .add();
    }

    public static void propertyOnlyOnOneSide(ReportNode reportNode, String propertyName, String propertyValue, int emptySide, String danglingLineId1, String danglingLineId2) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.PropertyOnlyOnOneSide", BUNDLE_BASE_NAME)
                .withUntypedValue("propertyName", propertyName)
                .withUntypedValue("side", emptySide)
                .withUntypedValue(DANGLING_LINE_ID_1, danglingLineId1)
                .withUntypedValue(DANGLING_LINE_ID_2, danglingLineId2)
                .withUntypedValue("propertyValue", propertyValue)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .add();
    }

    // WARN
    public static void inconsistentAliasTypes(ReportNode reportNode, String alias, String type1, String type2, String danglingLineId1, String danglingLineId2) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.InconsistentAliasTypes", BUNDLE_BASE_NAME)
                .withUntypedValue("alias", alias)
                .withUntypedValue(DANGLING_LINE_ID_1, danglingLineId1)
                .withUntypedValue("type1", type1)
                .withUntypedValue(DANGLING_LINE_ID_2, danglingLineId2)
                .withUntypedValue("type2", type2)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void inconsistentAliasValues(ReportNode reportNode, String alias1, String alias2, String type, String danglingLineId1, String danglingLineId2) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.InconsistentAliasValues", BUNDLE_BASE_NAME)
                .withUntypedValue("alias1", alias1)
                .withUntypedValue("alias2", alias2)
                .withUntypedValue(DANGLING_LINE_ID_1, danglingLineId1)
                .withUntypedValue(DANGLING_LINE_ID_2, danglingLineId2)
                .withUntypedValue("type", type)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static ReportNode createChildReportNode(ReportNode reportNode, ReadOnlyDataSource ds) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.importDataSource", BUNDLE_BASE_NAME)
                .withUntypedValue("dataSource", ds.getBaseName())
                .add();
    }

    public static void exportMock(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.export_test", BUNDLE_BASE_NAME)
                .add();
    }

    public static void testImportPostProcessor(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.testImportPostProcessor", BUNDLE_BASE_NAME)
                .add();
    }

    public static void undefinedShuntCompensatorSection(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.undefinedShuntSection", BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void invalidP0(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.invalidP0", BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void tapPositionNotSet(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.tapPositionNotSet", BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void tapPositionUnset(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.tapPositionUnset", BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unsetSectionCount(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.sectionCountUnset", BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void tapPositionIncorrect(ReportNode reportNode, int tapPosition, int lowTapPosition, int highTapPosition) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.tapPositionIncorrect", BUNDLE_BASE_NAME)
                .withUntypedValue("tapPosition", tapPosition)
                .withUntypedValue("lowTapPosition", lowTapPosition)
                .withUntypedValue("highTapPosition", highTapPosition)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void undefinedLossFactor(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.undefinedLossFactor", BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void invalidQ0(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.invalidQ0", BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void activePowerSetpointInvalid(double activePowerSetpoint, ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.activePowerSetpointInvalid", BUNDLE_BASE_NAME)
                .withTypedValue("activePowerSetpoint", activePowerSetpoint, TypedValue.ACTIVE_POWER)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void voltageSetpointInvalid(double voltageSetpoint, ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.voltageSetpointInvalid", BUNDLE_BASE_NAME)
                .withTypedValue("voltageSetpoint", voltageSetpoint, TypedValue.VOLTAGE)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void voltageSetpointInvalidVoltageRegulatorOn(double voltageSetpoint, ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.voltageSetpointInvalidVoltageRegulatorOn", BUNDLE_BASE_NAME)
                .withTypedValue("voltageSetpoint", voltageSetpoint, TypedValue.VOLTAGE)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void reactivePowerSetpointInvalid(double reactivePowerSetpoint, ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.reactivePowerSetpointInvalid", BUNDLE_BASE_NAME)
                .withTypedValue("reactivePowerSetpoint", reactivePowerSetpoint, TypedValue.REACTIVE_POWER)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void reactivePowerSetpointInvalidVoltageRegulatorOff(double reactivePowerSetpoint, ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.reactivePowerSetpointInvalidVoltageRegulatorOff", BUNDLE_BASE_NAME)
                .withTypedValue("reactivePowerSetpoint", reactivePowerSetpoint, TypedValue.REACTIVE_POWER)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void regulatingRtcNoRegulationMode(boolean errorSeverity, ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.regulatingRtcNoRegulationMode", BUNDLE_BASE_NAME)
                .withSeverity(errorSeverity ? TypedValue.ERROR_SEVERITY : TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void regulatingRtcNoRegulationValue(boolean errorSeverity, ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.regulatingRtcNoRegulationValue", BUNDLE_BASE_NAME)
                .withSeverity(errorSeverity ? TypedValue.ERROR_SEVERITY : TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void regulatingRtcNoRegulationTerminal(boolean errorSeverity, ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.regulatingRtcNoRegulationTerminal", BUNDLE_BASE_NAME)
                .withSeverity(errorSeverity ? TypedValue.ERROR_SEVERITY : TypedValue.WARN_SEVERITY)
                .add();

    }

    public static void temporaryLimitsButPermanentLimitUndefined(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.temporaryLimitsButPermanentLimitUndefined", BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void svcRegulationModeInvalid(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.svcRegulationModeInvalid", BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void hvdcConverterModeInvalid(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.hvdcConverterModeInvalid", BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void tooManyRegulatingControlEnabled(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.tooManyRegulatingControlEnabled", BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void ptcPhaseRegulationModeNotSet(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.ptcPhaseRegulationModeNotSet", BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void ptcPhaseRegulationRegulationValueNotSet(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.ptcPhaseRegulationRegulationValueNotSet", BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void ptcPhaseRegulationNoRegulatedTerminal(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.ptcPhaseRegulationNoRegulatedTerminal", BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void ptcPhaseRegulationFixedMode(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core.iidm.network.ptcPhaseRegulationFixedMode", BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void targetDeadbandUndefinedValue(String validableType, ReportNode reportNode) {
        String key = switch (validableType) {
            case "ratio tap changer" -> "core.iidm.network.rtcTargetDeadbandUndefinedValue";
            case "phase tap changer" -> "core.iidm.network.ptcTargetDeadbandUndefinedValue";
            case "shunt compensator" -> "core.iidm.network.scTargetDeadbandUndefinedValue";
            default -> throw new IllegalArgumentException("Unsupported validable type: " + validableType);
        };
        reportNode.newReportNode()
                .withLocaleMessageTemplate(key, BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }
}
