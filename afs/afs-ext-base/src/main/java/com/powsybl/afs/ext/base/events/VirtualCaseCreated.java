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
public class VirtualCaseCreated extends BusinessEvent {

    private static final String VIRTUAL_CASE_CREATED = "VIRTUAL_CASE_CREATED";

    @JsonCreator
    public VirtualCaseCreated(@JsonProperty("id") String id, @JsonProperty("parentId") String parentId,
                              @JsonProperty("path") String path) {
        super(id, parentId, path, VIRTUAL_CASE_CREATED);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VirtualCaseCreated) {
            VirtualCaseCreated other = (VirtualCaseCreated) obj;
            return id.equals(other.id) && Objects.equals(parentId, other.parentId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentId);
    }

    @Override
    public String toString() {
        return "VirtualCaseCreated(id=" + id + ", parentId=" + parentId + ")";
    }
}
