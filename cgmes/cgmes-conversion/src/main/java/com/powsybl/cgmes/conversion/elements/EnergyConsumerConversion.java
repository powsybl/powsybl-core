/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadAdder;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.extensions.LoadDetailAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class EnergyConsumerConversion extends AbstractConductingEquipmentConversion {

    public EnergyConsumerConversion(PropertyBag ec, Context context) {
        super("EnergyConsumer", ec, context);
        loadKind = ec.get("type");
    }

    @Override
    public void convert() {
        LoadType loadType = id.contains("fict") ? LoadType.FICTITIOUS : LoadType.UNDEFINED;
        LoadAdder adder = voltageLevel().newLoad()
                .setP0(p0())
                .setQ0(q0())
                .setLoadType(loadType);
        identify(adder);
        connect(adder);
        Load load = adder.add();
        addAliasesAndProperties(load);
        convertedTerminals(load.getTerminal());
        setLoadDetail(loadKind, load);
    }

    private static void setLoadDetail(String type, Load load) {
        if (type.endsWith("#ConformLoad")) { // ConformLoad represent loads that follow a daily load change pattern where the pattern can be used to scale the load with a system load
            load.newExtension(LoadDetailAdder.class)
                    .withFixedActivePower(0)
                    .withFixedReactivePower(0)
                    .withVariableActivePower((float) load.getP0())
                    .withVariableReactivePower((float) load.getQ0())
                    .add();
        } else if (type.endsWith("#NonConformLoad")) { // does not participate in scaling
            load.newExtension(LoadDetailAdder.class)
                    .withFixedActivePower((float) load.getP0())
                    .withFixedReactivePower((float) load.getQ0())
                    .withVariableActivePower(0)
                    .withVariableReactivePower(0)
                    .add();
        }
        // else: EnergyConsumer - undefined
    }

    @Override
    protected double p0() {
        return powerFlow().defined() ? powerFlow().p() : p.asDouble("pFixed", context.defaultValue());
    }

    @Override
    protected double q0() {
        return powerFlow().defined() ? powerFlow().q() : p.asDouble("qFixed", context.defaultValue());
    }

    private final String loadKind;
}
