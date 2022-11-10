/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Substation;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public final class CgmesReports {

    private CgmesReports() {
    }

    // INFO
    public static void importedCgmesNetworkReport(Reporter reporter, String networkId) {
        reporter.report(Report.builder()
                .withKey("importedCgmesNetwork")
                .withDefaultMessage("CGMES network ${networkId} is imported.")
                .withValue("networkId", networkId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    // WARN
    public static void badVoltageTargetValueRegulatingControlReport(Reporter reporter, String eqId, double targetValue) {
        reporter.report(Report.builder()
                .withKey("badVoltageTargetValueRegulatingControl")
                .withDefaultMessage("Equipment ${equipmentId} has a regulating control with bad target value for voltage: ${targetValue}")
                .withValue("equipmentId", eqId)
                .withTypedValue("targetValue", targetValue, TypedValue.VOLTAGE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    public static void badTargetDeadbandRegulatingControlReport(Reporter reporter, String eqId, double targetDeadband) {
        reporter.report(Report.builder()
                .withKey("badTargetDeadbandRegulatingControl")
                .withDefaultMessage("Equipment ${equipmentId} has a regulating control with bad target deadband: ${targetDeadband}")
                .withValue("equipmentId", eqId)
                .withTypedValue("targetDeadband", targetDeadband, TypedValue.VOLTAGE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    public static void invalidAngleVoltageBusReport(Reporter reporter, Bus bus, String nodeId, double v, double angle) {
        reporter.report(Report.builder()
                .withKey("invalidAngleVoltageBus")
                .withDefaultMessage("Node ${nodeId} in substation ${substation}, voltageLevel ${voltageLevel}, bus ${bus} has invalid value for voltage and/or angle. Voltage magnitude is ${voltage}, angle is ${angle}")
                .withValue("substation", bus.getVoltageLevel().getSubstation().map(Substation::getNameOrId).orElse("unknown"))
                .withValue("voltageLevel", bus.getVoltageLevel().getNameOrId())
                .withValue("bus", bus.getId())
                .withValue("nodeId", nodeId)
                .withTypedValue("voltage", v, TypedValue.VOLTAGE)
                .withTypedValue("angle", angle, TypedValue.ANGLE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    public static void invalidAngleVoltageNodeReport(Reporter reporter, String nodeId, double v, double angle) {
        reporter.report(Report.builder()
                .withKey("invalidAngleVoltageNode")
                .withDefaultMessage("Node ${nodeId} has invalid value for voltage and/or angle. Voltage magnitude is ${voltage}, angle is ${angle}")
                .withValue("nodeId", nodeId)
                .withTypedValue("voltage", v, TypedValue.VOLTAGE)
                .withTypedValue("angle", angle, TypedValue.ANGLE)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .build());
    }

    // ERROR
    public static void inconsistentProfilesTPRequiredReport(Reporter reporter, String networkId) {
        reporter.report(Report.builder()
                .withKey("inconsistentProfilesTPRequired")
                .withDefaultMessage("Network contains node/breaker ${networkId} information. References to Topological Nodes in SSH/SV files will not be valid if TP is not exported.")
                .withValue("networkId", networkId)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
    }
}
