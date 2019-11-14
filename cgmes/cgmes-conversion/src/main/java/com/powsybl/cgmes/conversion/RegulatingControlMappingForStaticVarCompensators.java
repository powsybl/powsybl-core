/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 * @author Marcos de Miguel <demiguelm at aia.es>
 */

public class RegulatingControlMappingForStaticVarCompensators {

    public RegulatingControlMappingForStaticVarCompensators(RegulatingControlMapping parent) {
        this.parent = parent;
        this.context = parent.context();
        mapping = new HashMap<>();
    }

    public static void initialize(StaticVarCompensatorAdder adder) {
        adder.setRegulationMode(StaticVarCompensator.RegulationMode.OFF);
    }

    public void add(String iidmId, PropertyBag sm) {
        String rcId = parent.getRegulatingControlId(sm);
        boolean controlEnabledProperty = sm.asBoolean("controlEnabled", false);
        double defaultTargetVoltage = sm.asDouble("voltageSetPoint");
        double defaultTargetReactivePower = sm.asDouble("q");
        String defaultRegulationMode = sm.getId("controlMode");

        if (mapping.containsKey(iidmId)) {
            throw new CgmesModelException(
                "StaticVarCompensator already added, IIDM StaticVarCompensator Id: " + iidmId);
        }

        CgmesRegulatingControlForStaticVarCompensator rc = new CgmesRegulatingControlForStaticVarCompensator();
        rc.regulatingControlId = rcId;
        rc.controlEnabledProperty = controlEnabledProperty;
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
        if (!rc.controlEnabledProperty) {
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
        if (!control.enabled) {
            return;
        }

        boolean okSet = false;
        if (context.terminalMapping().areAssociated(control.cgmesTerminal, control.topologicalNode)) {
            okSet = setRegulatingControl(control, svc);
        } else {
            context.pending(
                String.format(
                    "Remote regulating control for static var compensator %s replaced by control defined at Equipment",
                    svc.getId()),
                "IIDM model does not support remote control for static var compensators");

            setDefaultRegulatingControl(rc, svc);
        }
        control.setCorrectlySet(okSet);
    }

    private boolean setRegulatingControl(RegulatingControl control, StaticVarCompensator svc) {

        double targetVoltage = Double.NaN;
        double targetReactivePower = Double.NaN;
        StaticVarCompensator.RegulationMode regulationMode;

        boolean okSet = false;
        if (RegulatingControlMapping.isControlModeVoltage(control.mode.toLowerCase())) {
            regulationMode = StaticVarCompensator.RegulationMode.VOLTAGE;
            targetVoltage = control.targetValue;
            okSet = true;
        } else if (isControlModeReactivePower(control.mode.toLowerCase())) {
            regulationMode = StaticVarCompensator.RegulationMode.REACTIVE_POWER;
            targetReactivePower = control.targetValue;
            okSet = true;
        } else {
            context.fixed("SVCControlMode", String.format("Invalid control mode for static var compensator %s. Regulating control is disabled", svc.getId()));
            regulationMode = StaticVarCompensator.RegulationMode.OFF;
        }

        svc.setVoltageSetPoint(targetVoltage);
        svc.setReactivePowerSetPoint(targetReactivePower);
        svc.setRegulationMode(regulationMode);

        return okSet;
    }

    private void setDefaultRegulatingControl(CgmesRegulatingControlForStaticVarCompensator rc, StaticVarCompensator svc) {

        double targetVoltage = Double.NaN;
        double targetReactivePower = Double.NaN;
        StaticVarCompensator.RegulationMode regulationMode;

        if (RegulatingControlMapping.isControlModeVoltage(rc.defaultRegulationMode.toLowerCase())) {
            regulationMode = StaticVarCompensator.RegulationMode.VOLTAGE;
            targetVoltage = rc.defaultTargetVoltage;
        } else if (isControlModeReactivePower(rc.defaultRegulationMode.toLowerCase())) {
            regulationMode = StaticVarCompensator.RegulationMode.REACTIVE_POWER;
            targetReactivePower = rc.defaultTargetReactivePower;
        } else {
            context.fixed("SVCControlMode", String.format("Invalid control mode for static var compensator %s. Regulating control is disabled", svc.getId()));
            regulationMode = StaticVarCompensator.RegulationMode.OFF;
        }

        svc.setVoltageSetPoint(targetVoltage);
        svc.setReactivePowerSetPoint(targetReactivePower);
        svc.setRegulationMode(regulationMode);
    }

    private boolean isControlModeReactivePower(String controlMode) {
        return controlMode != null && controlMode.endsWith("reactivepower");
    }

    private static class CgmesRegulatingControlForStaticVarCompensator {
        String regulatingControlId;
        boolean controlEnabledProperty;
        double defaultTargetVoltage;
        double defaultTargetReactivePower;
        String defaultRegulationMode;
    }

    private final RegulatingControlMapping parent;
    private final Map<String, CgmesRegulatingControlForStaticVarCompensator> mapping;
    private final Context context;
}
