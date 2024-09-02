/**
 Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class GroundConversion extends AbstractConductingEquipmentConversion {

    public GroundConversion(PropertyBag ec, Context context) {
        super("Ground", ec, context);
    }

    @Override
    public void convert() {
        GroundAdder adder = voltageLevel().newGround();
        identify(adder);
        connect(adder);
        Ground g = adder.add();
        addAliasesAndProperties(g);
        convertedTerminals(g.getTerminal());
    }
}
