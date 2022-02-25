/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.HashMap;
import java.util.Map;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 * @author Marcos de Miguel <demiguelm at aia.es>
 */

public class RegulatingControlMappingForStaticVarCompensators {

    RegulatingControlMappingForStaticVarCompensators(RegulatingControlMapping parent, Context context) {
        this.parent = parent;
        this.context = context;
        mapping = new HashMap<>();
    }

    public static void initialize(StaticVarCompensatorAdder adder) {
        adder.setRegulationMode(StaticVarCompensator.RegulationMode.OFF);
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
            context.missing("Regulating control Id not defined");
            setDefaultRegulatingControl(rc, svc);
            return;
        }

        RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            setDefaultRegulatingControl(rc, svc);
            return;
        }

        boolean okSet = false;
        if (RegulatingControlMapping.isControlModeVoltage(control.mode.toLowerCase())) {
            setRegulatingControlVoltage(rc, control, svc);
            okSet = true;
        } else if (RegulatingControlMapping.isControlModeReactivePower(control.mode.toLowerCase())) {
            setRegulatingControlReactivePower(rc, control, svc);
            okSet = true;
        } else {
            context.ignored(control.mode, "Unsupported regulation mode for static var compensator " + svc.getId());
        }

        control.setCorrectlySet(okSet);
    }

    private void setRegulatingControlReactivePower(CgmesRegulatingControlForStaticVarCompensator rc, RegulatingControl control, StaticVarCompensator svc) {

        Terminal regulatingTerminal = parent.getRegulatingTerminal(control.cgmesTerminal);

        StaticVarCompensator.RegulationMode regulationMode = StaticVarCompensator.RegulationMode.OFF;
        if (control.enabled && rc.controlEnabled) {
            regulationMode = StaticVarCompensator.RegulationMode.REACTIVE_POWER;
        }
        regulationMode = parent.deactivateControlIfRegulatingTerminalIsUnset(getMessageId(svc.getId(), rc.regulatingControlId), regulatingTerminal, regulationMode);

        svc.setRegulatingTerminal(regulatingTerminal)
            .setVoltageSetpoint(Double.NaN)
            .setReactivePowerSetpoint(control.targetValue)
            .setRegulationMode(regulationMode);

        svc.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl", rc.regulatingControlId);
    }

    private void setRegulatingControlVoltage(CgmesRegulatingControlForStaticVarCompensator rc, RegulatingControl control, StaticVarCompensator svc) {

        Terminal regulatingTerminal = parent.getRegulatingTerminal(control.cgmesTerminal);

        StaticVarCompensator.RegulationMode regulationMode = StaticVarCompensator.RegulationMode.OFF;
        if (control.enabled && rc.controlEnabled) {
            regulationMode = StaticVarCompensator.RegulationMode.VOLTAGE;
        }
        regulationMode = parent.deactivateControlIfRegulatingTerminalIsUnset(getMessageId(svc.getId(), rc.regulatingControlId), regulatingTerminal, regulationMode);

        svc.setRegulatingTerminal(regulatingTerminal)
            .setVoltageSetpoint(control.targetValue)
            .setReactivePowerSetpoint(Double.NaN)
            .setRegulationMode(regulationMode);

        svc.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl", rc.regulatingControlId);
    }

    private void setDefaultRegulatingControl(CgmesRegulatingControlForStaticVarCompensator rc, StaticVarCompensator svc) {

        if (RegulatingControlMapping.isControlModeVoltage(rc.defaultRegulationMode.toLowerCase())) {
            setDefaultRegulatingControlVoltage(rc, svc);
        } else if (isControlModeReactivePower(rc.defaultRegulationMode.toLowerCase())) {
            setDefaultRegulatingControlReactivePower(rc, svc);
        } else {
            context.fixed("SVCControlMode", () -> String.format("Invalid control mode for static var compensator %s. Regulating control is disabled", svc.getId()));
            svc.setRegulationMode(StaticVarCompensator.RegulationMode.OFF);
        }
    }

    private void setDefaultRegulatingControlVoltage(CgmesRegulatingControlForStaticVarCompensator rc, StaticVarCompensator svc) {

        StaticVarCompensator.RegulationMode regulationMode = StaticVarCompensator.RegulationMode.OFF;
        if (rc.controlEnabled) {
            regulationMode = StaticVarCompensator.RegulationMode.VOLTAGE;
        }

        svc.setRegulatingTerminal(svc.getTerminal());
        svc.setVoltageSetpoint(rc.defaultTargetVoltage);
        svc.setReactivePowerSetpoint(Double.NaN);
        svc.setRegulationMode(regulationMode);
    }

    private void setDefaultRegulatingControlReactivePower(CgmesRegulatingControlForStaticVarCompensator rc, StaticVarCompensator svc) {

        StaticVarCompensator.RegulationMode regulationMode = StaticVarCompensator.RegulationMode.OFF;
        if (rc.controlEnabled) {
            regulationMode = StaticVarCompensator.RegulationMode.REACTIVE_POWER;
        }

        svc.setRegulatingTerminal(svc.getTerminal());
        svc.setVoltageSetpoint(Double.NaN);
        svc.setReactivePowerSetpoint(rc.defaultTargetReactivePower);
        svc.setRegulationMode(regulationMode);
    }

    private static boolean isControlModeReactivePower(String controlMode) {
        return controlMode != null && controlMode.endsWith("reactivepower");
    }

    private static String getMessageId(String id, String controlId) {
        return String.format("StaticVarCompensator: %s ControlId: %s", id, controlId);
    }

    private static class CgmesRegulatingControlForStaticVarCompensator {
        String regulatingControlId;
        boolean controlEnabled;
        double defaultTargetVoltage;
        double defaultTargetReactivePower;
        String defaultRegulationMode;
    }

    private final RegulatingControlMapping parent;
    private final Map<String, CgmesRegulatingControlForStaticVarCompensator> mapping;
    private final Context context;
}
