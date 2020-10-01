/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class EquivalentInjectionConversion extends AbstractReactiveLimitsOwnerConversion {

    private static final String REGULATION_TARGET = "regulationTarget";

    public EquivalentInjectionConversion(PropertyBag ei, Context context) {
        super("EquivalentInjection", ei, context);
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
            PowerFlow f = powerFlow();
            dl = adder
                    .setP0(fother.p() + f.p())
                    .setQ0(fother.q() + f.q())
                    .newGeneration()
                        .setTargetV(Double.NaN)
                        .setVoltageRegulationOn(false)
                        .setTargetP(0.0)
                        .setTargetQ(0.0)
                    .add()
                    .add();
        }
        // We do not call addAliases(dl) !
        // Because we do not want to add this equivalent injection
        // terminal id as a generic "Terminal" alias of the dangling line,
        // Terminal1 and Terminal2 aliases should be used for
        // the original ACLineSegment or Switch terminals
        // We want to keep track add this equivalent injection terminal
        // under a separate, specific, alias type
        dl.addAlias(this.id, Conversion.CGMES_PREFIX_ALIAS + "EquivalentInjection");
        CgmesTerminal cgmesTerminal = context.cgmes().terminal(terminalId());
        if (cgmesTerminal != null) {
            dl.addAlias(cgmesTerminal.id(), Conversion.CGMES_PREFIX_ALIAS + "EquivalentInjectionTerminal");
        }
        return dl;
    }

    private void convertToGenerator() {
        double minP = p.asDouble("minP", -Double.MAX_VALUE);
        double maxP = p.asDouble("maxP", Double.MAX_VALUE);
        EnergySource energySource = EnergySource.OTHER;

        Regulation regulation = getRegulation();
        GeneratorAdder adder = voltageLevel().newGenerator()
                .setMinP(minP)
                .setMaxP(maxP)
                .setVoltageRegulatorOn(regulation.status)
                .setTargetP(regulation.targetP)
                .setTargetQ(regulation.targetQ)
                .setTargetV(regulation.targetV)
                .setEnergySource(energySource);
        identify(adder);
        connect(adder);
        Generator g = adder.add();
        addAliases(g);
        convertedTerminals(g.getTerminal());
        convertReactiveLimits(g);
    }

    static class Regulation {
        private boolean status;
        private double targetV;
        private double targetP;
        private double targetQ;
    }

    private Regulation getRegulation() {
        Regulation regulation = new Regulation();

        boolean regulationCapability = p.asBoolean("regulationCapability", false);
        regulation.status = p.asBoolean("regulationStatus", false) && regulationCapability;
        if (!p.containsKey("regulationStatus") || !p.containsKey(REGULATION_TARGET)) {
            context.missing(String.format("Missing regulationStatus or regulationTarget for EquivalentInjection %s. Voltage regulation is considered as off.", id));
        }

        regulation.status = regulation.status && terminalConnected();
        regulation.targetV = Double.NaN;
        if (regulation.status) {
            regulation.targetV = p.asDouble(REGULATION_TARGET);
            if (regulation.targetV == 0) {
                fixed(REGULATION_TARGET, "Target voltage value can not be zero", regulation.targetV,
                        voltageLevel().getNominalV());
                regulation.targetV = voltageLevel().getNominalV();
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
}
