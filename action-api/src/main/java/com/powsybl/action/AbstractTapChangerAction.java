/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.network.ThreeSides;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public abstract class AbstractTapChangerAction extends AbstractAction {

    private final String transformerId;
    private final ThreeSides side;

    protected AbstractTapChangerAction(String id, String transformerId, ThreeSides side) {
        super(id);
        this.transformerId = Objects.requireNonNull(transformerId);
        this.side = side;
    }

    public String getTransformerId() {
        return transformerId;
    }

    public Optional<ThreeSides> getSide() {
        return Optional.ofNullable(side);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AbstractTapChangerAction that = (AbstractTapChangerAction) o;
        return Objects.equals(transformerId, that.transformerId) && side == that.side;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), transformerId, side);
    }
}
