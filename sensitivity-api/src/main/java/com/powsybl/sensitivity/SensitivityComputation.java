/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.Versionable;

import java.util.concurrent.CompletableFuture;

/**
 * Sensitivity computation interface
 *
 * <p>
 *     Sensitivity computation is used to assess the impact of a small modification
 *     of a network variables on the value of network functions.
 *     This computation can be assimilated to a partial derivative computed on a given
 *     network state.
 * </p>
 * <p>
 *     PTDFs used in Flowbased methodology for example are sensitivity computation
 *     results. The sensitivity variables are the GSK shift and the sensitivity function
 *     are the monitored lines/transformers flows.
 * </p>
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public interface SensitivityComputation extends Versionable {
    /**
     * Run an asynchronous sensitivity computation job using given parameters and input provider
     *
     * @param factorsProvider sensitivity factors provider for the computation
     * @param workingStateId id of the network base state for the computation
     * @param sensiParameters sensitivity computation parameters
     * @return the sensitivity computation results
     */
    CompletableFuture<SensitivityComputationResults> run(SensitivityFactorsProvider factorsProvider, String workingStateId, SensitivityComputationParameters sensiParameters);

}
