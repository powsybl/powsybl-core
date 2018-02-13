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
public class StartTaskEvent extends TaskEvent {

    private final String name;

    public StartTaskEvent(UUID taskId, long revision, String name) {
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
}
