/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeDataUpdated extends NodeEvent {

    public static final String TYPE = "NODE_DATA_UPDATED";

    @JsonProperty("dataName")
    private final String dataName;

    @JsonCreator
    public NodeDataUpdated(@JsonProperty("id") String id,
                           @JsonProperty("dataName") String dataName) {
        super(id, TYPE);
        this.dataName = Objects.requireNonNull(dataName);
    }

    public String getDataName() {
        return dataName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dataName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeDataUpdated) {
            NodeDataUpdated other = (NodeDataUpdated) obj;
            return id.equals(other.id) && dataName.equals(other.dataName);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeDataUpdated(id=" + id + ", dataName=" + dataName + ")";
    }
}
