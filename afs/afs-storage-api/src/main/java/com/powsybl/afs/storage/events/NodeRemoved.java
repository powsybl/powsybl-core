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
public class NodeRemoved extends NodeEvent {

    public static final String NODE_REMOVED = "NODE_REMOVED";

    @JsonProperty("parentId")
    protected final String parentId;

    @JsonCreator
    public NodeRemoved(@JsonProperty("id") String id, @JsonProperty("parentId") String parentId) {
        super(id, NODE_REMOVED);
        this.parentId = Objects.requireNonNull(parentId);
    }

    public String getParentId() {
        return parentId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeRemoved) {
            NodeRemoved other = (NodeRemoved) obj;
            return id.equals(other.id) && parentId.equals(other.parentId);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeRemoved(id=" + id + ", parentId=" + parentId + ")";
    }
}
