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
    static String voltageLevelIdString = "voltageLevelId";

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

    static void injectionPositionOrderAlreadyTakenReport(Reporter reporter, int injectionPositionOrder) {
        reporter.report(Report.builder()
                .withKey("injectionPositionOrderAlreadyTaken")
                .withDefaultMessage("InjectionPositionOrder ${injectionPositionOrder} already taken.")
                .withValue("injectionPositionOrder", injectionPositionOrder)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void newInjectionAddedReport(Reporter reporter, String voltageLevelId, String bbsId, Injection<?> injection, int parallelBbsNumber) {
        reporter.report(Report.builder()
                .withKey("newInjectionAdded")
                .withDefaultMessage("New feeder bay ${injectionId} of type ${injectionType} was created and connected to voltage level ${voltageLevelId} on busbar section ${bbsId} with a closed disconnector" +
                        "and on ${parallelBbsNumber} parallel busbar sections with an open disconnector.")
                .withValue("injectionId", injection.getId())
                .withValue("injectionType", injection.getType().toString())
                .withValue(voltageLevelIdString, voltageLevelId)
                .withValue("bbsId", bbsId)
                .withValue("parallelBbsNumber", parallelBbsNumber)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    static void noBusbarSectionPositionExtensionReport(Reporter reporter, BusbarSection bbs) {
        reporter.report(Report.builder()
                .withKey("noBusbarSectionPositionExtension")
                .withDefaultMessage("No busbar section position extension found on ${bbsId}, only one disconnector is created.")
                .withValue("bbsId", bbs.getId())
                .withSeverity(TypedValue.WARN_SEVERITY)
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
                .withValue("connectableId", connectable.getId())
                .withValue("voltageLevelId", voltageLevel.getId())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    static void noConnectablePositionExtension(Reporter reporter, VoltageLevel voltageLevel) {
        reporter.report(Report.builder()
                .withKey("noConnectablePositionExtensions")
                .withDefaultMessage("No extensions found on voltageLevel ${voltageLevel}. The extension on the injection is not created.")
                .withValue("voltageLevel", voltageLevel.getId())
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    static void notFoundLineReport(Reporter reporter, String lineId) {
        reporter.report(Report.builder()
                .withKey("lineNotFound")
                .withDefaultMessage("Line ${lineId} is not found")
                .withValue("lineId", lineId)
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

    static void removedLineReport(Reporter reporter, String lineId) {
        reporter.report(Report.builder()
                .withKey("lineRemoved")
                .withDefaultMessage("Line ${lineId} removed")
                .withValue("lineId", lineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    static void createdLineReport(Reporter reporter, String lineId) {
        reporter.report(Report.builder()
                .withKey("lineCreated")
                .withDefaultMessage("Line ${lineId} created")
                .withValue("lineId", lineId)
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

    static void noAttachmentPointAndOrAttachedVoltageLevelReport(Reporter reporter, String lineAZId, String lineBZId, String lineCZId) {
        reporter.report(Report.builder()
                .withKey("noAttachmentPointAndOrAttachedVoltageLevel")
                .withDefaultMessage("Unable to find the attachment point and the attached voltage level from lines ${lineAZId}, ${lineBZId} and ${lineCZId}")
                .withValue("lineAZId", lineAZId)
                .withValue("lineBZId", lineBZId)
                .withValue("lineCZId", lineCZId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    private ModificationReports() {
    }
}
