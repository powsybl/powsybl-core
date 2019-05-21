/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.distributed;

import com.google.common.base.Preconditions;
import com.powsybl.commons.compress.ZipHelper;
import com.powsybl.computation.ExecutionHandler;
import com.powsybl.computation.Partition;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.SecurityAnalysisResultMerger;
import com.powsybl.security.SecurityAnalysisResultWithLog;
import com.powsybl.security.execution.SecurityAnalysisExecutionInput;
import com.powsybl.security.json.SecurityAnalysisResultDeserializer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * Factory methods for security analysis execution handlers.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public final class SecurityAnalysisExecutionHandlers {

    private static final String OUTPUT_FILE_FMT = "task_%d_result.json";
    private static final String OUTPUT_FILE = "result.json";
    private static final String SA_TASK_CMD_ID = "security-analysis-task";
    private static final String SA_CMD_ID = "security-analysis";

    private static final String TASK_COUNT_ERROR_MESSAGE = "Number of subtasks must be 1 or greather, was %s.";

    private SecurityAnalysisExecutionHandlers() {
    }

    /**
     * Create an {@link ExecutionHandler} which forwards the security analysis execution through a call
     * to {@literal itools security-analysis}.
     */
    public static ExecutionHandler<SecurityAnalysisResult> forwarded(SecurityAnalysisExecutionInput input) {
        return forwarded(input, null);
    }

    /**
     * Create an {@link ExecutionHandler} which forwards the security analysis execution through a call
     * to {@literal itools security-analysis}, with the option {@literal --task-count}.
     */
    public static ExecutionHandler<SecurityAnalysisResult> forwarded(SecurityAnalysisExecutionInput input, Integer forwardedTaskCount) {
        Preconditions.checkArgument(forwardedTaskCount == null || forwardedTaskCount >= 1, TASK_COUNT_ERROR_MESSAGE, forwardedTaskCount);
        return new SecurityAnalysisExecutionHandler<>(SecurityAnalysisExecutionHandlers::readSingleResult,
            (workingDir, options) -> forwardedOptions(workingDir, options, forwardedTaskCount),
            1,
            input);
    }

    /**
     * Create an {@link ExecutionHandler} which forwards the security analysis execution through a call
     * to {@literal itools security-analysis}. It also retrieves execution logs.
     */
    public static ExecutionHandler<SecurityAnalysisResultWithLog> forwardedWithLogs(SecurityAnalysisExecutionInput input) {
        return forwardedWithLogs(input, null);
    }

    /**
     * Create an {@link ExecutionHandler} which forwards the security analysis execution through a call
     * to {@literal itools security-analysis}, with the option {@literal --task-count}. It also retrieves execution logs.
     */
    public static ExecutionHandler<SecurityAnalysisResultWithLog> forwardedWithLogs(SecurityAnalysisExecutionInput input, Integer forwardedTaskCount) {
        Preconditions.checkArgument(forwardedTaskCount == null || forwardedTaskCount >= 1, TASK_COUNT_ERROR_MESSAGE, forwardedTaskCount);
        return new SecurityAnalysisExecutionHandler<>(SecurityAnalysisExecutionHandlers::readSingleResultWithLogs,
            (workingDir, options) -> forwardedWithLogsOptions(workingDir, options, forwardedTaskCount),
            1,
            input);
    }

    /**
     * Create an {@link ExecutionHandler} which distributes the security analysis execution through multiple calls
     * to {@literal itools security-analysis}, as specified in argument.
     */
    public static ExecutionHandler<SecurityAnalysisResult> distributed(SecurityAnalysisExecutionInput input, int subtaskCount) {
        Preconditions.checkArgument(subtaskCount >= 1, TASK_COUNT_ERROR_MESSAGE, subtaskCount);
        return new SecurityAnalysisExecutionHandler<>(workingDir -> readResults(workingDir, subtaskCount),
            (workingDir, options) -> distributedOptions(workingDir, options, subtaskCount),
            subtaskCount,
            input);
    }

    /**
     * Create an {@link ExecutionHandler} which distributes the security analysis execution through multiple calls
     * to {@literal itools security-analysis}, as specified in argument. It also retrieves execution logs.
     */
    public static ExecutionHandler<SecurityAnalysisResultWithLog> distributedWithLog(SecurityAnalysisExecutionInput input, int subtaskCount) {
        Preconditions.checkArgument(subtaskCount >= 1, TASK_COUNT_ERROR_MESSAGE, subtaskCount);
        return new SecurityAnalysisExecutionHandler<>(workingDir -> readResultsWithLogs(workingDir, subtaskCount),
            (workingDir, options) -> distributedWithLogsOptions(workingDir, options, subtaskCount),
            subtaskCount,
            input);
    }

    public static SecurityAnalysisResult readSingleResult(Path workingDir) {
        Path taskResultFile = workingDir.resolve(OUTPUT_FILE);
        return SecurityAnalysisResultDeserializer.read(taskResultFile);
    }

    public static SecurityAnalysisResultWithLog readSingleResultWithLogs(Path workingDir) {
        SecurityAnalysisResult re = readSingleResult(workingDir);
        List<String> collectedLogsFilename = new ArrayList<>();
        collectedLogsFilename.add(workingDir.relativize(getLogPath(workingDir)).toString()); // logs_IDX.zip
        collectedLogsFilename.add(SA_CMD_ID + ".out");
        collectedLogsFilename.add(SA_CMD_ID + ".err");
        byte[] logBytes = ZipHelper.archiveFilesToZipBytes(workingDir, collectedLogsFilename);
        return new SecurityAnalysisResultWithLog(re, logBytes);
    }

    public static void forwardedOptions(Path workingDir, SecurityAnalysisCommandOptions options, Integer taskCount) {
        options.outputFile(workingDir.resolve(OUTPUT_FILE), "JSON");
        if (taskCount != null) {
            options.taskCount(taskCount);
        }
    }

    public static void forwardedWithLogsOptions(Path workingDir, SecurityAnalysisCommandOptions options, Integer taskCount) {
        forwardedOptions(workingDir, options, taskCount);
        options.logFile(getLogPath(workingDir));
    }

    public static void distributedOptions(Path workingDir, SecurityAnalysisCommandOptions options, int taskCount) {
        options.id(SA_TASK_CMD_ID);
        options.outputFile(i -> getOutputPathForTask(workingDir, i), "JSON");
        options.task(i -> new Partition(i + 1, taskCount));
    }

    public static void distributedWithLogsOptions(Path workingDir, SecurityAnalysisCommandOptions options, int taskCount) {
        distributedOptions(workingDir, options, taskCount);
        options.logFile(i -> getLogPathForTask(workingDir, i));
    }

    public static Path getOutputPathForTask(Path workingDir, int taskIndex) {
        return workingDir.resolve(String.format(OUTPUT_FILE_FMT, taskIndex));
    }

    public static SecurityAnalysisResult readResults(Path workingDir, int subtaskCount) {
        List<SecurityAnalysisResult> results = IntStream.range(0, subtaskCount)
                .mapToObj(taskIndex -> getOutputPathForTask(workingDir, taskIndex))
                .map(SecurityAnalysisResultDeserializer::read)
                .collect(Collectors.toList());
        return SecurityAnalysisResultMerger.merge(results);
    }

    public static SecurityAnalysisResultWithLog readResultsWithLogs(Path workingDir, int subtaskCount) {
        SecurityAnalysisResult re = readResults(workingDir, subtaskCount);
        List<String> collectedLogsFilename = new ArrayList<>();
        for (int i = 0; i < subtaskCount; i++) {
            collectedLogsFilename.add(workingDir.relativize(getLogPathForTask(workingDir, i)).toString()); // logs_IDX.zip
            collectedLogsFilename.add(SA_TASK_CMD_ID + "_" + i + ".out");
            collectedLogsFilename.add(SA_TASK_CMD_ID + "_" + i + ".err");
        }
        byte[] logBytes = ZipHelper.archiveFilesToZipBytes(workingDir, collectedLogsFilename);
        return new SecurityAnalysisResultWithLog(re, logBytes);
    }

    public static Path getLogPathForTask(Path workingDir, int taskNumber) {
        return workingDir.resolve("logs_" + taskNumber + ".zip");
    }

    public static Path getLogPath(Path workingDir) {
        return workingDir.resolve("logs.zip");
    }

}
