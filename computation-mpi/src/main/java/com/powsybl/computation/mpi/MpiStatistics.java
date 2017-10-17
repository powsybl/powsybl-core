/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import java.io.Writer;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface MpiStatistics extends AutoCloseable {

    /**
     * Log a common file transfer.
     *
     * @param fileName the file name
     * @param chunk chunk number
     * @param size file size in bytes
     * @param duration duration of the transfer in ms
     */
    void logCommonFileTransfer(String fileName, int chunk, long size, long duration);

    /**
     * Log a job start.
     *
     * @param jobId the job id
     * @param commandId the command id
     * @param tags tags associated to the job
     */
    void logJobStart(int jobId, String commandId, Map<String, String> tags);

    /**
     * Log a job end.
     *
     * @param jobId the job id
     */
    void logJobEnd(int jobId);

    /**
     * Log a task start.
     *
     * @param taskId the task id
     * @param jobId the job id
     * @param taskIndex index of the task in the job
     * @param startTime task start time
     * @param slaveRank slave rank that has managed the task
     * @param slaveThread slave thread that has managed the task
     * @param inputMessageSize input message size in bytes
     */
    void logTaskStart(int taskId, int jobId, int taskIndex, DateTime startTime, int slaveRank, int slaveThread, long inputMessageSize);

    /**
     * Log a task end.
     *
     * @param taskId the task id
     * @param taskDuration task duration in ms
     * @param commandsDuration duration in ms of all commands of the task
     * @param dataTransferDuration part of the task duration corresponding to data transfer in ms
     * @param outputMessageSize output message size in bytes
     * @param workingDataSize working data size in bytes on slave side
     * @param exitCode exit code of the command
     */
    void logTaskEnd(int taskId, long taskDuration, List<Long> commandsDuration, long dataTransferDuration, long outputMessageSize, long workingDataSize, int exitCode);

    /**
     * Export tasks statistics to CSV.
     *
     * @param writer
     */
    void exportTasksToCsv(Writer writer);

    @Override
    void close();
}
