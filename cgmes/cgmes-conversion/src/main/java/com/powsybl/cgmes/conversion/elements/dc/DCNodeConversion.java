/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.elements.AbstractIdentifiedObjectConversion;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class DCNodeConversion extends AbstractIdentifiedObjectConversion {

    double nominalV;

    public DCNodeConversion(PropertyBag p, String nodeClass, double nominalV, Context context) {
        super(nodeClass, p, context);
        this.nominalV = nominalV;
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public void convert() {
        context.network().newDcNode()
                .setId(id)
                .setName(name)
                .setNominalV(nominalV)
                .add();
    }
}
