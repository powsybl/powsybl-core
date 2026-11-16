/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.MinMaxReactiveLimits;
import com.powsybl.iidm.network.MinMaxReactiveLimitsAdder;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class MinMaxReactiveLimitsAdderImpl<O extends ReactiveLimitsOwner & Validable> implements MinMaxReactiveLimitsAdder {

    private final O owner;

    private double minQ = Double.NaN;

    private double maxQ = Double.NaN;

    MinMaxReactiveLimitsAdderImpl(O owner) {
        this.owner = owner;
    }

    @Override
    public MinMaxReactiveLimitsAdder setMinQ(double minQ) {
        this.minQ = minQ;
        return this;
    }

    @Override
    public MinMaxReactiveLimitsAdder setMaxQ(double maxQ) {
        this.maxQ = maxQ;
        return this;
    }

    @Override
    public MinMaxReactiveLimits add() {
        if (Double.isNaN(minQ)) {
            throw new ValidationException(owner, "minimum reactive power is not set");
        }
        if (Double.isNaN(maxQ)) {
            throw new ValidationException(owner, "maximum reactive power is not set");
        }
        if (maxQ < minQ) {
            throw new ValidationException(owner, "maximum reactive power is expected to be greater than or equal to minimum reactive power");
        }
        MinMaxReactiveLimitsImpl limits = new MinMaxReactiveLimitsImpl(minQ, maxQ);
        owner.setReactiveLimits(limits);
        return limits;
    }

}
