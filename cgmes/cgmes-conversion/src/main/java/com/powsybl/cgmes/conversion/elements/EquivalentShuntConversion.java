/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.iidm.network.ShuntCompensatorAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class EquivalentShuntConversion extends AbstractConductingEquipmentConversion {

    public EquivalentShuntConversion(PropertyBag p, Context context) {
        super("EquivalentShunt", p, context);
    }

    @Override
    public void convert() {
        ShuntCompensatorAdder adder = voltageLevel().newShuntCompensator()
                .setbPerSection(p.asDouble("b"))
                .setMaximumSectionCount(1)
                .setCurrentSectionCount(terminalConnected() ? 1 : 0);
        identify(adder);
        connect(adder);
        adder.add();
    }
}
