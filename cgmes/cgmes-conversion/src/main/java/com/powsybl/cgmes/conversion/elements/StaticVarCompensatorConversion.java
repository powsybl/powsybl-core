/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.*;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControlAdder;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class StaticVarCompensatorConversion extends AbstractConductingEquipmentConversion {

    public StaticVarCompensatorConversion(PropertyBag svc, Context context) {
        super("StaticVarCompensator", svc, context);
    }

    @Override
    public void convert() {
        double slope = checkSlope(p.asDouble("slope"));
        double capacitiveRating = p.asDouble("capacitiveRating", 0.0);
        double inductiveRating = p.asDouble("inductiveRating", 0.0);

        StaticVarCompensatorAdder adder = voltageLevel().newStaticVarCompensator()
            .setBmin(getB(inductiveRating, "inductive"))
            .setBmax(getB(capacitiveRating, "capacitive"));
        identify(adder);
        connect(adder);
        RegulatingControlMappingForStaticVarCompensators.initialize(adder);

        StaticVarCompensator svc = adder.add();
        addAliasesAndProperties(svc);
        convertedTerminals(svc.getTerminal());
        if (slope >= 0) {
            svc.newExtension(VoltagePerReactivePowerControlAdder.class).withSlope(slope).add();
        }

        context.regulatingControlMapping().forStaticVarCompensators().add(svc.getId(), p);
    }

    private double getB(double rating, String name) {
        if (rating == 0.0) {
            fixed(name + "Rating", "Undefined or equal to 0. Corresponding susceptance is Double.MAX_VALUE");
            return name.equals("inductive") ? -Double.MAX_VALUE : Double.MAX_VALUE;
        }
        return 1 / rating;
    }

    private double checkSlope(double slope) {
        if (Double.isNaN(slope)) {
            missing("slope");
        }
        if (slope < 0) {
            ignored("Slope must be positive");
        }
        return slope;
    }

    @Override
    public void update(Network network) {
        // super.update(network); // TODO JAM delete
        StaticVarCompensator svc = network.getStaticVarCompensator(id);
        if (svc == null) {
            return;
        }

        String controlId = svc.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl");
        String defaultRegulationMode = svc.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "defaultRegulationMode");
        String mode = svc.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "mode");
        double defaultTargetVoltage = Double.parseDouble(svc.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "defaultTargetVoltage"));

        double defaultTargetReactivePower = p.asDouble("q");
        boolean controlEnabled = p.asBoolean("controlEnabled", false);
        RC rc = new RC(controlEnabled, defaultTargetVoltage, defaultTargetReactivePower, defaultRegulationMode, mode);

        updateRegulatingControl(svc, rc, controlId);
    }

    private void updateRegulatingControl(StaticVarCompensator svc, RC rc, String controlId) {

        if (controlId == null) {
            LOG.trace("Regulating control Id not present for static var compensator {}", svc.getId());
            setDefaultRegulatingControl(rc, svc, false, null);
            return;
        }

        RegulatingControlUpdate.RegulatingControl control = context.regulatingControlUpdate().getRegulatingControl(controlId).orElse(null);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            setDefaultRegulatingControl(rc, svc, false, null);
            return;
        }

        setRegulatingControl(rc, control, svc);
    }

    private void setRegulatingControl(RC rc, RegulatingControlUpdate.RegulatingControl control, StaticVarCompensator svc) {

        double targetVoltage = Double.NaN;
        double targetReactivePower = Double.NaN;
        StaticVarCompensator.RegulationMode regulationMode;

        if (!control.getEnabled() && rc.controlEnabled) {
            context.fixed("SVCControlEnabledStatus", () -> String.format("Regulating control of %s is disabled but controlEnabled property is set to true." +
                    "Equipment and regulating control properties are used to set local default regulation if local default regulation is reactive power. Else, regulation is disabled.", svc.getId()));
            setDefaultRegulatingControl(rc, svc, true, control);
            return;
        }
        if (RegulatingControlMapping.isControlModeVoltage(rc.mode.toLowerCase())) {
            regulationMode = StaticVarCompensator.RegulationMode.VOLTAGE;
            targetVoltage = control.getTargetValue();
        } else if (RegulatingControlMapping.isControlModeReactivePower(rc.mode.toLowerCase())) {
            regulationMode = StaticVarCompensator.RegulationMode.REACTIVE_POWER;
            targetReactivePower = control.getTargetValue();
        } else {
            context.fixed("SVCControlMode", () -> String.format("Invalid control mode for static var compensator %s. Regulating control is disabled", svc.getId()));
            regulationMode = StaticVarCompensator.RegulationMode.OFF;
        }

        svc.setVoltageSetpoint(targetVoltage);
        svc.setReactivePowerSetpoint(targetReactivePower);
        if (control.getEnabled() && rc.controlEnabled) {
            svc.setRegulationMode(regulationMode);
        }
    }

    private boolean isValidVoltageFromRegulatingControl(RC rc, RegulatingControlUpdate.RegulatingControl control) {
        return control != null
                && RegulatingControlMapping.isControlModeVoltage(rc.mode)
                && Double.isFinite(control.getTargetValue())
                && control.getTargetValue() > 0;
    }

    private boolean isValidReactivePowerFromRegulatingControl(RC rc, RegulatingControlUpdate.RegulatingControl control) {
        return control != null
                && RegulatingControlMapping.isControlModeReactivePower(rc.mode)
                && Double.isFinite(control.getTargetValue());
    }

    private void setDefaultRegulatingControl(RC rc, StaticVarCompensator svc, boolean onlyReactivePowerReg, RegulatingControlUpdate.RegulatingControl control) {

        double targetVoltage = Double.NaN;
        double targetReactivePower = Double.NaN;
        StaticVarCompensator.RegulationMode regulationMode;

        // Before using the default target value,
        // We try to keep data from original regulating control if available.
        // Even if the regulating control was disabled it may have defined a valid target value
        if (RegulatingControlMapping.isControlModeVoltage(rc.defaultRegulationMode.toLowerCase())) {
            regulationMode = onlyReactivePowerReg ? StaticVarCompensator.RegulationMode.OFF : StaticVarCompensator.RegulationMode.VOLTAGE;
            targetVoltage = isValidVoltageFromRegulatingControl(rc, control) ? control.getTargetValue() : rc.defaultTargetVoltage;
        } else if (RegulatingControlMapping.isControlModeReactivePower(rc.defaultRegulationMode.toLowerCase())) {
            regulationMode = StaticVarCompensator.RegulationMode.REACTIVE_POWER;
            targetReactivePower = isValidReactivePowerFromRegulatingControl(rc, control) ? control.getTargetValue() : rc.defaultTargetReactivePower;
        } else {
            context.fixed("SVCControlMode", () -> String.format("Invalid control mode for static var compensator %s. Regulating control is disabled", svc.getId()));
            regulationMode = StaticVarCompensator.RegulationMode.OFF;
        }

        svc.setVoltageSetpoint(targetVoltage);
        svc.setReactivePowerSetpoint(targetReactivePower);
        if (rc.controlEnabled) {
            svc.setRegulationMode(regulationMode);
        }
    }

    private record RC(boolean controlEnabled, double defaultTargetVoltage, double defaultTargetReactivePower, String defaultRegulationMode, String mode) {
    }

    private static final Logger LOG = LoggerFactory.getLogger(StaticVarCompensatorConversion.class);
}
