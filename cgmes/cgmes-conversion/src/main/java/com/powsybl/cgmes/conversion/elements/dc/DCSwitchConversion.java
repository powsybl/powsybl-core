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
import com.powsybl.iidm.network.DcSwitch;
import com.powsybl.iidm.network.DcSwitchKind;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

public class DCSwitchConversion extends AbstractDCConductingEquipmentConversion {

    public DCSwitchConversion(PropertyBag p, Context context) {
        super(CgmesNames.DC_SWITCH, p, context, 2);
    }

    @Override
    public void convert() {
        DcSwitchKind kind = "DCBreaker".equals(p.getLocal("type")) ? DcSwitchKind.BREAKER : DcSwitchKind.DISCONNECTOR;

        boolean open = !(dcConnected1 && dcConnected2);

        DcSwitch dcSwitch = context.network().newDcSwitch()
                .setId(id)
                .setName(name)
                .setKind(kind)
                .setOpen(open)
                .setDcNode1(dcNode1)
                .setDcNode2(dcNode2)
                .add();

        addTerminalsAlias(dcSwitch);
    }
}
