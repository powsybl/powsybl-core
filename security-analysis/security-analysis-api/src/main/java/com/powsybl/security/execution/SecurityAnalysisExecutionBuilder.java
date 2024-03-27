/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.execution;

import com.powsybl.computation.Partition;
import com.powsybl.contingency.ContingenciesProviders;
import com.powsybl.security.SecurityAnalysis;
import com.powsybl.security.SecurityAnalysisInput;
import com.powsybl.security.distributed.DistributedSecurityAnalysisExecution;
import com.powsybl.security.distributed.ExternalSecurityAnalysisConfig;
import com.powsybl.security.distributed.ForwardedSecurityAnalysisExecution;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Helper class to build a {@link SecurityAnalysisExecution},
 * based on specified options, in particular distribution options.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class SecurityAnalysisExecutionBuilder {

    private final Supplier<ExternalSecurityAnalysisConfig> externalConfig;
    private final String providerName;
    private final SecurityAnalysisInputBuildStrategy inputBuildStrategy;

    private boolean forward = false;
    private Integer taskCount = null;
    private Partition subPart = null;

    /**
     * Create a new builder.
     *
     * @param externalConfig     The method to load an external security analysis config, only used for forwarded and distributed executions.
     * @param providerName       The named security-analysis implementation to use. If {@literal null}, the default would be used.
     * @param inputBuildStrategy The method to translates execution inputs into actual security analysis inputs. Only used for local executions.
     */
    public SecurityAnalysisExecutionBuilder(Supplier<ExternalSecurityAnalysisConfig> externalConfig,
                                            String providerName,
                                            SecurityAnalysisInputBuildStrategy inputBuildStrategy) {
        this.externalConfig = Objects.requireNonNull(externalConfig);
        this.providerName = providerName;
        this.inputBuildStrategy = Objects.requireNonNull(inputBuildStrategy);
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
            return new SecurityAnalysisExecutionImpl(SecurityAnalysis.find(providerName), inputBuildStrategy());
        }
    }

    private SecurityAnalysisInputBuildStrategy subPartBuildStrategy() {
        return executionInput -> {
            SecurityAnalysisInput input = inputBuildStrategy.buildFrom(executionInput);
            input.setContingencies(ContingenciesProviders.newSubProvider(input.getContingenciesProvider(), subPart));
            return input;
        };
    }

    private SecurityAnalysisInputBuildStrategy inputBuildStrategy() {
        if (subPart != null) {
            return subPartBuildStrategy();
        } else {
            return inputBuildStrategy;
        }
    }

}
