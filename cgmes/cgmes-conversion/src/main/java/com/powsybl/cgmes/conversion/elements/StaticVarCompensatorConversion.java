/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.RegulatingControlMapping;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.Optional;

import static com.powsybl.cgmes.conversion.Conversion.*;
import static com.powsybl.cgmes.conversion.RegulatingControlMapping.regulatingTerminalMapped;

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

        StaticVarCompensator svc = adder.add();
        addAliasesAndProperties(svc);
        convertedTerminalsWithOnlyEq(svc.getTerminal());
        if (svc.getVoltageRegulation() != null && slope >= 0) {
            svc.getVoltageRegulation().setSlope(slope);
        }

        setRegulatingControlProperty(svc);
        if (!svc.hasProperty(PROPERTY_REGULATING_CONTROL)) {
            // VoltageRegulation is mandatory for SVC in IIDM.
            setDefaultRegulatingControlData(svc);
        }

        // Store default target voltage for later use during update of the targetValue.
        double defaultTargetVoltage = p.asDouble("voltageSetPoint");
        if (!Double.isNaN(defaultTargetVoltage)) {
            svc.setProperty(PROPERTY_SVC_EQ_VOLTAGE_SET_POINT, String.valueOf(defaultTargetVoltage));
        }
    }

    private double getB(double rating, String name) {
        if (rating == 0.0) {
            fixed(name + "Rating", "Undefined or equal to 0. Corresponding susceptance is Double.MAX_VALUE");
            return "inductive".equals(name) ? -Double.MAX_VALUE : Double.MAX_VALUE;
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

    private void setDefaultRegulatingControlData(StaticVarCompensator svc) {
        // Create default regulation
        String defaultRegulationMode = p.getId("controlMode");
        if (RegulatingControlMapping.checkControlMode(defaultRegulationMode, CgmesNames.REACTIVE_POWER)) {
            svc.newVoltageRegulation()
                .withTerminal(svc.getTerminal())
                .withMode(RegulationMode.REACTIVE_POWER)
                .build();
        } else {
            if (!RegulatingControlMapping.checkControlMode(defaultRegulationMode, CgmesNames.VOLTAGE)) {
                context.fixed("SVCDefaultControlMode", () -> String.format("Invalid default control mode for static var compensator %s. Default regulationMode set to VOLTAGE", svc.getId()));
            }
            svc.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .build();
        }
    }

    public static void update(StaticVarCompensator staticVarCompensator, PropertyBag cgmesData, Context context) {
        updateTerminals(staticVarCompensator, context, staticVarCompensator.getTerminal());

        double defaultLocalTargetQ = getDefaultValue(null, staticVarCompensator.getLocalTargetQ(), Double.NaN, Double.NaN, context);
        double localTargetQ = cgmesData.asDouble("q");
        localTargetQ = Double.isNaN(localTargetQ) ? defaultLocalTargetQ : localTargetQ;
        staticVarCompensator.setLocalTargetQ(localTargetQ);
        Boolean controlEnabled = cgmesData.asBoolean(CgmesNames.CONTROL_ENABLED).orElse(null);
        updateRegulatingControl(staticVarCompensator, controlEnabled, context);
    }

    private static void updateRegulatingControl(StaticVarCompensator staticVarCompensator, Boolean controlEnabled, Context context) {
        if (context.config().getUseSvcRegulationDataAsAlternative() && !regulatingTerminalMapped(staticVarCompensator)) {
            // In case the regulation terminal couldn't be mapped and the SVC default regulation data should be used,
            // The RegulatingControl data is ignored. Then:
            // - If the SVC is in sVCControlMode = Voltage, the target comes from the EQ SVC voltageSetPoint
            // (all has been done during import, nothing to do in the update)
            // - If the SVC is in sVCControlMode = reactivePower, the target comes from the SSH SVC q,
            // set the regulating status to true if the q is valid
            VoltageRegulation regulation = staticVarCompensator.getVoltageRegulation();
            if (regulation.getMode() == RegulationMode.REACTIVE_POWER && isValidTargetQ(staticVarCompensator.getLocalTargetQ())) {
                regulation.setRegulating(true);
            }
            return;
        }

        Optional<PropertyBag> cgmesRegulatingControl = findCgmesRegulatingControl(staticVarCompensator, context);

        boolean defaultRegulatingOn = getDefaultRegulatingOn(staticVarCompensator, context);
        boolean updatedControlEnabled = controlEnabled != null ? controlEnabled : defaultRegulatingOn;
        boolean regulatingOn = cgmesRegulatingControl.map(propertyBag -> findRegulatingOn(propertyBag, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED)).orElse(defaultRegulatingOn);

        VoltageRegulation voltageRegulation = staticVarCompensator.getVoltageRegulation();
        if (staticVarCompensator.isWithMode(RegulationMode.VOLTAGE)) {
            double defaultTargetV = getDefaultTargetV(staticVarCompensator, context);
            double targetV = cgmesRegulatingControl.map(propertyBag -> findTargetV(propertyBag, defaultTargetV, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetV);
            boolean regulating = regulatingTerminalMapped(staticVarCompensator) && updatedControlEnabled && regulatingOn && isValidTargetV(targetV);
            setVoltageRegulation(staticVarCompensator, targetV, regulating);
        } else if (staticVarCompensator.isWithMode(RegulationMode.REACTIVE_POWER)) {
            double defaultTargetQ = getDefaultTargetQ(staticVarCompensator, context);
            int terminalSign = findTerminalSign(staticVarCompensator);
            double targetQ = cgmesRegulatingControl.map(propertyBag -> findTargetQ(propertyBag, terminalSign, defaultTargetQ, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetQ);
            boolean regulating = regulatingTerminalMapped(staticVarCompensator) && updatedControlEnabled && regulatingOn && isValidTargetQ(targetQ);

            voltageRegulation.setTargetValue(targetQ);
            voltageRegulation.setRegulating(regulating);
        }
    }

    private static double findDefaultEquipmentTargetV(StaticVarCompensator staticVarCompensator) {
        String defaultTargetVoltage = staticVarCompensator.getProperty(PROPERTY_SVC_EQ_VOLTAGE_SET_POINT);
        return defaultTargetVoltage != null ? Double.parseDouble(defaultTargetVoltage) : Double.NaN;
    }

    private static double getDefaultTargetV(StaticVarCompensator staticVarCompensator, Context context) {
        return getDefaultValue(findDefaultEquipmentTargetV(staticVarCompensator), staticVarCompensator.getRegulatingTargetV(), Double.NaN, Double.NaN, context);
    }

    private static double getDefaultTargetQ(StaticVarCompensator staticVarCompensator, Context context) {
        return getDefaultValue(null, staticVarCompensator.getRegulatingTargetQ(), Double.NaN, Double.NaN, context);
    }

    private static boolean getDefaultRegulatingOn(StaticVarCompensator staticVarCompensator, Context context) {
        return getDefaultValue(null, staticVarCompensator.getVoltageRegulation().isRegulating(), false, false, context);
    }
}
