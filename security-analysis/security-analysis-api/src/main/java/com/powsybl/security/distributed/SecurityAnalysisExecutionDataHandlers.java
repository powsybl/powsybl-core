/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.distributed;

import com.powsybl.commons.compress.ZipPackager;
import com.powsybl.computation.ComputationException;
import com.powsybl.computation.ComputationExceptionBuilder;
import com.powsybl.computation.Partition;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.SecurityAnalysisResultMerger;
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
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public final class SecurityAnalysisExecutionDataHandlers {

    private static final String OUTPUT_FILE_FMT = "task_%d_result.json";
    private static final String OUTPUT_FILE = "result.json";

    private SecurityAnalysisExecutionDataHandlers() {
    }

    public static SecurityAnalysisReport readSingleResult(Path workingDir, boolean withLogs, String cmdId) {
        Path taskResultFile = workingDir.resolve(OUTPUT_FILE);
        SecurityAnalysisResult re = SecurityAnalysisResultDeserializer.read(taskResultFile);
        SecurityAnalysisReport report = new SecurityAnalysisReport(re);
        if (withLogs) {
            List<String> collectedLogsFilename = new ArrayList<>();
            collectedLogsFilename.add(workingDir.relativize(getLogPath(workingDir)).toString()); // logs_IDX.zip
            collectedLogsFilename.add(saCmdOutLogName(cmdId));
            collectedLogsFilename.add(saCmdErrLogName(cmdId));
            byte[] logBytes = ZipPackager.archiveFilesToZipBytes(workingDir, collectedLogsFilename);
            report.setLogBytes(logBytes);
        }
        return report;
    }

    private static String saCmdOutLogName(String cmdId) {
        return cmdId + ".out";
    }

    private static String saCmdErrLogName(String cmdId) {
        return cmdId + ".err";
    }

    public static void forwardedOptions(Path workingDir, AbstractSecurityAnalysisCommandOptions<? extends AbstractSecurityAnalysisCommandOptions<?>> options, Integer taskCount, boolean withLogs) {
        options.outputFile(workingDir.resolve(OUTPUT_FILE), "JSON");
        if (taskCount != null) {
            options.taskCount(taskCount);
        }
        if (withLogs) {
            options.logFile(getLogPath(workingDir));
        }
    }

    public static void distributedOptions(Path workingDir, AbstractSecurityAnalysisCommandOptions<? extends AbstractSecurityAnalysisCommandOptions<?>> options, int taskCount, boolean withLogs, String taskCmdId) {
        options.id(taskCmdId);
        options.outputFile(i -> getOutputPathForTask(workingDir, i), "JSON");
        options.task(i -> new Partition(i + 1, taskCount));
        if (withLogs) {
            options.logFile(i -> getLogPathForTask(workingDir, i));
        }
    }

    public static Path getOutputPathForTask(Path workingDir, int taskIndex) {
        return workingDir.resolve(String.format(OUTPUT_FILE_FMT, taskIndex));
    }

    public static SecurityAnalysisReport readResults(Path workingDir, int subtaskCount, boolean withLogs, String taskCmdId) {
        List<SecurityAnalysisResult> results = IntStream.range(0, subtaskCount)
                .mapToObj(taskIndex -> getOutputPathForTask(workingDir, taskIndex))
                .map(SecurityAnalysisResultDeserializer::read)
                .collect(Collectors.toList());
        SecurityAnalysisResult re = SecurityAnalysisResultMerger.merge(results);
        SecurityAnalysisReport report = new SecurityAnalysisReport(re);
        if (withLogs) {
            List<String> collectedLogsFilename = new ArrayList<>();
            for (int i = 0; i < subtaskCount; i++) {
                collectedLogsFilename.add(workingDir.relativize(getLogPathForTask(workingDir, i)).toString()); // logs_IDX.zip
                collectedLogsFilename.add(satOutName(taskCmdId, i));
                collectedLogsFilename.add(satErrName(taskCmdId, i));
            }
            byte[] logBytes = ZipPackager.archiveFilesToZipBytes(workingDir, collectedLogsFilename);
            report.setLogBytes(logBytes);
        }
        return report;
    }

    public static ComputationException generateExceptionWithLogs(Path workingDir, Exception cause, int count, String taskCmdId) {
        ComputationExceptionBuilder ceb = new ComputationExceptionBuilder(cause)
                .message("An error occurred during security analysis command execution");
        IntStream.range(0, count).forEach(i -> {
            String outLogName = satOutName(taskCmdId, i);
            String errLogName = satErrName(taskCmdId, i);
            ceb.addOutLogIfExists(workingDir.resolve(outLogName))
                    .addErrLogIfExists(workingDir.resolve(errLogName))
                    .addFileIfExists(getLogPathForTask(workingDir, i));
        });
        return ceb.build();
    }

    public static ComputationException generateExceptionWithLogs(Path workingDir, Exception cause, String cmdId) {
        ComputationExceptionBuilder ceb = new ComputationExceptionBuilder(cause)
                .message("An error occurred during security analysis command execution");
        String outLogName = saCmdOutLogName(cmdId);
        String errLogName = saCmdErrLogName(cmdId);
        ceb.addOutLogIfExists(workingDir.resolve(outLogName))
                .addErrLogIfExists(workingDir.resolve(errLogName))
                .addFileIfExists(getLogPath(workingDir));
        return ceb.build();
    }

    private static String satErrName(String taskCmdId, int i) {
        return taskCmdId + "_" + i + ".err";
    }

    private static String satOutName(String taskCmdId, int i) {
        return taskCmdId + "_" + i + ".out";
    }

    public static Path getLogPathForTask(Path workingDir, int taskNumber) {
        return workingDir.resolve("logs_" + taskNumber + ".zip");
    }

    public static Path getLogPath(Path workingDir) {
        return workingDir.resolve("logs.zip");
    }

}
