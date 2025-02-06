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

        DefaultValueDouble defaultTargetV = getDefaultTargetV(generator);
        double targetV = findTargetV(cgmesData, CgmesNames.REGULATION_TARGET, defaultTargetV, DefaultValueUse.NOT_DEFINED, context);
        DefaultValueBoolean defaultRegulatingOn = getDefaultRegulatingOn(generator);
        boolean regulatingOn = findRegulatingOn(cgmesData, CgmesNames.REGULATION_STATUS, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED, context);

        generator.setTargetP(getTargetP(updatedPowerFlow, generator, context))
                .setTargetQ(getTargetQ(updatedPowerFlow, generator, context))
                .setTargetV(targetV)
                .setVoltageRegulatorOn(regulatingOn && regulationCapability && isValidTargetV(targetV));
    }

    private static double getTargetP(PowerFlow updatedPowerFlow, Generator generator, Context context) {
        return updatedPowerFlow.defined() ? -updatedPowerFlow.p() : defaultValue(getDefaultTargetP(generator), context);
    }

    private static DefaultValueDouble getDefaultTargetP(Generator generator) {
        return new DefaultValueDouble(null, generator.getTargetP(), 0.0, 0.0);
    }

    private static double getTargetQ(PowerFlow updatedPowerFlow, Generator generator, Context context) {
        return updatedPowerFlow.defined() ? -updatedPowerFlow.q() : defaultValue(getDefaultTargetQ(generator), context);
    }

    private static DefaultValueDouble getDefaultTargetQ(Generator generator) {
        return new DefaultValueDouble(null, generator.getTargetQ(), 0.0, 0.0);
    }

    private static DefaultValueDouble getDefaultTargetV(Generator generator) {
        return new DefaultValueDouble(null, generator.getTargetV(), Double.NaN, Double.NaN);
    }

    private static DefaultValueBoolean getDefaultRegulatingOn(Generator generator) {
        return new DefaultValueBoolean(false, generator.isVoltageRegulatorOn(), false, false);
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
            DefaultValueDouble defaultTargetV = getDefaultTargetV(danglingLine.getGeneration());
            double targetV = cgmesEquivalentInjection.map(propertyBag -> findTargetV(propertyBag, CgmesNames.REGULATION_TARGET, defaultTargetV, DefaultValueUse.NOT_DEFINED, context)).orElse(defaultValue(defaultTargetV, context));

            danglingLine.getGeneration().setTargetP(0.0);
            danglingLine.getGeneration().setTargetQ(0.0);
            danglingLine.getGeneration().setTargetV(targetV);
            danglingLine.getGeneration().setVoltageRegulationOn(false);
        }
        danglingLine.setP0(0.0);
        danglingLine.setQ0(0.0);
    }

    private static void update(DanglingLine danglingLine, Context context) {
        Optional<PropertyBag> cgmesEquivalentInjection = getCgmesEquivalentInjection(danglingLine, context);
        PowerFlow updatedPowerFlow = cgmesEquivalentInjection.map(propertyBag -> updatedPowerFlow(danglingLine, propertyBag, context)).orElse(PowerFlow.UNDEFINED);

        if (danglingLine.getGeneration() != null) {
            DefaultValueDouble defaultTargetV = getDefaultTargetV(danglingLine.getGeneration());
            double targetV = cgmesEquivalentInjection.map(propertyBag -> findTargetV(propertyBag, CgmesNames.REGULATION_TARGET, defaultTargetV, DefaultValueUse.NOT_DEFINED, context)).orElse(defaultValue(defaultTargetV, context));
            DefaultValueBoolean defaultRegulatingOn = getDefaultRegulatingOn(danglingLine.getGeneration());
            boolean regulatingOn = cgmesEquivalentInjection.map(propertyBag -> findRegulatingOn(propertyBag, CgmesNames.REGULATION_STATUS, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED, context)).orElse(defaultValue(defaultRegulatingOn, context));

            danglingLine.setP0(0.0);
            danglingLine.setQ0(0.0);
            danglingLine.getGeneration().setTargetP(getTargetP(updatedPowerFlow, danglingLine.getGeneration(), context));
            danglingLine.getGeneration().setTargetQ(getTargetQ(updatedPowerFlow, danglingLine.getGeneration(), context));
            danglingLine.getGeneration().setTargetV(targetV);
            danglingLine.getGeneration().setVoltageRegulationOn(regulatingOn && isValidTargetV(targetV));
        } else {
            danglingLine.setP0(getTargetP(updatedPowerFlow, danglingLine, context));
            danglingLine.setQ0(getTargetQ(updatedPowerFlow, danglingLine, context));
        }
    }

    private static Optional<PropertyBag> getCgmesEquivalentInjection(DanglingLine danglingLine, Context context) {
        String equivalentInjectionId = danglingLine.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.EQUIVALENT_INJECTION);
        return equivalentInjectionId != null ? Optional.ofNullable(context.equivalentInjection(equivalentInjectionId)) : Optional.empty();
    }

    private static double getTargetP(PowerFlow updatedPowerFlow, DanglingLine danglingLine, Context context) {
        return updatedPowerFlow.defined() ? updatedPowerFlow.p() : defaultValue(getDefaultTargetP(danglingLine), context);
    }

    private static DefaultValueDouble getDefaultTargetP(DanglingLine danglingLine) {
        return new DefaultValueDouble(null, danglingLine.getP0(), 0.0, 0.0);
    }

    private static double getTargetP(PowerFlow updatedPowerFlow, DanglingLine.Generation generation, Context context) {
        return updatedPowerFlow.defined() ? -updatedPowerFlow.p() : defaultValue(getDefaultTargetP(generation), context);
    }

    private static DefaultValueDouble getDefaultTargetP(DanglingLine.Generation generation) {
        return new DefaultValueDouble(null, generation.getTargetP(), 0.0, 0.0);
    }

    private static double getTargetQ(PowerFlow updatedPowerFlow, DanglingLine danglingLine, Context context) {
        return updatedPowerFlow.defined() ? updatedPowerFlow.q() : defaultValue(getDefaultTargetQ(danglingLine), context);
    }

    private static DefaultValueDouble getDefaultTargetQ(DanglingLine danglingLine) {
        return new DefaultValueDouble(null, danglingLine.getQ0(), 0.0, 0.0);
    }

    private static double getTargetQ(PowerFlow updatedPowerFlow, DanglingLine.Generation generation, Context context) {
        return updatedPowerFlow.defined() ? -updatedPowerFlow.q() : defaultValue(getDefaultTargetQ(generation), context);
    }

    private static DefaultValueDouble getDefaultTargetQ(DanglingLine.Generation generation) {
        return new DefaultValueDouble(null, generation.getTargetQ(), 0.0, 0.0);
    }

    private static DefaultValueDouble getDefaultTargetV(DanglingLine.Generation generation) {
        return new DefaultValueDouble(null, generation.getTargetV(), Double.NaN, Double.NaN);
    }

    private static DefaultValueBoolean getDefaultRegulatingOn(DanglingLine.Generation generation) {
        return new DefaultValueBoolean(false, generation.isVoltageRegulationOn(), false, false);
    }
}
