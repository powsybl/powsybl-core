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
public class NodeDataRemoved extends NodeEvent {

    public static final String NODE_DATA_REMOVED = "NODE_DATA_REMOVED";

    @JsonProperty("dataName")
    private final String dataName;

    @JsonCreator
    public NodeDataRemoved(@JsonProperty("id") String id,
                           @JsonProperty("dataName") String dataName) {
        super(id, NODE_DATA_REMOVED);
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
        if (obj instanceof NodeDataRemoved) {
            NodeDataRemoved other = (NodeDataRemoved) obj;
            return id.equals(other.id) && dataName.equals(other.dataName);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeDataRemoved(id=" + id + ", dataName=" + dataName + ")";
    }
}
