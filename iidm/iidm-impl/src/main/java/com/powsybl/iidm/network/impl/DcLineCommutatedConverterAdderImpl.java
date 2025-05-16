/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcLineCommutatedConverterAdderImpl extends AbstractDcConverterAdder<DcLineCommutatedConverterAdderImpl> implements DcLineCommutatedConverterAdder {

    DcLineCommutatedConverterAdderImpl(VoltageLevelExt voltageLevel) {
        super(voltageLevel);
    }

    @Override
    protected String getTypeDescription() {
        return "DC Line Commutated Converter";
    }

    @Override
    public DcLineCommutatedConverter add() {
        // TODO checks
        // TODO / note: dcNodes and voltage level must be in same network
        String id = checkAndGetUniqueId();
        super.preCheck();
        DcLineCommutatedConverterImpl dcCsConverter = new DcLineCommutatedConverterImpl(voltageLevel.getNetworkRef(), id, getName(), isFictitious(),
                idleLoss, switchingLoss, resistiveLoss,
                pccTerminal, controlMode, targetP, targetVdc);
        super.checkAndAdd(dcCsConverter);
        return dcCsConverter;
    }
}
