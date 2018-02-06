/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TaskMonitor {

    class Task {

        private final ProjectFile projectFile;

        protected String message;

        protected long revision;

        protected Task(ProjectFile projectFile, String message, long revision) {
            this.projectFile = Objects.requireNonNull(projectFile);
            this.message = message;
            this.revision = revision;
        }

        protected Task(ProjectFile projectFile, long revision) {
            this(projectFile, null, revision);
        }

        public ProjectFile getProjectFile() {
            return projectFile;
        }

        public String getMessage() {
            return message;
        }

        public long getRevision() {
            return revision;
        }

        @Override
        public int hashCode() {
            return Objects.hash(projectFile.getId(), message, revision);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Task) {
                Task task = (Task) obj;
                return task.projectFile.getId().equals(projectFile.getId()) &&
                        task.revision == revision &&
                        Objects.equals(task.message, message);
            }
            return false;
        }
    }

    class Snapshot {

        private final List<Task> tasks;

        private final long revision;

        Snapshot(List<Task> tasks, long revision) {
            this.tasks = Objects.requireNonNull(tasks);
            this.revision = revision;
        }

        public List<Task> getTasks() {
            return tasks;
        }

        public long getRevision() {
            return revision;
        }
    }

    void startTask(ProjectFile projectFile);

    void stopTask(ProjectFile projectFile);

    void updateTaskMessage(ProjectFile projectFile, String message);

    Snapshot takeSnapshot();

    void addListener(TaskListener listener);

    void removeListener(TaskListener listener);
}
