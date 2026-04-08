/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.GroundAdder;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class GroundAdderImpl extends AbstractInjectionAdder<GroundAdderImpl> implements GroundAdder {

    GroundAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    @Override
    protected String getTypeDescription() {
        return "Ground";
    }

    @Override
    public GroundImpl add() {
        NetworkImpl network = getNetwork();
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal();
        GroundImpl ground = new GroundImpl(getNetworkRef(), id, getName());
        ground.addTerminal(terminal);
        voltageLevel.getTopologyModel().attach(terminal, false);
        network.getIndex().checkAndAdd(ground);
        network.getListeners().notifyCreation(ground);
        return ground;
    }
}
