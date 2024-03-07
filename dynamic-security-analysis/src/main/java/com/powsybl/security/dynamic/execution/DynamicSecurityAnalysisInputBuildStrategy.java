/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.execution;

import com.powsybl.security.dynamic.DynamicSecurityAnalysisInput;

/**
 * In charge of transforming an execution input to an actual
 * dynamic security analysis input. Will typically be defined as a lambda.
 *
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
@FunctionalInterface
public interface DynamicSecurityAnalysisInputBuildStrategy {

    /**
     * Build a {@link DynamicSecurityAnalysisInput} for an actual security analysis computation,
     * based on the provided {@link DynamicSecurityAnalysisExecutionInput}, which may for example
     * be defined through a command line call.
     *
     * @param executionInput Execution inputs, as defined through a command line call for example.
     * @return The {@link DynamicSecurityAnalysisInput} to be used for actual computation.
     */
    DynamicSecurityAnalysisInput buildFrom(DynamicSecurityAnalysisExecutionInput executionInput, String providerName);

}
