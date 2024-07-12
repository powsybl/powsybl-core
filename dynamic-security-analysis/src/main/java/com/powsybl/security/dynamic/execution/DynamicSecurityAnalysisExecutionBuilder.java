/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.execution;

import com.powsybl.contingency.ContingenciesProviders;
import com.powsybl.security.distributed.*;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisInput;
import com.powsybl.security.dynamic.distributed.DistributedDynamicSecurityAnalysisExecution;
import com.powsybl.security.dynamic.distributed.ForwardedDynamicSecurityAnalysisExecution;
import com.powsybl.security.execution.AbstractSecurityAnalysisExecutionBuilder;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Helper class to build a {@link DynamicSecurityAnalysisExecution},
 * based on specified options, in particular distribution options.
 *
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSecurityAnalysisExecutionBuilder extends AbstractSecurityAnalysisExecutionBuilder<DynamicSecurityAnalysisExecutionBuilder> {

    private final DynamicSecurityAnalysisInputBuildStrategy inputBuildStrategy;

    /**
     * Create a new builder.
     *
     * @param externalConfig     The method to load an external security analysis config, only used for forwarded and distributed executions.
     * @param providerName       The named security-analysis implementation to use. If {@literal null}, the default would be used.
     * @param inputBuildStrategy The method to translates execution inputs into actual security analysis inputs. Only used for local executions.
     */
    public DynamicSecurityAnalysisExecutionBuilder(Supplier<ExternalSecurityAnalysisConfig> externalConfig,
                                                   String providerName,
                                                   DynamicSecurityAnalysisInputBuildStrategy inputBuildStrategy) {
        super(externalConfig, providerName);
        this.inputBuildStrategy = Objects.requireNonNull(inputBuildStrategy);
    }

    public DynamicSecurityAnalysisExecution build() {
        if (forward) {
            return new ForwardedDynamicSecurityAnalysisExecution(externalConfig.get(), taskCount);
        } else if (taskCount != null) {
            return new DistributedDynamicSecurityAnalysisExecution(externalConfig.get(), taskCount);
        } else {
            return new DynamicSecurityAnalysisExecutionImpl(providerName, inputBuildStrategy());
        }
    }

    private DynamicSecurityAnalysisInputBuildStrategy inputBuildStrategy() {
        return subPart != null ? subPartBuildStrategy() : inputBuildStrategy;
    }

    protected DynamicSecurityAnalysisInputBuildStrategy subPartBuildStrategy() {
        return (executionInput, providerName) -> {
            DynamicSecurityAnalysisInput input = inputBuildStrategy.buildFrom(executionInput, providerName);
            input.setContingencies(ContingenciesProviders.newSubProvider(input.getContingenciesProvider(), subPart));
            return input;
        };
    }

    @Override
    protected DynamicSecurityAnalysisExecutionBuilder self() {
        return this;
    }
}
