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
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
public class TaskCancellableStatusChangeEvent extends TaskEvent {

    @JsonProperty("cancellable")
    private final boolean cancellable;

    @JsonCreator
    public TaskCancellableStatusChangeEvent(@JsonProperty("taskId") UUID taskId, @JsonProperty("revision") long revision, @JsonProperty("cancellable") boolean cancellable) {
        super(taskId, revision);
        this.cancellable = cancellable;
    }

    public boolean isCancellable() {
        return cancellable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, revision, cancellable);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TaskCancellableStatusChangeEvent) {
            TaskCancellableStatusChangeEvent other = (TaskCancellableStatusChangeEvent) obj;
            return taskId.equals(other.taskId) &&
                    revision == other.revision &&
                    cancellable == other.cancellable;
        }
        return false;
    }

    @Override
    public String toString() {
        return "TaskCancelableStatusChangeEvent(taskId=" + taskId + ", revision=" + revision + ", cancellable=" + cancellable + ")";
    }
}
