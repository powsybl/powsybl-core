/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.execution;

import com.powsybl.security.SecurityAnalysisInput;

/**
 * In charge of transforming an execution input to an actual
 * security analysis input. Will typically be defined as a lambda.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
@FunctionalInterface
public interface SecurityAnalysisInputBuildStrategy {

    /**
     * Build a {@link SecurityAnalysisInput} for an actual security analysis computation,
     * based on the provided {@link SecurityAnalysisExecutionInput}, which may for example
     * be defined through a command line call.
     *
     * @param executionInput Execution inputs, as defined through a command line call for example.
     * @return The {@link SecurityAnalysisInput} to be used for actual computation.
     */
    SecurityAnalysisInput buildFrom(SecurityAnalysisExecutionInput executionInput);

}
