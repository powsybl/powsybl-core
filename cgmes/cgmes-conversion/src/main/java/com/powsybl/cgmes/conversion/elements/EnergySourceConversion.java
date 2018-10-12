/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class EnergySourceConversion extends AbstractConductingEquipmentConversion {

    public EnergySourceConversion(PropertyBag es, Conversion.Context context) {
        super("EnergySource", es, context);
    }

    @Override
    public void convert() {
        LoadType loadType = id.contains("fict") ? LoadType.FICTITIOUS : LoadType.UNDEFINED;
        PowerFlow f = powerFlow();

        Load load = voltageLevel().newLoad()
                .setId(iidmId())
                .setName(iidmName())
                .setEnsureIdUnicity(false)
                .setBus(terminalConnected() ? busId() : null)
                .setConnectableBus(busId())
                .setP0(f.p())
                .setQ0(f.q())
                .setLoadType(loadType)
                .add();

        convertedTerminals(load.getTerminal());
    }
}
