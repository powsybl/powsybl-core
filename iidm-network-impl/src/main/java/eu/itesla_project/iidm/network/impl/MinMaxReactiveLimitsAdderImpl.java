/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.MinMaxReactiveLimits;
import eu.itesla_project.iidm.network.MinMaxReactiveLimitsAdder;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class MinMaxReactiveLimitsAdderImpl<OWNER extends ReactiveLimitsOwner & Validable> implements MinMaxReactiveLimitsAdder {

    private final OWNER owner;

    private float minQ = Float.NaN;

    private float maxQ = Float.NaN;

    MinMaxReactiveLimitsAdderImpl(OWNER owner) {
        this.owner = owner;
    }

    @Override
    public MinMaxReactiveLimitsAdder setMinQ(float minQ) {
        this.minQ = minQ;
        return this;
    }

    @Override
    public MinMaxReactiveLimitsAdder setMaxQ(float maxQ) {
        this.maxQ = maxQ;
        return this;
    }

    @Override
    public MinMaxReactiveLimits add() {
        if (Float.isNaN(minQ)) {
            throw new ValidationException(owner, "minimum reactive power is not set");
        }
        if (Float.isNaN(maxQ)) {
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
