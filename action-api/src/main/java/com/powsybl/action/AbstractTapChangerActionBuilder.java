/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.network.ThreeSides;

import java.util.Optional;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public abstract class AbstractTapChangerActionBuilder<T extends ActionBuilder<T>> implements ActionBuilder<T> {

    private String id;
    private String transformerId;
    private ThreeSides side;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public T withId(String id) {
        this.id = id;
        return (T) this;
    }

    @Override
    public T withNetworkElementId(String elementId) {
        this.transformerId = elementId;
        return (T) this;
    }

    public T withTransformerId(String transformerId) {
        return withNetworkElementId(transformerId);
    }

    public T withSide(ThreeSides side) {
        this.side = side;
        return (T) this;
    }

    public String getTransformerId() {
        return transformerId;
    }

    public Optional<ThreeSides> getSide() {
        return Optional.ofNullable(side);
    }
}
