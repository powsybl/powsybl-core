/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

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

    public TopologyKind toTopologyKind() {
        return topologyKind;
    }

    public static TopologyLevel fromTopologyKind(final TopologyKind topologyKind) {
        switch (topologyKind) {
            case NODE_BREAKER:
                return TopologyLevel.NODE_BREAKER;
            case BUS_BREAKER:
                return TopologyLevel.BUS_BREAKER;
            default:
                return TopologyLevel.NODE_BREAKER;
        }
    }
}
