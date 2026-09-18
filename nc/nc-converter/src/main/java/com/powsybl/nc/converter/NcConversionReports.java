/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.converter;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;

/**
 * Functional logs produced while converting an NC model into security analysis inputs.
 *
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public final class NcConversionReports {

    private static final String CONTINGENCY_ID = "contingencyId";
    private static final String EQUIPMENT_ID = "equipmentId";
    private static final String ASSESSED_ELEMENT_ID = "assessedElementId";
    private static final String ACTION_ID = "actionId";
    private static final String RANGE_ID = "rangeId";
    private static final String REMEDIAL_ACTION_ID = "remedialActionId";
    private static final String PROPERTY_REFERENCE = "propertyReference";
    private static final String EXPECTED_PROPERTY_REFERENCE = "expectedPropertyReference";

    private NcConversionReports() {
    }

    // INFO
    public static ReportNode convertingNcModelReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.convertingNcModel")
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode convertingContingenciesReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.convertingContingencies")
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode convertingAssessedElementsReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.convertingAssessedElements")
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode convertingRemedialActionsReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.convertingRemedialActions")
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode convertingOperatorStrategiesReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.convertingOperatorStrategies")
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void contingencyEquipmentNotOutOfService(ReportNode reportNode, String equipmentId, String contingencyId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.contingencyEquipmentNotOutOfService")
                .withUntypedValue(EQUIPMENT_ID, equipmentId)
                .withUntypedValue(CONTINGENCY_ID, contingencyId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void contingencyEquipmentNotFound(ReportNode reportNode, String equipmentId, String contingencyId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.contingencyEquipmentNotFound")
                .withUntypedValue(EQUIPMENT_ID, equipmentId)
                .withUntypedValue(CONTINGENCY_ID, contingencyId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void assessedElementCombinableAssociationsIgnored(ReportNode reportNode, String assessedElementId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.assessedElementCombinableAssociationsIgnored")
                .withUntypedValue(ASSESSED_ELEMENT_ID, assessedElementId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void assessedElementAssociationDisabled(ReportNode reportNode, String contingencyId, String assessedElementId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.assessedElementAssociationDisabled")
                .withUntypedValue(CONTINGENCY_ID, contingencyId)
                .withUntypedValue(ASSESSED_ELEMENT_ID, assessedElementId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void assessedElementAssociationContingencyNotImported(ReportNode reportNode, String contingencyId,
                                                                        String assessedElementId, String equipmentId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.assessedElementAssociationContingencyNotImported")
                .withUntypedValue(CONTINGENCY_ID, contingencyId)
                .withUntypedValue(ASSESSED_ELEMENT_ID, assessedElementId)
                .withUntypedValue(EQUIPMENT_ID, equipmentId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    // WARN
    public static void contingencyNotToBeStudied(ReportNode reportNode, String contingencyId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.contingencyNotToBeStudied")
                .withUntypedValue(CONTINGENCY_ID, contingencyId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void contingencyWithoutValidEquipment(ReportNode reportNode, String contingencyId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.contingencyWithoutValidEquipment")
                .withUntypedValue(CONTINGENCY_ID, contingencyId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void contingencyEquipmentTypeNotSupported(ReportNode reportNode, String equipmentId,
                                                            String contingencyId, String equipmentType) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.contingencyEquipmentTypeNotSupported")
                .withUntypedValue(EQUIPMENT_ID, equipmentId)
                .withUntypedValue(CONTINGENCY_ID, contingencyId)
                .withUntypedValue("equipmentType", equipmentType)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void assessedElementDisabled(ReportNode reportNode, String assessedElementId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.assessedElementDisabled")
                .withUntypedValue(ASSESSED_ELEMENT_ID, assessedElementId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void assessedElementWithoutConductingEquipment(ReportNode reportNode, String assessedElementId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.assessedElementWithoutConductingEquipment")
                .withUntypedValue(ASSESSED_ELEMENT_ID, assessedElementId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void assessedElementEquipmentNotFound(ReportNode reportNode, String equipmentId, String assessedElementId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.assessedElementEquipmentNotFound")
                .withUntypedValue(EQUIPMENT_ID, equipmentId)
                .withUntypedValue(ASSESSED_ELEMENT_ID, assessedElementId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void assessedElementEquipmentNotSupported(ReportNode reportNode, String equipmentId, String assessedElementId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.assessedElementEquipmentNotSupported")
                .withUntypedValue(EQUIPMENT_ID, equipmentId)
                .withUntypedValue(ASSESSED_ELEMENT_ID, assessedElementId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void assessedElementWithoutImportedContingency(ReportNode reportNode, String assessedElementId, String equipmentId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.assessedElementWithoutImportedContingency")
                .withUntypedValue(ASSESSED_ELEMENT_ID, assessedElementId)
                .withUntypedValue(EQUIPMENT_ID, equipmentId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void assessedElementExcludedAssociationNotSupported(ReportNode reportNode, String contingencyId, String assessedElementId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.assessedElementExcludedAssociationNotSupported")
                .withUntypedValue(CONTINGENCY_ID, contingencyId)
                .withUntypedValue(ASSESSED_ELEMENT_ID, assessedElementId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void assessedElementAssociationKindNotSupported(ReportNode reportNode, String contingencyId,
                                                                  String assessedElementId, String combinationConstraintKind) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.assessedElementAssociationKindNotSupported")
                .withUntypedValue(CONTINGENCY_ID, contingencyId)
                .withUntypedValue(ASSESSED_ELEMENT_ID, assessedElementId)
                .withUntypedValue("combinationConstraintKind", combinationConstraintKind)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void actionWithoutStaticPropertyRange(ReportNode reportNode, String actionId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.actionWithoutStaticPropertyRange")
                .withUntypedValue(ACTION_ID, actionId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void actionWithMultipleStaticPropertyRanges(ReportNode reportNode, String actionId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.actionWithMultipleStaticPropertyRanges")
                .withUntypedValue(ACTION_ID, actionId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void actionDisabled(ReportNode reportNode, String actionId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.actionDisabled")
                .withUntypedValue(ACTION_ID, actionId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void actionEquipmentNotFound(ReportNode reportNode, String actionId, String equipmentId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.actionEquipmentNotFound")
                .withUntypedValue(ACTION_ID, actionId)
                .withUntypedValue(EQUIPMENT_ID, equipmentId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void actionPropertyReferenceNotSupported(ReportNode reportNode, String actionId,
                                                           String propertyReference, String expectedPropertyReference) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.actionPropertyReferenceNotSupported")
                .withUntypedValue(ACTION_ID, actionId)
                .withUntypedValue(PROPERTY_REFERENCE, propertyReference)
                .withUntypedValue(EXPECTED_PROPERTY_REFERENCE, expectedPropertyReference)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void staticPropertyRangePropertyReferenceNotSupported(ReportNode reportNode, String rangeId, String actionId,
                                                                        String propertyReference, String expectedPropertyReference) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.staticPropertyRangePropertyReferenceNotSupported")
                .withUntypedValue(RANGE_ID, rangeId)
                .withUntypedValue(ACTION_ID, actionId)
                .withUntypedValue(PROPERTY_REFERENCE, propertyReference)
                .withUntypedValue(EXPECTED_PROPERTY_REFERENCE, expectedPropertyReference)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void staticPropertyRangeNotAbsolute(ReportNode reportNode, String rangeId, String actionId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.staticPropertyRangeNotAbsolute")
                .withUntypedValue(RANGE_ID, rangeId)
                .withUntypedValue(ACTION_ID, actionId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void staticPropertyRangeValueNotSupported(ReportNode reportNode, String rangeId, String actionId, double value) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.staticPropertyRangeValueNotSupported")
                .withUntypedValue(RANGE_ID, rangeId)
                .withUntypedValue(ACTION_ID, actionId)
                .withUntypedValue("value", value)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void staticPropertyRangeDirectionNotSupported(ReportNode reportNode, String rangeId, String actionId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.staticPropertyRangeDirectionNotSupported")
                .withUntypedValue(RANGE_ID, rangeId)
                .withUntypedValue(ACTION_ID, actionId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void remedialActionKindNotSupported(ReportNode reportNode, String remedialActionId, String kind) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.remedialActionKindNotSupported")
                .withUntypedValue(REMEDIAL_ACTION_ID, remedialActionId)
                .withUntypedValue("kind", kind)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void remedialActionWithUnconvertedAction(ReportNode reportNode, String remedialActionId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.remedialActionWithUnconvertedAction")
                .withUntypedValue(REMEDIAL_ACTION_ID, remedialActionId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    // ERROR
    public static void tapChangerNotResolved(ReportNode reportNode, String actionId, String tapChangerId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.nc.conversion.tapChangerNotResolved")
                .withUntypedValue(ACTION_ID, actionId)
                .withUntypedValue("tapChangerId", tapChangerId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }
}
