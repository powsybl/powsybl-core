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
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class EnergyConsumerConversion extends AbstractConductingEquipmentConversion {

    public EnergyConsumerConversion(PropertyBag ec, Context context) {
        super("EnergyConsumer", ec, context);
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
        convertedTerminals(load.getTerminal());
    }

    private double p0() {
        return p.containsKey("pfixed") ? p.asDouble("pfixed") : powerFlow().p();
    }

    private double q0() {
        return p.containsKey("qfixed") ? p.asDouble("qfixed") : powerFlow().q();
    }
}
