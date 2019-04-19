/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.execution;

import com.powsybl.computation.Partition;
import com.powsybl.contingency.ContingenciesProviders;
import com.powsybl.security.LimitViolationFilter;
import com.powsybl.security.SecurityAnalysisFactories;
import com.powsybl.security.SecurityAnalysisFactory;
import com.powsybl.security.SecurityAnalysisInput;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessors;
import com.powsybl.security.distributed.DistributedSecurityAnalysisExecution;
import com.powsybl.security.distributed.ExternalSecurityAnalysisConfig;
import com.powsybl.security.distributed.ForwardedSecurityAnalysisExecution;

import java.util.function.Supplier;

/**
 * Helper class to build a {@link SecurityAnalysisExecution},
 * based on specified options, in particular distribution options.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisExecutionBuilder {

    private final Supplier<ExternalSecurityAnalysisConfig> externalConfig;
    private final Supplier<SecurityAnalysisFactory> factory;
    private final SecurityAnalysisInputBuildStrategy inputBuilder;

    private boolean forward = false;
    private Integer taskCount = null;
    private Partition subPart = null;

    public static SecurityAnalysisExecutionBuilder usingDefaultConfig() {
        return new SecurityAnalysisExecutionBuilder(ExternalSecurityAnalysisConfig::load,
                SecurityAnalysisFactories::newDefaultFactory,
                SecurityAnalysisExecutionBuilder::buildInputUsingDefaultConfig);
    }

    public SecurityAnalysisExecutionBuilder(Supplier<ExternalSecurityAnalysisConfig> externalConfig,
                                            Supplier<SecurityAnalysisFactory> factory,
                                            SecurityAnalysisInputBuildStrategy inputBuilder) {
        this.externalConfig = externalConfig;
        this.factory = factory;
        this.inputBuilder = inputBuilder;
    }

    public SecurityAnalysisExecutionBuilder forward(boolean forward) {
        this.forward = forward;
        return this;
    }

    public SecurityAnalysisExecutionBuilder distributed(Integer taskCount) {
        this.taskCount = taskCount;
        return this;
    }

    public SecurityAnalysisExecutionBuilder subTask(Partition part) {
        this.subPart = part;
        return this;
    }

    public SecurityAnalysisExecution build() {
        if (forward) {
            return new ForwardedSecurityAnalysisExecution(externalConfig.get(), taskCount);
        } else if (taskCount != null) {
            return new DistributedSecurityAnalysisExecution(externalConfig.get(), taskCount);
        } else {
            return new SecurityAnalysisExecutionImpl(factory.get(), inputBuildStrategy());
        }
    }

    private static SecurityAnalysisInput buildInputUsingDefaultConfig(SecurityAnalysisExecutionInput executionInput) {

        SecurityAnalysisInput input = new SecurityAnalysisInput(executionInput.getNetworkVariant());
        input.setFilter(LimitViolationFilter.load());
        executionInput.copyTo(input);
        executionInput.getContingenciesSource()
                .map(source -> SecurityAnalysisPreprocessors.defaultConfiguredFactory().newPreprocessor(source))
                .ifPresent(p -> p.preprocess(input));

        return input;
    }

    private SecurityAnalysisInputBuildStrategy inputBuildStrategy() {
        if (subPart == null) {
            return inputBuilder;
        }

        return executionInput -> {
            SecurityAnalysisInput input = inputBuilder.buildFrom(executionInput);
            input.setContingencies(ContingenciesProviders.newSubProvider(input.getContingenciesProvider(), subPart));
            return input;
        };
    }

}
