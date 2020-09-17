/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.Versionable;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;

import java.util.concurrent.CompletableFuture;

/**
 * Sensitivity computation provider
 *
 * <p>
 *     Sensitivity computation is used to assess the impact of a small modification
 *     of a network variables on the value of network functions.
 *     This computation can be assimilated to a partial derivative computed on a given
 *     network state and on that state modified based on a list of contingencies, if specified.
 * </p>
 * <p>
 *     PTDFs used in Flowbased methodology for example are sensitivity computation
 *     results. The sensitivity variables are the GSK shift and the sensitivity function
 *     are the monitored lines/transformers flows.
 * </p>
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public interface SensitivityComputationProvider extends Versionable {

    /**
     * Run an asynchronous single sensitivity computation job.
     * Factors will be computed by a {@code computationManager} on the {@code workingStateId} of the {@code network}
     * according to the {@code parameters}.
     *
     * @param network IIDM network on which the sensitivity computation will be performed
     * @param workingStateId network variant ID on which the computation will be performed
     * @param factorsProvider provider of sensitivity factors to be computed
     * @param parameters specific sensitivity computation parameters
     * @param computationManager a computation manager to external program execution
     * @return a {@link CompletableFuture} on {@link SensitivityComputationResults} that gathers sensitivity factor values
     */
    CompletableFuture<SensitivityComputationResults> run(Network network,
                                                         String workingStateId,
                                                         SensitivityFactorsProvider factorsProvider,
                                                         SensitivityComputationParameters parameters,
                                                         ComputationManager computationManager);

    /**
     * Run an asynchronous single sensitivity computation job.
     * Factors will be computed by a {@code computationManager} on the {@code workingStateId} of the {@code network}
     * after each {@link com.powsybl.contingency.Contingency} provided by {@code contingenciesProvider} according to
     * the {@code parameters}.
     *
     * @param network IIDM network on which the sensitivity computation will be performed
     * @param workingStateId network variant ID on which the computation will be performed
     * @param factorsProvider provider of sensitivity factors to be computed
     * @param contingenciesProvider provider of contingencies after which sensitivity factors will be computed
     * @param parameters specific sensitivity computation parameters
     * @param computationManager a computation manager to external program execution
     * @return a {@link CompletableFuture} on {@link SensitivityComputationResults} that gathers sensitivity factor values
     */
    default CompletableFuture<SensitivityComputationResults> run(Network network,
                                                                 String workingStateId,
                                                                 SensitivityFactorsProvider factorsProvider,
                                                                 ContingenciesProvider contingenciesProvider,
                                                                 SensitivityComputationParameters parameters,
                                                                 ComputationManager computationManager) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
