/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.execution;

import com.google.auto.service.AutoService;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisExecutionImplTest {

    private static LimitViolationFilter filter = mock(LimitViolationFilter.class);
    private static LimitViolationDetector detector = mock(LimitViolationDetector.class);
    private static ContingenciesProvider contingencies = mock(ContingenciesProvider.class);
    private static SecurityAnalysisParameters parameters = mock(SecurityAnalysisParameters.class);
    private SecurityAnalysisExecution execution;
    private static Network network = mock(Network.class);
    private static ComputationManager computationManager = mock(ComputationManager.class);
    private SecurityAnalysisExecutionInput input;

    private static int runCnt = 0;
    private static int runLogCnt = 0;

    @Before
    public void setUp() {
        execution = new SecurityAnalysisExecutionImpl("ExecutionImplMockProvider",
            execInput -> new SecurityAnalysisInput(execInput.getNetworkVariant())
                        .setFilter(filter)
                        .setDetector(detector)
                        .setContingencies(contingencies)
                        .setParameters(parameters)
        );

        input = new SecurityAnalysisExecutionInput();
        input.setNetworkVariant(network, "variantId");
    }

    @AutoService(SecurityAnalysisProvider.class)
    public static class ExecutionImplMockProvider implements SecurityAnalysisProvider {

        @Override
        public CompletableFuture<SecurityAnalysisResult> run(Network network, LimitViolationDetector detector, LimitViolationFilter filter, ComputationManager computationManager, String workingVariantId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider, List<SecurityAnalysisInterceptor> interceptors) {
            runCnt++;
            assertSameArguments(network, detector, filter, computationManager, workingVariantId, parameters, contingenciesProvider, interceptors);
            return null;
        }

        @Override
        public String getName() {
            return "ExecutionImplMockProvider";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }

        @Override
        public CompletableFuture<SecurityAnalysisResultWithLog> runWithLog(Network network, LimitViolationDetector detector, LimitViolationFilter filter, ComputationManager computationManager, String workingVariantId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider, List<SecurityAnalysisInterceptor> interceptors) {
            runLogCnt++;
            assertSameArguments(network, detector, filter, computationManager, workingVariantId, parameters, contingenciesProvider, interceptors);
            return null;
        }

        private static void assertSameArguments(Network network, LimitViolationDetector detector, LimitViolationFilter filter, ComputationManager computationManager, String workingVariantId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider, List<SecurityAnalysisInterceptor> interceptors) {
            assertSame(SecurityAnalysisExecutionImplTest.network, network);
            assertSame(SecurityAnalysisExecutionImplTest.detector, detector);
            assertSame(SecurityAnalysisExecutionImplTest.filter, filter);
            assertSame(SecurityAnalysisExecutionImplTest.computationManager, computationManager);
            assertSame(SecurityAnalysisExecutionImplTest.parameters, parameters);
            assertSame(SecurityAnalysisExecutionImplTest.contingencies, contingenciesProvider);
        }
    }

    @Test
    public void checkExecutionCallAndArguments() {
        int oldRunCnt = runCnt;
        int oldRunLogCnt = runLogCnt;
        execution.execute(computationManager, input);
        assertEquals(++oldRunCnt, runCnt);
        assertEquals(oldRunLogCnt, runLogCnt);
    }

    @Test
    public void checkExecutionWithLogCallAndArguments() {
        int oldRunCnt = runCnt;
        int oldRunLogCnt = runLogCnt;
        execution.executeWithLog(computationManager, input);
        assertEquals(oldRunCnt, runCnt);
        assertEquals(++oldRunLogCnt, runLogCnt);
    }
}
