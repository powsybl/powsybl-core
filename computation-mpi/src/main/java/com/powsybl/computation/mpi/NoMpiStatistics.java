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
public class NoMpiStatistics implements MpiStatistics {

    @Override
    public void logCommonFileTransfer(String fileName, int chunk, long size, long duration) {
        // no-op implementation
    }

    @Override
    public void logJobStart(int jobId, String commandId, Map<String, String> tags) {
        // no-op implementation
    }

    @Override
    public void logJobEnd(int jobId) {
        // no-op implementation
    }

    @Override
    public void logTaskStart(int taskId, int jobId, int taskIndex, DateTime startTime, int slaveRank, int slaveThread, long inputMessageSize) {
        // no-op implementation
    }

    @Override
    public void logTaskEnd(int taskId, long taskDuration, List<Long> commandsDuration, long dataTransferDuration, long outputMessageSize, long workingDataSize, int exitCode) {
        // no-op implementation
    }

    @Override
    public void exportTasksToCsv(Writer writer) {
        // no-op implementation
    }

    @Override
    public void close() {
        // no-op implementation
    }

}
