/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.google.auto.service.AutoService;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
@AutoService(SecurityAnalysisProvider.class)
public class SecurityAnalysisImplProvider implements SecurityAnalysisProvider {

    private static final String PROVIDER_NAME = "SecurityAnalysisImpl";
    private static final String PROVIDER_VERSION = "1.0";

    @Override
    public CompletableFuture<SecurityAnalysisResult> run(Network network,
                                                         String workingVariantId,
                                                         LimitViolationDetector detector,
                                                         LimitViolationFilter filter,
                                                         ComputationManager computationManager,
                                                         SecurityAnalysisParameters parameters,
                                                         ContingenciesProvider contingenciesProvider,
                                                         List<SecurityAnalysisInterceptor> interceptors) {
        SecurityAnalysisImpl securityAnalysis = new SecurityAnalysisImpl(network, detector, filter, computationManager);
        interceptors.forEach(securityAnalysis::addInterceptor);
        return securityAnalysis.run(workingVariantId, parameters, contingenciesProvider);
    }

    @Override
    public String getName() {
        return PROVIDER_NAME;
    }

    @Override
    public String getVersion() {
        return PROVIDER_VERSION;
    }
}
