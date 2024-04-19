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
 *
 * Forwards the execution of a security analysis to another itools process.
 * The {@link #forwardedTaskCount} parameter will be forward to that process, so if it is greater than one,
 * it will spawn multiple "slave" processes.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class ForwardedSecurityAnalysisExecution extends AbstractForwardedSecurityAnalysisExecution implements SecurityAnalysisExecution {

    public ForwardedSecurityAnalysisExecution(ExternalSecurityAnalysisConfig config) {
        this(config, null);
    }

    public ForwardedSecurityAnalysisExecution(ExternalSecurityAnalysisConfig config, Integer forwardedTaskCount) {
        super(config, forwardedTaskCount);
    }

    @Override
    public CompletableFuture<SecurityAnalysisReport> execute(ComputationManager computationManager, SecurityAnalysisExecutionInput data) {
        ExecutionEnvironment itoolsEnv = new ExecutionEnvironment(Collections.emptyMap(), "security_analysis_", config.isDebug());
        ExecutionHandler<SecurityAnalysisReport> executionHandler = SecurityAnalysisExecutionHandlers.forwarded(data, forwardedTaskCount);
        return computationManager.execute(itoolsEnv, executionHandler);
    }
}
