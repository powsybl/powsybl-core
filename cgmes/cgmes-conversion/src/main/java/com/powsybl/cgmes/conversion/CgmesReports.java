/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.commons.report.ReportBundleBaseName;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Substation;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class CgmesReports {

    private CgmesReports() {
    }

    // INFO
    public static ReportNode applyingPreprocessorsReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-applyingPreprocessors", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void applyingProcessorReport(ReportNode reportNode, String processorName) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-applyingProcessor", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("processorName", processorName)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode buildingMappingsReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-buildingMappings", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode convertingElementTypeReport(ReportNode reportNode, String elementType) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-convertingElementType", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("elementType", elementType)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode fixingDanglingLinesIssuesReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-fixingDanglingLinesIssues", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode settingVoltagesAndAnglesReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-settingVoltagesAndAngles", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode applyingPostprocessorsReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-applyingPostprocessors", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void importedCgmesNetworkReport(ReportNode reportNode, String networkId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-importedCgmesNetwork", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("networkId", networkId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    // WARN
    public static void badVoltageTargetValueRegulatingControlReport(ReportNode reportNode, String eqId, double targetValue) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-badVoltageTargetValueRegulatingControl", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("equipmentId", eqId)
                .withTypedValue("targetValue", targetValue, TypedValue.VOLTAGE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void badTargetDeadbandRegulatingControlReport(ReportNode reportNode, String eqId, double targetDeadband) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-badTargetDeadbandRegulatingControl", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("equipmentId", eqId)
                .withTypedValue("targetDeadband", targetDeadband, TypedValue.VOLTAGE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidAngleVoltageBusReport(ReportNode reportNode, Bus bus, String nodeId, double v, double angle) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-invalidAngleVoltageBus", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("substation", bus.getVoltageLevel().getSubstation().map(Substation::getNameOrId).orElse("unknown"))
                .withUntypedValue("voltageLevel", bus.getVoltageLevel().getNameOrId())
                .withUntypedValue("bus", bus.getId())
                .withUntypedValue("nodeId", nodeId)
                .withTypedValue("voltage", v, TypedValue.VOLTAGE)
                .withTypedValue("angle", angle, TypedValue.ANGLE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidAngleVoltageNodeReport(ReportNode reportNode, String nodeId, double v, double angle) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-invalidAngleVoltageNode", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("nodeId", nodeId)
                .withTypedValue("voltage", v, TypedValue.VOLTAGE)
                .withTypedValue("angle", angle, TypedValue.ANGLE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void removingUnattachedHvdcConverterStationReport(ReportNode reportNode, String converterId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-removingUnattachedHvdcConverterStation", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("converterId", converterId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void voltageLevelMappingReport(ReportNode reportNode, int voltageLevelMappingSize, String mapAsString) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-voltageLevelMapping", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("voltageLevelMappingSize", voltageLevelMappingSize)
                .withUntypedValue("mapAsString", mapAsString)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void substationMappingReport(ReportNode reportNode, int substationMappingSize, String mapAsString) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-substationMapping", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("substationMappingSize", substationMappingSize)
                .withUntypedValue("mapAsString", mapAsString)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void nominalVoltageIsZeroReport(ReportNode reportNode, String voltageLevelId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-nominalVoltageIsZero", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("voltageLevelId", voltageLevelId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    // ERROR
    public static void inconsistentProfilesTPRequiredReport(ReportNode reportNode, String networkId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-inconsistentProfilesTPRequired", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("networkId", networkId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void danglingLineDisconnectedAtBoundaryHasBeenDisconnectedReport(ReportNode reportNode, String danglingLineId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-danglingLineDisconnectedAtBoundaryHasBeenDisconnected", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("danglingLineId", danglingLineId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void multipleUnpairedDanglingLinesAtSameBoundaryReport(ReportNode reportNode, String danglingLineId, double p0, double q0, double p0Adjusted, double q0Adjusted) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-multipleUnpairedDanglingLinesAtSameBoundary", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("danglingLineId", danglingLineId)
                .withUntypedValue("p0", p0)
                .withUntypedValue("q0", q0)
                .withUntypedValue("p0Adjusted", p0Adjusted)
                .withUntypedValue("q0Adjusted", q0Adjusted)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void missingMandatoryAttributeReport(ReportNode reportNode, String attributeName, String objectClass, String objectId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-missingMandatoryAttribute", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("attributeName", attributeName)
                .withUntypedValue("objectClass", objectClass)
                .withUntypedValue("objectId", objectId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static ReportNode importingCgmesFileReport(ReportNode reportNode, String basename) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-CGMESConversion", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("basename", basename)
                .add();
    }

    public static ReportNode readingCgmesTriplestoreReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-CGMESTriplestore", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .add();
    }

    public static void exportedModelIdentifierReport(ReportNode reportNode, String description, String identifier, String networkId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-cgmes-conversion-ExportedCgmesId", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withTypedValue("cgmesId", description, TypedValue.URN_UUID)
                .withTypedValue("cgmesSubset", identifier, TypedValue.CGMES_SUBSET)
                .withTypedValue("networkId", networkId, TypedValue.ID)
                .add();
    }
}
