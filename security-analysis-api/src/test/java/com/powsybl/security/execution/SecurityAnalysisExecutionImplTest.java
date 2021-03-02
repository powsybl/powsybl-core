/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.execution;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class SecurityAnalysisExecutionImplTest {

    private static LimitViolationFilter filter;
    private static LimitViolationDetector detector;
    private static ContingenciesProvider contingencies;
    private static SecurityAnalysisParameters parameters;
    private static SecurityAnalysisExecution execution;
    private static Network network;
    private static ComputationManager computationManager;
    private static SecurityAnalysisExecutionInput input;

    @BeforeClass
    public static void setUpClass() {
        filter = Mockito.mock(LimitViolationFilter.class);
        detector = Mockito.mock(LimitViolationDetector.class);
        contingencies = Mockito.mock(ContingenciesProvider.class);
        parameters = Mockito.mock(SecurityAnalysisParameters.class);

        execution = new SecurityAnalysisExecutionImpl(SecurityAnalysis.find("ExecutionImplTestProvider"),
            execInput -> new SecurityAnalysisInput(execInput.getNetworkVariant())
                    .setFilter(filter)
                    .setDetector(detector)
                    .setContingencies(contingencies)
                    .setParameters(parameters)
        );

        network = mock(Network.class);
        computationManager = mock(ComputationManager.class);
        input = new SecurityAnalysisExecutionInput();
        input.setNetworkVariant(network, "variantId");
    }

    @Test
    public void checkExecutionCallAndArguments() {
        assertThrows("run", PowsyblException.class, () -> execution.execute(computationManager, input));
    }

    @Test
    public void checkExecutionWithLogCallAndArguments() {
        assertThrows("runWithLog", PowsyblException.class, () -> execution.executeWithLog(computationManager, input));
    }

    @AutoService(SecurityAnalysisProvider.class)
    public static class SecurityAnalysisProviderMock implements SecurityAnalysisProvider {
        @Override
        public CompletableFuture<SecurityAnalysisResult> run(Network network, String workingVariantId, LimitViolationDetector detector, LimitViolationFilter filter, ComputationManager computationManager, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider, List<SecurityAnalysisInterceptor> interceptors) {
            assertSame(SecurityAnalysisExecutionImplTest.network, network);
            assertSame(SecurityAnalysisExecutionImplTest.input.getNetworkVariant().getVariantId(), workingVariantId);
            assertSame(SecurityAnalysisExecutionImplTest.detector, detector);
            assertSame(SecurityAnalysisExecutionImplTest.filter, filter);
            assertSame(SecurityAnalysisExecutionImplTest.computationManager, computationManager);
            assertSame(SecurityAnalysisExecutionImplTest.parameters, parameters);
            assertSame(SecurityAnalysisExecutionImplTest.contingencies, contingenciesProvider);
            assertTrue(interceptors.isEmpty());
            throw new PowsyblException("run");
        }

        @Override
        public CompletableFuture<SecurityAnalysisResultWithLog> runWithLog(Network network, String workingVariantId, LimitViolationDetector detector, LimitViolationFilter filter, ComputationManager computationManager, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider, List<SecurityAnalysisInterceptor> interceptors) {
            assertSame(SecurityAnalysisExecutionImplTest.network, network);
            assertSame(SecurityAnalysisExecutionImplTest.input.getNetworkVariant().getVariantId(), workingVariantId);
            assertSame(SecurityAnalysisExecutionImplTest.detector, detector);
            assertSame(SecurityAnalysisExecutionImplTest.filter, filter);
            assertSame(SecurityAnalysisExecutionImplTest.computationManager, computationManager);
            assertSame(SecurityAnalysisExecutionImplTest.parameters, parameters);
            assertSame(SecurityAnalysisExecutionImplTest.contingencies, contingenciesProvider);
            assertTrue(interceptors.isEmpty());
            throw new PowsyblException("runWithLog");
        }

        @Override
        public String getName() {
            return "ExecutionImplTestProvider";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }
    }
}
