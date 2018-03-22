/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.afs;

import com.google.common.collect.ImmutableList;
import com.powsybl.afs.ServiceExtension;
import com.powsybl.afs.ext.base.LocalNetworkServiceExtension;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.security.*;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class LocalSecurityAnalysisRunningServiceTest extends SecurityAnalysisRunnerTest {

    private static class SecurityAnalysisFactoryMock implements SecurityAnalysisFactory {
        @Override
        public SecurityAnalysis create(Network network, ComputationManager computationManager, int priority) {
            return new SecurityAnalysis() {
                @Override
                public CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider contingenciesProvider, String workingStateId, LoadFlowParameters parameters) {
                    LimitViolationsResult preContingencyResult = new LimitViolationsResult(true, ImmutableList.of(new LimitViolation("s1", LimitViolationType.HIGH_VOLTAGE, 400f, 1f, 440f)));
                    SecurityAnalysisResult result = new SecurityAnalysisResult(preContingencyResult, Collections.emptyList());
                    return CompletableFuture.completedFuture(result);
                }

                @Override
                public CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider contingenciesProvider, String workingStateId) {
                    throw new AssertionError();
                }

                @Override
                public CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider contingenciesProvider) {
                    throw new AssertionError();
                }
            };
        }
    }

    @Override
    protected List<ServiceExtension> getServiceExtensions() {
        return ImmutableList.of(new LocalSecurityAnalysisRunningServiceExtension(new SecurityAnalysisFactoryMock()),
                new LocalNetworkServiceExtension());
    }

    @Test
    public void test() {
        super.test();
    }

}
