/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.util;

import com.powsybl.commons.reporter.ReportMessage;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;
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
    public static void createdConnectable(Reporter reporter, Connectable<?> connectable) {
        reporter.report(ReportMessage.builder()
                .withKey("connectableCreated")
                .withDefaultMessage("New connectable ${connectableId} of type ${connectableType} created.")
                .withValue(CONNECTABLE_ID, connectable.getId())
                .withValue("connectableType", connectable.getType().name())
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void createdNodeBreakerFeederBay(Reporter reporter, String voltageLevelId, String bbsId, Connectable<?> connectable, int parallelBbsNumber) {
        reporter.report(ReportMessage.builder()
                .withKey("newConnectableAdded")
                .withDefaultMessage("New feeder bay associated to ${connectableId} of type ${connectableType} was created and connected to voltage level ${voltageLevelId} on busbar section ${bbsId} with a closed disconnector " +
                        "and on ${parallelBbsNumber} parallel busbar sections with an open disconnector.")
                .withValue(CONNECTABLE_ID, connectable.getId())
                .withValue("connectableType", connectable.getType().toString())
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withValue(BBS_ID, bbsId)
                .withValue("parallelBbsNumber", parallelBbsNumber)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void removedLineReport(Reporter reporter, String lineId) {
        reporter.report(ReportMessage.builder()
                .withKey("lineRemoved")
                .withDefaultMessage("Line ${lineId} removed")
                .withValue(LINE_ID, lineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void removedTieLineAndAssociatedDanglingLines(Reporter reporter, String tieLineId, String danglingLineId1, String danglingLineId2, String pairingKey) {
        reporter.report(ReportMessage.builder()
                .withKey("removedTieLineAndAssociatedDanglingLines")
                .withDefaultMessage("Removed tie line ${tieLineId} and associated dangling lines ${danglingLineId1} and ${danglingLineId2} with pairing key ${pairingKey}")
                .withValue("tieLineId", tieLineId)
                .withValue("danglingLineId1", danglingLineId1)
                .withValue("danglingLineId2", danglingLineId2)
                .withValue("pairingKey", pairingKey == null ? "" : pairingKey)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void createdLineReport(Reporter reporter, String lineId) {
        reporter.report(ReportMessage.builder()
                .withKey("lineCreated")
                .withDefaultMessage("Line ${lineId} created")
                .withValue(LINE_ID, lineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void voltageLevelRemovedReport(Reporter reporter, String vlId) {
        reporter.report(ReportMessage.builder()
                .withKey("voltageLevelRemoved")
                .withDefaultMessage("Voltage level ${vlId} removed")
                .withValue("vlId", vlId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void substationRemovedReport(Reporter reporter, String substationId) {
        reporter.report(ReportMessage.builder()
                .withKey("substationRemoved")
                .withDefaultMessage("Substation ${substationId} removed")
                .withValue(SUBSTATION_ID, substationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void newCouplingDeviceAddedReport(Reporter reporter, String voltageLevelId, String busOrBbsId1, String busOrBbsId2) {
        reporter.report(ReportMessage.builder()
                .withKey("newCouplingDeviceAdded")
                .withDefaultMessage("New coupling device was created on voltage level ${voltageLevelId}. It connects ${busOrBbsId1} and ${busOrBbsId2} with closed disconnectors")
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withValue("busOrBbsId1", busOrBbsId1)
                .withValue("busOrBbsId2", busOrBbsId2)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void openDisconnectorsAddedReport(Reporter reporter, String voltageLevelId, int nbOpenDisconnectors) {
        reporter.report(ReportMessage.builder()
                .withKey("openDisconnectorsAdded")
                .withDefaultMessage("${nbOpenDisconnectors} open disconnectors created on parallel busbar sections in voltage level ${voltageLevelId}")
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withValue("nbOpenDisconnectors", nbOpenDisconnectors)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void createdNewSymmetricalTopology(Reporter reporter, String voltageLevelId, int busbarCount, int sectionCount) {
        reporter.report(ReportMessage.builder()
                .withKey("SymmetricalTopologyCreated")
                .withDefaultMessage("New symmetrical topology in voltage level ${voltageLevelId}: creation of ${busbarCount} bus(es) or busbar(s) with ${sectionCount} section(s) each.")
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withValue("busbarCount", busbarCount)
                .withValue("sectionCount", sectionCount)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void removedSwitchReport(Reporter reporter, String switchId) {
        reporter.report(ReportMessage.builder()
                .withKey("SwitchRemoved")
                .withDefaultMessage("Switch ${switchId} removed")
                .withValue("switchId", switchId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void removedInternalConnectionReport(Reporter reporter, int node1, int node2) {
        reporter.report(ReportMessage.builder()
                .withKey("InternalConnectionRemoved")
                .withDefaultMessage("Internal connection between ${node1} and ${node2} removed")
                .withValue("node1", node1)
                .withValue("node2", node2)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void removedConnectableReport(Reporter reporter, String connectableId) {
        reporter.report(ReportMessage.builder()
                .withKey("ConnectableRemoved")
                .withDefaultMessage("Connectable ${connectableId} removed")
                .withValue(CONNECTABLE_ID, connectableId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void removeFeederBayAborted(Reporter reporter, String connectableId, int node, String otherConnectableId) {
        reporter.report(ReportMessage.builder()
                .withKey("RemoveFeederBayAborted")
                .withDefaultMessage("Remove feeder bay of ${connectableId} cannot go further node ${node}, as it is connected to ${otherConnectableId}")
                .withValue(CONNECTABLE_ID, connectableId)
                .withValue("node", node)
                .withValue("otherConnectableId", otherConnectableId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void removedSubstationReport(Reporter reporter, String substationId) {
        reporter.report(ReportMessage.builder()
                .withKey("removeSubstation")
                .withDefaultMessage("Substation ${substationId} and its voltage levels have been removed")
                .withValue(SUBSTATION_ID, substationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void removedVoltageLevelReport(Reporter reporter, String voltageLevelId) {
        reporter.report(ReportMessage.builder()
                .withKey("removeVoltageLevel")
                .withDefaultMessage("Voltage level ${voltageLevelId}, its equipments and the branches it is connected to have been removed")
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void removedHvdcLineReport(Reporter reporter, String hvdcLineId) {
        reporter.report(ReportMessage.builder()
                .withKey("removeHvdcLine")
                .withDefaultMessage("Hvdc line ${hvdcLineId} has been removed")
                .withValue(HVDC_LINE_ID, hvdcLineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void removedVscConverterStationReport(Reporter reporter, String vscConverterStationId) {
        reporter.report(ReportMessage.builder()
                .withKey("removeVscConverterStation")
                .withDefaultMessage("Vsc converter station ${vscConverterStationId} has been removed")
                .withValue("vscConverterStationId", vscConverterStationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void removedLccConverterStationReport(Reporter reporter, String lccConverterStationId) {
        reporter.report(ReportMessage.builder()
                .withKey("removeLccConverterStation")
                .withDefaultMessage("Lcc converter station ${lccConverterStationId} has been removed")
                .withValue("lccConverterStationId", lccConverterStationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void removedShuntCompensatorReport(Reporter reporter, String shuntCompensatorId) {
        reporter.report(ReportMessage.builder()
                .withKey("removeShuntCompensator")
                .withDefaultMessage("Shunt compensator ${shuntCompensatorId} has been removed")
                .withValue("shuntCompensatorId", shuntCompensatorId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    // WARN
    public static void ignoredVscShunts(Reporter reporter, String shuntsIds, String converterStationId1, String converterStationId2) {
        reporter.report(ReportMessage.builder()
                .withKey("ignoredVscShunts")
                .withDefaultMessage("Shunts ${shuntsIds} are ignored since converter stations ${converterStationId1} and ${converterStationId2} are VSC")
                .withValue("shuntsIds", shuntsIds)
                .withValue("converterStationId1", converterStationId1)
                .withValue("converterStationId2", converterStationId2)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    public static void ignoredShuntInAnotherVoltageLevel(Reporter reporter, String shuntId, String voltageLevelId1, String voltageLevelId2) {
        reporter.report(ReportMessage.builder()
                .withKey("ignoredShuntInAnotherVoltageLevel")
                .withDefaultMessage("Shunt compensator ${shuntId} has been ignored because it is not in the same voltage levels as the Lcc (${voltageLevelId1} or ${voltageLevelId2})")
                .withValue("shuntId", shuntId)
                .withValue(VOLTAGE_LEVEL_ID + 1, voltageLevelId1)
                .withValue(VOLTAGE_LEVEL_ID + 2, voltageLevelId2)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    public static void ignoredPositionOrder(Reporter reporter, int positionOrder, VoltageLevel voltageLevel) {
        reporter.report(ReportMessage.builder()
                .withKey("ignoredPositionOrder")
                .withDefaultMessage("Voltage level ${voltageLevelId} is BUS_BREAKER. Position order ${positionOrder} is ignored.")
                .withValue(VOLTAGE_LEVEL_ID, voltageLevel.getId())
                .withValue(POSITION_ORDER, positionOrder)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    public static void lostDanglingLineExtensions(Reporter reporter, String extensions, String danglingLineId) {
        reporter.report(ReportMessage.builder()
                .withKey("lostDanglingLineExtensions")
                .withDefaultMessage("Extension [${extensions}] of dangling line ${danglingLineId} will be lost")
                .withValue("extensions", extensions)
                .withValue("danglingLineId", danglingLineId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    public static void lostTieLineExtensions(Reporter reporter, String extensions, String tieLineId) {
        reporter.report(ReportMessage.builder()
                .withKey("lostTieLineExtensions")
                .withDefaultMessage("Extension [${extensions}] of tie line ${tieLineId} will be lost")
                .withValue("extensions", extensions)
                .withValue("tieLineId", tieLineId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    public static void noBusbarSectionPositionExtensionReport(Reporter reporter, BusbarSection bbs) {
        reporter.report(ReportMessage.builder()
                .withKey("noBusbarSectionPositionExtension")
                .withDefaultMessage("No busbar section position extension found on ${bbsId}, only one disconnector is created.")
                .withValue(BBS_ID, bbs.getId())
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    public static void positionOrderAlreadyTakenReport(Reporter reporter, int positionOrder) {
        reporter.report(ReportMessage.builder()
                .withKey("positionOrderAlreadyTaken")
                .withDefaultMessage("PositionOrder ${positionOrder} already taken. No position extension created.")
                .withValue(POSITION_ORDER, positionOrder)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    public static void positionNoSlotLeftByAdjacentBbsReport(Reporter reporter, String bbsId) {
        reporter.report(ReportMessage.builder()
                .withKey("positionAdjacentBbsIncoherent")
                .withDefaultMessage("Positions of adjacent busbar sections do not leave slots for new positions on busbar section ${bbsId}")
                .withValue(BBS_ID, bbsId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    public static void positionOrderTooLowReport(Reporter reporter, int minValue, int positionOrder) {
        reporter.report(ReportMessage.builder()
                .withKey("positionOrderTooLow")
                .withDefaultMessage("PositionOrder ${positionOrder} too low (<${minValue}). No position extension created.")
                .withValue(POSITION_ORDER, positionOrder)
                .withValue("minValue", minValue)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    public static void positionOrderTooHighReport(Reporter reporter, int maxValue, int positionOrder) {
        reporter.report(ReportMessage.builder()
                .withKey("positionOrderTooHigh")
                .withDefaultMessage("PositionOrder ${positionOrder} too high (>${minValue}). No position extension created.")
                .withValue(POSITION_ORDER, positionOrder)
                .withValue("maxValue", maxValue)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    public static void noConnectablePositionExtension(Reporter reporter, VoltageLevel voltageLevel) {
        reporter.report(ReportMessage.builder()
                .withKey("noConnectablePositionExtensions")
                .withDefaultMessage("No extensions found on voltageLevel ${voltageLevel}. The extension on the connectable is not created.")
                .withValue("voltageLevel", voltageLevel.getId())
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    public static void voltageLevelRemovingEquipmentsLeftReport(Reporter reporter, String vlId) {
        reporter.report(ReportMessage.builder()
                .withKey("voltageLevelRemovingEquipmentsLeft")
                .withDefaultMessage("Voltage level ${vlId} still contains equipments")
                .withValue("vlId", vlId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    // ERROR
    public static void notFoundBusOrBusbarSectionReport(Reporter reporter, String identifiableId) {
        reporter.report(ReportMessage.builder()
                .withKey("notFoundBusOrBusbarSection")
                .withDefaultMessage("Bus or busbar section ${identifiableId} not found")
                .withValue(IDENTIFIABLE_ID, identifiableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void notFoundShuntReport(Reporter reporter, String shuntId) {
        reporter.report(ReportMessage.builder()
                .withKey("notFoundShunt")
                .withDefaultMessage("Shunt ${shuntId} not found")
                .withValue("shuntId", shuntId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void networkMismatchReport(Reporter reporter, String injectionId, IdentifiableType identifiableType) {
        reporter.report(ReportMessage.builder()
                .withKey("networkMismatch")
                .withDefaultMessage("Network given in parameters and in injectionAdder are different. Injection '${injectionId}' of type {identifiableType} was added then removed")
                .withValue("injectionId", injectionId)
                .withValue(IDENTIFIABLE_TYPE, identifiableType.toString())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void connectableNotSupported(Reporter reporter, Connectable<?> connectable) {
        reporter.report(ReportMessage.builder()
                .withKey("connectableNotSupported")
                .withDefaultMessage("Given connectable not supported: ${connectableClassName}.")
                .withValue("connectableClassName", connectable.getClass().getName())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void connectableNotInVoltageLevel(Reporter reporter, Connectable<?> connectable, VoltageLevel voltageLevel) {
        reporter.report(ReportMessage.builder()
                .withKey("connectableNotInVoltageLevel")
                .withDefaultMessage("Given connectable ${connectableId} not in voltageLevel ${voltageLevelId}")
                .withValue(CONNECTABLE_ID, connectable.getId())
                .withValue(VOLTAGE_LEVEL_ID, voltageLevel.getId())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void notFoundLineReport(Reporter reporter, String lineId) {
        reporter.report(ReportMessage.builder()
                .withKey("lineNotFound")
                .withDefaultMessage("Line ${lineId} is not found")
                .withValue(LINE_ID, lineId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void notFoundConnectableReport(Reporter reporter, String connectableId) {
        reporter.report(ReportMessage.builder()
                .withKey("connectableNotFound")
                .withDefaultMessage("Connectable ${connectableId} is not found")
                .withValue(CONNECTABLE_ID, connectableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void removeFeederBayBusbarSectionReport(Reporter reporter, String busbarSectionConnectableId) {
        reporter.report(ReportMessage.builder()
                .withKey("removeBayBusbarSectionConnectable")
                .withDefaultMessage("Cannot remove feeder bay for connectable ${connectableId}, as it is a busbarSection")
                .withValue(CONNECTABLE_ID, busbarSectionConnectableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void noVoltageLevelInCommonReport(Reporter reporter, String line1Id, String line2Id) {
        reporter.report(ReportMessage.builder()
                .withKey("noVoltageLevelInCommon")
                .withDefaultMessage("Lines ${line1Id} and ${line2Id} should have one and only one voltage level in common at their extremities")
                .withValue("line1Id", line1Id)
                .withValue("line2Id", line2Id)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void notFoundVoltageLevelReport(Reporter reporter, String voltageLevelId) {
        reporter.report(ReportMessage.builder()
                .withKey("voltageLevelNotFound")
                .withDefaultMessage("Voltage level ${voltageLevelId} is not found")
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void notFoundSubstationReport(Reporter reporter, String substationId) {
        reporter.report(ReportMessage.builder()
                .withKey("substationNotFound")
                .withDefaultMessage("Substation ${substationId} is not found")
                .withValue(SUBSTATION_ID, substationId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void notFoundHvdcLineReport(Reporter reporter, String hvdcLineId) {
        reporter.report(ReportMessage.builder()
                .withKey("HvdcNotFound")
                .withDefaultMessage("Hvdc line ${hvdcLineId} is not found")
                .withValue(HVDC_LINE_ID, hvdcLineId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void notFoundBusOrBusbarSectionVoltageLevelReport(Reporter reporter, String busOrBusbarSectionId1, String busOrBusbarSectionId2) {
        reporter.report(ReportMessage.builder()
                .withKey("busOrBusbarSectionVoltageLevelNotFound")
                .withDefaultMessage("Voltage level associated to ${busOrBusbarSectionId1} or ${busOrBusbarSectionId2} not found.")
                .withValue("busOrBusbarSectionId1", busOrBusbarSectionId1)
                .withValue("busOrBusbarSectionId2", busOrBusbarSectionId2)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void noTeePointAndOrTappedVoltageLevelReport(Reporter reporter, String line1Id, String line2Id, String line3Id) {
        reporter.report(ReportMessage.builder()
                .withKey("noTeePointAndOrTappedVoltageLevel")
                .withDefaultMessage("Unable to find the tee point and the tapped voltage level from lines ${line1Id}, ${line2Id} and ${line3Id}")
                .withValue("line1Id", line1Id)
                .withValue("line2Id", line2Id)
                .withValue("line3Id", line3Id)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void notFoundBusInVoltageLevelReport(Reporter reporter, String busId, String voltageLevelId) {
        reporter.report(ReportMessage.builder()
                .withKey("busNotFound")
                .withDefaultMessage("Bus ${busId} is not found in voltage level ${voltageLevelId}")
                .withValue("busId", busId)
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void notFoundBusbarSectionInVoltageLevelReport(Reporter reporter, String busbarSectionId, String voltageLevelId) {
        reporter.report(ReportMessage.builder()
                .withKey("busbarSectionNotFound")
                .withDefaultMessage("Busbar section ${busbarSectionId} is not found in voltage level ${voltageLevelId}")
                .withValue("busbarSectionId", busbarSectionId)
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void noCouplingDeviceOnSameBusOrBusbarSection(Reporter reporter, String busbarSectionId) {
        reporter.report(ReportMessage.builder()
                .withKey("noCouplingDeviceOnSameBusOrBusbarSection")
                .withDefaultMessage("No coupling device can be created on a same bus or busbar section (${busOrBbsId}).")
                .withValue("busOrBbsId", busbarSectionId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void unexpectedDifferentVoltageLevels(Reporter reporter, String busbarSectionId1, String busbarSectionId2) {
        reporter.report(ReportMessage.builder()
                .withKey("unexpectedDifferentVoltageLevels")
                .withDefaultMessage("${busOrBbsId1} and ${busOrBbsId2} are in two different voltage levels.")
                .withValue("busOrBbsId1", busbarSectionId1)
                .withValue("busOrBbsId2", busbarSectionId2)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void unsupportedVoltageLevelTopologyKind(Reporter reporter, String voltageLevelId, TopologyKind expected, TopologyKind actual) {
        reporter.report(ReportMessage.builder()
                .withKey("unsupportedVoltageLevelTopologyKind")
                .withDefaultMessage("Voltage Level ${voltageLevelId} has an unsupported topology ${actualTopology}. Should be ${expectedTopology}")
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withValue("actualTopology", actual.name())
                .withValue("expectedTopology", expected.name())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void unsupportedIdentifiableType(Reporter reporter, IdentifiableType type, String identifiableId) {
        reporter.report(ReportMessage.builder()
                .withKey("unsupportedIdentifiableType")
                .withDefaultMessage("Unsupported type ${identifiableType} for identifiable ${identifiableId}")
                .withValue(IDENTIFIABLE_TYPE, type.name())
                .withValue(IDENTIFIABLE_ID, identifiableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void unexpectedNullPositionOrder(Reporter reporter, String voltageLevelId) {
        reporter.report(ReportMessage.builder()
                .withKey("unexpectedNullPositionOrder")
                .withDefaultMessage("Position order is null for attachment in node-breaker voltage level ${voltageLevelId}")
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void unexpectedNegativePositionOrder(Reporter reporter, int positionOrder, String voltageLevelId) {
        reporter.report(ReportMessage.builder()
                .withKey("unexpectedNegativePositionOrder")
                .withDefaultMessage("Position order is negative (${positionOrder}) for attachment in voltage level ${voltageLevelId}")
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withValue(POSITION_ORDER, positionOrder)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void unexpectedIdentifiableType(Reporter reporter, Identifiable<?> identifiable) {
        reporter.report(ReportMessage.builder()
                .withKey("unexpectedIdentifiableType")
                .withDefaultMessage("Unexpected type of identifiable ${identifiableId}: ${identifiableType}")
                .withValue(IDENTIFIABLE_ID, identifiable.getId())
                .withValue(IDENTIFIABLE_TYPE, identifiable.getType().name())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void countLowerThanMin(Reporter reporter, String type, int min) {
        reporter.report(ReportMessage.builder()
                .withKey("countLowerThanMin")
                .withDefaultMessage("${type} must be >= ${min}")
                .withValue("type", type)
                .withValue("min", min)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void unexpectedSwitchKindsCount(Reporter reporter, int switchKindsCount, int expectedSwitchKindsCount) {
        reporter.report(ReportMessage.builder()
                .withKey("unexpectedSwitchKindsCount")
                .withDefaultMessage("Unexpected switch kinds count (${switchKindsCount}). Should be ${expectedSwitchKindsCount}")
                .withValue("switchKindsCount", switchKindsCount)
                .withValue("expectedSwitchKindsCount", expectedSwitchKindsCount)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void undefinedSwitchKind(Reporter reporter) {
        reporter.report(ReportMessage.builder()
                .withKey("undefinedSwitchKind")
                .withDefaultMessage("All switch kinds must be defined")
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void wrongSwitchKind(Reporter reporter) {
        reporter.report(ReportMessage.builder()
                .withKey("wrongSwitchKind")
                .withDefaultMessage("Switch kinds must be DISCONNECTOR or BREAKER")
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void undefinedFictitiousSubstationId(Reporter reporter) {
        reporter.report(ReportMessage.builder()
                .withKey("undefinedFictitiousSubstationId")
                .withDefaultMessage("Fictitious substation ID must be defined if a fictitious substation is to be created")
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void undefinedPercent(Reporter reporter) {
        reporter.report(ReportMessage.builder()
                .withKey("undefinedPercent")
                .withDefaultMessage("Percent should not be undefined")
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    private ModificationReports() {
    }

    public static void scalingReport(Reporter reporter, String type, DistributionMode mode, ScalingType scalingType, double asked, double done) {
        reporter.report(ReportMessage.builder()
            .withKey("scalingApplied")
            .withDefaultMessage("Successfully scaled on ${identifiableType} using mode ${mode} and type ${type} with a variation value asked of ${asked}. Variation done is ${done}")
            .withValue(IDENTIFIABLE_TYPE, type)
            .withValue("mode", mode.name())
            .withValue("type", scalingType.name())
            .withValue("asked", asked)
            .withValue("done", done)
            .withSeverity(TypedValue.INFO_SEVERITY)
            .build());
    }

    public static void scalingReport(Reporter reporter, String type, ScalingType scalingType, double asked, double done) {
        reporter.report(ReportMessage.builder()
            .withKey("scalingApplied")
            .withDefaultMessage("Successfully scaled on ${identifiableType} using mode STACKING and type ${type} with a variation value asked of ${asked}. Variation done is ${done}")
            .withValue(IDENTIFIABLE_TYPE, type)
            .withValue("type", scalingType.name())
            .withValue("asked", asked)
            .withValue("done", done)
            .withSeverity(TypedValue.INFO_SEVERITY)
            .build());
    }

    public static void connectableConnectionReport(Reporter reporter, Connectable<?> connectable, boolean connectionSuccessful) {
        String defaultMessage = connectionSuccessful ?
            "Connectable ${connectable} has been connected." :
            "Connectable ${connectable} has NOT been connected.";
        String key = connectionSuccessful ? "connectableConnected" : "connectableNotConnected";
        reporter.report(ReportMessage.builder()
            .withKey(key)
            .withDefaultMessage(defaultMessage)
            .withValue("connectable", connectable.getId())
            .withSeverity(TypedValue.INFO_SEVERITY)
            .build());
    }

    public static void connectableDisconnectionReport(Reporter reporter, Connectable<?> connectable, boolean disconnectionSuccessful, boolean isPlanned) {
        String defaultMessage = disconnectionSuccessful ?
            "Connectable ${connectable} has been disconnected" :
            "Connectable ${connectable} has NOT been disconnected";
        defaultMessage += isPlanned ? " (planned disconnection)." : " (unplanned disconnection).";
        String key = isPlanned ? "planned" : "unplanned";
        key += disconnectionSuccessful ? "ConnectableDisconnected" : "ConnectableNotDisconnected";
        reporter.report(ReportMessage.builder()
            .withKey(key)
            .withDefaultMessage(defaultMessage)
            .withValue("connectable", connectable.getId())
            .withSeverity(TypedValue.INFO_SEVERITY)
            .build());
    }
}
