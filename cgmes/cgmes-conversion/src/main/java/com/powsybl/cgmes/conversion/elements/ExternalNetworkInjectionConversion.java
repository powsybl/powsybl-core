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
import com.powsybl.cgmes.conversion.RegulatingControlMappingForGenerators;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.triplestore.api.PropertyBag;

import static com.powsybl.cgmes.model.CgmesNames.EXTERNAL_NETWORK_INJECTION;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class ExternalNetworkInjectionConversion extends AbstractReactiveLimitsOwnerConversion {

    public ExternalNetworkInjectionConversion(PropertyBag sm, Context context) {
        super(EXTERNAL_NETWORK_INJECTION, sm, context);
    }

    @Override
    public void convert() {
        double minP = p.asDouble("minP", -Double.MAX_VALUE);
        double maxP = p.asDouble("maxP", Double.MAX_VALUE);
        double targetP = 0;
        double targetQ = 0;
        PowerFlow f = powerFlow();
        if (f.defined()) {
            targetP = -f.p();
            targetQ = -f.q();
        }

        GeneratorAdder adder = voltageLevel().newGenerator();
        RegulatingControlMappingForGenerators.initialize(adder);
        setMinPMaxP(adder, minP, maxP);
        adder.setTargetP(targetP)
                .setTargetQ(targetQ)
                .setEnergySource(EnergySource.OTHER);
        identify(adder);
        connect(adder);
        Generator g = adder.add();
        addAliasesAndProperties(g);
        convertedTerminals(g.getTerminal());
        convertReactiveLimits(g);

        context.regulatingControlMapping().forGenerators().add(g.getId(), p);

        addSpecificProperties(g, p);
    }

    private static void addSpecificProperties(Generator generator, PropertyBag p) {
        generator.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, EXTERNAL_NETWORK_INJECTION);
        double governorSCD = p.asDouble("governorSCD");
        if (!Double.isNaN(governorSCD)) {
            generator.setProperty(Conversion.PROPERTY_CGMES_GOVERNOR_SCD, String.valueOf(governorSCD));
        }
    }
}
