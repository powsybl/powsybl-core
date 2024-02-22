/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Ground;
import com.powsybl.iidm.network.impl.util.Ref;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class GroundImpl extends AbstractConnectable<Ground> implements Ground {

    GroundImpl(Ref<NetworkImpl> networkRef,
               String id, String name) {
        super(networkRef, id, name, false);
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    public void setFictitious(boolean fictitious) {
        if (fictitious) {
            throw new PowsyblException("The ground cannot be fictitious.");
        } else {
            this.fictitious = false;
        }
    }

    @Override
    protected String getTypeDescription() {
        return "Ground";
    }
}
