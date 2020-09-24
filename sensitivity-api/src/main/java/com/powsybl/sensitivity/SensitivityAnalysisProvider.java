/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;

import java.util.concurrent.CompletableFuture;

/**
 * Sensitivity analysis provider
 *
 * <p>
 *     Sensitivity analysis is used to assess the impact of a small modification
 *     of a network variables on the value of network functions.
 *     This analysis can be assimilated to a partial derivative computed on a given
 *     network state and on that state modified based on a list of contingencies, if specified.
 * </p>
 * <p>
 *     PTDFs used in Flowbased methodology for example are sensitivity analysis
 *     results. The sensitivity variables are the GSK shift and the sensitivity function
 *     are the monitored lines/transformers flows.
 * </p>
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public interface SensitivityAnalysisProvider extends Versionable, PlatformConfigNamedProvider {

    /**
     * Run an asynchronous single sensitivity analysis job.
     * Factors will be computed by a {@code computationManager} on the {@code workingStateId} of the {@code network}
     * on pre-contingency state and after each {@link com.powsybl.contingency.Contingency} provided by
     * {@code contingenciesProvider} according to the {@code parameters}.
     *
     * @param network IIDM network on which the sensitivity analysis will be performed
     * @param workingStateId network variant ID on which the analysis will be performed
     * @param factorsProvider provider of sensitivity factors to be computed
     * @param contingenciesProvider provider of contingencies after which sensitivity factors will be computed
     * @param parameters specific sensitivity analysis parameters
     * @param computationManager a computation manager to external program execution
     * @return a {@link CompletableFuture} on {@link SensitivityAnalysisResult} that gathers sensitivity factor values
     */
    CompletableFuture<SensitivityAnalysisResult> run(Network network,
                                                     String workingStateId,
                                                     SensitivityFactorsProvider factorsProvider,
                                                     ContingenciesProvider contingenciesProvider,
                                                     SensitivityAnalysisParameters parameters,
                                                     ComputationManager computationManager);
}
