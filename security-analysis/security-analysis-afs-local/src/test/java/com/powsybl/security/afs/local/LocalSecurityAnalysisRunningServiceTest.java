/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.afs.local;

import com.google.common.collect.ImmutableList;
import com.powsybl.afs.ServiceExtension;
import com.powsybl.afs.ext.base.LocalNetworkCacheServiceExtension;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;
import com.powsybl.security.afs.SecurityAnalysisRunnerTest;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class LocalSecurityAnalysisRunningServiceTest extends SecurityAnalysisRunnerTest {

    private static class SecurityAnalysisFactoryMock implements SecurityAnalysisFactory {
        @Override
        public SecurityAnalysis create(Network network, ComputationManager computationManager, int priority) {
            return new SecurityAnalysis() {

                @Override
                public void addInterceptor(SecurityAnalysisInterceptor interceptor) {
                    // Not implemented
                }

                @Override
                public boolean removeInterceptor(SecurityAnalysisInterceptor interceptor) {
                    // Not implemented
                    return false;
                }

                @Override
                public CompletableFuture<SecurityAnalysisResult> run(String workingStateId, SecurityAnalysisParameters securityAnalysisParameters, ContingenciesProvider contingenciesProvider) {
                    LimitViolationsResult preContingencyResult = new LimitViolationsResult(true, ImmutableList.of(new LimitViolation("s1", LimitViolationType.HIGH_VOLTAGE, 400.0, 1f, 440.0)));
                    SecurityAnalysisResult result = new SecurityAnalysisResult(preContingencyResult, Collections.emptyList());
                    return CompletableFuture.completedFuture(result);
                }
            };
        }
    }

    @Override
    protected List<ServiceExtension> getServiceExtensions() {
        return ImmutableList.of(new LocalSecurityAnalysisRunningServiceExtension(SecurityAnalysisFactoryMock::new),
                new LocalNetworkCacheServiceExtension());
    }
}
