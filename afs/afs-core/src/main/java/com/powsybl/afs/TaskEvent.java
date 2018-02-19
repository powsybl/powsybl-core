/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY)
public class TaskEvent {

    @JsonProperty("taskId")
    protected final UUID taskId;

    @JsonProperty("revision")
    protected final long revision;

    protected TaskEvent(UUID taskId, long revision) {
        this.taskId = Objects.requireNonNull(taskId);
        this.revision = revision;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public long getRevision() {
        return revision;
    }
}
