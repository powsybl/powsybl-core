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
public class TaskMonitorLogger implements AppLogger {

    private final TaskMonitor taskMonitor;

    private final UUID taskId;

    public TaskMonitorLogger(TaskMonitor taskMonitor, UUID taskId) {
        this.taskMonitor = Objects.requireNonNull(taskMonitor);
        this.taskId = Objects.requireNonNull(taskId);
    }

    @Override
    public void log(String message, Object... args) {
        Objects.requireNonNull(message);
        taskMonitor.updateTaskMessage(taskId, String.format(message, args));
    }

    @Override
    public AppLogger tagged(String tag) {
        return this;
    }
}
