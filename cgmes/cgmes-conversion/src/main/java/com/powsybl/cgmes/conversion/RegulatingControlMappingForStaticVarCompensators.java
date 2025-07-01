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
import com.powsybl.cgmes.model.CgmesNames;
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
        double defaultTargetVoltage = sm.asDouble("voltageSetPoint");
        String defaultRegulationMode = sm.getId("controlMode");

        if (mapping.containsKey(iidmId)) {
            throw new CgmesModelException(
                    "StaticVarCompensator already added, IIDM StaticVarCompensator Id: " + iidmId);
        }

        CgmesRegulatingControlForStaticVarCompensator rc = new CgmesRegulatingControlForStaticVarCompensator();
        rc.regulatingControlId = rcId;
        rc.defaultTargetVoltage = defaultTargetVoltage;
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
            setDefaultRegulatingControl(rc, svc);
            return;
        }

        RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            setDefaultRegulatingControl(rc, svc);
            return;
        }

        setRegulatingControl(rc, control, svc);
    }

    private void setDefaultRegulatingControl(CgmesRegulatingControlForStaticVarCompensator rc, StaticVarCompensator svc) {
        if (RegulatingControlMapping.isControlModeVoltage(rc.defaultRegulationMode)) {
            setDefaultRegulatingControlData(rc, svc);
            svc.setRegulatingTerminal(svc.getTerminal()).setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE);
        } else if (RegulatingControlMapping.isControlModeReactivePower(rc.defaultRegulationMode)) {
            svc.setRegulatingTerminal(svc.getTerminal()).setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER);
        } else {
            context.fixed("SVCDefaultControlMode", () -> String.format("Invalid default control mode for static var compensator %s. Default regulationMode set to VOLTAGE", svc.getId()));
            setDefaultRegulatingControlData(rc, svc);
            svc.setRegulatingTerminal(svc.getTerminal()).setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE);
        }
    }

    private void setRegulatingControl(CgmesRegulatingControlForStaticVarCompensator rc, RegulatingControl control, StaticVarCompensator svc) {
        boolean okSet = false;
        if (RegulatingControlMapping.isControlModeVoltage(control.mode)) {
            okSet = setRegulatingControlVoltage(rc, control, svc);
        } else if (RegulatingControlMapping.isControlModeReactivePower(control.mode)) {
            okSet = setRegulatingControlReactivePower(rc, control, svc);
        } else {
            context.fixed("SVCControlMode", () -> String.format("Invalid control mode for static var compensator %s. RegulationMode set to VOLTAGE", svc.getId()));
            okSet = setRegulatingControlVoltage(rc, control, svc);
        }

        svc.setProperty(Conversion.PROPERTY_REGULATING_CONTROL, rc.regulatingControlId);
        control.setCorrectlySet(okSet);
    }

    private boolean setRegulatingControlVoltage(CgmesRegulatingControlForStaticVarCompensator rc, RegulatingControl control, StaticVarCompensator svc) {
        setDefaultRegulatingControlData(rc, svc);
        Terminal regulatingTerminal = RegulatingTerminalMapper.mapForVoltageControl(control.cgmesTerminal, context).orElse(svc.getTerminal());
        svc.setRegulatingTerminal(regulatingTerminal).setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE);
        return true;
    }

    private boolean setRegulatingControlReactivePower(CgmesRegulatingControlForStaticVarCompensator rc, RegulatingControl control, StaticVarCompensator svc) {
        RegulatingTerminalMapper.TerminalAndSign mappedRegulatingTerminal = RegulatingTerminalMapper
                .mapForFlowControl(control.cgmesTerminal, context)
                .orElseGet(() -> new RegulatingTerminalMapper.TerminalAndSign(null, 1));
        if (mappedRegulatingTerminal.getTerminal() == null) {
            context.ignored(rc.regulatingControlId, String.format("Regulation terminal %s is not mapped or mapped to a switch", control.cgmesTerminal));
            return false;
        }
        svc.setRegulatingTerminal(mappedRegulatingTerminal.getTerminal()).setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER);

        svc.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL_SIGN, String.valueOf(mappedRegulatingTerminal.getSign()));
        return true;
    }

    private void setDefaultRegulatingControlData(CgmesRegulatingControlForStaticVarCompensator rc, StaticVarCompensator svc) {
        if (!Double.isNaN(rc.defaultTargetVoltage)) {
            svc.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.SVC_EQ_VOLTAGE_SET_POINT, String.valueOf(rc.defaultTargetVoltage));
        }
    }

    private static final class CgmesRegulatingControlForStaticVarCompensator {
        String regulatingControlId;
        double defaultTargetVoltage;
        String defaultRegulationMode;
    }

    private final RegulatingControlMapping parent;
    private final Map<String, CgmesRegulatingControlForStaticVarCompensator> mapping;
    private final Context context;

    private static final Logger LOG = LoggerFactory.getLogger(RegulatingControlMappingForStaticVarCompensators.class);
}
