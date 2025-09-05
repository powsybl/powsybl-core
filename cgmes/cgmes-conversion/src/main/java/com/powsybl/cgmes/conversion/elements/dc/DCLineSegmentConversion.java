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
import com.powsybl.iidm.network.DcLine;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

public class DCLineSegmentConversion extends AbstractDCConductingEquipmentConversion {

    public DCLineSegmentConversion(PropertyBag p, Context context) {
        super(CgmesNames.DC_LINE_SEGMENT, p, context, 2);
    }

    @Override
    public void convert() {
        double resistance = p.asPositiveDouble("r");

        DcLine dcLine = context.network().newDcLine()
                .setId(id)
                .setName(name)
                .setR(resistance)
                .setDcNode1(dcNode1)
                .setDcNode2(dcNode2)
                .setConnected1(dcConnected1)
                .setConnected2(dcConnected2)
                .add();

        addTerminalsAlias(dcLine);
    }
}
