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
public class NodeDescriptionUpdated extends NodeEvent {

    public static final String TYPE = "NODE_DESCRIPTION_UPDATED";

    @JsonProperty("description")
    private final String description;

    @JsonCreator
    public NodeDescriptionUpdated(@JsonProperty("id") String id,
                                  @JsonProperty("description") String description) {
        super(id, TYPE);
        this.description = Objects.requireNonNull(description);
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeDescriptionUpdated) {
            NodeDescriptionUpdated other = (NodeDescriptionUpdated) obj;
            return id.equals(other.id) && Objects.equals(description, other.description);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeDescriptionUpdated(id=" + id + ", description=" + description + ")";
    }
}
