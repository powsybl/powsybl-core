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
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadAdder;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class EnergySourceConversion extends AbstractConductingEquipmentConversion {

    public EnergySourceConversion(PropertyBag es, Context context) {
        super(CgmesNames.ENERGY_SOURCE, es, context);
    }

    @Override
    public void convert() {
        LoadType loadType = id.contains("fict") ? LoadType.FICTITIOUS : LoadType.UNDEFINED;

        LoadAdder adder = voltageLevel().newLoad()
                .setP0(0.0)
                .setQ0(0.0)
                .setLoadType(loadType);
        identify(adder);
        connect(adder);
        Load load = adder.add();
        addAliasesAndProperties(load);
        convertedTerminals(load.getTerminal());

        addSpecificProperties(load);
    }

    private static void addSpecificProperties(Load load) {
        load.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.ENERGY_SOURCE);
    }

    public void update(Network network) {
        //super.update(network); TODO JAM delete
        Load load = network.getLoad(id);
        if (load == null) {
            return;
        }
        load.setP0(p0()).setQ0(q0());
    }
}
