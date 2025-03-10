/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.util;

import com.powsybl.commons.report.ReportBundleBaseName;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.iidm.modification.scalable.ScalingParameters.ScalingType;
import com.powsybl.iidm.network.*;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public final class ModificationReports {

    private static final String SUBSTATION_ID = "substationId";
    private static final String VOLTAGE_LEVEL_ID = "voltageLevelId";
    private static final String LINE_ID = "lineId";
    private static final String BBS_ID = "bbsId";
    private static final String CONNECTABLE_ID = "connectableId";
    private static final String IDENTIFIABLE_ID = "identifiableId";
    private static final String IDENTIFIABLE_TYPE = "identifiableType";
    private static final String HVDC_LINE_ID = "hvdcLineId";
    private static final String TWO_WINDINGS_TRANSFORMER_ID = "twoWindingsTransformerId";
    private static final String THREE_WINDINGS_TRANSFORMER_ID = "threeWindingsTransformerId";
    private static final String EXTENSIONS = "extensions";
    public static final String POSITION_ORDER = "positionOrder";
    public static final String TIE_LINE_ID = "tieLineId";

    // INFO
    public static void createdConnectable(ReportNode reportNode, Connectable<?> connectable) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-connectableCreated", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(CONNECTABLE_ID, connectable.getId())
                .withUntypedValue("connectableType", connectable.getType().name())
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void createdNodeBreakerFeederBay(ReportNode reportNode, String voltageLevelId, String bbsId, Connectable<?> connectable, int parallelBbsNumber) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-newConnectableAdded", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(CONNECTABLE_ID, connectable.getId())
                .withUntypedValue("connectableType", connectable.getType().toString())
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withUntypedValue(BBS_ID, bbsId)
                .withUntypedValue("parallelBbsNumber", parallelBbsNumber)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedLineReport(ReportNode reportNode, String lineId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-lineRemoved", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(LINE_ID, lineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedTieLineReport(ReportNode reportNode, String tieLineId, String pairingKey) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-removedTieLine", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withTypedValue(TIE_LINE_ID, tieLineId, TypedValue.ID)
                .withUntypedValue("pairingKey", pairingKey == null ? "" : pairingKey)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedTieLineAndAssociatedDanglingLines(ReportNode reportNode, String tieLineId, String danglingLineId1, String danglingLineId2, String pairingKey) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-removedTieLineAndAssociatedDanglingLines", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withTypedValue(TIE_LINE_ID, tieLineId, TypedValue.ID)
                .withUntypedValue("danglingLineId1", danglingLineId1)
                .withUntypedValue("danglingLineId2", danglingLineId2)
                .withUntypedValue("pairingKey", pairingKey == null ? "" : pairingKey)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void createdLineReport(ReportNode reportNode, String lineId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-lineCreated", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(LINE_ID, lineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void createdVoltageLevelReport(ReportNode reportNode, String voltageLevelId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-voltageLevelCreated", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void createdTwoWindingsTransformerReport(ReportNode reportNode, String twoWindingsTransformerId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-twoWindingsTransformerCreated", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(TWO_WINDINGS_TRANSFORMER_ID, twoWindingsTransformerId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void createdThreeWindingsTransformerReport(ReportNode reportNode, String threeWindingsTransformerId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-threeWindingsTransformerCreated", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(THREE_WINDINGS_TRANSFORMER_ID, threeWindingsTransformerId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedTwoWindingsTransformerReport(ReportNode reportNode, String twoWindingsTransformerId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-twoWindingsTransformerRemoved", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(TWO_WINDINGS_TRANSFORMER_ID, twoWindingsTransformerId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedThreeWindingsTransformerReport(ReportNode reportNode, String threeWindingsTransformerId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-threeWindingsTransformerRemoved", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(THREE_WINDINGS_TRANSFORMER_ID, threeWindingsTransformerId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void lostTwoWindingsTransformerAliases(ReportNode reportNode, String aliases, String twoWindingsTransformerId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-lostTwoWindingsTransformerAliases", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("aliases", aliases)
                .withUntypedValue(TWO_WINDINGS_TRANSFORMER_ID, twoWindingsTransformerId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostThreeWindingsTransformerAliases(ReportNode reportNode, String aliases, String threeWindingsTransformerId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-lostThreeWindingsTransformerAliases", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("aliases", aliases)
                .withUntypedValue(THREE_WINDINGS_TRANSFORMER_ID, threeWindingsTransformerId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostTwoWindingsTransformerProperties(ReportNode reportNode, String properties, String twoWindingsTransformerId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-lostTwoWindingsTransformerProperties", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("properties", properties)
                .withUntypedValue(TWO_WINDINGS_TRANSFORMER_ID, twoWindingsTransformerId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostThreeWindingsTransformerProperties(ReportNode reportNode, String properties, String threeWindingsTransformerId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-lostThreeWindingsTransformerProperties", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("properties", properties)
                .withUntypedValue(THREE_WINDINGS_TRANSFORMER_ID, threeWindingsTransformerId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostTwoWindingsTransformerExtensions(ReportNode reportNode, String extensions, String twoWindingsTransformerId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-lostTwoWindingsTransformerExtensions", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(EXTENSIONS, extensions)
                .withUntypedValue(TWO_WINDINGS_TRANSFORMER_ID, twoWindingsTransformerId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostThreeWindingsTransformerExtensions(ReportNode reportNode, String extensions, String threeWindingsTransformerId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-lostThreeWindingsTransformerExtensions", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(EXTENSIONS, extensions)
                .withUntypedValue(THREE_WINDINGS_TRANSFORMER_ID, threeWindingsTransformerId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostTwoWindingsTransformerOperationalLimitsGroups(ReportNode reportNode, String limits, String twoWindingsTransformerId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-lostTwoWindingsTransformerOperationalLimitsGroups", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("limits", limits)
                .withUntypedValue(TWO_WINDINGS_TRANSFORMER_ID, twoWindingsTransformerId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void voltageLevelRemovedReport(ReportNode reportNode, String vlId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-voltageLevelRemoved", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("vlId", vlId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void substationRemovedReport(ReportNode reportNode, String substationId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-substationRemoved", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(SUBSTATION_ID, substationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void newCouplingDeviceAddedReport(ReportNode reportNode, String voltageLevelId, String busOrBbsId1, String busOrBbsId2) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-newCouplingDeviceAdded", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withUntypedValue("busOrBbsId1", busOrBbsId1)
                .withUntypedValue("busOrBbsId2", busOrBbsId2)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void openDisconnectorsAddedReport(ReportNode reportNode, String voltageLevelId, int nbOpenDisconnectors) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-openDisconnectorsAdded", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withUntypedValue("nbOpenDisconnectors", nbOpenDisconnectors)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void createdNewSymmetricalTopology(ReportNode reportNode, String voltageLevelId, int busbarCount, int sectionCount) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-SymmetricalTopologyCreated", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withUntypedValue("busbarCount", busbarCount)
                .withUntypedValue("sectionCount", sectionCount)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedSwitchReport(ReportNode reportNode, String switchId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-SwitchRemoved", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("switchId", switchId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedInternalConnectionReport(ReportNode reportNode, int node1, int node2) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-InternalConnectionRemoved", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("node1", node1)
                .withUntypedValue("node2", node2)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedConnectableReport(ReportNode reportNode, String connectableId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-ConnectableRemoved", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(CONNECTABLE_ID, connectableId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removeFeederBayAborted(ReportNode reportNode, String connectableId, int node, String otherConnectableId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-RemoveFeederBayAborted", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(CONNECTABLE_ID, connectableId)
                .withUntypedValue("node", node)
                .withUntypedValue("otherConnectableId", otherConnectableId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedSubstationReport(ReportNode reportNode, String substationId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-removeSubstation", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(SUBSTATION_ID, substationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedVoltageLevelReport(ReportNode reportNode, String voltageLevelId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-removeVoltageLevel", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedHvdcLineReport(ReportNode reportNode, String hvdcLineId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-removeHvdcLine", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(HVDC_LINE_ID, hvdcLineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedVscConverterStationReport(ReportNode reportNode, String vscConverterStationId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-removeVscConverterStation", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("vscConverterStationId", vscConverterStationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedLccConverterStationReport(ReportNode reportNode, String lccConverterStationId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-removeLccConverterStation", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("lccConverterStationId", lccConverterStationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedShuntCompensatorReport(ReportNode reportNode, String shuntCompensatorId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-removeShuntCompensator", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("shuntCompensatorId", shuntCompensatorId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    // WARN
    public static void ignoredVscShunts(ReportNode reportNode, String shuntsIds, String converterStationId1, String converterStationId2) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-ignoredVscShunts", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("shuntsIds", shuntsIds)
                .withUntypedValue("converterStationId1", converterStationId1)
                .withUntypedValue("converterStationId2", converterStationId2)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void ignoredShuntInAnotherVoltageLevel(ReportNode reportNode, String shuntId, String voltageLevelId1, String voltageLevelId2) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-ignoredShuntInAnotherVoltageLevel", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("shuntId", shuntId)
                .withUntypedValue(VOLTAGE_LEVEL_ID + 1, voltageLevelId1)
                .withUntypedValue(VOLTAGE_LEVEL_ID + 2, voltageLevelId2)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void ignoredPositionOrder(ReportNode reportNode, int positionOrder, VoltageLevel voltageLevel) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-ignoredPositionOrder", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevel.getId())
                .withUntypedValue(POSITION_ORDER, positionOrder)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostDanglingLineExtensions(ReportNode reportNode, String extensions, String danglingLineId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-lostDanglingLineExtensions", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(EXTENSIONS, extensions)
                .withUntypedValue("danglingLineId", danglingLineId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostTieLineExtensions(ReportNode reportNode, String extensions, String tieLineId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-lostTieLineExtensions", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(EXTENSIONS, extensions)
                .withTypedValue(TIE_LINE_ID, tieLineId, TypedValue.ID)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void noBusbarSectionPositionExtensionReport(ReportNode reportNode, BusbarSection bbs) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-noBusbarSectionPositionExtension", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(BBS_ID, bbs.getId())
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void positionOrderAlreadyTakenReport(ReportNode reportNode, int positionOrder) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-positionOrderAlreadyTaken", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(POSITION_ORDER, positionOrder)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void positionNoSlotLeftByAdjacentBbsReport(ReportNode reportNode, String bbsId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-positionAdjacentBbsIncoherent", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(BBS_ID, bbsId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void positionOrderTooLowReport(ReportNode reportNode, int minValue, int positionOrder) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-positionOrderTooLow", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(POSITION_ORDER, positionOrder)
                .withUntypedValue("minValue", minValue)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void positionOrderTooHighReport(ReportNode reportNode, int maxValue, int positionOrder) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-positionOrderTooHigh", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(POSITION_ORDER, positionOrder)
                .withUntypedValue("maxValue", maxValue)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void noConnectablePositionExtension(ReportNode reportNode, VoltageLevel voltageLevel, String connectableId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-noConnectablePositionExtensions", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("voltageLevel", voltageLevel.getId())
                .withUntypedValue(CONNECTABLE_ID, connectableId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void voltageLevelRemovingEquipmentsLeftReport(ReportNode reportNode, String vlId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-voltageLevelRemovingEquipmentsLeft", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("vlId", vlId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    // ERROR
    public static void notFoundBusOrBusbarSectionReport(ReportNode reportNode, String identifiableId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-notFoundBusOrBusbarSection", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(IDENTIFIABLE_ID, identifiableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundShuntReport(ReportNode reportNode, String shuntId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-notFoundShunt", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("shuntId", shuntId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void networkMismatchReport(ReportNode reportNode, String injectionId, IdentifiableType identifiableType) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-networkMismatch", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("injectionId", injectionId)
                .withUntypedValue(IDENTIFIABLE_TYPE, identifiableType.toString())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void connectableNotSupported(ReportNode reportNode, Connectable<?> connectable) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-connectableNotSupported", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("connectableClassName", connectable.getClass().getName())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void connectableNotInVoltageLevel(ReportNode reportNode, Connectable<?> connectable, VoltageLevel voltageLevel) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-connectableNotInVoltageLevel", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(CONNECTABLE_ID, connectable.getId())
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevel.getId())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundLineReport(ReportNode reportNode, String lineId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-lineNotFound", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(LINE_ID, lineId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundConnectableReport(ReportNode reportNode, String connectableId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-connectableNotFound", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(CONNECTABLE_ID, connectableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void removeFeederBayBusbarSectionReport(ReportNode reportNode, String busbarSectionConnectableId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-removeBayBusbarSectionConnectable", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(CONNECTABLE_ID, busbarSectionConnectableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void noVoltageLevelInCommonReport(ReportNode reportNode, String line1Id, String line2Id) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-noVoltageLevelInCommon", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("line1Id", line1Id)
                .withUntypedValue("line2Id", line2Id)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundVoltageLevelReport(ReportNode reportNode, String voltageLevelId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-voltageLevelNotFound", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundSubstationReport(ReportNode reportNode, String substationId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-substationNotFound", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(SUBSTATION_ID, substationId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundHvdcLineReport(ReportNode reportNode, String hvdcLineId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-HvdcNotFound", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(HVDC_LINE_ID, hvdcLineId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundBusOrBusbarSectionVoltageLevelReport(ReportNode reportNode, String busOrBusbarSectionId1, String busOrBusbarSectionId2) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-busOrBusbarSectionVoltageLevelNotFound", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("busOrBusbarSectionId1", busOrBusbarSectionId1)
                .withUntypedValue("busOrBusbarSectionId2", busOrBusbarSectionId2)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void noTeePointAndOrTappedVoltageLevelReport(ReportNode reportNode, String line1Id, String line2Id, String line3Id) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-noTeePointAndOrTappedVoltageLevel", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("line1Id", line1Id)
                .withUntypedValue("line2Id", line2Id)
                .withUntypedValue("line3Id", line3Id)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundBusInVoltageLevelReport(ReportNode reportNode, String busId, String voltageLevelId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-busNotFound", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("busId", busId)
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundBusbarSectionInVoltageLevelReport(ReportNode reportNode, String busbarSectionId, String voltageLevelId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-busbarSectionNotFound", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("busbarSectionId", busbarSectionId)
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void noCouplingDeviceOnSameBusOrBusbarSection(ReportNode reportNode, String busbarSectionId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-noCouplingDeviceOnSameBusOrBusbarSection", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("busOrBbsId", busbarSectionId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unexpectedDifferentVoltageLevels(ReportNode reportNode, String busbarSectionId1, String busbarSectionId2) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-unexpectedDifferentVoltageLevels", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("busOrBbsId1", busbarSectionId1)
                .withUntypedValue("busOrBbsId2", busbarSectionId2)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unsupportedIdentifiableType(ReportNode reportNode, IdentifiableType type, String identifiableId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-unsupportedIdentifiableType", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(IDENTIFIABLE_TYPE, type.name())
                .withUntypedValue(IDENTIFIABLE_ID, identifiableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unexpectedNullPositionOrder(ReportNode reportNode, String voltageLevelId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-unexpectedNullPositionOrder", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unexpectedNegativePositionOrder(ReportNode reportNode, int positionOrder, String voltageLevelId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-unexpectedNegativePositionOrder", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withUntypedValue(POSITION_ORDER, positionOrder)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unexpectedIdentifiableType(ReportNode reportNode, Identifiable<?> identifiable) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-unexpectedIdentifiableType", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(IDENTIFIABLE_ID, identifiable.getId())
                .withUntypedValue(IDENTIFIABLE_TYPE, identifiable.getType().name())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void countLowerThanMin(ReportNode reportNode, String type, int min) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-countLowerThanMin", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("type", type)
                .withUntypedValue("min", min)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unexpectedSwitchKindsCount(ReportNode reportNode, int switchKindsCount, int expectedSwitchKindsCount) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-unexpectedSwitchKindsCount", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("switchKindsCount", switchKindsCount)
                .withUntypedValue("expectedSwitchKindsCount", expectedSwitchKindsCount)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void undefinedSwitchKind(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-undefinedSwitchKind", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void wrongSwitchKind(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-wrongSwitchKind", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void undefinedFictitiousSubstationId(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-undefinedFictitiousSubstationId", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void undefinedPercent(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-undefinedPercent", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    private ModificationReports() {
    }

    public static void scalingReport(ReportNode reportNode, String type, String mode, ScalingType scalingType, double asked, double done) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-scalingApplied", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue(IDENTIFIABLE_TYPE, type)
                .withUntypedValue("mode", mode)
                .withUntypedValue("type", scalingType.name())
                .withUntypedValue("asked", asked)
                .withUntypedValue("done", done)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void connectableConnectionReport(ReportNode reportNode, Identifiable<?> identifiable, boolean connectionSuccessful, ThreeSides side) {
        if (connectionSuccessful) {
            if (null == side) {
                reportNode.newReportNode()
                        .withLocaleMessageTemplate("core-iidm-modification-connectableConnected", ReportBundleBaseName.BUNDLE_BASE_NAME)
                        .withUntypedValue("identifiable", identifiable.getId())
                        .withSeverity(TypedValue.INFO_SEVERITY)
                        .add();
            } else {
                reportNode.newReportNode()
                        .withLocaleMessageTemplate("core-iidm-modification-connectableConnectedSide", ReportBundleBaseName.BUNDLE_BASE_NAME)
                        .withUntypedValue("identifiable", identifiable.getId())
                        .withUntypedValue("side", side.getNum())
                        .withSeverity(TypedValue.INFO_SEVERITY)
                        .add();
            }
        } else if (null == side) {
            reportNode.newReportNode()
                    .withLocaleMessageTemplate("core-iidm-modification-connectableNotConnected", ReportBundleBaseName.BUNDLE_BASE_NAME)
                    .withUntypedValue("identifiable", identifiable.getId())
                    .withSeverity(TypedValue.INFO_SEVERITY)
                    .add();
        } else {
            reportNode.newReportNode()
                    .withLocaleMessageTemplate("core-iidm-modification-connectableNotConnectedSide", ReportBundleBaseName.BUNDLE_BASE_NAME)
                    .withUntypedValue("identifiable", identifiable.getId())
                    .withUntypedValue("side", side.getNum())
                    .withSeverity(TypedValue.INFO_SEVERITY)
                    .add();
        }
    }

    public static void identifiableDisconnectionReport(ReportNode reportNode, Identifiable<?> identifiable, boolean disconnectionSuccessful, boolean isPlanned, ThreeSides side) {
        if (isPlanned) {
            if (disconnectionSuccessful) {
                if (null == side) {
                    reportNode.newReportNode()
                            .withLocaleMessageTemplate("core-iidm-modification-plannedIdentifiableDisconnected", ReportBundleBaseName.BUNDLE_BASE_NAME)
                            .withUntypedValue("identifiable", identifiable.getId())
                            .withSeverity(TypedValue.INFO_SEVERITY)
                            .add();
                } else {
                    reportNode.newReportNode()
                            .withLocaleMessageTemplate("core-iidm-modification-plannedIdentifiableDisconnectedSide", ReportBundleBaseName.BUNDLE_BASE_NAME)
                            .withUntypedValue("identifiable", identifiable.getId())
                            .withUntypedValue("side", side.getNum())
                            .withSeverity(TypedValue.INFO_SEVERITY)
                            .add();
                }
            } else {
                if (null == side) {
                    reportNode.newReportNode()
                            .withLocaleMessageTemplate("core-iidm-modification-plannedIdentifiableNotDisconnected", ReportBundleBaseName.BUNDLE_BASE_NAME)
                            .withUntypedValue("identifiable", identifiable.getId())
                            .withSeverity(TypedValue.INFO_SEVERITY)
                            .add();
                } else {
                    reportNode.newReportNode()
                            .withLocaleMessageTemplate("core-iidm-modification-plannedIdentifiableNotDisconnectedSide", ReportBundleBaseName.BUNDLE_BASE_NAME)
                            .withUntypedValue("identifiable", identifiable.getId())
                            .withUntypedValue("side", side.getNum())
                            .withSeverity(TypedValue.INFO_SEVERITY)
                            .add();
                }
            }
        } else {
            if (disconnectionSuccessful) {
                if (null == side) {
                    reportNode.newReportNode()
                            .withLocaleMessageTemplate("core-iidm-modification-unplannedIdentifiableDisconnected", ReportBundleBaseName.BUNDLE_BASE_NAME)
                            .withUntypedValue("identifiable", identifiable.getId())
                            .withSeverity(TypedValue.INFO_SEVERITY)
                            .add();
                } else {
                    reportNode.newReportNode()
                            .withLocaleMessageTemplate("core-iidm-modification-unplannedIdentifiableDisconnectedSide", ReportBundleBaseName.BUNDLE_BASE_NAME)
                            .withUntypedValue("identifiable", identifiable.getId())
                            .withUntypedValue("side", side.getNum())
                            .withSeverity(TypedValue.INFO_SEVERITY)
                            .add();
                }
            } else {
                if (null == side) {
                    reportNode.newReportNode()
                            .withLocaleMessageTemplate("core-iidm-modification-unplannedIdentifiableNotDisconnected", ReportBundleBaseName.BUNDLE_BASE_NAME)
                            .withUntypedValue("identifiable", identifiable.getId())
                            .withSeverity(TypedValue.INFO_SEVERITY)
                            .add();
                } else {
                    reportNode.newReportNode()
                            .withLocaleMessageTemplate("core-iidm-modification-unplannedIdentifiableNotDisconnectedSide", ReportBundleBaseName.BUNDLE_BASE_NAME)
                            .withUntypedValue("identifiable", identifiable.getId())
                            .withUntypedValue("side", side.getNum())
                            .withSeverity(TypedValue.INFO_SEVERITY)
                            .add();
                }
            }
        }
    }

    public static ReportNode replaceThreeWindingsTransformersBy3TwoWindingsTransformersReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-replaced-t3w-by-3t2w", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode replace3TwoWindingsTransformersByThreeWindingsTransformersReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-replaced-3t2w-by-t3w", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void generatorLocalRegulationReport(ReportNode reportNode, String generatorId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-generatorLocalRegulation", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("generatorId", generatorId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void reportOnInconclusiveDryRun(ReportNode reportNode, String cause, String name) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-networkModificationDryRun-failure", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("dryRunError", cause)
                .withUntypedValue("networkModification", name)
                .add();
    }

    public static void dryRunReportNode(ReportNode reportNode) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-networkModificationDryRun-success", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode reportOnDryRunStart(ReportNode reportNode, Network network, String name) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-networkModificationDryRun", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("networkModification", name)
                .withUntypedValue("networkNameOrId", network.getNameOrId())
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void ignoreTemporaryLimitsOnBothLineSides(ReportNode reportNode, String lineId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-limitsLost", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("lineId", lineId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void createNewLineAndReplaceOldOne(ReportNode reportNode, String newLineId, String line1Id, String line2Id, String originalLineId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-newLineOnLineCreated", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("newLineId", newLineId)
                .withUntypedValue("line1Id", line1Id)
                .withUntypedValue("line2Id", line2Id)
                .withUntypedValue("originalLineId", originalLineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void connectVoltageLevelToLines(ReportNode reportNode, String voltageLevelId, String line1Id, String line2Id, String originalLineId) {
        reportNode.newReportNode()
                .withLocaleMessageTemplate("core-iidm-modification-voltageConnectedOnLine", ReportBundleBaseName.BUNDLE_BASE_NAME)
                .withUntypedValue("voltageLevelId", voltageLevelId)
                .withUntypedValue("line1Id", line1Id)
                .withUntypedValue("line2Id", line2Id)
                .withUntypedValue("originalLineId", originalLineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

}
