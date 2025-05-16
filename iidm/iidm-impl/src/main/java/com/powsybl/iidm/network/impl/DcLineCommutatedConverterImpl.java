/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcLineCommutatedConverterImpl extends AbstractDcConverter<DcLineCommutatedConverter> implements DcLineCommutatedConverter {

    DcLineCommutatedConverterImpl(Ref<NetworkImpl> ref, String id, String name, boolean fictitious,
                                  double idleLoss, double switchingLoss, double resistiveLoss,
                                  TerminalExt pccTerminal, ControlMode controlMode, double targetP, double targetVdc) {
        super(ref, id, name, fictitious, idleLoss, switchingLoss, resistiveLoss,
                pccTerminal, controlMode, targetP, targetVdc);
    }

    @Override
    protected String getTypeDescription() {
        return "DC Line Commutated Converter";
    }
}
