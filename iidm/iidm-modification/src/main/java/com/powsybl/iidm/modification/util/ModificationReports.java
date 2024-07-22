/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.util;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.iidm.modification.scalable.ProportionalScalable.DistributionMode;
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
    public static final String POSITION_ORDER = "positionOrder";

    // INFO
    public static void createdConnectable(ReportNode reportNode, Connectable<?> connectable) {
        reportNode.newReportNode()
                .withMessageTemplate("connectableCreated", "New connectable ${connectableId} of type ${connectableType} created.")
                .withUntypedValue(CONNECTABLE_ID, connectable.getId())
                .withUntypedValue("connectableType", connectable.getType().name())
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void createdNodeBreakerFeederBay(ReportNode reportNode, String voltageLevelId, String bbsId, Connectable<?> connectable, int parallelBbsNumber) {
        reportNode.newReportNode()
                .withMessageTemplate("newConnectableAdded", "New feeder bay associated to ${connectableId} of type ${connectableType} was created and connected to voltage level ${voltageLevelId} on busbar section ${bbsId} with a closed disconnector " +
                        "and on ${parallelBbsNumber} parallel busbar sections with an open disconnector.")
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
                .withMessageTemplate("lineRemoved", "Line ${lineId} removed")
                .withUntypedValue(LINE_ID, lineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedTieLineAndAssociatedDanglingLines(ReportNode reportNode, String tieLineId, String danglingLineId1, String danglingLineId2, String pairingKey) {
        reportNode.newReportNode()
                .withMessageTemplate("removedTieLineAndAssociatedDanglingLines", "Removed tie line ${tieLineId} and associated dangling lines ${danglingLineId1} and ${danglingLineId2} with pairing key ${pairingKey}")
                .withUntypedValue("tieLineId", tieLineId)
                .withUntypedValue("danglingLineId1", danglingLineId1)
                .withUntypedValue("danglingLineId2", danglingLineId2)
                .withUntypedValue("pairingKey", pairingKey == null ? "" : pairingKey)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void createdLineReport(ReportNode reportNode, String lineId) {
        reportNode.newReportNode()
                .withMessageTemplate("lineCreated", "Line ${lineId} created")
                .withUntypedValue(LINE_ID, lineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void voltageLevelRemovedReport(ReportNode reportNode, String vlId) {
        reportNode.newReportNode()
                .withMessageTemplate("voltageLevelRemoved", "Voltage level ${vlId} removed")
                .withUntypedValue("vlId", vlId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void substationRemovedReport(ReportNode reportNode, String substationId) {
        reportNode.newReportNode()
                .withMessageTemplate("substationRemoved", "Substation ${substationId} removed")
                .withUntypedValue(SUBSTATION_ID, substationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void newCouplingDeviceAddedReport(ReportNode reportNode, String voltageLevelId, String busOrBbsId1, String busOrBbsId2) {
        reportNode.newReportNode()
                .withMessageTemplate("newCouplingDeviceAdded", "New coupling device was created on voltage level ${voltageLevelId}. It connects ${busOrBbsId1} and ${busOrBbsId2} with closed disconnectors")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withUntypedValue("busOrBbsId1", busOrBbsId1)
                .withUntypedValue("busOrBbsId2", busOrBbsId2)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void openDisconnectorsAddedReport(ReportNode reportNode, String voltageLevelId, int nbOpenDisconnectors) {
        reportNode.newReportNode()
                .withMessageTemplate("openDisconnectorsAdded", "${nbOpenDisconnectors} open disconnectors created on parallel busbar sections in voltage level ${voltageLevelId}")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withUntypedValue("nbOpenDisconnectors", nbOpenDisconnectors)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void createdNewSymmetricalTopology(ReportNode reportNode, String voltageLevelId, int busbarCount, int sectionCount) {
        reportNode.newReportNode()
                .withMessageTemplate("SymmetricalTopologyCreated", "New symmetrical topology in voltage level ${voltageLevelId}: creation of ${busbarCount} bus(es) or busbar(s) with ${sectionCount} section(s) each.")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withUntypedValue("busbarCount", busbarCount)
                .withUntypedValue("sectionCount", sectionCount)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedSwitchReport(ReportNode reportNode, String switchId) {
        reportNode.newReportNode()
                .withMessageTemplate("SwitchRemoved", "Switch ${switchId} removed")
                .withUntypedValue("switchId", switchId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedInternalConnectionReport(ReportNode reportNode, int node1, int node2) {
        reportNode.newReportNode()
                .withMessageTemplate("InternalConnectionRemoved", "Internal connection between ${node1} and ${node2} removed")
                .withUntypedValue("node1", node1)
                .withUntypedValue("node2", node2)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedConnectableReport(ReportNode reportNode, String connectableId) {
        reportNode.newReportNode()
                .withMessageTemplate("ConnectableRemoved", "Connectable ${connectableId} removed")
                .withUntypedValue(CONNECTABLE_ID, connectableId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removeFeederBayAborted(ReportNode reportNode, String connectableId, int node, String otherConnectableId) {
        reportNode.newReportNode()
                .withMessageTemplate("RemoveFeederBayAborted", "Remove feeder bay of ${connectableId} cannot go further node ${node}, as it is connected to ${otherConnectableId}")
                .withUntypedValue(CONNECTABLE_ID, connectableId)
                .withUntypedValue("node", node)
                .withUntypedValue("otherConnectableId", otherConnectableId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedSubstationReport(ReportNode reportNode, String substationId) {
        reportNode.newReportNode()
                .withMessageTemplate("removeSubstation", "Substation ${substationId} and its voltage levels have been removed")
                .withUntypedValue(SUBSTATION_ID, substationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedVoltageLevelReport(ReportNode reportNode, String voltageLevelId) {
        reportNode.newReportNode()
                .withMessageTemplate("removeVoltageLevel", "Voltage level ${voltageLevelId}, its equipments and the branches it is connected to have been removed")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedHvdcLineReport(ReportNode reportNode, String hvdcLineId) {
        reportNode.newReportNode()
                .withMessageTemplate("removeHvdcLine", "Hvdc line ${hvdcLineId} has been removed")
                .withUntypedValue(HVDC_LINE_ID, hvdcLineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedVscConverterStationReport(ReportNode reportNode, String vscConverterStationId) {
        reportNode.newReportNode()
                .withMessageTemplate("removeVscConverterStation", "Vsc converter station ${vscConverterStationId} has been removed")
                .withUntypedValue("vscConverterStationId", vscConverterStationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedLccConverterStationReport(ReportNode reportNode, String lccConverterStationId) {
        reportNode.newReportNode()
                .withMessageTemplate("removeLccConverterStation", "Lcc converter station ${lccConverterStationId} has been removed")
                .withUntypedValue("lccConverterStationId", lccConverterStationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    public static void removedShuntCompensatorReport(ReportNode reportNode, String shuntCompensatorId) {
        reportNode.newReportNode()
                .withMessageTemplate("removeShuntCompensator", "Shunt compensator ${shuntCompensatorId} has been removed")
                .withUntypedValue("shuntCompensatorId", shuntCompensatorId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }

    // WARN
    public static void ignoredVscShunts(ReportNode reportNode, String shuntsIds, String converterStationId1, String converterStationId2) {
        reportNode.newReportNode()
                .withMessageTemplate("ignoredVscShunts", "Shunts ${shuntsIds} are ignored since converter stations ${converterStationId1} and ${converterStationId2} are VSC")
                .withUntypedValue("shuntsIds", shuntsIds)
                .withUntypedValue("converterStationId1", converterStationId1)
                .withUntypedValue("converterStationId2", converterStationId2)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void ignoredShuntInAnotherVoltageLevel(ReportNode reportNode, String shuntId, String voltageLevelId1, String voltageLevelId2) {
        reportNode.newReportNode()
                .withMessageTemplate("ignoredShuntInAnotherVoltageLevel", "Shunt compensator ${shuntId} has been ignored because it is not in the same voltage levels as the Lcc (${voltageLevelId1} or ${voltageLevelId2})")
                .withUntypedValue("shuntId", shuntId)
                .withUntypedValue(VOLTAGE_LEVEL_ID + 1, voltageLevelId1)
                .withUntypedValue(VOLTAGE_LEVEL_ID + 2, voltageLevelId2)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void ignoredPositionOrder(ReportNode reportNode, int positionOrder, VoltageLevel voltageLevel) {
        reportNode.newReportNode()
                .withMessageTemplate("ignoredPositionOrder", "Voltage level ${voltageLevelId} is BUS_BREAKER. Position order ${positionOrder} is ignored.")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevel.getId())
                .withUntypedValue(POSITION_ORDER, positionOrder)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostDanglingLineExtensions(ReportNode reportNode, String extensions, String danglingLineId) {
        reportNode.newReportNode()
                .withMessageTemplate("lostDanglingLineExtensions", "Extension [${extensions}] of dangling line ${danglingLineId} will be lost")
                .withUntypedValue("extensions", extensions)
                .withUntypedValue("danglingLineId", danglingLineId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void lostTieLineExtensions(ReportNode reportNode, String extensions, String tieLineId) {
        reportNode.newReportNode()
                .withMessageTemplate("lostTieLineExtensions", "Extension [${extensions}] of tie line ${tieLineId} will be lost")
                .withUntypedValue("extensions", extensions)
                .withUntypedValue("tieLineId", tieLineId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void noBusbarSectionPositionExtensionReport(ReportNode reportNode, BusbarSection bbs) {
        reportNode.newReportNode()
                .withMessageTemplate("noBusbarSectionPositionExtension", "No busbar section position extension found on ${bbsId}, only one disconnector is created.")
                .withUntypedValue(BBS_ID, bbs.getId())
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void positionOrderAlreadyTakenReport(ReportNode reportNode, int positionOrder) {
        reportNode.newReportNode()
                .withMessageTemplate("positionOrderAlreadyTaken", "PositionOrder ${positionOrder} already taken. No position extension created.")
                .withUntypedValue(POSITION_ORDER, positionOrder)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void positionNoSlotLeftByAdjacentBbsReport(ReportNode reportNode, String bbsId) {
        reportNode.newReportNode()
                .withMessageTemplate("positionAdjacentBbsIncoherent", "Positions of adjacent busbar sections do not leave slots for new positions on busbar section ${bbsId}")
                .withUntypedValue(BBS_ID, bbsId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void positionOrderTooLowReport(ReportNode reportNode, int minValue, int positionOrder) {
        reportNode.newReportNode()
                .withMessageTemplate("positionOrderTooLow", "PositionOrder ${positionOrder} too low (<${minValue}). No position extension created.")
                .withUntypedValue(POSITION_ORDER, positionOrder)
                .withUntypedValue("minValue", minValue)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void positionOrderTooHighReport(ReportNode reportNode, int maxValue, int positionOrder) {
        reportNode.newReportNode()
                .withMessageTemplate("positionOrderTooHigh", "PositionOrder ${positionOrder} too high (>${minValue}). No position extension created.")
                .withUntypedValue(POSITION_ORDER, positionOrder)
                .withUntypedValue("maxValue", maxValue)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void noConnectablePositionExtension(ReportNode reportNode, VoltageLevel voltageLevel) {
        reportNode.newReportNode()
                .withMessageTemplate("noConnectablePositionExtensions", "No extensions found on voltageLevel ${voltageLevel}. The extension on the connectable is not created.")
                .withUntypedValue("voltageLevel", voltageLevel.getId())
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    public static void voltageLevelRemovingEquipmentsLeftReport(ReportNode reportNode, String vlId) {
        reportNode.newReportNode()
                .withMessageTemplate("voltageLevelRemovingEquipmentsLeft", "Voltage level ${vlId} still contains equipments")
                .withUntypedValue("vlId", vlId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
    }

    // ERROR
    public static void notFoundBusOrBusbarSectionReport(ReportNode reportNode, String identifiableId) {
        reportNode.newReportNode()
                .withMessageTemplate("notFoundBusOrBusbarSection", "Bus or busbar section ${identifiableId} not found")
                .withUntypedValue(IDENTIFIABLE_ID, identifiableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundShuntReport(ReportNode reportNode, String shuntId) {
        reportNode.newReportNode()
                .withMessageTemplate("notFoundShunt", "Shunt ${shuntId} not found")
                .withUntypedValue("shuntId", shuntId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void networkMismatchReport(ReportNode reportNode, String injectionId, IdentifiableType identifiableType) {
        reportNode.newReportNode()
                .withMessageTemplate("networkMismatch", "Network given in parameters and in injectionAdder are different. Injection '${injectionId}' of type {identifiableType} was added then removed")
                .withUntypedValue("injectionId", injectionId)
                .withUntypedValue(IDENTIFIABLE_TYPE, identifiableType.toString())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void connectableNotSupported(ReportNode reportNode, Connectable<?> connectable) {
        reportNode.newReportNode()
                .withMessageTemplate("connectableNotSupported", "Given connectable not supported: ${connectableClassName}.")
                .withUntypedValue("connectableClassName", connectable.getClass().getName())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void connectableNotInVoltageLevel(ReportNode reportNode, Connectable<?> connectable, VoltageLevel voltageLevel) {
        reportNode.newReportNode()
                .withMessageTemplate("connectableNotInVoltageLevel", "Given connectable ${connectableId} not in voltageLevel ${voltageLevelId}")
                .withUntypedValue(CONNECTABLE_ID, connectable.getId())
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevel.getId())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundLineReport(ReportNode reportNode, String lineId) {
        reportNode.newReportNode()
                .withMessageTemplate("lineNotFound", "Line ${lineId} is not found")
                .withUntypedValue(LINE_ID, lineId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundConnectableReport(ReportNode reportNode, String connectableId) {
        reportNode.newReportNode()
                .withMessageTemplate("connectableNotFound", "Connectable ${connectableId} is not found")
                .withUntypedValue(CONNECTABLE_ID, connectableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void removeFeederBayBusbarSectionReport(ReportNode reportNode, String busbarSectionConnectableId) {
        reportNode.newReportNode()
                .withMessageTemplate("removeBayBusbarSectionConnectable", "Cannot remove feeder bay for connectable ${connectableId}, as it is a busbarSection")
                .withUntypedValue(CONNECTABLE_ID, busbarSectionConnectableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void noVoltageLevelInCommonReport(ReportNode reportNode, String line1Id, String line2Id) {
        reportNode.newReportNode()
                .withMessageTemplate("noVoltageLevelInCommon", "Lines ${line1Id} and ${line2Id} should have one and only one voltage level in common at their extremities")
                .withUntypedValue("line1Id", line1Id)
                .withUntypedValue("line2Id", line2Id)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundVoltageLevelReport(ReportNode reportNode, String voltageLevelId) {
        reportNode.newReportNode()
                .withMessageTemplate("voltageLevelNotFound", "Voltage level ${voltageLevelId} is not found")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundSubstationReport(ReportNode reportNode, String substationId) {
        reportNode.newReportNode()
                .withMessageTemplate("substationNotFound", "Substation ${substationId} is not found")
                .withUntypedValue(SUBSTATION_ID, substationId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundHvdcLineReport(ReportNode reportNode, String hvdcLineId) {
        reportNode.newReportNode()
                .withMessageTemplate("HvdcNotFound", "Hvdc line ${hvdcLineId} is not found")
                .withUntypedValue(HVDC_LINE_ID, hvdcLineId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundBusOrBusbarSectionVoltageLevelReport(ReportNode reportNode, String busOrBusbarSectionId1, String busOrBusbarSectionId2) {
        reportNode.newReportNode()
                .withMessageTemplate("busOrBusbarSectionVoltageLevelNotFound", "Voltage level associated to ${busOrBusbarSectionId1} or ${busOrBusbarSectionId2} not found.")
                .withUntypedValue("busOrBusbarSectionId1", busOrBusbarSectionId1)
                .withUntypedValue("busOrBusbarSectionId2", busOrBusbarSectionId2)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void noTeePointAndOrTappedVoltageLevelReport(ReportNode reportNode, String line1Id, String line2Id, String line3Id) {
        reportNode.newReportNode()
                .withMessageTemplate("noTeePointAndOrTappedVoltageLevel", "Unable to find the tee point and the tapped voltage level from lines ${line1Id}, ${line2Id} and ${line3Id}")
                .withUntypedValue("line1Id", line1Id)
                .withUntypedValue("line2Id", line2Id)
                .withUntypedValue("line3Id", line3Id)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundBusInVoltageLevelReport(ReportNode reportNode, String busId, String voltageLevelId) {
        reportNode.newReportNode()
                .withMessageTemplate("busNotFound", "Bus ${busId} is not found in voltage level ${voltageLevelId}")
                .withUntypedValue("busId", busId)
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void notFoundBusbarSectionInVoltageLevelReport(ReportNode reportNode, String busbarSectionId, String voltageLevelId) {
        reportNode.newReportNode()
                .withMessageTemplate("busbarSectionNotFound", "Busbar section ${busbarSectionId} is not found in voltage level ${voltageLevelId}")
                .withUntypedValue("busbarSectionId", busbarSectionId)
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void noCouplingDeviceOnSameBusOrBusbarSection(ReportNode reportNode, String busbarSectionId) {
        reportNode.newReportNode()
                .withMessageTemplate("noCouplingDeviceOnSameBusOrBusbarSection", "No coupling device can be created on a same bus or busbar section (${busOrBbsId}).")
                .withUntypedValue("busOrBbsId", busbarSectionId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unexpectedDifferentVoltageLevels(ReportNode reportNode, String busbarSectionId1, String busbarSectionId2) {
        reportNode.newReportNode()
                .withMessageTemplate("unexpectedDifferentVoltageLevels", "${busOrBbsId1} and ${busOrBbsId2} are in two different voltage levels.")
                .withUntypedValue("busOrBbsId1", busbarSectionId1)
                .withUntypedValue("busOrBbsId2", busbarSectionId2)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unsupportedVoltageLevelTopologyKind(ReportNode reportNode, String voltageLevelId, TopologyKind expected, TopologyKind actual) {
        reportNode.newReportNode()
                .withMessageTemplate("unsupportedVoltageLevelTopologyKind", "Voltage Level ${voltageLevelId} has an unsupported topology ${actualTopology}. Should be ${expectedTopology}")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withUntypedValue("actualTopology", actual.name())
                .withUntypedValue("expectedTopology", expected.name())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unsupportedIdentifiableType(ReportNode reportNode, IdentifiableType type, String identifiableId) {
        reportNode.newReportNode()
                .withMessageTemplate("unsupportedIdentifiableType", "Unsupported type ${identifiableType} for identifiable ${identifiableId}")
                .withUntypedValue(IDENTIFIABLE_TYPE, type.name())
                .withUntypedValue(IDENTIFIABLE_ID, identifiableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unexpectedNullPositionOrder(ReportNode reportNode, String voltageLevelId) {
        reportNode.newReportNode()
                .withMessageTemplate("unexpectedNullPositionOrder", "Position order is null for attachment in node-breaker voltage level ${voltageLevelId}")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unexpectedNegativePositionOrder(ReportNode reportNode, int positionOrder, String voltageLevelId) {
        reportNode.newReportNode()
                .withMessageTemplate("unexpectedNegativePositionOrder", "Position order is negative (${positionOrder}) for attachment in voltage level ${voltageLevelId}")
                .withUntypedValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withUntypedValue(POSITION_ORDER, positionOrder)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unexpectedIdentifiableType(ReportNode reportNode, Identifiable<?> identifiable) {
        reportNode.newReportNode()
                .withMessageTemplate("unexpectedIdentifiableType", "Unexpected type of identifiable ${identifiableId}: ${identifiableType}")
                .withUntypedValue(IDENTIFIABLE_ID, identifiable.getId())
                .withUntypedValue(IDENTIFIABLE_TYPE, identifiable.getType().name())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void countLowerThanMin(ReportNode reportNode, String type, int min) {
        reportNode.newReportNode()
                .withMessageTemplate("countLowerThanMin", "${type} must be >= ${min}")
                .withUntypedValue("type", type)
                .withUntypedValue("min", min)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void unexpectedSwitchKindsCount(ReportNode reportNode, int switchKindsCount, int expectedSwitchKindsCount) {
        reportNode.newReportNode()
                .withMessageTemplate("unexpectedSwitchKindsCount", "Unexpected switch kinds count (${switchKindsCount}). Should be ${expectedSwitchKindsCount}")
                .withUntypedValue("switchKindsCount", switchKindsCount)
                .withUntypedValue("expectedSwitchKindsCount", expectedSwitchKindsCount)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void undefinedSwitchKind(ReportNode reportNode) {
        reportNode.newReportNode()
                .withMessageTemplate("undefinedSwitchKind", "All switch kinds must be defined")
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void wrongSwitchKind(ReportNode reportNode) {
        reportNode.newReportNode()
                .withMessageTemplate("wrongSwitchKind", "Switch kinds must be DISCONNECTOR or BREAKER")
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void undefinedFictitiousSubstationId(ReportNode reportNode) {
        reportNode.newReportNode()
                .withMessageTemplate("undefinedFictitiousSubstationId", "Fictitious substation ID must be defined if a fictitious substation is to be created")
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    public static void undefinedPercent(ReportNode reportNode) {
        reportNode.newReportNode()
                .withMessageTemplate("undefinedPercent", "Percent should not be undefined")
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
    }

    private ModificationReports() {
    }

    public static void scalingReport(ReportNode reportNode, String type, DistributionMode mode, ScalingType scalingType, double asked, double done) {
        reportNode.newReportNode()
            .withMessageTemplate("scalingApplied", "Successfully scaled on ${identifiableType} using mode ${mode} and type ${type} with a variation value asked of ${asked}. Variation done is ${done}")
            .withUntypedValue(IDENTIFIABLE_TYPE, type)
            .withUntypedValue("mode", mode.name())
            .withUntypedValue("type", scalingType.name())
            .withUntypedValue("asked", asked)
            .withUntypedValue("done", done)
            .withSeverity(TypedValue.INFO_SEVERITY)
            .add();
    }

    public static void scalingReport(ReportNode reportNode, String type, ScalingType scalingType, double asked, double done) {
        reportNode.newReportNode()
            .withMessageTemplate("scalingApplied", "Successfully scaled on ${identifiableType} using mode STACKING and type ${type} with a variation value asked of ${asked}. Variation done is ${done}")
            .withUntypedValue(IDENTIFIABLE_TYPE, type)
            .withUntypedValue("type", scalingType.name())
            .withUntypedValue("asked", asked)
            .withUntypedValue("done", done)
            .withSeverity(TypedValue.INFO_SEVERITY)
            .add();
    }

    public static void connectableConnectionReport(ReportNode reportNode, Identifiable<?> identifiable, boolean connectionSuccessful, ThreeSides side) {
        String defaultMessage = connectionSuccessful ?
            "Connectable ${identifiable} has been connected" :
            "Connectable ${identifiable} has NOT been connected";
        defaultMessage += side == null ? " on each side." : " on side " + side.getNum() + ".";
        String key = connectionSuccessful ? "connectableConnected" : "connectableNotConnected";
        key += side == null ? "" : "Side" + side.getNum();
        reportNode.newReportNode()
            .withMessageTemplate(key, defaultMessage)
            .withUntypedValue("identifiable", identifiable.getId())
            .withSeverity(TypedValue.INFO_SEVERITY)
            .add();
    }

    public static void identifiableDisconnectionReport(ReportNode reportNode, Identifiable<?> identifiable, boolean disconnectionSuccessful, boolean isPlanned, ThreeSides side) {
        String defaultMessage = disconnectionSuccessful ?
            "Identifiable ${identifiable} has been disconnected" :
            "Identifiable ${identifiable} has NOT been disconnected";
        defaultMessage += isPlanned ? " (planned disconnection)" : " (unplanned disconnection)";
        defaultMessage += side == null ? " on each side." : " on side " + side.getNum() + ".";
        String key = isPlanned ? "planned" : "unplanned";
        key += disconnectionSuccessful ? "IdentifiableDisconnected" : "IdentifiableNotDisconnected";
        key += side == null ? "" : "Side" + side.getNum();
        reportNode.newReportNode()
            .withMessageTemplate(key, defaultMessage)
            .withUntypedValue("identifiable", identifiable.getId())
            .withSeverity(TypedValue.INFO_SEVERITY)
            .add();
    }
}
