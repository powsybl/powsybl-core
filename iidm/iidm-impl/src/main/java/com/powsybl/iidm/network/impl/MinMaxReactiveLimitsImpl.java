/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.MinMaxReactiveLimits;
import com.powsybl.iidm.network.ReactiveLimitsKind;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class MinMaxReactiveLimitsImpl implements MinMaxReactiveLimits {

    private final float minQ;

    private final float maxQ;

    MinMaxReactiveLimitsImpl(float minQ, float maxQ) {
        this.minQ = minQ;
        this.maxQ = maxQ;
    }

    @Override
    public float getMinQ() {
        return minQ;
    }

    @Override
    public float getMaxQ() {
        return maxQ;
    }

    @Override
    public ReactiveLimitsKind getKind() {
        return ReactiveLimitsKind.MIN_MAX;
    }

    @Override
    public float getMinQ(float p) {
        return minQ;
    }

    @Override
    public float getMaxQ(float p) {
        return maxQ;
    }

}
