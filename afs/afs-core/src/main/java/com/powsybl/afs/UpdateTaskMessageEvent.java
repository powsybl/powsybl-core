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
public class UpdateTaskMessageEvent extends TaskEvent {

    @JsonProperty("message")
    private final String message;

    @JsonCreator
    public UpdateTaskMessageEvent(@JsonProperty("taskId") UUID taskId, @JsonProperty("revision") long revision, @JsonProperty("message") String message) {
        super(taskId, revision);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, revision, message);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UpdateTaskMessageEvent) {
            UpdateTaskMessageEvent other = (UpdateTaskMessageEvent) obj;
            return taskId.equals(other.taskId) &&
                    revision == other.revision &&
                    Objects.equals(message, other.message);
        }
        return false;
    }

    @Override
    public String toString() {
        return "UpdateTaskMessageEvent(taskId=" + taskId + ", revision=" + revision + ", message=" + message + ")";
    }
}
