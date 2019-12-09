/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TaskMonitor extends AutoCloseable {

    class NotACancellableTaskMonitor extends Exception {
        public NotACancellableTaskMonitor() {
            super();
        }

        public NotACancellableTaskMonitor(String messsage) {
            super(messsage);
        }
    }

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

        @JsonIgnore
        private Future future;

        @JsonProperty("cancellable")
        private boolean cancellable;

        public Task(String name,
                    String message,
                    long revision,
                    String projectId) {
            this(name, message, revision, projectId, false);
        }

        @JsonCreator
        public Task(@JsonProperty("name") String name,
                    @JsonProperty("message") String message,
                    @JsonProperty("revision") long revision,
                    @JsonProperty("projectId") String projectId,
                    @JsonProperty("cancellable") boolean cancellable) {
            id = UUID.randomUUID();
            this.name = Objects.requireNonNull(name);
            this.message = message;
            this.revision = revision;
            this.projectId = Objects.requireNonNull(projectId);
            this.cancellable = cancellable;
        }

        protected Task(Task other) {
            Objects.requireNonNull(other);
            id = other.id;
            name = other.name;
            message = other.message;
            revision = other.revision;
            projectId = other.projectId;
            cancellable = other.cancellable;
            future = other.future;
        }

        void setFuture(Future future) {
            this.future = future;
            this.cancellable = future != null;
        }

        public boolean isCancellable() {
            return cancellable;
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

        boolean cancel() {
            if (future != null) {
                return future.cancel(true);
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

    /**
     * Create a task monitoring object
     *
     * @param projectFile related project file
     * @return the newly created task
     */
    Task startTask(ProjectFile projectFile);

    /**
     * Create a task monitoring object
     *
     * @param name    name of the task
     * @param project related project
     * @return the newly created task
     */
    Task startTask(String name, Project project);

    /**
     * Remove the task monitoring object.
     * To stop the process monitored by this task, use {@link TaskMonitor#cancelTaskComputation}
     *
     * @param id id of the task
     */
    void stopTask(UUID id);

    /**
     * Update the display status of the task
     *
     * @param id      id of the task
     * @param message new status message
     */
    void updateTaskMessage(UUID id, String message);

    /**
     * Return the complete state of tasks related to a project
     *
     * @param projectId related project
     * @return
     */
    Snapshot takeSnapshot(String projectId);

    /**
     * Try cancel/stop the computation process monitored by this task.
     * The {@link TaskMonitor#stopTask(UUID)} must still be called afterward to clean the task monitor object
     *
     * @param id the id of the task
     * @return a boolean indicating if the cancellation process has succeeded
     */
    boolean cancelTaskComputation(UUID id);

    /**
     * Add a listener to task events
     *
     * @param listener
     */
    void addListener(TaskListener listener);

    /**
     * Remove a listener of task events
     *
     * @param listener
     */
    void removeListener(TaskListener listener);

    /**
     * Update the future of the computation process monitored by this task
     *
     * @param taskId
     * @param future
     * @throws NotACancellableTaskMonitor in case the task monitor is operating as a remote task monitor
     */
    void updateTaskFuture(UUID taskId, Future future) throws NotACancellableTaskMonitor;

    @Override
    void close();
}
