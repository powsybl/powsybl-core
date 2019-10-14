/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.execution;

import com.powsybl.computation.ComputationManager;
import com.powsybl.security.SecurityAnalysis;
import com.powsybl.security.SecurityAnalysisInput;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.SecurityAnalysisResultWithLog;

import javax.annotation.Nullable;
import java.util.ArrayList;
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

    private final @Nullable String securityAnalysisName;
    private final SecurityAnalysisInputBuildStrategy inputBuildStrategy;

    /**
     * The execution will use the default security-analysis implentment defined in the platform.
     */
    public SecurityAnalysisExecutionImpl() {
        this(null, SecurityAnalysisExecutionImpl::buildDefault);
    }

    /**
     * The execution will use the {@literal securityAnalysisName} implentment.
     */
    public SecurityAnalysisExecutionImpl(@Nullable String securityAnalysisName) {
        this(securityAnalysisName, SecurityAnalysisExecutionImpl::buildDefault);
    }

    public SecurityAnalysisExecutionImpl(@Nullable String securityAnalysisName, SecurityAnalysisInputBuildStrategy inputBuildStrategy) {
        this.securityAnalysisName = securityAnalysisName;
        this.inputBuildStrategy = requireNonNull(inputBuildStrategy);
    }

    private static SecurityAnalysisInput buildDefault(SecurityAnalysisExecutionInput executionInput) {
        return new SecurityAnalysisInput(executionInput.getNetworkVariant());
    }

    private SecurityAnalysisInput buildInput(SecurityAnalysisExecutionInput executionInput) {
        return inputBuildStrategy.buildFrom(executionInput);
    }

    @Override
    public CompletableFuture<SecurityAnalysisResult> execute(ComputationManager computationManager, SecurityAnalysisExecutionInput data) {
        SecurityAnalysisInput input = buildInput(data);
        return SecurityAnalysis.find(securityAnalysisName)
                .with(new ArrayList<>(input.getInterceptors()))
                .with(input.getLimitViolationDetector())
                .with(input.getFilter())
                .with(input.getNetworkVariant().getVariantId())
                .run(input.getNetworkVariant().getNetwork(), computationManager, input.getParameters(), input.getContingenciesProvider());
    }

    @Override
    public CompletableFuture<SecurityAnalysisResultWithLog> executeWithLog(ComputationManager computationManager, SecurityAnalysisExecutionInput data) {
        SecurityAnalysisInput input = buildInput(data);
        return SecurityAnalysis.find(securityAnalysisName)
                .with(new ArrayList<>(input.getInterceptors()))
                .with(input.getLimitViolationDetector())
                .with(input.getFilter())
                .with(input.getNetworkVariant().getVariantId())
                .runWithLog(input.getNetworkVariant().getNetwork(), computationManager, input.getParameters(), input.getContingenciesProvider());
    }
}
