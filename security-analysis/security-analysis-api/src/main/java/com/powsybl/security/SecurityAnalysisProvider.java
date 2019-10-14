/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.Versionable;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public interface SecurityAnalysisProvider extends Versionable {

    CompletableFuture<SecurityAnalysisResult> run(Network network, LimitViolationDetector detector, LimitViolationFilter filter,
                                                  ComputationManager computationManager, String workingVariantId,
                                                  SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider,
                                                  List<SecurityAnalysisInterceptor> interceptors);

    default CompletableFuture<SecurityAnalysisResultWithLog> runWithLog(Network network, LimitViolationDetector detector, LimitViolationFilter filter,
                                                                        ComputationManager computationManager, String workingVariantId,
                                                                        SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider,
                                                                        List<SecurityAnalysisInterceptor> interceptors) {
        return run(network, detector, filter, computationManager, workingVariantId, parameters, contingenciesProvider, interceptors).thenApply(r -> new SecurityAnalysisResultWithLog(r, null));
    }

    default LimitViolationDetector getDefaultLimitViolationDetector() {
        return new DefaultLimitViolationDetector();
    }

    default LimitViolationFilter getDefaultLimitViolationFilter() {
        return new LimitViolationFilter();
    }

    default List<SecurityAnalysisInterceptor> getDefaultInterceptors() {
        return Collections.emptyList();
    }

}
