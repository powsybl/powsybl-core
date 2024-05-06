/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ReactiveLimits;
import com.powsybl.iidm.network.ValidationException;

import java.util.Objects;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@gmail.com>}
 */
class ReactiveLimitsHolderImpl implements ReactiveLimitsOwner {

    private final AbstractConnectable<?> connectable;

    private ReactiveLimits reactiveLimits;

    public ReactiveLimitsHolderImpl(AbstractConnectable<?> connectable, ReactiveLimits reactiveLimits) {
        this.connectable = Objects.requireNonNull(connectable);
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
            throw new ValidationException(connectable, "incorrect reactive limits type "
                    + type.getName() + ", expected " + reactiveLimits.getClass());
        }
    }

    @Override
    public NetworkImpl getNetwork() {
        return connectable.getNetwork();
    }

    @Override
    public void setReactiveLimits(ReactiveLimits reactiveLimits) {
        ReactiveLimits oldValue = this.reactiveLimits;
        this.reactiveLimits = Objects.requireNonNull(reactiveLimits);
        connectable.notifyUpdate("reactiveLimits", oldValue, reactiveLimits);
    }
}
