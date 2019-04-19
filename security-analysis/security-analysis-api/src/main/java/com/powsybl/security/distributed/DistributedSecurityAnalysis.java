/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.distributed;

import com.powsybl.computation.*;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.SecurityAnalysisResultWithLog;
import com.powsybl.security.execution.SecurityAnalysisExecutionInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Security analysis implementation which distributes the work through X
 * executions of the "itools security-analysis" command.
 *
 * @deprecated Use instead {@link DistributedSecurityAnalysisExecution}, which clarifies the input data for that kind
 *             of execution, and tries to differentiate more between a {@link com.powsybl.security.SecurityAnalysis}
 *             and its mode of execution.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
@Deprecated
public class DistributedSecurityAnalysis extends ExternalSecurityAnalysis {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedSecurityAnalysis.class);

    public DistributedSecurityAnalysis(ExternalSecurityAnalysisConfig config, Network network,
                                       ComputationManager computationManager, List<String> extensions, int taskCount) {
        super(config, network, computationManager, extensions, taskCount);
    }

    @Override
    public CompletableFuture<SecurityAnalysisResult> run(String workingVariantId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider) {
        LOGGER.debug("Starting distributed security analysis.");

        ExecutionEnvironment itoolsEnv = new ExecutionEnvironment(Collections.emptyMap(), "security_analysis_task_", config.isDebug());

        SecurityAnalysisExecutionInput input = buildInput(workingVariantId, parameters, contingenciesProvider);

        List<Contingency> contingencies = contingenciesProvider.getContingencies(network);
        int actualTaskCount = Math.min(taskCount, Math.max(1, contingencies.size()));
        return computationManager.execute(itoolsEnv,
                SecurityAnalysisExecutionHandlers.distributed(input, actualTaskCount));
    }

    @Override
    public CompletableFuture<SecurityAnalysisResultWithLog> runWithLog(String workingVariantId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider) {
        LOGGER.debug("Starting distributed security analysis(with log).");

        ExecutionEnvironment itoolsEnv = new ExecutionEnvironment(Collections.emptyMap(), "security_analysis_task_", config.isDebug());

        SecurityAnalysisExecutionInput input = buildInput(workingVariantId, parameters, contingenciesProvider);

        List<Contingency> contingencies = contingenciesProvider.getContingencies(network);
        int actualTaskCount = Math.min(taskCount, Math.max(1, contingencies.size()));
        return computationManager.execute(itoolsEnv,
                SecurityAnalysisExecutionHandlers.distributedWithLog(input, actualTaskCount));
    }
}
