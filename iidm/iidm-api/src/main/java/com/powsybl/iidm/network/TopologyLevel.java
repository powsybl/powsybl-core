/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;

/**
 * The level of topology.
 *
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public enum TopologyLevel {
    NODE_BREAKER(TopologyKind.NODE_BREAKER),
    BUS_BREAKER(TopologyKind.BUS_BREAKER),
    BUS_BRANCH(TopologyKind.BUS_BREAKER);

    private final TopologyKind topologyKind;

    TopologyLevel(TopologyKind topologyKind) {
        this.topologyKind = Objects.requireNonNull(topologyKind);
    }

    public TopologyKind getTopologyKind() {
        return topologyKind;
    }

    /**
     * Return the least detailed topology level between a TopologyKind and a TopologyLevel
     */
    public static TopologyLevel min(TopologyKind topologyKind, TopologyLevel topologyLevel) {
        Objects.requireNonNull(topologyKind);
        Objects.requireNonNull(topologyLevel);

        TopologyLevel level = (topologyKind == TopologyKind.NODE_BREAKER) ? TopologyLevel.NODE_BREAKER : TopologyLevel.BUS_BREAKER;
        return Collections.max(EnumSet.of(level, topologyLevel));
    }
}
