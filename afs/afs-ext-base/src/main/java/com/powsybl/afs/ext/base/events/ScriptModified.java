/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public class ScriptModified extends BusinessEvent {

    public static final String TYPE = "SCRIPT_MODIFIED";

    @JsonCreator
    public ScriptModified(@JsonProperty("id") String id, @JsonProperty("parentId") String parentId, @JsonProperty("path") String path) {
        super(id, parentId, path, TYPE);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ScriptModified) {
            ScriptModified other = (ScriptModified) obj;
            return id.equals(other.id) && Objects.equals(parentId, other.parentId) && path.equals(other.path);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentId);
    }

    @Override
    public String toString() {
        return "ScriptModified(id=" + id + ", parentId=" + parentId + " path=" + path + ")";
    }
}
