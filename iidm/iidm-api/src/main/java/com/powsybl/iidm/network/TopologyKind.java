/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * The kind of topology.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum TopologyKind {
    BUS_BREAKER(0),
    NODE_BREAKER(1);

    private Integer level;

    TopologyKind(int level) {
        this.level = level;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public static TopologyKind min(TopologyKind first, TopologyKind second) {
        int min = Math.min(first.getLevel(), second.getLevel());
        return TopologyKind.valueOf(min);
    }

    public static TopologyKind valueOf(int level) {
        for (TopologyKind topologyKind : values()) {
            if (topologyKind.level == level) {
                return topologyKind;
            }
        }
        throw new IllegalArgumentException("level of TopologyKind" + String.valueOf(level));
    }
}
