/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import eu.itesla_project.modules.offline.OfflineTaskType;
import eu.itesla_project.modules.offline.OfflineTaskStatus;
import java.io.Serializable;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OfflineTaskEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final OfflineTaskType taskType;

    private final OfflineTaskStatus taskStatus;

    private final String failureReason;

    public OfflineTaskEvent(OfflineTaskType taskType, OfflineTaskStatus taskStatus, String failureReason) {
        this.taskType = taskType;
        this.taskStatus = taskStatus;
        this.failureReason = failureReason;
    }

    public OfflineTaskType getTaskType() {
        return taskType;
    }

    public OfflineTaskStatus getTaskStatus() {
        return taskStatus;
    }

    public String getFailureReason() {
        return failureReason;
    }

}
