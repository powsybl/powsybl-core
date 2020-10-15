/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.execution;

import com.powsybl.computation.ComputationManager;
import com.powsybl.security.*;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

/**
 * A local execution of a security analysis. Before the actual execution,
 * security analysis inputs are built from the so called execution inputs using a specified strategy,
 * including possible user defined transformation or preprocessing.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisExecutionImpl implements SecurityAnalysisExecution {

    private final SecurityAnalysisFactory factory;
    private final SecurityAnalysisInputBuildStrategy inputBuildStrategy;

    public SecurityAnalysisExecutionImpl(SecurityAnalysisFactory factory) {
        this(factory, SecurityAnalysisExecutionImpl::buildDefault);
    }

    public SecurityAnalysisExecutionImpl(SecurityAnalysisFactory factory, SecurityAnalysisInputBuildStrategy inputBuildStrategy) {
        this.factory = requireNonNull(factory);
        this.inputBuildStrategy = requireNonNull(inputBuildStrategy);
    }

    private static SecurityAnalysisInput buildDefault(SecurityAnalysisExecutionInput executionInput) {
        return new SecurityAnalysisInput(executionInput.getNetworkVariant());
    }

    private SecurityAnalysisInput buildInput(SecurityAnalysisExecutionInput executionInput) {
        return inputBuildStrategy.buildFrom(executionInput);
    }

    private SecurityAnalysis buildSecurityAnalysis(SecurityAnalysisInput input, ComputationManager computationManager) {
        SecurityAnalysis securityAnalysis = factory.create(input.getNetworkVariant().getNetwork(), input.getLimitViolationDetector(),
                input.getFilter(), computationManager, 0);
        input.getInterceptors().forEach(securityAnalysis::addInterceptor);
        return securityAnalysis;
    }

    @Override
    public CompletableFuture<SecurityAnalysisResult> execute(ComputationManager computationManager, SecurityAnalysisExecutionInput data) {
        SecurityAnalysisInput input = buildInput(data);
        SecurityAnalysis securityAnalysis = buildSecurityAnalysis(input, computationManager);
        return securityAnalysis.run(input.getNetworkVariant().getVariantId(), input.getParameters(), input.getContingenciesProvider());
    }

    @Override
    public CompletableFuture<SecurityAnalysisResultWithLog> executeWithLog(ComputationManager computationManager, SecurityAnalysisExecutionInput data) {
        SecurityAnalysisInput input = buildInput(data);
        SecurityAnalysis securityAnalysis = buildSecurityAnalysis(input, computationManager);
        return securityAnalysis.runWithLog(input.getNetworkVariant().getVariantId(), input.getParameters(), input.getContingenciesProvider());
    }
}
