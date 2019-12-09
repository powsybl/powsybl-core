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
public class TaskCancelableStatusChangeEvent extends TaskEvent {

    @JsonProperty("cancelable")
    private final boolean cancelable;

    @JsonCreator
    public TaskCancelableStatusChangeEvent(@JsonProperty("taskId") UUID taskId, @JsonProperty("revision") long revision, @JsonProperty("cancelable") boolean cancelable) {
        super(taskId, revision);
        this.cancelable = cancelable;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, revision, cancelable);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TaskCancelableStatusChangeEvent) {
            TaskCancelableStatusChangeEvent other = (TaskCancelableStatusChangeEvent) obj;
            return taskId.equals(other.taskId) &&
                    revision == other.revision &&
                    cancelable == other.cancelable;
        }
        return false;
    }

    @Override
    public String toString() {
        return "TaskCancelableStatusChangeEvent(taskId=" + taskId + ", revision=" + revision + ", cancelable=" + cancelable + ")";
    }
}
