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
 *
 * @author Nassirou Nambiena <nassirou.nambiena at rte-france.com>
 */
public class NodeNameUpdated extends NodeEvent {

    public static final String TYPE = "NODE_NAME_UPDATED";

    @JsonProperty("name")
    private final String name;

    @JsonCreator
    public NodeNameUpdated(@JsonProperty("id") String id,
                           @JsonProperty("name") String name) {
        super(id, TYPE);
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeNameUpdated) {
            NodeNameUpdated other = (NodeNameUpdated) obj;
            return id.equals(other.id) && Objects.equals(name, other.name);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeNameUpdated(id=" + id + ", name=" + name + ")";
    }
}
