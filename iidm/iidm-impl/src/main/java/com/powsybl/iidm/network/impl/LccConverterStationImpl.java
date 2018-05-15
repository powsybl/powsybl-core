/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.LccConverterStation;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class LccConverterStationImpl extends AbstractHvdcConverterStation<LccConverterStation> implements LccConverterStation {

    static final String TYPE_DESCRIPTION = "lccConverterStation";

    private float powerFactor;

    LccConverterStationImpl(String id, String name, float lossFactor, float powerFactor) {
        super(id, name, lossFactor);
        this.powerFactor = powerFactor;
    }

    @Override
    public HvdcType getHvdcType() {
        return HvdcType.LCC;
    }

    @Override
    protected String getTypeDescription() {
        return TYPE_DESCRIPTION;
    }

    @Override
    public float getPowerFactor() {
        return powerFactor;
    }

    @Override
    public LccConverterStation setPowerFactor(float powerFactor) {
        ValidationUtil.checkPowerFactor(this, powerFactor);
        float oldValue = this.powerFactor;
        this.powerFactor = powerFactor;
        notifyUpdate("powerFactor", oldValue, powerFactor);
        return this;
    }
}
