/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;

import java.util.List;
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
 *     results. The sensitivity variables are the GSK shift and the function reference
 *     are the monitored lines/transformers flows.
 * </p>
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public interface SensitivityAnalysisProvider extends Versionable, PlatformConfigNamedProvider {

    /**
     * Run a single sensitivity analysis.
     * Factors are given by a {@code factorReader} on the {@code workingStateId} of the {@code network}
     * on pre-contingency state and after each {@link com.powsybl.contingency.Contingency} provided by
     * {@code contingencies} according to the {@code parameters}.
     *
     * @param network IIDM network on which the sensitivity analysis will be performed
     * @param workingStateId network variant ID on which the analysis will be performed
     * @param factorReader provider of sensitivity factors to be computed
     * @param valueWriter provider of sensitivity values results
     * @param contingencies list of contingencies after which sensitivity factors will be computed
     * @param variableSets list of variableSets
     * @param parameters specific sensitivity analysis parameters
     * @param computationManager a computation manager to external program execution
     * @param reporter a reporter for functional logs
     * @return a {@link CompletableFuture} on {@link SensitivityAnalysisResult} that gathers sensitivity factor values
     */
    CompletableFuture<Void> run(Network network,
                                String workingStateId,
                                SensitivityFactorReader factorReader,
                                SensitivityValueWriter valueWriter,
                                List<Contingency> contingencies,
                                List<SensitivityVariableSet> variableSets,
                                SensitivityAnalysisParameters parameters,
                                ComputationManager computationManager,
                                Reporter reporter);
}
