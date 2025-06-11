/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */

public class RegulatingControlMappingForStaticVarCompensators {

    RegulatingControlMappingForStaticVarCompensators(RegulatingControlMapping parent, Context context) {
        this.parent = parent;
        this.context = context;
        mapping = new HashMap<>();
    }

    public static void initialize(StaticVarCompensatorAdder adder) {
        adder.setRegulating(false);
    }

    public void add(String iidmId, PropertyBag sm) {
        String rcId = RegulatingControlMapping.getRegulatingControlId(sm);
        boolean controlEnabled = sm.asBoolean("controlEnabled", false);
        double defaultTargetVoltage = sm.asDouble("voltageSetPoint");
        double defaultTargetReactivePower = sm.asDouble("q");
        String defaultRegulationMode = sm.getId("controlMode");

        if (mapping.containsKey(iidmId)) {
            throw new CgmesModelException(
                    "StaticVarCompensator already added, IIDM StaticVarCompensator Id: " + iidmId);
        }

        CgmesRegulatingControlForStaticVarCompensator rc = new CgmesRegulatingControlForStaticVarCompensator();
        rc.regulatingControlId = rcId;
        rc.controlEnabled = controlEnabled;
        rc.defaultTargetVoltage = defaultTargetVoltage;
        rc.defaultTargetReactivePower = defaultTargetReactivePower;
        rc.defaultRegulationMode = defaultRegulationMode;

        mapping.put(iidmId, rc);
    }

    void applyRegulatingControls(Network network) {
        network.getStaticVarCompensatorStream().forEach(this::apply);
    }

    private void apply(StaticVarCompensator svc) {
        CgmesRegulatingControlForStaticVarCompensator rd = mapping.get(svc.getId());
        apply(svc, rd);
    }

    private void apply(StaticVarCompensator svc, CgmesRegulatingControlForStaticVarCompensator rc) {
        if (rc == null) {
            return;
        }

        String controlId = rc.regulatingControlId;
        if (controlId == null) {
            LOG.trace("Regulating control Id not present for static var compensator {}", svc.getId());
            setDefaultRegulatingControl(rc, svc, false, null);
            return;
        }

        RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            setDefaultRegulatingControl(rc, svc, false, null);
            return;
        }

