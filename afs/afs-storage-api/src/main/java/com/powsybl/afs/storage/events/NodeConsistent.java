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
 * @author Benhamed Chamseddine <benhamed.chamseddine at rte-france.com>
 */
public class NodeConsistent extends NodeEvent {

    public static final String TYPE = "NODE_CONSISTENT";

    @JsonCreator
    public NodeConsistent(@JsonProperty("id") String id) {
        super(id, TYPE);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeConsistent) {
            NodeConsistent other = (NodeConsistent) obj;
            return id.equals(other.id);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeConsistent(id=" + id + ")";
    }
}
