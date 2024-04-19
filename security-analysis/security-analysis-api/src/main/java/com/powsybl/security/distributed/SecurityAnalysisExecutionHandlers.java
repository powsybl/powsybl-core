/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.distributed;

import com.google.common.base.Preconditions;
import com.powsybl.computation.ExecutionHandler;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.execution.SecurityAnalysisExecutionInput;

/**
 *
 * Factory methods for security analysis execution handlers.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public final class SecurityAnalysisExecutionHandlers {

    private static final String SA_TASK_CMD_ID = "security-analysis-task";
    private static final String SA_CMD_ID = "security-analysis";

    private static final String TASK_COUNT_ERROR_MESSAGE = "Number of subtasks must be 1 or greater, was %s.";

    private SecurityAnalysisExecutionHandlers() {
    }

    /**
     * Create an {@link ExecutionHandler} which forwards the security analysis execution through a call
     * to {@literal itools security-analysis}.
     */
    public static ExecutionHandler<SecurityAnalysisReport> forwarded(SecurityAnalysisExecutionInput input) {
        return forwarded(input, null);
    }

    /**
     * Create an {@link ExecutionHandler} which forwards the security analysis execution through a call
     * to {@literal itools security-analysis}, with the option {@literal --task-count}.
     */
    public static ExecutionHandler<SecurityAnalysisReport> forwarded(SecurityAnalysisExecutionInput input, Integer forwardedTaskCount) {
        Preconditions.checkArgument(forwardedTaskCount == null || forwardedTaskCount >= 1, TASK_COUNT_ERROR_MESSAGE, forwardedTaskCount);
        return new SecurityAnalysisExecutionHandler<>(workingDir -> SecurityAnalysisExecutionDataHandlers.readSingleResult(workingDir, input.isWithLogs(), SA_CMD_ID),
                (workingDir, options) -> SecurityAnalysisExecutionDataHandlers.forwardedOptions(workingDir, options, forwardedTaskCount, input.isWithLogs()),
                (workingDir, cause) -> SecurityAnalysisExecutionDataHandlers.generateExceptionWithLogs(workingDir, cause, SA_CMD_ID),
                1,
                input);
    }

    /**
     * Create an {@link ExecutionHandler} which distributes the security analysis execution through multiple calls
     * to {@literal itools security-analysis}, as specified in argument.
     */
    public static ExecutionHandler<SecurityAnalysisReport> distributed(SecurityAnalysisExecutionInput input, int subtaskCount) {
        Preconditions.checkArgument(subtaskCount >= 1, TASK_COUNT_ERROR_MESSAGE, subtaskCount);
        return new SecurityAnalysisExecutionHandler<>(workingDir -> SecurityAnalysisExecutionDataHandlers.readResults(workingDir, subtaskCount, input.isWithLogs(), SA_TASK_CMD_ID),
            (workingDir, options) -> SecurityAnalysisExecutionDataHandlers.distributedOptions(workingDir, options, subtaskCount, input.isWithLogs(), SA_TASK_CMD_ID),
            (workingDir, cause) -> SecurityAnalysisExecutionDataHandlers.generateExceptionWithLogs(workingDir, cause, subtaskCount, SA_TASK_CMD_ID),
            subtaskCount,
            input);
    }
}
