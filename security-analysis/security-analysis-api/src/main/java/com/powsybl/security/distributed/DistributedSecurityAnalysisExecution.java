/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.distributed;

import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.ExecutionEnvironment;
import com.powsybl.computation.ExecutionHandler;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.execution.SecurityAnalysisExecution;
import com.powsybl.security.execution.SecurityAnalysisExecutionInput;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * Execute a security analysis by spawning a specified number of subtasks, each of which
 * will consist of a separate call to {@literal itools security-analysis} through the specified
 * {@link ComputationManager}.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class DistributedSecurityAnalysisExecution extends AbstractDistributedSecurityAnalysisExecution implements SecurityAnalysisExecution {

    public DistributedSecurityAnalysisExecution(ExternalSecurityAnalysisConfig config, int subtaskCount) {
        super(config, subtaskCount);
    }

    @Override
    public CompletableFuture<SecurityAnalysisReport> execute(ComputationManager computationManager, SecurityAnalysisExecutionInput data) {
        ExecutionEnvironment itoolsEnv = new ExecutionEnvironment(Collections.emptyMap(), "security_analysis_task_", config.isDebug());
        ExecutionHandler<SecurityAnalysisReport> executionHandler = SecurityAnalysisExecutionHandlers.distributed(data, subtaskCount);
        return computationManager.execute(itoolsEnv, executionHandler);
    }
}
