/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.distributed;

import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.ExecutionEnvironment;
import com.powsybl.computation.ExecutionHandler;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.SecurityAnalysisResultWithLog;
import com.powsybl.security.execution.SecurityAnalysisExecution;
import com.powsybl.security.execution.SecurityAnalysisExecutionInput;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkArgument;

/**
 *
 * Forwards the execution of a security analysis to another itools process.
 *
 * The {@link #forwardedTaskCount} parameter will be forward to that process, so if it is greater than one,
 * it will spawn multiple "slave" processes.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class ForwardedSecurityAnalysisExecution implements SecurityAnalysisExecution {

    private final ExternalSecurityAnalysisConfig config;
    private Integer forwardedTaskCount;

    public ForwardedSecurityAnalysisExecution(ExternalSecurityAnalysisConfig config) {
        this(config, null);
    }

    public ForwardedSecurityAnalysisExecution(ExternalSecurityAnalysisConfig config, Integer forwardedTaskCount) {
        this.config = Objects.requireNonNull(config);
        this.forwardedTaskCount = checkForwardedTaskCount(forwardedTaskCount);
    }

    @Override
    public CompletableFuture<SecurityAnalysisResult> execute(ComputationManager computationManager,
                                                             SecurityAnalysisExecutionInput data) {

        ExecutionEnvironment itoolsEnv = new ExecutionEnvironment(Collections.emptyMap(), "security_analysis_", config.isDebug());
        ExecutionHandler<SecurityAnalysisResult> executionHandler = SecurityAnalysisExecutionHandlers.forwarded(data, forwardedTaskCount);
        return computationManager.execute(itoolsEnv, executionHandler);
    }

    @Override
    public CompletableFuture<SecurityAnalysisResultWithLog> executeWithLog(ComputationManager computationManager,
                                                                           SecurityAnalysisExecutionInput data) {

        ExecutionEnvironment itoolsEnv = new ExecutionEnvironment(Collections.emptyMap(), "security_analysis_", config.isDebug());
        ExecutionHandler<SecurityAnalysisResultWithLog> executionHandler = SecurityAnalysisExecutionHandlers.forwardedWithLogs(data, forwardedTaskCount);
        return computationManager.execute(itoolsEnv, executionHandler);
    }

    private static Integer checkForwardedTaskCount(Integer count) {
        checkArgument(count == null || count > 0, "Forwarded task count must be positive.");
        return count;
    }
}
