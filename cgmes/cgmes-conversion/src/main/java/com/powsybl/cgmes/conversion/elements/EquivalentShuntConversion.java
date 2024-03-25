/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class EquivalentShuntConversion extends AbstractConductingEquipmentConversion {

    public EquivalentShuntConversion(PropertyBag p, Context context) {
        super("EquivalentShunt", p, context);
    }

    @Override
    public void convert() {
        ShuntCompensatorAdder adder = voltageLevel().newShuntCompensator()
                .setSectionCount(terminalConnected() ? 1 : 0)
                .newLinearModel()
                    .setGPerSection(p.asDouble("g"))
                    .setBPerSection(p.asDouble("b"))
                    .setMaximumSectionCount(1)
                    .add();
        identify(adder);
        connect(adder);
        ShuntCompensator sc = adder.add();
        sc.setProperty(Conversion.PROPERTY_IS_EQUIVALENT_SHUNT, "true");
        addAliasesAndProperties(sc);
    }
}
