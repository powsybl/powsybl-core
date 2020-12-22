/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Security analysis provider
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
public interface SecurityAnalysisProvider extends Versionable, PlatformConfigNamedProvider {

    /**
     * Run an asynchronous single security analysis job.
     *
     * @param network IIDM network on which the security analysis will be performed
     * @param workingVariantId network variant ID on which the analysis will be performed
     * @param detector
     * @param filter
     * @param computationManager
     * @param parameters specific security analysis parameters
     * @param contingenciesProvider provides list of contingencies
     * @param interceptors
     * @return a {@link CompletableFuture} on {@link SecurityAnalysisResult} that gathers sensitivity factor values
     */
    CompletableFuture<SecurityAnalysisResult> run(Network network,
                                                  String workingVariantId,
                                                  LimitViolationDetector detector,
                                                  LimitViolationFilter filter,
                                                  ComputationManager computationManager,
                                                  SecurityAnalysisParameters parameters,
                                                  ContingenciesProvider contingenciesProvider,
                                                  List<SecurityAnalysisInterceptor> interceptors);

    default CompletableFuture<SecurityAnalysisResultWithLog> runWithLog(Network network,
                                                                        String workingVariantId,
                                                                        LimitViolationDetector detector,
                                                                        LimitViolationFilter filter,
                                                                        ComputationManager computationManager,
                                                                        SecurityAnalysisParameters parameters,
                                                                        ContingenciesProvider contingenciesProvider,
                                                                        List<SecurityAnalysisInterceptor> interceptors) {
        return run(network, workingVariantId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors).thenApply(r -> new SecurityAnalysisResultWithLog(r, null));
    }
}
