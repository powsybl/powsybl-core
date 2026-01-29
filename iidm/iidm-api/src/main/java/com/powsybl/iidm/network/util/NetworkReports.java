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

/**
 * @author Alice Caron {@literal <alice.caron at rte-france.com>}
 */
public final class NetworkReports {

    private static final String DANGLING_LINE_ID_1 = "boundaryLineId1";
    private static final String DANGLING_LINE_ID_2 = "boundaryLineId2";

    private NetworkReports() {
    }

    public static void alreadyConnectedIdentifiableTerminal(ReportNode reportNode, String identifiableId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.alreadyConnectedTerminal")
                .withUntypedValue("identifiable", identifiableId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void alreadyDisconnectedIdentifiableTerminal(ReportNode reportNode, String identifiableId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.alreadyDisconnectedTerminal")
                .withUntypedValue("identifiable", identifiableId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void transformerHasBothRatioAndPhaseTapChanger(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.validationWarningBothRatioPhase")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void parentHasDuplicatePointForActivePower(ReportNode reportNode, String id, Double p) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.validationWarningReactiveCapabilityCurveDuplicate")
                .withTypedValue("id", id, TypedValue.ID)
                .withUntypedValue("p", p)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static ReportNode runIidmNetworkValidationCHecks(ReportNode reportNode, String networkId) {
        return Objects.requireNonNull(reportNode).newReportNode()
                .withMessageTemplate("core.iidm.network.IIDMValidation")
                .withUntypedValue("networkId", networkId)
                .add();
    }

    // DEBUG
    public static void inconsistentPropertyValues(ReportNode reportNode, String propertyName, String propertyValue1, String propertyValue2, String boundaryLineId1, String boundaryLineId2) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.InconsistentPropertyValues")
                .withUntypedValue("propertyName", propertyName)
                .withUntypedValue(DANGLING_LINE_ID_1, boundaryLineId1)
                .withUntypedValue("propertyValue1", propertyValue1)
                .withUntypedValue(DANGLING_LINE_ID_2, boundaryLineId2)
                .withUntypedValue("propertyValue2", propertyValue2)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .add();
    }

    public static void moveCommonAliases(ReportNode reportNode, String alias, String boundaryLineId1, String boundaryLineId2) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.MoveCommonAlias")
                .withUntypedValue("alias", alias)
                .withUntypedValue(DANGLING_LINE_ID_1, boundaryLineId1)
                .withUntypedValue(DANGLING_LINE_ID_2, boundaryLineId2)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .add();
    }

    public static void propertyOnlyOnOneSide(ReportNode reportNode, String propertyName, String propertyValue, int emptySide, String boundaryLineId1, String boundaryLineId2) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.PropertyOnlyOnOneSide")
                .withUntypedValue("propertyName", propertyName)
                .withUntypedValue("side", emptySide)
                .withUntypedValue(DANGLING_LINE_ID_1, boundaryLineId1)
                .withUntypedValue(DANGLING_LINE_ID_2, boundaryLineId2)
                .withUntypedValue("propertyValue", propertyValue)
                .withSeverity(TypedValue.DEBUG_SEVERITY)
                .add();
    }

    // WARN
    public static void inconsistentAliasTypes(ReportNode reportNode, String alias, String type1, String type2, String boundaryLineId1, String boundaryLineId2) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.InconsistentAliasTypes")
                .withUntypedValue("alias", alias)
                .withUntypedValue(DANGLING_LINE_ID_1, boundaryLineId1)
                .withUntypedValue("type1", type1)
                .withUntypedValue(DANGLING_LINE_ID_2, boundaryLineId2)
                .withUntypedValue("type2", type2)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void inconsistentAliasValues(ReportNode reportNode, String alias1, String alias2, String type, String boundaryLineId1, String boundaryLineId2) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.InconsistentAliasValues")
                .withUntypedValue("alias1", alias1)
                .withUntypedValue("alias2", alias2)
                .withUntypedValue(DANGLING_LINE_ID_1, boundaryLineId1)
                .withUntypedValue(DANGLING_LINE_ID_2, boundaryLineId2)
                .withUntypedValue("type", type)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static ReportNode createChildReportNode(ReportNode reportNode, ReadOnlyDataSource ds) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.importDataSource")
                .withUntypedValue("dataSource", ds.getBaseName())
                .add();
    }

