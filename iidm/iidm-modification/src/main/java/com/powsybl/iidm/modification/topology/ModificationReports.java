/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;
import com.powsybl.iidm.network.*;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
final class ModificationReports {

    private static final String VOLTAGE_LEVEL_ID = "voltageLevelId";
    private static final String LINE_ID = "lineId";
    private static final String BBS_ID = "bbsId";
    private static final String CONNECTABLE_ID = "connectableId";

    // INFO
    static void createdConnectable(Reporter reporter, Connectable<?> connectable) {
        reporter.report(Report.builder()
                .withKey("connectableCreated")
                .withDefaultMessage("New connectable ${connectableId} of type ${connectableType} created.")
                .withValue(CONNECTABLE_ID, connectable.getId())
                .withValue("connectableType", connectable.getType().name())
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    static void createdNodeBreakerFeederBay(Reporter reporter, String voltageLevelId, String bbsId, Connectable<?> connectable, int parallelBbsNumber) {
        reporter.report(Report.builder()
                .withKey("newConnectableAdded")
                .withDefaultMessage("New feeder bay associated to ${connectableId} of type ${connectableType} was created and connected to voltage level ${voltageLevelId} on busbar section ${bbsId} with a closed disconnector" +
                        "and on ${parallelBbsNumber} parallel busbar sections with an open disconnector.")
                .withValue(CONNECTABLE_ID, connectable.getId())
                .withValue("connectableType", connectable.getType().toString())
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withValue(BBS_ID, bbsId)
                .withValue("parallelBbsNumber", parallelBbsNumber)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    static void removedLineReport(Reporter reporter, String lineId) {
        reporter.report(Report.builder()
                .withKey("lineRemoved")
                .withDefaultMessage("Line ${lineId} removed")
                .withValue(LINE_ID, lineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    static void createdLineReport(Reporter reporter, String lineId) {
        reporter.report(Report.builder()
                .withKey("lineCreated")
                .withDefaultMessage("Line ${lineId} created")
                .withValue(LINE_ID, lineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    static void voltageLevelRemovedReport(Reporter reporter, String vlId) {
        reporter.report(Report.builder()
                .withKey("voltageLevelRemoved")
                .withDefaultMessage("Voltage level ${vlId} removed")
                .withValue("vlId", vlId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    static void substationRemovedReport(Reporter reporter, String substationId) {
        reporter.report(Report.builder()
                .withKey("substationRemoved")
                .withDefaultMessage("Substation ${substationId} removed")
                .withValue("substationId", substationId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    static void newCouplingDeviceAddedReport(Reporter reporter, String voltageLevelId, String bbsId1, String bbsId2, int nbOpenDisconnectors) {
        reporter.report(Report.builder()
                .withKey("newCouplingDeviceAdded")
                .withDefaultMessage("New coupling device was created on voltage level ${voltageLevelId}. It connects busbar sections ${bbsId1} and ${bbsId2} with closed disconnectors" +
                        "and ${nbOpenDisconnectors} were created on parallel busbar sections.")
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withValue("bbsId1", bbsId1)
                .withValue("bbsId2", bbsId2)
                .withValue("nbOpenDisconnectors", nbOpenDisconnectors)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    static void createdNewSymmetricalTopology(Reporter reporter, String voltageLevelId, int busbarCount, int sectionCount) {
        reporter.report(Report.builder()
                .withKey("SymmetricalTopologyCreated")
                .withDefaultMessage("New symmetrical topology in voltage level ${voltageLevelId}: creation of ${busbarCount} busbar(s) with ${sectionCount} section(s) each.")
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withValue("busbarCount", busbarCount)
                .withValue("sectionCount", sectionCount)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public static void removedSwitchReport(Reporter reporter, String switchId) {
        reporter.report(Report.builder()
                .withKey("SwitchRemoved")
                .withDefaultMessage("Switch ${switchId} removed")
                .withValue("switchId", switchId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    static void removedInternalConnectionReport(Reporter reporter, int node1, int node2) {
        reporter.report(Report.builder()
                .withKey("InternalConnectionRemoved")
                .withDefaultMessage("Internal connection between ${node1} and ${node2} removed")
                .withValue("node1", node1)
                .withValue("node2", node2)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    static void removedConnectableReport(Reporter reporter, String connectableId) {
        reporter.report(Report.builder()
                .withKey("ConnectableRemoved")
                .withDefaultMessage("Connectable ${connectableId} removed")
                .withValue(CONNECTABLE_ID, connectableId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    static void removeFeederBayAborted(Reporter reporter, String connectableId, int node, String otherConnectableId) {
        reporter.report(Report.builder()
                .withKey("RemoveFeederBayAborted")
                .withDefaultMessage("Remove feeder bay of ${connectableId} cannot go further node ${node}, as it is connected to ${otherConnectableId}")
                .withValue(CONNECTABLE_ID, connectableId)
                .withValue("node", node)
                .withValue("otherConnectableId", otherConnectableId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    // WARN
    static void ignoredPositionOrder(Reporter reporter, int positionOrder, VoltageLevel voltageLevel) {
        reporter.report(Report.builder()
                .withKey("ignoredPositionOrder")
                .withDefaultMessage("Voltage level ${voltageLevelId} is BUS_BREAKER. Position order ${positionOrder} is ignored.")
                .withValue(VOLTAGE_LEVEL_ID, voltageLevel.getId())
                .withValue("positionOrder", positionOrder)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    static void noBusbarSectionPositionExtensionReport(Reporter reporter, BusbarSection bbs) {
        reporter.report(Report.builder()
                .withKey("noBusbarSectionPositionExtension")
                .withDefaultMessage("No busbar section position extension found on ${bbsId}, only one disconnector is created.")
                .withValue(BBS_ID, bbs.getId())
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    static void positionOrderAlreadyTakenReport(Reporter reporter, int positionOrder) {
        reporter.report(Report.builder()
                .withKey("positionOrderAlreadyTaken")
                .withDefaultMessage("PositionOrder ${positionOrder} already taken. No position extension created.")
                .withValue("positionOrder", positionOrder)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    static void noConnectablePositionExtension(Reporter reporter, VoltageLevel voltageLevel) {
        reporter.report(Report.builder()
                .withKey("noConnectablePositionExtensions")
                .withDefaultMessage("No extensions found on voltageLevel ${voltageLevel}. The extension on the connectable is not created.")
                .withValue("voltageLevel", voltageLevel.getId())
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    static void voltageLevelRemovingEquipmentsLeftReport(Reporter reporter, String vlId) {
        reporter.report(Report.builder()
                .withKey("voltageLevelRemovingEquipmentsLeft")
                .withDefaultMessage("Voltage level ${vlId} still contains equipments")
                .withValue("vlId", vlId)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    // ERROR
    static void notFoundIdentifiableReport(Reporter reporter, String identifiableId) {
        reporter.report(Report.builder()
                .withKey("notFoundIdentifiable")
                .withDefaultMessage("Identifiable ${busbarSectionId} not found")
                .withValue("identifiableId", identifiableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void notFoundBusbarSectionReport(Reporter reporter, String bbsId) {
        reporter.report(Report.builder()
                .withKey("notFoundBusbarSection")
                .withDefaultMessage("Busbar section ${busbarSectionId} not found")
                .withValue("busbarSectionId", bbsId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void networkMismatchReport(Reporter reporter, String injectionId, IdentifiableType identifiableType) {
        reporter.report(Report.builder()
                .withKey("networkMismatch")
                .withDefaultMessage("Network given in parameters and in injectionAdder are different. Injection '${injectionId}' of type {identifiableType} was added then removed")
                .withValue("injectionId", injectionId)
                .withValue("identifiableType", identifiableType.toString())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void connectableNotSupported(Reporter reporter, Connectable<?> connectable) {
        reporter.report(Report.builder()
                .withKey("connectableNotSupported")
                .withDefaultMessage("Given connectable not supported: ${connectableClassName}.")
                .withValue("connectableClassName", connectable.getClass().getName())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void connectableNotInVoltageLevel(Reporter reporter, Connectable<?> connectable, VoltageLevel voltageLevel) {
        reporter.report(Report.builder()
                .withKey("connectableNotInVoltageLevel")
                .withDefaultMessage("Given connectable ${connectableId} not in voltageLevel ${voltageLevelId}")
                .withValue(CONNECTABLE_ID, connectable.getId())
                .withValue(VOLTAGE_LEVEL_ID, voltageLevel.getId())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void notFoundLineReport(Reporter reporter, String lineId) {
        reporter.report(Report.builder()
                .withKey("lineNotFound")
                .withDefaultMessage("Line ${lineId} is not found")
                .withValue(LINE_ID, lineId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void notFoundConnectableReport(Reporter reporter, String connectableId) {
        reporter.report(Report.builder()
                .withKey("connectableNotFound")
                .withDefaultMessage("Connectable ${connectableId} is not found")
                .withValue(CONNECTABLE_ID, connectableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void removeFeederBayBusbarSectionReport(Reporter reporter, String busbarSectionConnectableId) {
        reporter.report(Report.builder()
                .withKey("removeBayBusbarSectionConnectable")
                .withDefaultMessage("Cannot remove feeder bay for connectable ${connectableId}, as it is a busbarSection")
                .withValue(CONNECTABLE_ID, busbarSectionConnectableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void noVoltageLevelInCommonReport(Reporter reporter, String line1Id, String line2Id) {
        reporter.report(Report.builder()
                .withKey("noVoltageLevelInCommon")
                .withDefaultMessage("Lines ${line1Id} and ${line2Id} should have one and only one voltage level in common at their extremities")
                .withValue("line1Id", line1Id)
                .withValue("line2Id", line2Id)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void notFoundVoltageLevelReport(Reporter reporter, String voltageLevelId) {
        reporter.report(Report.builder()
                .withKey("voltageLevelNotFound")
                .withDefaultMessage("Voltage level ${voltageLevelId} is not found")
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void noTeePointAndOrTappedVoltageLevelReport(Reporter reporter, String line1Id, String line2Id, String line3Id) {
        reporter.report(Report.builder()
                .withKey("noTeePointAndOrTappedVoltageLevel")
                .withDefaultMessage("Unable to find the tee point and the tapped voltage level from lines ${line1Id}, ${line2Id} and ${line3Id}")
                .withValue("line1Id", line1Id)
                .withValue("line2Id", line2Id)
                .withValue("line3Id", line3Id)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void notFoundBusInVoltageLevelReport(Reporter reporter, String busId, String voltageLevelId) {
        reporter.report(Report.builder()
                .withKey("busNotFound")
                .withDefaultMessage("Bus ${busId} is not found in voltage level ${voltageLevelId}")
                .withValue("busId", busId)
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void notFoundBusbarSectionInVoltageLevelReport(Reporter reporter, String busbarSectionId, String voltageLevelId) {
        reporter.report(Report.builder()
                .withKey("busbarSectionNotFound")
                .withDefaultMessage("Busbar section ${busbarSectionId} is not found in voltage level ${voltageLevelId}")
                .withValue("busbarSectionId", busbarSectionId)
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void noCouplingDeviceOnSameBusbarSection(Reporter reporter, String busbarSectionId) {
        reporter.report(Report.builder()
                .withKey("noCouplingDeviceOnSameBusbarSection")
                .withDefaultMessage("No coupling device can be created on a same busbar section (${bbsId}).")
                .withValue(BBS_ID, busbarSectionId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void busbarsInDifferentVoltageLevels(Reporter reporter, String busbarSectionId1, String busbarSectionId2) {
        reporter.report(Report.builder()
                .withKey("busbarsInDifferentVoltageLevels")
                .withDefaultMessage("Busbar sections ${busbarSectionId1} and ${busbarSectionId2} are in two different voltage levels.")
                .withValue("busbarSectionId1", busbarSectionId1)
                .withValue("busbarSectionId2", busbarSectionId2)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void unsupportedVoltageLevelTopologyKind(Reporter reporter, String voltageLevelId, TopologyKind expected, TopologyKind actual) {
        reporter.report(Report.builder()
                .withKey("unsupportedVoltageLevelTopologyKind")
                .withDefaultMessage("Voltage Level ${voltageLevelId} has an unsupported topology ${actualTopology}. Should be ${expectedTopology}")
                .withValue(VOLTAGE_LEVEL_ID, voltageLevelId)
                .withValue("actualTopology", actual.name())
                .withValue("expectedTopology", expected.name())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void unsupportedIdentifiableType(Reporter reporter, IdentifiableType type, String identifiableId) {
        reporter.report(Report.builder()
                .withKey("unsupportedIdentifiableType")
                .withDefaultMessage("Unsupported type ${identifiableType} for identifiable ${identifiableId}")
                .withValue("identifiableType", type.name())
                .withValue("identifiableId", identifiableId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void unexpectedNullPositionOrder(Reporter reporter, VoltageLevel voltageLevel) {
        reporter.report(Report.builder()
                .withKey("unexpectedNullPositionOrder")
                .withDefaultMessage("Position order is null for attachment in node-breaker voltage level ${voltageLevelId}")
                .withValue(VOLTAGE_LEVEL_ID, voltageLevel.getId())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void connectableHasNoOrderReport(Reporter reporter, Connectable<?> connectable) {
        reporter.report(Report.builder()
                .withKey("connectableHasNoOrders")
                .withDefaultMessage("Given connectable {connectableId} has no orders")
                .withValue("connectableId", connectable.getId())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    private ModificationReports() {
    }
}
