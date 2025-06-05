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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public DanglingLine convertOverDanglingLine(DanglingLineAdder adder, PowerFlow fother) {
        Regulation regulation = getRegulation();
        DanglingLine dl;
        if (regulation.status) {
            // If this equivalent injection is regulating voltage,
            // map it over the dangling line 'virtual generator'
            dl = adder
                    .setP0(fother.p())
                    .setQ0(fother.q())
                    .newGeneration()
                        .setVoltageRegulationOn(true)
                        .setMinP(-Double.MAX_VALUE)
                        .setMaxP(Double.MAX_VALUE)
                        .setTargetP(regulation.targetP)
                        .setTargetQ(regulation.targetQ)
                        .setTargetV(regulation.targetV)
                    .add()
                    .add();
        } else {
            // Map all the observed flows to the 'virtual load'
            // of the dangling line
            dl = adder
                    .setP0(fother.p() + p0())
                    .setQ0(fother.q() + q0())
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

    static class Regulation {
        private boolean status;
        private double targetV;
        private double targetP;
        private double targetQ;
    }

    private Regulation getRegulation() {
        Regulation regulation = new Regulation();

        boolean regulationCapability = p.asBoolean(CgmesNames.REGULATION_CAPABILITY, false);
        regulation.status = p.asBoolean(CgmesNames.REGULATION_STATUS, false) && regulationCapability;
        if (!p.containsKey(CgmesNames.REGULATION_STATUS) || !p.containsKey(CgmesNames.REGULATION_TARGET)) {
            LOG.trace("Attributes regulationStatus or regulationTarget not present for equivalent injection {}. Voltage regulation is considered as off.", id);
        }

        regulation.status = regulation.status && terminalConnected();
        regulation.targetV = Double.NaN;
        if (regulation.status) {
            regulation.targetV = p.asDouble(CgmesNames.REGULATION_TARGET);
            if (Double.isNaN(regulation.targetV) || regulation.targetV == 0) {
                missing("Valid target voltage value (voltage regulation is considered as off)");
                regulation.status = false;
            }
        }

        PowerFlow f = powerFlow();
        regulation.targetP = 0;
        regulation.targetQ = 0;
        if (f.defined()) {
            regulation.targetP = -f.p();
            regulation.targetQ = -f.q();
        }

        return regulation;
    }

    public static void update(Generator generator, PropertyBag cgmesData, Context context) {
        updateTerminals(generator, context, generator.getTerminal());

        boolean regulationCapability = Boolean.parseBoolean(generator.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS + CgmesNames.REGULATION_CAPABILITY));

        double targetP = 0.0;
        double targetQ = 0.0;
        PowerFlow updatedPowerFlow = updatedPowerFlow(generator, cgmesData, context);
        if (updatedPowerFlow.defined()) {
            targetP = -updatedPowerFlow.p();
            targetQ = -updatedPowerFlow.q();
        }

        double defaultTargetV = getDefaultTargetV(generator, context);
        double targetV = findTargetV(cgmesData, CgmesNames.REGULATION_TARGET, defaultTargetV, DefaultValueUse.NOT_DEFINED);
        boolean defaultRegulatingOn = getDefaultRegulatingOn(generator, context);
        boolean regulatingOn = findRegulatingOn(cgmesData, CgmesNames.REGULATION_STATUS, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED);

        generator.setTargetP(targetP)
                .setTargetQ(targetQ)
                .setTargetV(targetV)
                .setVoltageRegulatorOn(regulatingOn && regulationCapability && isValidTargetV(targetV));
    }

    private static double getDefaultTargetV(Generator generator, Context context) {
        return getDefaultValue(null, generator.getTargetV(), Double.NaN, Double.NaN, context);
    }

    private static boolean getDefaultRegulatingOn(Generator generator, Context context) {
        return getDefaultValue(false, generator.isVoltageRegulatorOn(), false, false, context);
    }

    private static final Logger LOG = LoggerFactory.getLogger(EquivalentInjectionConversion.class);
}
