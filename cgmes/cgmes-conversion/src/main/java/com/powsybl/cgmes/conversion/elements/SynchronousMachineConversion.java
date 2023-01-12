/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForGenerators;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class SynchronousMachineConversion extends AbstractReactiveLimitsOwnerConversion {

    public SynchronousMachineConversion(PropertyBag sm, Context context) {
        super("SynchronousMachine", sm, context);
    }

    @Override
    public void convert() {
        double minP = p.asDouble("minP", -Double.MAX_VALUE);
        double maxP = p.asDouble("maxP", Double.MAX_VALUE);
        double ratedS = p.asDouble("ratedS");
        ratedS = ratedS > 0 ? ratedS : Double.NaN;
        String generatingUnitType = p.getLocal("generatingUnitType");
        PowerFlow f = powerFlow();

        // Default targetP from initial P defined in EQ GeneratingUnit. Removed since CGMES 3.0
        double targetP = p.asDouble("initialP", 0);
        double targetQ = 0;
        // Flow values may come from Terminal or Equipment (SSH RotatingMachine)
        if (f.defined()) {
            targetP = -f.p();
            targetQ = -f.q();
        }

        GeneratorAdder adder = voltageLevel().newGenerator();
        RegulatingControlMappingForGenerators.initialize(adder);
        setMinPMaxP(adder, minP, maxP);
        adder.setTargetP(targetP)
                .setTargetQ(targetQ)
                .setEnergySource(fromGeneratingUnitType(generatingUnitType))
                .setRatedS(ratedS);
        identify(adder);
        connect(adder);
        Generator g = adder.add();
        addAliasesAndProperties(g);
        convertedTerminals(g.getTerminal());
        convertReactiveLimits(g);
        if (p.asInt("referencePriority", 0) > 0) {
            // We could find multiple generators with the same priority,
            // we will only change the terminal of the slack extension if the previous was not connected
            SlackTerminal st = g.getTerminal().getVoltageLevel().getExtension(SlackTerminal.class);
            if (st == null) {
                SlackTerminal.reset(g.getTerminal().getVoltageLevel(), g.getTerminal());
            } else if (!st.getTerminal().isConnected()) {
                st.setTerminal(g.getTerminal());
            }
        }
        double normalPF = p.asDouble("normalPF");
        if (!Double.isNaN(normalPF)) {
            g.newExtension(ActivePowerControlAdder.class)
                    .withParticipate(normalPF != 0.0)
                    .withParticipationFactor(normalPF)
                    .add();
        }
        String generatingUnit = p.getId("GeneratingUnit");
        if (generatingUnit != null) {
            g.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit", generatingUnit);
        }

        context.regulatingControlMapping().forGenerators().add(g.getId(), p);
    }

    private static EnergySource fromGeneratingUnitType(String gut) {
        EnergySource es = EnergySource.OTHER;
        if (gut.contains("HydroGeneratingUnit")) {
            es = EnergySource.HYDRO;
        } else if (gut.contains("NuclearGeneratingUnit")) {
            es = EnergySource.NUCLEAR;
        } else if (gut.contains("ThermalGeneratingUnit")) {
            es = EnergySource.THERMAL;
        } else if (gut.contains("WindGeneratingUnit")) {
            es = EnergySource.WIND;
        } else if (gut.contains("SolarGeneratingUnit")) {
            es = EnergySource.SOLAR;
        }
        return es;
    }
}
