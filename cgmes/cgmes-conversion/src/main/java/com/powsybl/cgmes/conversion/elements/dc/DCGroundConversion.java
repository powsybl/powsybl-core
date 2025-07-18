/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.DcGround;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

public class DCGroundConversion extends AbstractDCConductingEquipmentConversion {

    public DCGroundConversion(PropertyBag p, Context context) {
        super(CgmesNames.DC_GROUND, p, context, 1);
    }

    @Override
    public void convert() {
        double resistance = p.asPositiveDouble("r");

        DcGround dcGround = context.network().newDcGround()
                .setId(id)
                .setName(name)
                .setR(resistance)
                .setDcNode(dcNode1)
                .setConnected(dcConnected1)
                .add();

        addTerminalsAlias(dcGround);
    }
}
