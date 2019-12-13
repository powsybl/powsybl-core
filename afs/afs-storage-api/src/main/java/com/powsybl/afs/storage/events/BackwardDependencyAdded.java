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
public class BackwardDependencyAdded extends NodeEvent implements DependencyEvent {

    @JsonProperty("dependencyName")
    private final String dependencyName;

    public static final String TYPE = "BACKWARD_DEPENDENCY_ADDED";

    @JsonCreator
    public BackwardDependencyAdded(@JsonProperty("id") String id,
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
        if (obj instanceof BackwardDependencyAdded) {
            BackwardDependencyAdded other = (BackwardDependencyAdded) obj;
            return id.equals(other.id) && dependencyName.equals(other.dependencyName);
        }
        return false;
    }

    @Override
    public String toString() {
        return "BackwardDependencyAdded(id=" + id + ", dependencyName=" + dependencyName + ")";
    }
}
