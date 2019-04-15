/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ReactiveLimits;

import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@gmail.com>
 */
class ReactiveLimitsHolderImpl implements ReactiveLimitsOwner {

    private Validable validable;

    private ReactiveLimits reactiveLimits;

    public ReactiveLimitsHolderImpl(Validable validable, ReactiveLimits reactiveLimits) {
        this.validable = Objects.requireNonNull(validable);
        this.reactiveLimits = Objects.requireNonNull(reactiveLimits);
    }

    public ReactiveLimits getReactiveLimits() {
        return reactiveLimits;
    }

    public <L extends ReactiveLimits> L getReactiveLimits(Class<L> type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        if (type.isInstance(reactiveLimits)) {
            return type.cast(reactiveLimits);
        } else {
            throw new ValidationException(validable, "incorrect reactive limits type "
                    + type.getName() + ", expected " + reactiveLimits.getClass());
        }
    }

    @Override
    public void setReactiveLimits(ReactiveLimits reactiveLimits) {
        this.reactiveLimits = Objects.requireNonNull(reactiveLimits);
    }
}