        control.setCorrectlySet(setRegulatingControl(rc, control, svc));
    }

    private boolean setRegulatingControl(CgmesRegulatingControlForStaticVarCompensator rc, RegulatingControl control, StaticVarCompensator svc) {
        // Always save the original RC id,
        // even if we can not complete setting all the regulation data
        svc.setProperty(Conversion.PROPERTY_REGULATING_CONTROL, rc.regulatingControlId);

        // Take default terminal if it has not been defined in CGMES files (it is never null)
        Terminal regulatingTerminal = RegulatingTerminalMapper
                .mapForVoltageControl(control.cgmesTerminal, context)
                .orElse(svc.getTerminal());

        boolean okSet = false;
        if (!control.enabled && rc.controlEnabled) {
            context.fixed("SVCControlEnabledStatus", () -> String.format("Regulating control of %s is disabled but controlEnabled property is set to true." +
                    "Equipment and regulating control properties are used to set local default regulation if local default regulation is reactive power. Else, regulation is disabled.", svc.getId()));
            setDefaultRegulatingControl(rc, svc, true, control);
            return false;
        }

        StaticVarCompensator.RegulationMode regulationMode = StaticVarCompensator.RegulationMode.VOLTAGE;
        double targetVoltage = Double.NaN;
        double targetReactivePower = Double.NaN;
        boolean regulating = control.enabled && rc.controlEnabled;

        if (RegulatingControlMapping.isControlModeVoltage(control.mode.toLowerCase())) {
            targetVoltage = control.targetValue;
            okSet = true;
        } else if (isControlModeReactivePower(control.mode.toLowerCase())) {
            regulationMode = StaticVarCompensator.RegulationMode.REACTIVE_POWER;
            targetReactivePower = control.targetValue;
            okSet = true;
        } else {
            context.fixed("SVCControlMode", () -> String.format("Invalid control mode for static var compensator %s. Regulating control is disabled and regulationMode set to VOLTAGE", svc.getId()));
            regulating = false;
        }
        svc.setVoltageSetpoint(targetVoltage);
        svc.setReactivePowerSetpoint(targetReactivePower);
        svc.setRegulatingTerminal(regulatingTerminal);
        svc.setRegulationMode(regulationMode);
        svc.setRegulating(regulating);
        return okSet;
    }

    private boolean isValidVoltageFromRegulatingControl(RegulatingControl control) {
        return control != null
                && RegulatingControlMapping.isControlModeVoltage(control.mode)
                && Double.isFinite(control.targetValue)
                && control.targetValue > 0;
    }

    private boolean isValidReactivePowerFromRegulatingControl(RegulatingControl control) {
        return control != null
                && RegulatingControlMapping.isControlModeReactivePower(control.mode)
                && Double.isFinite(control.targetValue);
    }

    private void setDefaultRegulatingControl(CgmesRegulatingControlForStaticVarCompensator rc, StaticVarCompensator svc, boolean onlyReactivePowerReg, RegulatingControl control) {
        if (!defaultRegulatingControlIsWellDefined(rc)) {
            return;
        }

        StaticVarCompensator.RegulationMode regulationMode = StaticVarCompensator.RegulationMode.VOLTAGE;
        double targetVoltage = isValidVoltageFromRegulatingControl(control) ? control.targetValue : rc.defaultTargetVoltage;
        double targetReactivePower = Double.NaN;
        boolean regulating = rc.controlEnabled;

        // Before using the default target value,
        // We try to keep data from original regulating control if available.
        // Even if the regulating control was disabled it may have defined a valid target value
        if (isDefaultRegulationModeVoltage(rc)) {
            if (onlyReactivePowerReg) {
                regulating = false;
            }
        } else if (isControlModeReactivePower(rc.defaultRegulationMode.toLowerCase())) {
            regulationMode = StaticVarCompensator.RegulationMode.REACTIVE_POWER;
            targetReactivePower = isValidReactivePowerFromRegulatingControl(control) ? control.targetValue : rc.defaultTargetReactivePower;
        } else {
            context.fixed("SVCControlMode", () -> String.format("Invalid control mode for static var compensator %s. Regulating control is disabled and regulationMode set to VOLTAGE", svc.getId()));
            regulating = false;
        }
        svc.setVoltageSetpoint(targetVoltage);
        svc.setReactivePowerSetpoint(targetReactivePower);
        svc.setRegulationMode(regulationMode);
        svc.setRegulating(regulating);
    }

    // SVCControlMode and voltageSetPoint are optional in Cgmes 3.0
    private static boolean defaultRegulatingControlIsWellDefined(CgmesRegulatingControlForStaticVarCompensator rc) {
        return rc.defaultRegulationMode != null
                && (isDefaultRegulationModeVoltage(rc) && rc.defaultTargetVoltage > 0.0
                || isControlModeReactivePower(rc.defaultRegulationMode.toLowerCase()) && Double.isFinite(rc.defaultTargetReactivePower));
    }

    private static boolean isDefaultRegulationModeVoltage(CgmesRegulatingControlForStaticVarCompensator rc) {
        return rc.defaultRegulationMode != null
                && RegulatingControlMapping.isControlModeVoltage(rc.defaultRegulationMode.toLowerCase());
    }

    private static boolean isControlModeReactivePower(String controlMode) {
        return controlMode != null && controlMode.endsWith("reactivepower");
    }

    private static final class CgmesRegulatingControlForStaticVarCompensator {
        String regulatingControlId;
        boolean controlEnabled;
        double defaultTargetVoltage;
        double defaultTargetReactivePower;
        String defaultRegulationMode;
    }

    private final RegulatingControlMapping parent;
    private final Map<String, CgmesRegulatingControlForStaticVarCompensator> mapping;
    private final Context context;

    private static final Logger LOG = LoggerFactory.getLogger(RegulatingControlMappingForStaticVarCompensators.class);
}
