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
    public BoundaryLine convertOverDanglingLine(BoundaryLineAdder adder) {
        boolean regulationCapability = p.asBoolean(CgmesNames.REGULATION_CAPABILITY, false);
        BoundaryLine dl;
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
        generator.setProperty(Conversion.PROPERTY_CGMES_REGULATION_CAPABILITY, propertyBag.getOrDefault(CgmesNames.REGULATION_CAPABILITY, "false"));
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

    public static void update(BoundaryLine boundaryLine, boolean isConnectedOnBoundarySide, Context context) {
        if (!isConnectedOnBoundarySide && boundaryLine.getTerminal().isConnected()) {
            updateWhenIsConnectedAndBoundarySideIsOpen(boundaryLine, context);
        } else {
            update(boundaryLine, context);
        }
    }

    private static void updateWhenIsConnectedAndBoundarySideIsOpen(BoundaryLine boundaryLine, Context context) {
        if (boundaryLine.getGeneration() != null) {
            Optional<PropertyBag> cgmesEquivalentInjection = getCgmesEquivalentInjection(boundaryLine, context);
            double defaultTargetV = getDefaultTargetV(boundaryLine.getGeneration(), context);
            double targetV = cgmesEquivalentInjection.map(propertyBag -> findTargetV(propertyBag, CgmesNames.REGULATION_TARGET, defaultTargetV, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetV);

            boundaryLine.getGeneration().setTargetP(0.0);
            boundaryLine.getGeneration().setTargetQ(0.0);
            setRegulation(boundaryLine, targetV, false);
        }
        boundaryLine.setP0(0.0);
        boundaryLine.setQ0(0.0);
    }

    private static void update(BoundaryLine boundaryLine, Context context) {
        Optional<PropertyBag> cgmesEquivalentInjection = getCgmesEquivalentInjection(boundaryLine, context);
        PowerFlow updatedPowerFlow = cgmesEquivalentInjection.map(propertyBag -> updatedPowerFlow(boundaryLine, propertyBag, context)).orElse(PowerFlow.UNDEFINED);

        if (boundaryLine.getGeneration() != null) {
            double defaultTargetV = getDefaultTargetV(boundaryLine.getGeneration(), context);
            double targetV = cgmesEquivalentInjection.map(propertyBag -> findTargetV(propertyBag, CgmesNames.REGULATION_TARGET, defaultTargetV, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetV);
            boolean defaultRegulatingOn = getDefaultRegulatingOn(boundaryLine.getGeneration(), context);
            boolean regulatingOn = cgmesEquivalentInjection.map(propertyBag -> findRegulatingOn(propertyBag, CgmesNames.REGULATION_STATUS, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED)).orElse(defaultRegulatingOn);

            boundaryLine.setP0(0.0);
            boundaryLine.setQ0(0.0);
            boundaryLine.getGeneration().setTargetP(getTargetP(updatedPowerFlow, boundaryLine.getGeneration(), context));
            boundaryLine.getGeneration().setTargetQ(getTargetQ(updatedPowerFlow, boundaryLine.getGeneration(), context));
            setRegulation(boundaryLine, targetV, regulatingOn && isValidTargetV(targetV));
        } else {
            boundaryLine.setP0(getTargetP(updatedPowerFlow, boundaryLine, context));
            boundaryLine.setQ0(getTargetQ(updatedPowerFlow, boundaryLine, context));
        }
    }

    private static void setRegulation(BoundaryLine boundaryLine, double targetV, boolean regulatingOn) {
        if (regulatingOn) {
            boundaryLine.getGeneration().setTargetV(targetV).setVoltageRegulationOn(true);
        } else {
            boundaryLine.getGeneration().setVoltageRegulationOn(false).setTargetV(targetV);
        }
    }

    private static Optional<PropertyBag> getCgmesEquivalentInjection(BoundaryLine boundaryLine, Context context) {
        String equivalentInjectionId = boundaryLine.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.EQUIVALENT_INJECTION);
        return equivalentInjectionId != null ? Optional.ofNullable(context.equivalentInjection(equivalentInjectionId)) : Optional.empty();
    }

    private static double getTargetP(PowerFlow updatedPowerFlow, BoundaryLine boundaryLine, Context context) {
        return updatedPowerFlow.defined() ? updatedPowerFlow.p() : getDefaultTargetP(boundaryLine, context);
    }

    private static double getDefaultTargetP(BoundaryLine boundaryLine, Context context) {
        return getDefaultValue(null, boundaryLine.getP0(), 0.0, 0.0, context);
    }

    private static double getTargetP(PowerFlow updatedPowerFlow, BoundaryLine.Generation generation, Context context) {
        return updatedPowerFlow.defined() ? -updatedPowerFlow.p() : getDefaultTargetP(generation, context);
    }

    private static double getDefaultTargetP(BoundaryLine.Generation generation, Context context) {
        return getDefaultValue(null, generation.getTargetP(), 0.0, 0.0, context);
    }

    private static double getTargetQ(PowerFlow updatedPowerFlow, BoundaryLine boundaryLine, Context context) {
        return updatedPowerFlow.defined() ? updatedPowerFlow.q() : getDefaultTargetQ(boundaryLine, context);
    }

    private static double getDefaultTargetQ(BoundaryLine boundaryLine, Context context) {
        return getDefaultValue(null, boundaryLine.getQ0(), 0.0, 0.0, context);
    }

    private static double getTargetQ(PowerFlow updatedPowerFlow, BoundaryLine.Generation generation, Context context) {
        return updatedPowerFlow.defined() ? -updatedPowerFlow.q() : getDefaultTargetQ(generation, context);
    }

    private static double getDefaultTargetQ(BoundaryLine.Generation generation, Context context) {
        return getDefaultValue(null, generation.getTargetQ(), 0.0, 0.0, context);
    }

    private static double getDefaultTargetV(BoundaryLine.Generation generation, Context context) {
        return getDefaultValue(null, generation.getTargetV(), Double.NaN, Double.NaN, context);
    }

    private static boolean getDefaultRegulatingOn(BoundaryLine.Generation generation, Context context) {
        return getDefaultValue(false, generation.isVoltageRegulationOn(), false, false, context);
    }
}
