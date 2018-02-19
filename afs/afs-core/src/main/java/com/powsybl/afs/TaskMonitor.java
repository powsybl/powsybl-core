/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TaskMonitor extends AutoCloseable {

    class Task {

        @JsonProperty("id")
        private final UUID id;

        @JsonProperty("name")
        private final String name;

        @JsonProperty("message")
        protected String message;

        @JsonProperty("revision")
        protected long revision;

        protected Task(String name, String message, long revision) {
            id = UUID.randomUUID();
            this.name = name;
            this.message = message;
            this.revision = revision;
        }

        protected Task(String name, long revision) {
            this(name, null, revision);
        }

        protected Task(Task other) {
            Objects.requireNonNull(other);
            id = other.id;
            name = other.name;
            message = other.message;
            revision = other.revision;
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getMessage() {
            return message;
        }

        public long getRevision() {
            return revision;
        }
    }

    class Snapshot {

        @JsonProperty("tasks")
        private final List<Task> tasks;

        @JsonProperty("revision")
        private final long revision;

        @JsonCreator
        Snapshot(@JsonProperty("tasks") List<Task> tasks, @JsonProperty("revision") long revision) {
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

    Task startTask(ProjectFile projectFile);

    void stopTask(UUID id);

    void updateTaskMessage(UUID id, String message);

    Snapshot takeSnapshot();

    void addListener(TaskListener listener);

    void removeListener(TaskListener listener);

    @Override
    void close();
}
