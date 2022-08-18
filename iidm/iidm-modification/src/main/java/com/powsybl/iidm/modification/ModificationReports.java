/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Injection;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public final class ModificationReports {
    static String voltageLevelIdString = "voltageLevelId";

    public static void notFoundBusbarSectionReport(Reporter reporter, String bbsId) {
        reporter.report(Report.builder()
                .withKey("notFoundBusbarSection")
                .withDefaultMessage("Busbar section ${busbarSectionId} not found")
                .withValue("busbarSectionId", bbsId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void notNodeBreakerVoltageLevelReport(Reporter reporter, String voltageLevelId) {
        reporter.report(Report.builder()
                .withKey("notNodeBreakerVoltageLevel")
                .withDefaultMessage("Voltage level ${voltageLevelId} is not in node/breaker")
                .withValue(voltageLevelIdString, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void noBusbarSectionInVoltageLevelReport(Reporter reporter, String voltageLevelId) {
        reporter.report(Report.builder()
                .withKey("noBusbarSectionInVoltageLevel")
                .withDefaultMessage("Voltage level ${voltageLevelId} has no busbar section.")
                .withValue(voltageLevelIdString, voltageLevelId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void networkMismatchReport(Reporter reporter, String injectionId, IdentifiableType identifiableType) {
        reporter.report(Report.builder()
                .withKey("networkMismatch")
                .withDefaultMessage("Network given in parameters and in injectionAdder are different. Injection '${injectionId}' of type {identifiableType} was added then removed")
                .withValue("injectionId", injectionId)
                .withValue("identifiableType", identifiableType.toString())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void injectionPositionOrderAlreadyTakenReport(Reporter reporter, int injectionPositionOrder) {
        reporter.report(Report.builder()
                .withKey("injectionPositionOrderAlreadyTaken")
                .withDefaultMessage("InjectionPositionOrder ${injectionPositionOrder} already taken.")
                .withValue("injectionPositionOrder", injectionPositionOrder)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }

    public static void newInjectionAddedReport(Reporter reporter, String voltageLevelId, String bbsId, Injection<?> injection, int parallelBbsNumber) {
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

    public static void noBusbarSectionPositionExtensionReport(Reporter reporter, BusbarSection bbs) {
        reporter.report(Report.builder()
                .withKey("noBusbarSectionPositionExtension")
                .withDefaultMessage("No busbar section position extension found on ${bbsId}, only one disconnector is created.")
                .withValue("bbsId", bbs.getId())
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    private ModificationReports() {
    }
}
