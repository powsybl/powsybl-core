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
import com.powsybl.iidm.network.DcSwitchAdder;
import com.powsybl.iidm.network.DcSwitchKind;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.Optional;

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

        DcSwitchAdder dcSwitchAdder = context.network().newDcSwitch()
                .setKind(kind)
                .setOpen(false)
                .setDcNode1(dcNode1)
                .setDcNode2(dcNode2);

        identify(dcSwitchAdder);

        DcSwitch dcSwitch = dcSwitchAdder.add();

        addTerminalsAlias(dcSwitch);
    }

    public static void update(DcSwitch dcSwitch, Context context) {
        Optional<Boolean> dcTerminalConnected1 = isDcTerminalConnected(dcSwitch, TwoSides.ONE, context);
        Optional<Boolean> dcTerminalConnected2 = isDcTerminalConnected(dcSwitch, TwoSides.TWO, context);
        Optional<Boolean> isOpenFromAtLeastOneTerminal = dcTerminalConnected1.flatMap(c1 -> dcTerminalConnected2.map(c2 -> !c1 || !c2))
                .or(() -> dcTerminalConnected1.map(c1 -> !c1))
                .or(() -> dcTerminalConnected2.map(c2 -> !c2));

        boolean defaultOpen = getDefaultValue(null, dcSwitch.isOpen(), false, false, context);

        boolean open = isOpenFromAtLeastOneTerminal.orElse(defaultOpen);

        dcSwitch.setOpen(open);
    }
}
