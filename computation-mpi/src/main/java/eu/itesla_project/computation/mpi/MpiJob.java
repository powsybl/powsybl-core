/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation.mpi;

import eu.itesla_project.computation.ExecutionListener;
import eu.itesla_project.computation.CommandExecution;
import eu.itesla_project.computation.ExecutionError;
import eu.itesla_project.computation.ExecutionReport;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MpiJob {

    private final int id;

    private final CommandExecution execution;

    private final ExecutionListener listener;

    private final CompletableFuture<ExecutionReport> future;

    private final Path workingDir;

    private final Map<String, String> env;

    private int taskIndex = 0;

    private final List<MpiTask> runningTasks = new ArrayList<>();

    private final List<ExecutionError> errors = new ArrayList<>();

    private final Set<Integer> usedRanks = new HashSet<>();

    MpiJob(int id, CommandExecution execution, Path workingDir, Map<String, String> env, ExecutionListener listener, CompletableFuture<ExecutionReport> future) {
        this.id = id;
        this.execution = execution;
        this.workingDir = workingDir;
        this.env = env;
        this.listener = new ProfiledExecutionListener(listener);
        this.future = future;
    }

    CommandExecution getExecution() {
        return execution;
    }

    int getId() {
        return id;
    }

    Path getWorkingDir() {
        return workingDir;
    }

    Map<String, String> getEnv() {
        return env;
    }

    ExecutionListener getListener() {
        return listener;
    }

    CompletableFuture<ExecutionReport> getFuture() {
        return future;
    }

    int getTaskIndex() {
        return taskIndex;
    }

    void setTaskIndex(int taskIndex) {
        this.taskIndex = taskIndex;
    }

    List<MpiTask> getRunningTasks() {
        return runningTasks;
    }

    List<ExecutionError> getErrors() {
        return errors;
    }

    Set<Integer> getUsedRanks() {
        return usedRanks;
    }

    boolean isCompleted() {
        return taskIndex >= execution.getExecutionCount() && runningTasks.isEmpty();
    }

}
