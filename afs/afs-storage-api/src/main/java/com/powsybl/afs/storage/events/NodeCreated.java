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
public class NodeCreated extends NodeEvent {

    public static final String TYPE = "NODE_CREATED";

    @JsonProperty("parentId")
    protected final String parentId;

    @JsonCreator
    public NodeCreated(@JsonProperty("id") String id, @JsonProperty("parentId") String parentId) {
        super(id, TYPE);
        this.parentId = parentId;
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
        if (obj instanceof NodeCreated) {
            NodeCreated other = (NodeCreated) obj;
            return id.equals(other.id) && Objects.equals(parentId, other.parentId);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeCreated(id=" + id + ", parentId=" + parentId + ")";
    }
}
