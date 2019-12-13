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
public class DependencyAdded extends NodeEvent implements DependencyEvent {

    public static final String TYPE = "DEPENDENCY_ADDED";

    @JsonProperty("dependencyName")
    private final String dependencyName;

    @JsonCreator
    public DependencyAdded(@JsonProperty("id") String id,
                           @JsonProperty("dependencyName") String dependencyName) {
        super(id, TYPE);
        this.dependencyName = Objects.requireNonNull(dependencyName);
    }

    @Override
    public String getDependencyName() {
        return dependencyName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dependencyName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DependencyAdded) {
            DependencyAdded other = (DependencyAdded) obj;
            return id.equals(other.id) && dependencyName.equals(other.dependencyName);
        }
        return false;
    }

    @Override
    public String toString() {
        return "DependencyAdded(id=" + id + ", dependencyName=" + dependencyName + ")";
    }
}
