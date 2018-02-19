/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StartTaskEvent extends TaskEvent {

    @JsonProperty("name")
    private final String name;

    @JsonCreator
    public StartTaskEvent(@JsonProperty("taskId") UUID taskId, @JsonProperty("revision") long revision, @JsonProperty("name") String name) {
        super(taskId, revision);
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, revision, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StartTaskEvent) {
            StartTaskEvent other = (StartTaskEvent) obj;
            return taskId.equals(other.taskId) &&
                    revision == other.revision &&
                    name.equals(other.name);
        }
        return false;
    }

    @Override
    public String toString() {
        return "StartTaskEvent(taskId=" + taskId + ", revision=" + revision + ", name=" + name + ")";
    }
}
