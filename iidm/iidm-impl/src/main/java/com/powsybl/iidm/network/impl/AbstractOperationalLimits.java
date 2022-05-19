/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.OperationalLimits;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractOperationalLimits<L extends OperationalLimits> implements OperationalLimits {

    private String id;
    private AbstractOperationalLimitsSet<L> set;

    @Override
    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    @Override
    public void setId(String id) {
        // TODO: check no same id in the set
        this.id = Objects.requireNonNull(id);
    }

    void setLimitSet(AbstractOperationalLimitsSet<L> set) {
        this.set = set;
    }

    AbstractOperationalLimits(String id) {
        this.id = id;
    }

    @Override
    public void remove() {
        set.removeLimit((L) this);
        this.set = null;
    }
}
