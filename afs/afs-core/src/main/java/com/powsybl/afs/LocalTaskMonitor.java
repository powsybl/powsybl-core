/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalTaskMonitor implements TaskMonitor {

    private static final class ModifiableTask extends Task {

        private ModifiableTask(ProjectFile projectFile, long revision) {
            super(projectFile, revision);
        }

        private void setMessage(String message) {
            this.message = message;
        }

        private void setRevision(long revision) {
            this.revision = revision;
        }
    }

    private final Map<String, ModifiableTask> tasks = new HashMap<>();

    private long revision = 0L;

    private final Lock lock = new ReentrantLock();

    private final List<TaskListener> listeners = new ArrayList<>();

    @Override
    public void startTask(ProjectFile projectFile) {
        Objects.requireNonNull(projectFile);
        lock.lock();
        try {
            revision++;
            tasks.put(projectFile.getId(), new ModifiableTask(projectFile, revision));

            // notification
            for (TaskListener listener : listeners) {
                listener.taskStarted(projectFile);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void stopTask(ProjectFile projectFile) {
        Objects.requireNonNull(projectFile);
        lock.lock();
        try {
            revision++;
            tasks.remove(projectFile.getId());

            // notification
            for (TaskListener listener : listeners) {
                listener.taskStopped(projectFile);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Snapshot takeSnapshot() {
        lock.lock();
        try {
            return new Snapshot(new ArrayList<>(tasks.values()), revision);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateTaskMessage(ProjectFile projectFile, String message) {
        Objects.requireNonNull(projectFile);
        lock.lock();
        try {
            ModifiableTask task = tasks.get(projectFile.getId());
            if (task == null) {
                throw new AfsException("Project file " + projectFile.getId() + " is not associated to any task");
            }
            revision++;
            task.setMessage(message);
            task.setRevision(revision);

            // notification
            for (TaskListener listener : listeners) {
                listener.taskMessageUpdated(projectFile, message);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void addListener(TaskListener listener) {
        Objects.requireNonNull(listener);
        lock.lock();
        try {
            listeners.add(listener);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeListener(TaskListener listener) {
        Objects.requireNonNull(listener);
        lock.lock();
        try {
            listeners.remove(listener);
        } finally {
            lock.unlock();
        }
    }
}
