/**
 * Copyright (c) 2016-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public interface SecurityAnalysis {

    default void addInterceptor(SecurityAnalysisInterceptor interceptor) {
    }

    default boolean removeInterceptor(SecurityAnalysisInterceptor interceptor) {
        return false;
    }

    default CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider contingenciesProvider, String workingStateId, SecurityAnalysisParameters securityAnalysisParameters) {
        Objects.requireNonNull(securityAnalysisParameters);
        return runAsync(contingenciesProvider, workingStateId, securityAnalysisParameters.getLoadFlowParameters());
    }

    /**
     * @deprecated Use CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider, String, LoadFlowParameters, SecurityAnalysisParameters) instead
     */
    @Deprecated
    CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider contingenciesProvider, String workingStateId, LoadFlowParameters parameters);

    /**
     * @deprecated Use CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider, String, LoadFlowParameters, SecurityAnalysisParameters) instead
     */
    @Deprecated
    CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider contingenciesProvider, String workingStateId);

    /**
     * @deprecated Use CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider, String, LoadFlowParameters, SecurityAnalysisParameters) instead
     */
    @Deprecated
    CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider contingenciesProvider);
}
