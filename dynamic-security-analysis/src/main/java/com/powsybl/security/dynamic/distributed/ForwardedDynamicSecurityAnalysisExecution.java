/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.distributed;

import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.ExecutionEnvironment;
import com.powsybl.computation.ExecutionHandler;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.distributed.AbstractForwardedSecurityAnalysisExecution;
import com.powsybl.security.distributed.ExternalSecurityAnalysisConfig;
import com.powsybl.security.dynamic.execution.DynamicSecurityAnalysisExecution;
import com.powsybl.security.dynamic.execution.DynamicSecurityAnalysisExecutionInput;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 *
 * Forwards the execution of a security analysis to another itools process.
 * The {@link #forwardedTaskCount} parameter will be forward to that process, so if it is greater than one,
 * it will spawn multiple "slave" processes.
 *
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class ForwardedDynamicSecurityAnalysisExecution extends AbstractForwardedSecurityAnalysisExecution implements DynamicSecurityAnalysisExecution {

    public ForwardedDynamicSecurityAnalysisExecution(ExternalSecurityAnalysisConfig config) {
        this(config, null);
    }

    public ForwardedDynamicSecurityAnalysisExecution(ExternalSecurityAnalysisConfig config, Integer forwardedTaskCount) {
        super(config, forwardedTaskCount);
    }

    @Override
    public CompletableFuture<SecurityAnalysisReport> execute(ComputationManager computationManager, DynamicSecurityAnalysisExecutionInput data) {
        ExecutionEnvironment itoolsEnv = new ExecutionEnvironment(Collections.emptyMap(), "dynamic_security_analysis_", config.isDebug());
        ExecutionHandler<SecurityAnalysisReport> executionHandler = DynamicSecurityAnalysisExecutionHandlers.forwarded(data, forwardedTaskCount);
        return computationManager.execute(itoolsEnv, executionHandler);
    }
}
