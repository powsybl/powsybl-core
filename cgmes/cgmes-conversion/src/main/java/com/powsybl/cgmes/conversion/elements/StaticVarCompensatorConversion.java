/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForStaticVarCompensators;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControlAdder;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class StaticVarCompensatorConversion extends AbstractConductingEquipmentConversion {

    public StaticVarCompensatorConversion(PropertyBag svc, Context context) {
        super(CgmesNames.STATIC_VAR_COMPENSATOR, svc, context);
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
        connectWithOnlyEq(adder);
        RegulatingControlMappingForStaticVarCompensators.initialize(adder);

        StaticVarCompensator svc = adder.add();
        addAliasesAndProperties(svc);
        convertedTerminalsWithOnlyEq(svc.getTerminal());
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

    public static void update(StaticVarCompensator staticVarCompensator, PropertyBag cgmesData, Context context) {
        updateTerminals(staticVarCompensator, context, staticVarCompensator.getTerminal());

        double defaultQ = cgmesData.asDouble("q");
        boolean controlEnabled = cgmesData.asBoolean(CgmesNames.CONTROL_ENABLED, false);
        updateRegulatingControl(staticVarCompensator, defaultQ, controlEnabled, context);
    }

    private static void updateRegulatingControl(StaticVarCompensator staticVarCompensator, double defaultQ, boolean controlEnabled, Context context) {
        String defaultRegulationMode = staticVarCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "defaultRegulationMode");
        String mode = staticVarCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.MODE);

        Optional<PropertyBag> cgmesRegulatingControl = findCgmesRegulatingControl(staticVarCompensator, context);
        double defaultEquipmentTargetV = findDefaultEquipmentTargetV(staticVarCompensator);

        if (isDefaultVoltageControl(cgmesRegulatingControl.isEmpty(), controlEnabled, defaultRegulationMode, defaultEquipmentTargetV)) {
            setDefaultVoltageControl(staticVarCompensator, defaultEquipmentTargetV, context);
            return;
        }
        boolean defaultRegulatingOn = getDefaultRegulatingOn(staticVarCompensator, context);
        boolean regulatingOn = cgmesRegulatingControl.map(propertyBag -> findRegulatingOn(propertyBag, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED)).orElse(defaultRegulatingOn);
        if (isDefaultReactivePowerControl(regulatingOn, controlEnabled, defaultRegulationMode, defaultQ)) {
            setDefaultReactivePowerControl(staticVarCompensator, defaultQ, context);
            return;
        }

        String selectedMode = mode != null ? mode : defaultRegulationMode;
        if (selectedMode != null && isControlModeVoltage(selectedMode.toLowerCase())) {
            double defaultTargetV = getDefaultTargetV(staticVarCompensator, defaultEquipmentTargetV, context);
            double targetV = cgmesRegulatingControl.map(propertyBag -> findTargetV(propertyBag, defaultTargetV, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetV);
            StaticVarCompensator.RegulationMode regulationMode = controlEnabled && regulatingOn && isValidTargetV(targetV) ? StaticVarCompensator.RegulationMode.VOLTAGE : StaticVarCompensator.RegulationMode.OFF;

            setRegulation(staticVarCompensator, staticVarCompensator.getReactivePowerSetpoint(), targetV, regulationMode);
        } else if (selectedMode != null && isControlModeReactivePower(selectedMode.toLowerCase())) {
            double defaultTargetQ = getDefaultTargetQ(staticVarCompensator, defaultQ, context);
            double targetQ = cgmesRegulatingControl.map(propertyBag -> findTargetQ(propertyBag, 1, defaultTargetQ, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetQ);
            StaticVarCompensator.RegulationMode regulationMode = controlEnabled && regulatingOn && isValidTargetQ(targetQ) ? StaticVarCompensator.RegulationMode.REACTIVE_POWER : StaticVarCompensator.RegulationMode.OFF;

            setRegulation(staticVarCompensator, targetQ, staticVarCompensator.getVoltageSetpoint(), regulationMode);
        } else {
            context.ignored(mode, "Unsupported regulation mode for staticVarCompensator " + staticVarCompensator.getId());
        }
    }

    private static void setRegulation(StaticVarCompensator staticVarCompensator, double targetQ, double targetV, StaticVarCompensator.RegulationMode regulationMode) {
        switch (regulationMode) {
            case OFF -> staticVarCompensator.setRegulationMode(regulationMode).setVoltageSetpoint(targetV).setReactivePowerSetpoint(targetQ);
            case VOLTAGE -> staticVarCompensator.setVoltageSetpoint(targetV).setRegulationMode(regulationMode).setReactivePowerSetpoint(targetQ);
            case REACTIVE_POWER -> staticVarCompensator.setReactivePowerSetpoint(targetQ).setRegulationMode(regulationMode).setVoltageSetpoint(targetV);
        }
    }

    private static boolean isDefaultVoltageControl(boolean cgmesRegulatingControlIsNotDefined, boolean controlEnabled, String defaultRegulationMode, double defaultTargetV) {
        return cgmesRegulatingControlIsNotDefined
                && controlEnabled
                && defaultRegulationMode != null && isControlModeVoltage(defaultRegulationMode.toLowerCase())
                && isValidTargetV(defaultTargetV);
    }

    private static void setDefaultVoltageControl(StaticVarCompensator staticVarCompensator, double defaultTargetV, Context context) {
        context.fixed("SVCControlEnabledStatus", () -> String.format("Regulating control of %s is disabled but controlEnabled property is set to true." +
                "Equipment and regulating control properties are used to set local default regulation if local default regulation is voltage.", staticVarCompensator.getId()));
        staticVarCompensator.setVoltageSetpoint(defaultTargetV).setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE);
    }

    private static boolean isDefaultReactivePowerControl(boolean regulatingOn, boolean controlEnabled, String defaultRegulationMode, double defaultTargetQ) {
        return !regulatingOn
                && controlEnabled
                && defaultRegulationMode != null && isControlModeReactivePower(defaultRegulationMode.toLowerCase())
                && isValidTargetQ(defaultTargetQ);
    }

    private static void setDefaultReactivePowerControl(StaticVarCompensator staticVarCompensator, double defaultTargetQ, Context context) {
        context.fixed("SVCControlEnabledStatus", () -> String.format("Regulating control of %s is disabled but controlEnabled property is set to true." +
                "Equipment and regulating control properties are used to set local default regulation if local default regulation is reactive power.", staticVarCompensator.getId()));
        staticVarCompensator.setReactivePowerSetpoint(defaultTargetQ).setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER);
    }

    private static double findDefaultEquipmentTargetV(StaticVarCompensator staticVarCompensator) {
        String defaultTargetVoltage = staticVarCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "defaultTargetVoltage");
        return defaultTargetVoltage != null ? Double.parseDouble(defaultTargetVoltage) : Double.NaN;
    }

    private static double getDefaultTargetV(StaticVarCompensator staticVarCompensator, double defaultEquipmentTargetV, Context context) {
        return getDefaultValue(defaultEquipmentTargetV, staticVarCompensator.getVoltageSetpoint(), Double.NaN, Double.NaN, context);
    }

    private static double getDefaultTargetQ(StaticVarCompensator staticVarCompensator, double defaultTargetQ, Context context) {
        return getDefaultValue(null, staticVarCompensator.getReactivePowerSetpoint(), defaultTargetQ, Double.NaN, context);
    }

    private static boolean getDefaultRegulatingOn(StaticVarCompensator staticVarCompensator, Context context) {
        return getDefaultValue(null,
                staticVarCompensator.getRegulationMode() != StaticVarCompensator.RegulationMode.OFF,
                false, false, context);
    }
}
