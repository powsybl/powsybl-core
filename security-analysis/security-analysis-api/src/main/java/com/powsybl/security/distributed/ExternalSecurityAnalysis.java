/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.distributed;

import com.google.common.io.ByteSource;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.ExecutionEnvironment;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.SecurityAnalysis;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.execution.SecurityAnalysisExecutionInput;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

/**
 * Submits execution of an "itools security-analysis" command to the ComputationManager,
 * for example to remotely execute the security-analysis.
 *
 * @deprecated Use instead {@link ForwardedSecurityAnalysisExecution}, which clarifies the input data for that kind
 *             of execution, and tries to differentiate more between a {@link com.powsybl.security.SecurityAnalysis}
 *             and its mode of execution.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
@Deprecated
public class ExternalSecurityAnalysis implements SecurityAnalysis {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalSecurityAnalysis.class);

    protected final ExternalSecurityAnalysisConfig config;
    protected final Network network;
    protected final ComputationManager computationManager;
    protected final List<String> extensions;
    protected final Integer taskCount;

    public ExternalSecurityAnalysis(ExternalSecurityAnalysisConfig config, Network network,
                                    ComputationManager computationManager, List<String> extensions) {
        this(config, network, computationManager, extensions, null);
    }

    private ExternalSecurityAnalysis(ExternalSecurityAnalysisConfig config, Network network,
                                     ComputationManager computationManager, List<String> extensions, Integer taskCount) {
        this.config = requireNonNull(config);
        this.network = requireNonNull(network);
        this.computationManager = requireNonNull(computationManager);
        this.extensions = new ArrayList<>(requireNonNull(extensions));
        this.taskCount = taskCount;
    }

    public ExternalSecurityAnalysis(ExternalSecurityAnalysisConfig config, Network network,
                                    ComputationManager computationManager, List<String> extensions, int taskCount) {
        this(config, network, computationManager, extensions, Integer.valueOf(taskCount));
    }

    @Override
    public void addInterceptor(SecurityAnalysisInterceptor interceptor) {
        throw new UnsupportedOperationException("External security analysis does not support interceptors. Use extension names instead.");
    }

    @Override
    public boolean removeInterceptor(SecurityAnalysisInterceptor interceptor) {
        throw new UnsupportedOperationException("External security analysis does not support interceptors.");
    }

    @Override
    public CompletableFuture<SecurityAnalysisResult> run(String workingStateId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider) {
        LOGGER.debug("Starting external security analysis.");

        ExecutionEnvironment itoolsEnv = new ExecutionEnvironment(Collections.emptyMap(), "security_analysis_", config.isDebug());
        SecurityAnalysisExecutionInput input = buildInput(workingStateId, parameters, contingenciesProvider);
        return computationManager.execute(itoolsEnv, SecurityAnalysisExecutionHandlers.forwarded(input, taskCount));
    }

    protected SecurityAnalysisExecutionInput buildInput(String workingStateId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider) {
        return new SecurityAnalysisExecutionInput()
                .setNetworkVariant(network, workingStateId)
                .setParameters(parameters)
                .setContingenciesSource(ByteSource.wrap(contingenciesProvider.asScript().getBytes(StandardCharsets.UTF_8)))
                .addResultExtensions(extensions);
    }
}
