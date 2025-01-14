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
import com.powsybl.cgmes.conversion.RegulatingControlMapping;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForStaticVarCompensators;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControlAdder;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.List;
import java.util.Optional;

import static com.powsybl.cgmes.conversion.Conversion.Config.DefaultValue.*;

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

        double defaultTargetQ = cgmesData.asDouble("q");
        boolean controlEnabled = cgmesData.asBoolean("controlEnabled", false);
        updateRegulatingControl(staticVarCompensator, defaultTargetQ, controlEnabled, context);
    }

    private static void updateRegulatingControl(StaticVarCompensator staticVarCompensator, double defaultTargetQ, boolean controlEnabled, Context context) {
        String defaultRegulationMode = staticVarCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "defaultRegulationMode");
        String mode = staticVarCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.MODE);

        Optional<PropertyBag> cgmesRegulatingControl = findCgmesRegulatingControl(staticVarCompensator, context);
        double defaultTargetV = findDefaultTargetV(staticVarCompensator);

        if (isDefaultVoltageControl(cgmesRegulatingControl.isEmpty(), controlEnabled, defaultRegulationMode, defaultTargetV)) {
            setDefaultVoltageControl(staticVarCompensator, defaultTargetV, context);
            return;
        }
        boolean regulatingOn = cgmesRegulatingControl.map(propertyBag -> findRegulatingOn(propertyBag, staticVarCompensator, context)).orElseGet(() -> findDefaultRegulatingOn(staticVarCompensator, context));
        if (isDefaultReactivePowerControl(regulatingOn, controlEnabled, defaultRegulationMode, defaultTargetQ)) {
            setDefaultReactivePowerControl(staticVarCompensator, defaultTargetQ, context);
            return;
        }

        String selectedMode = mode != null ? mode : defaultRegulationMode;
        if (selectedMode != null && RegulatingControlMapping.isControlModeVoltage(selectedMode.toLowerCase())) {
            double targetV = cgmesRegulatingControl.map(StaticVarCompensatorConversion::findTargetV).orElseGet(() -> findDefaultTargetV(staticVarCompensator, defaultTargetV, context));
            StaticVarCompensator.RegulationMode regulationMode = controlEnabled && regulatingOn && isValidTargetV(targetV) ? StaticVarCompensator.RegulationMode.VOLTAGE : StaticVarCompensator.RegulationMode.OFF;
            staticVarCompensator.setVoltageSetpoint(targetV).setRegulationMode(regulationMode);
        } else if (selectedMode != null && RegulatingControlMapping.isControlModeReactivePower(selectedMode.toLowerCase())) {
            double targetQ = cgmesRegulatingControl.map(StaticVarCompensatorConversion::findTargetQ).orElseGet(() -> findDefaultTargetQ(staticVarCompensator, defaultTargetQ, context));
            StaticVarCompensator.RegulationMode regulationMode = controlEnabled && regulatingOn && isValidTargetQ(targetQ) ? StaticVarCompensator.RegulationMode.REACTIVE_POWER : StaticVarCompensator.RegulationMode.OFF;
            staticVarCompensator.setReactivePowerSetpoint(targetQ).setRegulationMode(regulationMode);
        } else {
            context.ignored(mode, "Unsupported regulation mode for staticVarCompensator " + staticVarCompensator.getId());
        }
    }

    private static boolean isDefaultVoltageControl(boolean cgmesRegulatingControlIsNotDefined, boolean controlEnabled, String defaultRegulationMode, double defaultTargetV) {
        return cgmesRegulatingControlIsNotDefined
                && controlEnabled
                && defaultRegulationMode != null && RegulatingControlMapping.isControlModeVoltage(defaultRegulationMode.toLowerCase())
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
                && defaultRegulationMode != null && RegulatingControlMapping.isControlModeReactivePower(defaultRegulationMode.toLowerCase())
                && isValidTargetQ(defaultTargetQ);
    }

    private static void setDefaultReactivePowerControl(StaticVarCompensator staticVarCompensator, double defaultTargetQ, Context context) {
        context.fixed("SVCControlEnabledStatus", () -> String.format("Regulating control of %s is disabled but controlEnabled property is set to true." +
                "Equipment and regulating control properties are used to set local default regulation if local default regulation is reactive power.", staticVarCompensator.getId()));
        staticVarCompensator.setReactivePowerSetpoint(defaultTargetQ).setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER);
    }

    private static Optional<PropertyBag> findCgmesRegulatingControl(StaticVarCompensator staticVarCompensator, Context context) {
        String regulatingControlId = staticVarCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.REGULATING_CONTROL);
        return regulatingControlId != null ? Optional.ofNullable(context.regulatingControl(regulatingControlId)) : Optional.empty();
    }

    private static double findDefaultTargetV(StaticVarCompensator staticVarCompensator) {
        String defaultTargetVoltage = staticVarCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "defaultTargetVoltage");
        return defaultTargetVoltage != null ? Double.parseDouble(defaultTargetVoltage) : Double.NaN;
    }

    private static double findTargetV(PropertyBag regulatingControl) {
        return regulatingControl.asDouble("targetValue");
    }

    private static double findDefaultTargetV(StaticVarCompensator staticVarCompensator, double defaultTargetV, Context context) {
        return defaultValue(Double.NaN, staticVarCompensator.getVoltageSetpoint(), defaultTargetV, Double.NaN, getDefaultValueSelector(context));
    }

    private static boolean isValidTargetV(double targetV) {
        return Double.isFinite(targetV) && targetV > 0.0;
    }

    private static double findTargetQ(PropertyBag regulatingControl) {
        return regulatingControl.asDouble("targetValue");
    }

    private static double findDefaultTargetQ(StaticVarCompensator staticVarCompensator, double defaultTargetQ, Context context) {
        return defaultValue(Double.NaN, staticVarCompensator.getReactivePowerSetpoint(), defaultTargetQ, Double.NaN, getDefaultValueSelector(context));
    }

    private static boolean isValidTargetQ(double targetValue) {
        return Double.isFinite(targetValue);
    }

    private static Conversion.Config.DefaultValue getDefaultValueSelector(Context context) {
        return getDefaultValueSelector(List.of(PREVIOUS, DEFAULT, EMPTY), context);
    }

    private static boolean findRegulatingOn(PropertyBag regulatingControl, StaticVarCompensator staticVarCompensator, Context context) {
        return regulatingControl.asBoolean("enabled").orElse(findDefaultRegulatingOn(staticVarCompensator, context));
    }

    private static boolean findDefaultRegulatingOn(StaticVarCompensator staticVarCompensator, Context context) {
        return defaultValue(false, staticVarCompensator.getRegulationMode() == StaticVarCompensator.RegulationMode.VOLTAGE ||
                        staticVarCompensator.getRegulationMode() == StaticVarCompensator.RegulationMode.REACTIVE_POWER,
                false, false, getDefaultValueSelector(context));
    }
}
