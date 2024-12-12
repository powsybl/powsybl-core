/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.util;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.ReportNodeAdder;
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
    public static final String IDENTIFIABLE = "identifiable";
    public static final String LINE_1_ID = "line1Id";
    public static final String LINE_2_ID = "line2Id";
    public static final String LINE_3_ID = "line3Id";
    public static final String ORIGINAL_LINE_ID = "originalLineId";
    public static final String CONNECTABLE_TYPE = "connectableType";

    // INFO
    public static void createdConnectable(ReportNode reportNode, Connectable<?> connectable) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.connectableCreated")
                .withUntypedValue(CONNECTABLE_ID, connectable.getId())
                .withUntypedValue(CONNECTABLE_TYPE, connectable.getType().name())
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void createdNodeBreakerFeederBay(ReportNode reportNode, String voltageLevelId, String bbsId, Connectable<?> connectable, int parallelBbsNumber) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.newConnectableAdded")
                .withUntypedValue(CONNECTABLE_ID, connectable.getId())
                .withUntypedValue(CONNECTABLE_TYPE, connectable.getType().toString())
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withUntypedValue(BBS_ID, bbsId)
                .withUntypedValue("parallelBbsNumber", parallelBbsNumber)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedLineReport(ReportNode reportNode, String lineId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.lineRemoved")
                .withUntypedValue(LINE_ID, lineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedTieLineReport(ReportNode reportNode, String tieLineId, String pairingKey) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.removedTieLine")
                .withTypedValue(TIE_LINE_ID, tieLineId, TypedValue.ID)
                .withUntypedValue("pairingKey", pairingKey == null ? "" : pairingKey)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedTieLineAndAssociatedDanglingLines(ReportNode reportNode, String tieLineId, String danglingLineId1, String danglingLineId2, String pairingKey) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.removedTieLineAndAssociatedDanglingLines")
                .withTypedValue(TIE_LINE_ID, tieLineId, TypedValue.ID)
                .withUntypedValue("danglingLineId1", danglingLineId1)
                .withUntypedValue("danglingLineId2", danglingLineId2)
                .withUntypedValue("pairingKey", pairingKey == null ? "" : pairingKey)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void createdLineReport(ReportNode reportNode, String lineId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.lineCreated")
                .withUntypedValue(LINE_ID, lineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void createdVoltageLevelReport(ReportNode reportNode, String voltageLevelId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.voltageLevelCreated")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void createdTwoWindingsTransformerReport(ReportNode reportNode, String twoWindingsTransformerId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.twoWindingsTransformerCreated")
                .withUntypedValue(TWO_WINDINGS_TRANSFORMER_ID, twoWindingsTransformerId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void createdThreeWindingsTransformerReport(ReportNode reportNode, String threeWindingsTransformerId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.threeWindingsTransformerCreated")
                .withUntypedValue(THREE_WINDINGS_TRANSFORMER_ID, threeWindingsTransformerId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedTwoWindingsTransformerReport(ReportNode reportNode, String twoWindingsTransformerId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.twoWindingsTransformerRemoved")
                .withUntypedValue(TWO_WINDINGS_TRANSFORMER_ID, twoWindingsTransformerId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedThreeWindingsTransformerReport(ReportNode reportNode, String threeWindingsTransformerId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.threeWindingsTransformerRemoved")
                .withUntypedValue(THREE_WINDINGS_TRANSFORMER_ID, threeWindingsTransformerId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void lostTwoWindingsTransformerAliases(ReportNode reportNode, String aliases, String twoWindingsTransformerId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.lostTwoWindingsTransformerAliases")
                .withUntypedValue("aliases", aliases)
                .withUntypedValue(TWO_WINDINGS_TRANSFORMER_ID, twoWindingsTransformerId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostThreeWindingsTransformerAliases(ReportNode reportNode, String aliases, String threeWindingsTransformerId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.lostThreeWindingsTransformerAliases")
                .withUntypedValue("aliases", aliases)
                .withUntypedValue(THREE_WINDINGS_TRANSFORMER_ID, threeWindingsTransformerId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostTwoWindingsTransformerProperties(ReportNode reportNode, String properties, String twoWindingsTransformerId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.lostTwoWindingsTransformerProperties")
                .withUntypedValue("properties", properties)
                .withUntypedValue(TWO_WINDINGS_TRANSFORMER_ID, twoWindingsTransformerId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostThreeWindingsTransformerProperties(ReportNode reportNode, String properties, String threeWindingsTransformerId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.lostThreeWindingsTransformerProperties")
                .withUntypedValue("properties", properties)
                .withUntypedValue(THREE_WINDINGS_TRANSFORMER_ID, threeWindingsTransformerId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostTwoWindingsTransformerExtensions(ReportNode reportNode, String extensions, String twoWindingsTransformerId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.lostTwoWindingsTransformerExtensions")
                .withUntypedValue(EXTENSIONS, extensions)
                .withUntypedValue(TWO_WINDINGS_TRANSFORMER_ID, twoWindingsTransformerId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostThreeWindingsTransformerExtensions(ReportNode reportNode, String extensions, String threeWindingsTransformerId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.lostThreeWindingsTransformerExtensions")
                .withUntypedValue(EXTENSIONS, extensions)
                .withUntypedValue(THREE_WINDINGS_TRANSFORMER_ID, threeWindingsTransformerId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostTwoWindingsTransformerOperationalLimitsGroups(ReportNode reportNode, String limits, String twoWindingsTransformerId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.lostTwoWindingsTransformerOperationalLimitsGroups")
                .withUntypedValue("limits", limits)
                .withUntypedValue(TWO_WINDINGS_TRANSFORMER_ID, twoWindingsTransformerId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void voltageLevelRemovedReport(ReportNode reportNode, String vlId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.voltageLevelRemoved")
                .withUntypedValue("vlId", vlId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void substationRemovedReport(ReportNode reportNode, String substationId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.substationRemoved")
                .withUntypedValue(SUBSTATION_ID, substationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void newCouplingDeviceAddedReport(ReportNode reportNode, String voltageLevelId, String busOrBbsId1, String busOrBbsId2) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.newCouplingDeviceAdded")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withUntypedValue("busOrBbsId1", busOrBbsId1)
                .withUntypedValue("busOrBbsId2", busOrBbsId2)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void openDisconnectorsAddedReport(ReportNode reportNode, String voltageLevelId, int nbOpenDisconnectors) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.openDisconnectorsAdded")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withUntypedValue("nbOpenDisconnectors", nbOpenDisconnectors)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void createdNewSymmetricalTopology(ReportNode reportNode, String voltageLevelId, int busbarCount, int sectionCount) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.SymmetricalTopologyCreated")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withUntypedValue("busbarCount", busbarCount)
                .withUntypedValue("sectionCount", sectionCount)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedSwitchReport(ReportNode reportNode, String switchId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.SwitchRemoved")
                .withUntypedValue("switchId", switchId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedInternalConnectionReport(ReportNode reportNode, int node1, int node2) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.InternalConnectionRemoved")
                .withUntypedValue("node1", node1)
                .withUntypedValue("node2", node2)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedConnectableReport(ReportNode reportNode, String connectableId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.ConnectableRemoved")
                .withUntypedValue(CONNECTABLE_ID, connectableId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removeFeederBayAborted(ReportNode reportNode, String connectableId, int node, String otherConnectableId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.RemoveFeederBayAborted")
                .withUntypedValue(CONNECTABLE_ID, connectableId)
                .withUntypedValue("node", node)
                .withUntypedValue("otherConnectableId", otherConnectableId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedSubstationReport(ReportNode reportNode, String substationId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.removeSubstation")
                .withUntypedValue(SUBSTATION_ID, substationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedVoltageLevelReport(ReportNode reportNode, String voltageLevelId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.removeVoltageLevel")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedHvdcLineReport(ReportNode reportNode, String hvdcLineId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.removeHvdcLine")
                .withUntypedValue(HVDC_LINE_ID, hvdcLineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedVscConverterStationReport(ReportNode reportNode, String vscConverterStationId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.removeVscConverterStation")
                .withUntypedValue("vscConverterStationId", vscConverterStationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedLccConverterStationReport(ReportNode reportNode, String lccConverterStationId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.removeLccConverterStation")
                .withUntypedValue("lccConverterStationId", lccConverterStationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedShuntCompensatorReport(ReportNode reportNode, String shuntCompensatorId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.removeShuntCompensator")
                .withUntypedValue("shuntCompensatorId", shuntCompensatorId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    // WARN
    public static void ignoredVscShunts(ReportNode reportNode, String shuntsIds, String converterStationId1, String converterStationId2) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.ignoredVscShunts")
                .withUntypedValue("shuntsIds", shuntsIds)
                .withUntypedValue("converterStationId1", converterStationId1)
                .withUntypedValue("converterStationId2", converterStationId2)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void ignoredShuntInAnotherVoltageLevel(ReportNode reportNode, String shuntId, String voltageLevelId1, String voltageLevelId2) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.ignoredShuntInAnotherVoltageLevel")
                .withUntypedValue("shuntId", shuntId)
                .withUntypedValue(VOLTAGE_LEVEL_ID + 1, voltageLevelId1)
                .withUntypedValue(VOLTAGE_LEVEL_ID + 2, voltageLevelId2)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void ignoredPositionOrder(ReportNode reportNode, int positionOrder, VoltageLevel voltageLevel) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.ignoredPositionOrder")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevel.getId())
                .withUntypedValue(POSITION_ORDER, positionOrder)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostDanglingLineExtensions(ReportNode reportNode, String extensions, String danglingLineId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.lostDanglingLineExtensions")
                .withUntypedValue(EXTENSIONS, extensions)
                .withUntypedValue("danglingLineId", danglingLineId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostTieLineExtensions(ReportNode reportNode, String extensions, String tieLineId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.lostTieLineExtensions")
                .withUntypedValue(EXTENSIONS, extensions)
                .withTypedValue(TIE_LINE_ID, tieLineId, TypedValue.ID)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void noBusbarSectionPositionExtensionReport(ReportNode reportNode, BusbarSection bbs) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.noBusbarSectionPositionExtension")
                .withUntypedValue(BBS_ID, bbs.getId())
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void positionOrderAlreadyTakenReport(ReportNode reportNode, int positionOrder, TypedValue severity) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.positionOrderAlreadyTaken")
                .withUntypedValue(POSITION_ORDER, positionOrder)
                .withSeverity(severity)
                .add();
    }

    public static void positionNoSlotLeftByAdjacentBbsReport(ReportNode reportNode, String bbsId, TypedValue severity) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.positionAdjacentBbsIncoherent")
                .withUntypedValue(BBS_ID, bbsId)
                .withSeverity(severity)
                .add();
    }

    public static void positionOrderTooLowReport(ReportNode reportNode, int minValue, int positionOrder, TypedValue severity) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.positionOrderTooLow")
                .withUntypedValue(POSITION_ORDER, positionOrder)
                .withUntypedValue("minValue", minValue)
                .withSeverity(severity)
                .add();
    }

    public static void positionOrderTooHighReport(ReportNode reportNode, int maxValue, int positionOrder, TypedValue severity) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.positionOrderTooHigh")
                .withUntypedValue(POSITION_ORDER, positionOrder)
                .withUntypedValue("maxValue", maxValue)
                .withSeverity(severity)
                .add();
    }

    public static void noConnectablePositionExtension(ReportNode reportNode, VoltageLevel voltageLevel, String connectableId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.noConnectablePositionExtensions")
                .withUntypedValue("voltageLevel", voltageLevel.getId())
                .withUntypedValue(CONNECTABLE_ID, connectableId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void voltageLevelRemovingEquipmentsLeftReport(ReportNode reportNode, String vlId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.voltageLevelRemovingEquipmentsLeft")
                .withUntypedValue("vlId", vlId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    // ERROR
    public static void notFoundBusOrBusbarSectionReport(ReportNode reportNode, String identifiableId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.notFoundBusOrBusbarSection")
                .withUntypedValue(IDENTIFIABLE_ID, identifiableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundShuntReport(ReportNode reportNode, String shuntId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.notFoundShunt")
                .withUntypedValue("shuntId", shuntId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void networkMismatchReport(ReportNode reportNode, String injectionId, IdentifiableType identifiableType) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.networkMismatch")
                .withUntypedValue("injectionId", injectionId)
                .withUntypedValue(IDENTIFIABLE_TYPE, identifiableType.toString())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void connectableNotSupported(ReportNode reportNode, Connectable<?> connectable) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.connectableNotSupported")
                .withUntypedValue("connectableClassName", connectable.getClass().getName())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void connectableNotInVoltageLevel(ReportNode reportNode, Connectable<?> connectable, VoltageLevel voltageLevel) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.connectableNotInVoltageLevel")
                .withUntypedValue(CONNECTABLE_ID, connectable.getId())
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevel.getId())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundLineReport(ReportNode reportNode, String lineId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.lineNotFound")
                .withUntypedValue(LINE_ID, lineId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundConnectableReport(ReportNode reportNode, String connectableId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.connectableNotFound")
                .withUntypedValue(CONNECTABLE_ID, connectableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void removeFeederBayBusbarSectionReport(ReportNode reportNode, String busbarSectionConnectableId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.removeBayBusbarSectionConnectable")
                .withUntypedValue(CONNECTABLE_ID, busbarSectionConnectableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void moveFeederBayBusbarSectionReport(ReportNode reportNode, String busbarSectionConnectableId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.moveBayBusbarSectionConnectable")
                .withUntypedValue(CONNECTABLE_ID, busbarSectionConnectableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void noVoltageLevelInCommonReport(ReportNode reportNode, String line1Id, String line2Id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.noVoltageLevelInCommon")
                .withUntypedValue(LINE_1_ID, line1Id)
                .withUntypedValue(LINE_2_ID, line2Id)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundVoltageLevelReport(ReportNode reportNode, String voltageLevelId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.voltageLevelNotFound")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundSubstationReport(ReportNode reportNode, String substationId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.substationNotFound")
                .withUntypedValue(SUBSTATION_ID, substationId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundHvdcLineReport(ReportNode reportNode, String hvdcLineId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.HvdcNotFound")
                .withUntypedValue(HVDC_LINE_ID, hvdcLineId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundBusOrBusbarSectionVoltageLevelReport(ReportNode reportNode, String busOrBusbarSectionId1, String busOrBusbarSectionId2) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.busOrBusbarSectionVoltageLevelNotFound")
                .withUntypedValue("busOrBusbarSectionId1", busOrBusbarSectionId1)
                .withUntypedValue("busOrBusbarSectionId2", busOrBusbarSectionId2)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void noTeePointAndOrTappedVoltageLevelReport(ReportNode reportNode, String line1Id, String line2Id, String line3Id) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.noTeePointAndOrTappedVoltageLevel")
                .withUntypedValue(LINE_1_ID, line1Id)
                .withUntypedValue(LINE_2_ID, line2Id)
                .withUntypedValue(LINE_3_ID, line3Id)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundBusInVoltageLevelReport(ReportNode reportNode, String busId, String voltageLevelId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.busNotFound")
                .withUntypedValue("busId", busId)
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundBusbarSectionInVoltageLevelReport(ReportNode reportNode, String busbarSectionId, String voltageLevelId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.busbarSectionNotFound")
                .withUntypedValue("busbarSectionId", busbarSectionId)
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void noCouplingDeviceOnSameBusOrBusbarSection(ReportNode reportNode, String busbarSectionId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.noCouplingDeviceOnSameBusOrBusbarSection")
                .withUntypedValue("busOrBbsId", busbarSectionId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unexpectedDifferentVoltageLevels(ReportNode reportNode, String busbarSectionId1, String busbarSectionId2) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.unexpectedDifferentVoltageLevels")
                .withUntypedValue("busOrBbsId1", busbarSectionId1)
                .withUntypedValue("busOrBbsId2", busbarSectionId2)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unsupportedIdentifiableType(ReportNode reportNode, IdentifiableType type, String identifiableId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.unsupportedIdentifiableType")
                .withUntypedValue(IDENTIFIABLE_TYPE, type.name())
                .withUntypedValue(IDENTIFIABLE_ID, identifiableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unexpectedNullPositionOrder(ReportNode reportNode, String voltageLevelId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.unexpectedNullPositionOrder")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unexpectedNegativePositionOrder(ReportNode reportNode, int positionOrder, String voltageLevelId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.unexpectedNegativePositionOrder")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withUntypedValue(POSITION_ORDER, positionOrder)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unexpectedIdentifiableType(ReportNode reportNode, Identifiable<?> identifiable) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.unexpectedIdentifiableType")
                .withUntypedValue(IDENTIFIABLE_ID, identifiable.getId())
                .withUntypedValue(IDENTIFIABLE_TYPE, identifiable.getType().name())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void countLowerThanMin(ReportNode reportNode, String type, int min) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.countLowerThanMin")
                .withUntypedValue("type", type)
                .withUntypedValue("min", min)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unexpectedSwitchKindsCount(ReportNode reportNode, int switchKindsCount, int expectedSwitchKindsCount) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.unexpectedSwitchKindsCount")
                .withUntypedValue("switchKindsCount", switchKindsCount)
                .withUntypedValue("expectedSwitchKindsCount", expectedSwitchKindsCount)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void undefinedSwitchKind(ReportNode reportNode) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.undefinedSwitchKind")
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void wrongSwitchKind(ReportNode reportNode) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.wrongSwitchKind")
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void wrongBusbarPosition(ReportNode reportNode) {
        reportNode.newReportNode()
            .withMessageTemplate("core.iidm.modification.wrongBusbarPosition")
            .withSeverity(TypedValue.ERROR_SEVERITY)
            .add();
    }

    public static void undefinedFictitiousSubstationId(ReportNode reportNode) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.undefinedFictitiousSubstationId")
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void undefinedPercent(ReportNode reportNode) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.undefinedPercent")
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    private ModificationReports() {
    }

    public static void scalingReport(ReportNode reportNode, String type, String mode, ScalingType scalingType, double asked, double done) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.scalingApplied")
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
                        .withMessageTemplate("core.iidm.modification.connectableConnected")
                        .withUntypedValue(IDENTIFIABLE, identifiable.getId())
                        .withSeverity(TypedValue.INFO_SEVERITY)
                        .add();
            } else {
                reportNode.newReportNode()
                        .withMessageTemplate("core.iidm.modification.connectableConnectedSide")
                        .withUntypedValue(IDENTIFIABLE, identifiable.getId())
                        .withUntypedValue("side", side.getNum())
                        .withSeverity(TypedValue.INFO_SEVERITY)
                        .add();
            }
        } else if (null == side) {
            reportNode.newReportNode()
                    .withMessageTemplate("core.iidm.modification.connectableNotConnected")
                    .withUntypedValue(IDENTIFIABLE, identifiable.getId())
                    .withSeverity(TypedValue.INFO_SEVERITY)
                    .add();
        } else {
            reportNode.newReportNode()
                    .withMessageTemplate("core.iidm.modification.connectableNotConnectedSide")
                    .withUntypedValue(IDENTIFIABLE, identifiable.getId())
                    .withUntypedValue("side", side.getNum())
                    .withSeverity(TypedValue.INFO_SEVERITY)
                    .add();
        }
    }

    public static void identifiableDisconnectionReport(ReportNode reportNode, Identifiable<?> identifiable, boolean disconnectionSuccessful, boolean isPlanned, ThreeSides side) {
        ReportNodeAdder adder = reportNode.newReportNode()
                .withMessageTemplate(getKey(isPlanned, disconnectionSuccessful, side != null))
                .withUntypedValue(IDENTIFIABLE, identifiable.getId())
                .withSeverity(TypedValue.INFO_SEVERITY);
        if (side != null) {
            adder.withUntypedValue("side", side.getNum());
        }
        adder.add();
    }

    private static String getKey(boolean isPlanned, boolean disconnectionSuccessful, boolean hasSide) {
        // Transforming the 3 booleans into a 3-bits integer 0bXYZ to operate switch case on it
        // MSB (leading bit) X corresponds to isPlanned
        // Second bit Y corresponds to disconnectionSuccessful
        // LSB (least significant bit) Z corresponds to hasSide
        int index = (isPlanned ? 1 : 0) << 2
                | (disconnectionSuccessful ? 1 : 0) << 1
                | (hasSide ? 1 : 0);
        return switch (index) {
            case 0b000 -> "core.iidm.modification.unplannedIdentifiableNotDisconnected";
            case 0b001 -> "core.iidm.modification.unplannedIdentifiableNotDisconnectedSide";
            case 0b010 -> "core.iidm.modification.unplannedIdentifiableDisconnected";
            case 0b011 -> "core.iidm.modification.unplannedIdentifiableDisconnectedSide";
            case 0b100 -> "core.iidm.modification.plannedIdentifiableNotDisconnected";
            case 0b101 -> "core.iidm.modification.plannedIdentifiableNotDisconnectedSide";
            case 0b110 -> "core.iidm.modification.plannedIdentifiableDisconnected";
            case 0b111 -> "core.iidm.modification.plannedIdentifiableDisconnectedSide";
            default -> throw new IllegalStateException();
        };
    }

    public static ReportNode replaceThreeWindingsTransformersBy3TwoWindingsTransformersReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.replaced-t3w-by-3t2w")
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode replace3TwoWindingsTransformersByThreeWindingsTransformersReport(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.replaced-3t2w-by-t3w")
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void generatorLocalRegulationReport(ReportNode reportNode, String generatorId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.generatorLocalRegulation")
                .withUntypedValue("generatorId", generatorId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void reportOnInconclusiveDryRun(ReportNode reportNode, String cause, String name) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.networkModificationDryRun-failure")
                .withUntypedValue("dryRunError", cause)
                .withUntypedValue("networkModification", name)
                .add();
    }

    public static void dryRunReportNode(ReportNode reportNode) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.networkModificationDryRun-success")
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static ReportNode reportOnDryRunStart(ReportNode reportNode, Network network, String name) {
        return reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.networkModificationDryRun")
                .withUntypedValue("networkModification", name)
                .withUntypedValue("networkNameOrId", network.getNameOrId())
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void ignoreTemporaryLimitsOnBothLineSides(ReportNode reportNode, String lineId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.limitsLost")
                .withUntypedValue(LINE_ID, lineId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void createNewLineAndReplaceOldOne(ReportNode reportNode, String newLineId, String line1Id, String line2Id, String originalLineId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.newLineOnLineCreated")
                .withUntypedValue("newLineId", newLineId)
                .withUntypedValue(LINE_1_ID, line1Id)
                .withUntypedValue(LINE_2_ID, line2Id)
                .withUntypedValue(ORIGINAL_LINE_ID, originalLineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void connectVoltageLevelToLines(ReportNode reportNode, String voltageLevelId, String line1Id, String line2Id, String originalLineId) {
        reportNode.newReportNode()
                .withMessageTemplate("core.iidm.modification.voltageConnectedOnLine")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withUntypedValue(LINE_1_ID, line1Id)
                .withUntypedValue(LINE_2_ID, line2Id)
                .withUntypedValue(ORIGINAL_LINE_ID, originalLineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

}
