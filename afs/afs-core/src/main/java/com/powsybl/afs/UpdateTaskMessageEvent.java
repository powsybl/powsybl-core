/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UpdateTaskMessageEvent extends TaskEvent {

    private final String message;

    public UpdateTaskMessageEvent(UUID taskId, long revision, String message) {
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
}
