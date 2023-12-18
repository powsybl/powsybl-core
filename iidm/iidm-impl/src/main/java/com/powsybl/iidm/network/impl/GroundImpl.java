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
import com.powsybl.iidm.network.Network;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class GroundImpl extends AbstractIdentifiable<Ground> implements Ground {

    private final VoltageLevelExt voltageLevel;

    GroundImpl(VoltageLevelExt voltageLevel, String id, String name) {
        super(id, name, false);
        this.voltageLevel = voltageLevel;
    }

    @Override
    public void setFictitious(boolean fictitious) {
        throw new PowsyblException("The ground cannot be fictitious.");
    }

    @Override
    public NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    @Override
    public Network getParentNetwork() {
        return voltageLevel.getParentNetwork();
    }

    @Override
    public VoltageLevelExt getVoltageLevel() {
        return voltageLevel;
    }

    @Override
    protected String getTypeDescription() {
        return "Ground";
    }
}
