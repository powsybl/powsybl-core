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
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class EquivalentInjectionConversion extends AbstractReactiveLimitsOwnerConversion {

    public EquivalentInjectionConversion(PropertyBag ei, Context context) {
        super(CgmesNames.EQUIVALENT_INJECTION, ei, context);
    }

    @Override
    public void convertInsideBoundary() {
        if (context.config().convertBoundary()) {
            if (valid()) {
                convert();
            }
        } else {
            // If we find an Equivalent Injection at a boundary
            // we will decide later what to do with it
            //
            // If finally a dangling line is created at the boundary node
            // and the equivalent injection is regulating voltage
            // we will have to transfer regulating voltage data
            // from the equivalent injection to the dangling line
            context.boundary().addEquivalentInjectionAtNode(this.p, nodeId());
        }
    }

    @Override
    public void convert() {
        // An equivalent injection found inside a modeling authority data
        // will be mapped to a Generator
        convertToGenerator();
    }

    // A dangling line has been created at the boundary node of the equivalent injection
    public DanglingLine convertOverDanglingLine(DanglingLineAdder adder) {
        boolean regulationCapability = p.asBoolean(CgmesNames.REGULATION_CAPABILITY, false);
        DanglingLine dl;
        if (regulationCapability) {
            // If this equivalent injection is regulating voltage,
            // map it over the dangling line 'virtual generator'
            dl = adder
                    .setP0(Double.NaN)
                    .setQ0(Double.NaN)
                    .newGeneration()
                        .setVoltageRegulationOn(false)
                        .setMinP(-Double.MAX_VALUE)
                        .setMaxP(Double.MAX_VALUE)
                        .setTargetP(Double.NaN)
                        .setTargetQ(Double.NaN)
                        .setTargetV(Double.NaN)
                    .add()
                    .add();
        } else {
            // Map all the observed flows to the 'virtual load'
            // of the dangling line
            dl = adder
                    .setP0(Double.NaN)
                    .setQ0(Double.NaN)
                    .add();
        }
        // We do not call addAliasesAndProperties(dl) !
        // Because we do not want to add this equivalent injection
        // terminal id as a generic "Terminal" alias of the dangling line,
        // Terminal1 and Terminal2 aliases should be used for
        // the original ACLineSegment or Switch terminals
        // We want to keep track add this equivalent injection terminal
        // under a separate, specific, alias type
        dl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.EQUIVALENT_INJECTION, this.id);
        CgmesTerminal cgmesTerminal = context.cgmes().terminal(terminalId());
        if (cgmesTerminal != null) {
            dl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal", cgmesTerminal.id());
        }
        return dl;
    }

    private void convertToGenerator() {
        double minP = p.asDouble("minP", -Double.MAX_VALUE);
        double maxP = p.asDouble("maxP", Double.MAX_VALUE);

        GeneratorAdder adder = voltageLevel().newGenerator();
        setMinPMaxP(adder, minP, maxP);
        adder.setEnergySource(EnergySource.OTHER);
        identify(adder);
        connectWithOnlyEq(adder);
        Generator g = adder.add();
        addAliasesAndProperties(g);
        convertedTerminalsWithOnlyEq(g.getTerminal());
        convertReactiveLimits(g);

        addSpecificProperties(g, p);
    }

    private static void addSpecificProperties(Generator generator, PropertyBag propertyBag) {
        generator.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.EQUIVALENT_INJECTION);
        generator.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS + CgmesNames.REGULATION_CAPABILITY,
                String.valueOf(propertyBag.asBoolean(CgmesNames.REGULATION_CAPABILITY, false)));
    }

    public static void update(Generator generator, PropertyBag cgmesData, Context context) {
        updateTerminals(generator, context, generator.getTerminal());

        boolean regulationCapability = Boolean.parseBoolean(generator.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS + CgmesNames.REGULATION_CAPABILITY));

        PowerFlow updatedPowerFlow = updatedPowerFlow(generator, cgmesData, context);

        double defaultTargetV = getDefaultTargetV(generator, context);
        double targetV = findTargetV(cgmesData, CgmesNames.REGULATION_TARGET, defaultTargetV, DefaultValueUse.NOT_DEFINED);
        boolean defaultRegulatingOn = getDefaultRegulatingOn(generator, context);
        boolean regulatingOn = findRegulatingOn(cgmesData, CgmesNames.REGULATION_STATUS, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED);

        generator.setTargetP(getTargetP(updatedPowerFlow, generator, context))
                .setTargetQ(getTargetQ(updatedPowerFlow, generator, context))
                .setTargetV(targetV)
                .setVoltageRegulatorOn(regulatingOn && regulationCapability && isValidTargetV(targetV));
    }

    private static double getTargetP(PowerFlow updatedPowerFlow, Generator generator, Context context) {
        return updatedPowerFlow.defined() ? -updatedPowerFlow.p() : getDefaultTargetP(generator, context);
    }

    private static double getDefaultTargetP(Generator generator, Context context) {
        return getDefaultValue(null, generator.getTargetP(), 0.0, 0.0, context);
    }

    private static double getTargetQ(PowerFlow updatedPowerFlow, Generator generator, Context context) {
        return updatedPowerFlow.defined() ? -updatedPowerFlow.q() : getDefaultTargetQ(generator, context);
    }

    private static double getDefaultTargetQ(Generator generator, Context context) {
        return getDefaultValue(null, generator.getTargetQ(), 0.0, 0.0, context);
    }

    private static double getDefaultTargetV(Generator generator, Context context) {
        return getDefaultValue(null, generator.getTargetV(), Double.NaN, Double.NaN, context);
    }

    private static boolean getDefaultRegulatingOn(Generator generator, Context context) {
        return getDefaultValue(false, generator.isVoltageRegulatorOn(), false, false, context);
    }

    public static void update(DanglingLine danglingLine, boolean isConnectedOnBoundarySide, Context context) {
        if (!isConnectedOnBoundarySide && danglingLine.getTerminal().isConnected()) {
            updateWhenIsConnectedAndBoundarySideIsOpen(danglingLine, context);
        } else {
            update(danglingLine, context);
        }
    }

    private static void updateWhenIsConnectedAndBoundarySideIsOpen(DanglingLine danglingLine, Context context) {
        if (danglingLine.getGeneration() != null) {
            Optional<PropertyBag> cgmesEquivalentInjection = getCgmesEquivalentInjection(danglingLine, context);
            double defaultTargetV = getDefaultTargetV(danglingLine.getGeneration(), context);
            double targetV = cgmesEquivalentInjection.map(propertyBag -> findTargetV(propertyBag, CgmesNames.REGULATION_TARGET, defaultTargetV, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetV);

            danglingLine.getGeneration().setTargetP(0.0);
            danglingLine.getGeneration().setTargetQ(0.0);
            setRegulation(danglingLine, targetV, false);
        }
        danglingLine.setP0(0.0);
        danglingLine.setQ0(0.0);
    }

    private static void update(DanglingLine danglingLine, Context context) {
        Optional<PropertyBag> cgmesEquivalentInjection = getCgmesEquivalentInjection(danglingLine, context);
        PowerFlow updatedPowerFlow = cgmesEquivalentInjection.map(propertyBag -> updatedPowerFlow(danglingLine, propertyBag, context)).orElse(PowerFlow.UNDEFINED);

        if (danglingLine.getGeneration() != null) {
            double defaultTargetV = getDefaultTargetV(danglingLine.getGeneration(), context);
            double targetV = cgmesEquivalentInjection.map(propertyBag -> findTargetV(propertyBag, CgmesNames.REGULATION_TARGET, defaultTargetV, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetV);
            boolean defaultRegulatingOn = getDefaultRegulatingOn(danglingLine.getGeneration(), context);
            boolean regulatingOn = cgmesEquivalentInjection.map(propertyBag -> findRegulatingOn(propertyBag, CgmesNames.REGULATION_STATUS, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED)).orElse(defaultRegulatingOn);

            danglingLine.setP0(0.0);
            danglingLine.setQ0(0.0);
            danglingLine.getGeneration().setTargetP(getTargetP(updatedPowerFlow, danglingLine.getGeneration(), context));
            danglingLine.getGeneration().setTargetQ(getTargetQ(updatedPowerFlow, danglingLine.getGeneration(), context));
            setRegulation(danglingLine, targetV, regulatingOn && isValidTargetV(targetV));
        } else {
            danglingLine.setP0(getTargetP(updatedPowerFlow, danglingLine, context));
            danglingLine.setQ0(getTargetQ(updatedPowerFlow, danglingLine, context));
        }
    }

    private static void setRegulation(DanglingLine danglingLine, double targetV, boolean regulatingOn) {
        if (regulatingOn) {
            danglingLine.getGeneration().setTargetV(targetV).setVoltageRegulationOn(true);
        } else {
            danglingLine.getGeneration().setVoltageRegulationOn(false).setTargetV(targetV);
        }
    }

    private static Optional<PropertyBag> getCgmesEquivalentInjection(DanglingLine danglingLine, Context context) {
        String equivalentInjectionId = danglingLine.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.EQUIVALENT_INJECTION);
        return equivalentInjectionId != null ? Optional.ofNullable(context.equivalentInjection(equivalentInjectionId)) : Optional.empty();
    }

    private static double getTargetP(PowerFlow updatedPowerFlow, DanglingLine danglingLine, Context context) {
        return updatedPowerFlow.defined() ? updatedPowerFlow.p() : getDefaultTargetP(danglingLine, context);
    }

    private static double getDefaultTargetP(DanglingLine danglingLine, Context context) {
        return getDefaultValue(null, danglingLine.getP0(), 0.0, 0.0, context);
    }

    private static double getTargetP(PowerFlow updatedPowerFlow, DanglingLine.Generation generation, Context context) {
        return updatedPowerFlow.defined() ? -updatedPowerFlow.p() : getDefaultTargetP(generation, context);
    }

    private static double getDefaultTargetP(DanglingLine.Generation generation, Context context) {
        return getDefaultValue(null, generation.getTargetP(), 0.0, 0.0, context);
    }

    private static double getTargetQ(PowerFlow updatedPowerFlow, DanglingLine danglingLine, Context context) {
        return updatedPowerFlow.defined() ? updatedPowerFlow.q() : getDefaultTargetQ(danglingLine, context);
    }

    private static double getDefaultTargetQ(DanglingLine danglingLine, Context context) {
        return getDefaultValue(null, danglingLine.getQ0(), 0.0, 0.0, context);
    }

    private static double getTargetQ(PowerFlow updatedPowerFlow, DanglingLine.Generation generation, Context context) {
        return updatedPowerFlow.defined() ? -updatedPowerFlow.q() : getDefaultTargetQ(generation, context);
    }

    private static double getDefaultTargetQ(DanglingLine.Generation generation, Context context) {
        return getDefaultValue(null, generation.getTargetQ(), 0.0, 0.0, context);
    }

    private static double getDefaultTargetV(DanglingLine.Generation generation, Context context) {
        return getDefaultValue(null, generation.getTargetV(), Double.NaN, Double.NaN, context);
    }

    private static boolean getDefaultRegulatingOn(DanglingLine.Generation generation, Context context) {
        return getDefaultValue(false, generation.isVoltageRegulationOn(), false, false, context);
    }
}
