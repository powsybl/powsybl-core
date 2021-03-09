/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class NodeNumberBasedBusBean extends AbstractBusRefBean {

    private final String voltageLevelId;

    private final int node;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public NodeNumberBasedBusBean(@JsonProperty("voltageLevelId") String voltageLevelId, @JsonProperty("node") int node) {
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
        this.node = node;
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    public int getNode() {
        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NodeNumberBasedBusBean)) {
            return false;
        }

        NodeNumberBasedBusBean that = (NodeNumberBasedBusBean) o;

        if (getNode() != that.getNode()) {
            return false;
        }
        return getVoltageLevelId().equals(that.getVoltageLevelId());
    }

    @Override
    public int hashCode() {
        int result = getVoltageLevelId().hashCode();
        result = 31 * result + getNode();
        return result;
    }
}