    public static void exportMock(ReportNode reportNode) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.export_test")
                .add();
    }

    public static void testImportPostProcessor(ReportNode reportNode) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.testImportPostProcessor")
                .add();
    }

    public static void undefinedShuntCompensatorSection(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.undefinedShuntSection")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void invalidP0(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.invalidP0")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void tapPositionNotSet(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.tapPositionNotSet")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void hvdcUndefinedLossFactor(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.hvdcUndefinedLossFactor")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void invalidQ0(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.invalidQ0")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void activePowerSetpointInvalid(ReportNode reportNode, String id, double activePowerSetpoint) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.activePowerSetpointInvalid")
                .withTypedValue("id", id, TypedValue.ID)
                .withTypedValue("activePowerSetpoint", activePowerSetpoint, TypedValue.ACTIVE_POWER)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void svcVoltageSetpointInvalid(ReportNode reportNode, String id, double voltageSetpoint) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.svcVoltageSetpointInvalid")
                .withTypedValue("id", id, TypedValue.ID)
                .withTypedValue("voltageSetpoint", voltageSetpoint, TypedValue.VOLTAGE)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void voltageSetpointInvalidVoltageRegulatorOn(ReportNode reportNode, String id, double voltageSetpoint) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.voltageSetpointInvalidVoltageRegulatorOn")
                .withTypedValue("id", id, TypedValue.ID)
                .withTypedValue("voltageSetpoint", voltageSetpoint, TypedValue.VOLTAGE)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void svcReactivePowerSetpointInvalid(ReportNode reportNode, String id, double reactivePowerSetpoint) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.svcReactivePowerSetpointInvalid")
                .withTypedValue("id", id, TypedValue.ID)
                .withTypedValue("reactivePowerSetpoint", reactivePowerSetpoint, TypedValue.REACTIVE_POWER)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void reactivePowerSetpointInvalidVoltageRegulatorOff(ReportNode reportNode, String id, double reactivePowerSetpoint) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.reactivePowerSetpointInvalidVoltageRegulatorOff")
                .withTypedValue("id", id, TypedValue.ID)
                .withTypedValue("reactivePowerSetpoint", reactivePowerSetpoint, TypedValue.REACTIVE_POWER)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void rtcRegulationCannotBeEnabledWithoutLoadTapChanging(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.rtcRegulationCannotBeEnabledWithoutLoadTapChanging")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void regulatingRtcNoRegulationMode(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.regulatingRtcNoRegulationMode")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void regulatingRtcNoRegulationValue(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.regulatingRtcNoRegulationValue")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void regulatingRtcBadTargetVoltage(ReportNode reportNode, String id, double regulationValue) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.regulatingRtcBadTargetVoltage")
                .withTypedValue("id", id, TypedValue.ID)
                .withTypedValue("regulationValue", regulationValue, TypedValue.VOLTAGE)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void regulatingRtcNoRegulationTerminal(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.regulatingRtcNoRegulationTerminal")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();

    }

    public static void temporaryLimitsButPermanentLimitUndefined(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.temporaryLimitsButPermanentLimitUndefined")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void svcRegulationModeInvalid(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.svcRegulationModeInvalid")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void hvdcConverterModeInvalid(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.hvdcConverterModeInvalid")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void acDcConverterControlModeNotSet(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.acDcConverterControlModeNotSet")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void acDcConverterControlTargetPInvalid(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.acDcConverterControlTargetPInvalid")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void acDcConverterControlInvalidTargetVDc(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.acDcConverterControlInvalidTargetVDc")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void tooManyRegulatingControlEnabled(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.tooManyRegulatingControlEnabled")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void ptcPhaseRegulationModeNotSet(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.ptcPhaseRegulationModeNotSet")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void ptcPhaseRegulationCannotBeEnabledWithoutLoadTapChanging(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.ptcPhaseRegulationCannotBeEnabledWithoutLoadTapChanging")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void ptcPhaseRegulationRegulationValueNotSet(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.ptcPhaseRegulationRegulationValueNotSet")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void ptcPhaseRegulationNoRegulatedTerminal(ReportNode reportNode, String id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.network.ptcPhaseRegulationNoRegulatedTerminal")
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void targetDeadbandUndefinedValue(ReportNode reportNode, String validableType, String id) {
        String key = switch (validableType) {
            case "ratio tap changer" -> "core.iidm.network.rtcTargetDeadbandUndefinedValue";
            case "phase tap changer" -> "core.iidm.network.ptcTargetDeadbandUndefinedValue";
            case "shunt compensator" -> "core.iidm.network.scTargetDeadbandUndefinedValue";
            default -> throw new IllegalArgumentException("Unsupported validable type: " + validableType);
        };
        reportNode.newReportNode()
                .withMessageTemplate(key)
                .withTypedValue("id", id, TypedValue.ID)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }
}
