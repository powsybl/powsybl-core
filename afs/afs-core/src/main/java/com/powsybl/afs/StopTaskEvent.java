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
public class StopTaskEvent extends TaskEvent {

    @JsonCreator
    public StopTaskEvent(@JsonProperty("taskId") UUID taskId, @JsonProperty("revision") long revision) {
        super(taskId, revision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, revision);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StopTaskEvent) {
            StopTaskEvent other = (StopTaskEvent) obj;
            return taskId.equals(other.taskId) &&
                    revision == other.revision;
        }
        return false;
    }

    @Override
    public String toString() {
        return "StopTaskEvent(taskId=" + taskId + ", revision=" + revision + ")";
    }
}
