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
        private String message;

        @JsonProperty("revision")
        private long revision;

        @JsonProperty("projectId")
        private final String projectId;

        @JsonCreator
        public Task(@JsonProperty("name") String name,
                    @JsonProperty("message") String message,
                    @JsonProperty("revision") long revision,
                    @JsonProperty("projectId") String projectId) {
            id = UUID.randomUUID();
            this.name = Objects.requireNonNull(name);
            this.message = message;
            this.revision = revision;
            this.projectId = Objects.requireNonNull(projectId);
        }

        protected Task(Task other) {
            Objects.requireNonNull(other);
            id = other.id;
            name = other.name;
            message = other.message;
            revision = other.revision;
            projectId = other.projectId;
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

        void setMessage(String message) {
            this.message = message;
        }

        public long getRevision() {
            return revision;
        }

        void setRevision(long revision) {
            this.revision = revision;
        }

        String getProjectId() {
            return projectId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, message, revision, projectId);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Task) {
                Task other = (Task) obj;
                return id.equals(other.id)
                        && name.equals(other.name)
                        && Objects.equals(message, other.message)
                        && revision == other.revision
                        && projectId.equals(other.projectId);
            }
            return false;
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

        @Override
        public int hashCode() {
            return Objects.hash(tasks, revision);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Snapshot) {
                Snapshot snapshot = (Snapshot) obj;
                return tasks.equals(snapshot.tasks) && revision == snapshot.revision;
            }
            return false;
        }
    }

    Task startTask(ProjectFile projectFile);

    Task startTask(String name, Project project);

    void stopTask(UUID id);

    void updateTaskMessage(UUID id, String message);

    Snapshot takeSnapshot(String projectId);

    void addListener(TaskListener listener);

    void removeListener(TaskListener listener);

    @Override
    void close();
}
