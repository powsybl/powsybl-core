/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Substation;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class CgmesReports {

    private static final String CONVERTER_IDS = "converterIds";

    private CgmesReports() {
    }

    // INFO
    public static ReportNode applyingPreprocessorsReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.applyingPreprocessors")
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void applyingProcessorReport(ReportNode reportNode, String processorName) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.applyingProcessor")
                .withUntypedValue("processorName", processorName)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode buildingMappingsReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.buildingMappings")
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode convertingElementTypeReport(ReportNode reportNode, String elementType) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.convertingElementType")
                .withUntypedValue("elementType", elementType)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode fixingDanglingLinesIssuesReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.fixingDanglingLinesIssues")
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode settingVoltagesAndAnglesReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.settingVoltagesAndAngles")
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode applyingPostprocessorsReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.applyingPostprocessors")
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void importedCgmesNetworkReport(ReportNode reportNode, String networkId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.importedCgmesNetwork")
                .withUntypedValue("networkId", networkId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    // WARN
    public static void badVoltageTargetValueRegulatingControlReport(ReportNode reportNode, String eqId, double targetValue) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.badVoltageTargetValueRegulatingControl")
                .withUntypedValue("equipmentId", eqId)
                .withTypedValue("targetValue", targetValue, TypedValue.VOLTAGE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void badTargetDeadbandRegulatingControlReport(ReportNode reportNode, String eqId, double targetDeadband) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.badTargetDeadbandRegulatingControl")
                .withUntypedValue("equipmentId", eqId)
                .withTypedValue("targetDeadband", targetDeadband, TypedValue.VOLTAGE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void invalidAngleVoltageBusReport(ReportNode reportNode, Bus bus, String nodeId, double v, double angle) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.invalidAngleVoltageBus")
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
                .withMessageTemplate("core.cgmes.conversion.invalidAngleVoltageNode")
                .withUntypedValue("nodeId", nodeId)
                .withTypedValue("voltage", v, TypedValue.VOLTAGE)
                .withTypedValue("angle", angle, TypedValue.ANGLE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void removingUnattachedHvdcConverterStationReport(ReportNode reportNode, String converterId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.removingUnattachedHvdcConverterStation")
                .withUntypedValue("converterId", converterId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void voltageLevelMappingReport(ReportNode reportNode, int voltageLevelMappingSize, String mapAsString) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.voltageLevelMapping")
                .withUntypedValue("voltageLevelMappingSize", voltageLevelMappingSize)
                .withUntypedValue("mapAsString", mapAsString)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void substationMappingReport(ReportNode reportNode, int substationMappingSize, String mapAsString) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.substationMapping")
                .withUntypedValue("substationMappingSize", substationMappingSize)
                .withUntypedValue("mapAsString", mapAsString)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void nominalVoltageIsZeroReport(ReportNode reportNode, String voltageLevelId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.nominalVoltageIsZero")
                .withUntypedValue("voltageLevelId", voltageLevelId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    // ERROR
    public static void inconsistentProfilesTPRequiredReport(ReportNode reportNode, String networkId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.inconsistentProfilesTPRequired")
                .withUntypedValue("networkId", networkId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void danglingLineDisconnectedAtBoundaryHasBeenDisconnectedReport(ReportNode reportNode, String danglingLineId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.danglingLineDisconnectedAtBoundaryHasBeenDisconnected")
                .withUntypedValue("danglingLineId", danglingLineId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void multipleUnpairedDanglingLinesAtSameBoundaryReport(ReportNode reportNode, String danglingLineId, double p0, double q0, double p0Adjusted, double q0Adjusted) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.multipleUnpairedDanglingLinesAtSameBoundary")
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
                .withMessageTemplate("core.cgmes.conversion.missingMandatoryAttribute")
                .withUntypedValue("attributeName", attributeName)
                .withUntypedValue("objectClass", objectClass)
                .withUntypedValue("objectId", objectId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static ReportNode importingCgmesFileReport(ReportNode reportNode, String basename) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.CGMESConversion")
                .withUntypedValue("basename", basename)
                .add();
    }

    public static ReportNode readingCgmesTriplestoreReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.CGMESTriplestore")
                .add();
    }

    public static void exportedModelIdentifierReport(ReportNode reportNode, String description, String identifier, String networkId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.ExportedCgmesId")
                .withTypedValue("cgmesId", description, TypedValue.URN_UUID)
                .withTypedValue("cgmesSubset", identifier, TypedValue.CGMES_SUBSET)
                .withTypedValue("networkId", networkId, TypedValue.ID)
                .add();
    }

    public static void notVisitedDcEquipmentReport(ReportNode reportNode, String dcEquipmentId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.notVisitedDcEquipment")
                .withUntypedValue("dcEquipmentId", dcEquipmentId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void dcLineSegmentNotInTwoDCIslandEndReport(ReportNode reportNode, String dcLineSegmentId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.dcLineSegmentNotInTwoDCIslandEnd")
                .withUntypedValue("dcLineSegmentId", dcLineSegmentId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void inconsistentNumberOfConvertersReport(ReportNode reportNode, String converterIds) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.inconsistentNumberOfConverters")
                .withUntypedValue(CONVERTER_IDS, converterIds)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void unsupportedDcConfigurationReport(ReportNode reportNode, String converterIds, String dcConfiguration) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.unsupportedDcConfiguration")
                .withUntypedValue(CONVERTER_IDS, converterIds)
                .withUntypedValue("dcConfiguration", dcConfiguration)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void unexpectedPointToPointDcConfigurationReport(ReportNode reportNode, String converterIds, int numberOfLines, int numberOfConverterPairs) {
        reportNode.newReportNode()
                .withMessageTemplate("core.cgmes.conversion.unexpectedPointToPointDcConfiguration")
                .withUntypedValue(CONVERTER_IDS, converterIds)
                .withUntypedValue("numberOfLines", numberOfLines)
                .withUntypedValue("numberOfConverterPairs", numberOfConverterPairs)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }
}
