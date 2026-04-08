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
import com.powsybl.iidm.network.DcLineAdder;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.Optional;

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

        DcLineAdder dcLineAdder = context.network().newDcLine()
                .setR(resistance)
                .setDcNode1(dcNode1)
                .setDcNode2(dcNode2)
                .setConnected1(true)
                .setConnected2(true);

        identify(dcLineAdder);

        DcLine dcLine = dcLineAdder.add();

        addTerminalsAlias(dcLine);
    }

    public static void update(DcLine dcLine, Context context) {
        Optional<Boolean> dcTerminalConnected1 = isDcTerminalConnected(dcLine, 1, context);
        boolean defaultConnected1 = getDefaultValue(null, dcLine.getDcTerminal1().isConnected(), true, true, context);
        boolean connected1 = dcTerminalConnected1.orElse(defaultConnected1);

        Optional<Boolean> dcTerminalConnected2 = isDcTerminalConnected(dcLine, 2, context);
        boolean defaultConnected2 = getDefaultValue(null, dcLine.getDcTerminal2().isConnected(), true, true, context);
        boolean connected2 = dcTerminalConnected2.orElse(defaultConnected2);

        dcLine.getDcTerminal1().setConnected(connected1);
        dcLine.getDcTerminal2().setConnected(connected2);
    }
}
