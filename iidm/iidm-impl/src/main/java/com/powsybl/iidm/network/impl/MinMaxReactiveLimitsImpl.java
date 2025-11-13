/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.MinMaxReactiveLimits;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReactiveLimitsKind;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class MinMaxReactiveLimitsImpl extends AbstractPropertiesHolder implements MinMaxReactiveLimits {

    private final double minQ;

    private final double maxQ;

    private Network network;

    MinMaxReactiveLimitsImpl(Network network, double minQ, double maxQ) {
        this.minQ = minQ;
        this.maxQ = maxQ;
        this.network = network;
    }

    @Override
    public double getMinQ() {
        return minQ;
    }

    @Override
    public double getMaxQ() {
        return maxQ;
    }

    @Override
    public ReactiveLimitsKind getKind() {
        return ReactiveLimitsKind.MIN_MAX;
    }

    @Override
    public double getMinQ(double p) {
        return minQ;
    }

    @Override
    public double getMaxQ(double p) {
        return maxQ;
    }

    @Override
    public Network getNetwork() {
        return network;
    }
}
