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
        Optional<PropertyBag> cgmesRegulatingControl = findCgmesRegulatingControl(staticVarCompensator, context);

        boolean defaultRegulatingOn = getDefaultRegulatingOn(staticVarCompensator, context);
        boolean regulatingOn = cgmesRegulatingControl.map(propertyBag -> findRegulatingOn(propertyBag, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED)).orElse(defaultRegulatingOn);

        if (staticVarCompensator.getRegulationMode() == StaticVarCompensator.RegulationMode.VOLTAGE) {
            double defaultTargetV = getDefaultTargetV(staticVarCompensator, context);
            double targetV = cgmesRegulatingControl.map(propertyBag -> findTargetV(propertyBag, defaultTargetV, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetV);
            boolean regulating = controlEnabled && regulatingOn && isValidTargetV(targetV);

            staticVarCompensator.setVoltageSetpoint(targetV).setRegulating(regulating);
        } else if (staticVarCompensator.getRegulationMode() == StaticVarCompensator.RegulationMode.REACTIVE_POWER) {
            double defaultTargetQ = getDefaultTargetQ(staticVarCompensator, defaultQ, context);
            int terminalSign = findTerminalSign(staticVarCompensator);
            double targetQ = cgmesRegulatingControl.map(propertyBag -> findTargetQ(propertyBag, terminalSign, defaultTargetQ, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetQ);
            boolean regulating = controlEnabled && regulatingOn && isValidTargetQ(targetQ);

            staticVarCompensator.setReactivePowerSetpoint(targetQ).setRegulating(regulating);
        }
    }

    private static double findDefaultEquipmentTargetV(StaticVarCompensator staticVarCompensator) {
        String defaultTargetVoltage = staticVarCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.SVC_EQ_VOLTAGE_SET_POINT);
        return defaultTargetVoltage != null ? Double.parseDouble(defaultTargetVoltage) : Double.NaN;
    }

    private static double getDefaultTargetV(StaticVarCompensator staticVarCompensator, Context context) {
        return getDefaultValue(findDefaultEquipmentTargetV(staticVarCompensator), staticVarCompensator.getVoltageSetpoint(), Double.NaN, Double.NaN, context);
    }

    private static double getDefaultTargetQ(StaticVarCompensator staticVarCompensator, double defaultTargetQ, Context context) {
        return getDefaultValue(null, staticVarCompensator.getReactivePowerSetpoint(), defaultTargetQ, Double.NaN, context);
    }

    private static boolean getDefaultRegulatingOn(StaticVarCompensator staticVarCompensator, Context context) {
        return getDefaultValue(null,
                staticVarCompensator.isRegulating(),
                false, false, context);
    }
}
