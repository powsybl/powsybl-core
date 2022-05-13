/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.shortcircuit.interceptors.ShortCircuitAnalysisInterceptor;
import com.powsybl.shortcircuit.interceptors.ShortCircuitAnalysisInterceptorMock;
import com.powsybl.shortcircuit.monitor.StateMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * @author Bertrand Rix <bertrand.rix at artelys.com>
 */
public class ShortCircuitAnalysisTest {

    @Test
    public void shortCircuitAnalysisWithDummyProvider() {

        ShortCircuitAnalysisProvider provider = new ShortCircuitAnalysisProvider() {
            @Override
            public String getName() {
                return "DummyProvider";
            }

            @Override
            public String getVersion() {
                return "0.0";
            }

            @Override
            public void addInterceptor(final ShortCircuitAnalysisInterceptor interceptor) {

            }

            @Override
            public boolean removeInterceptor(final ShortCircuitAnalysisInterceptor interceptor) {
                return false;
            }

            @Override
            public CompletableFuture<ShortCircuitAnalysisResult> run(Network network,
                                                                     List<Fault> fault,
                                                                     ShortCircuitParameters parameters,
                                                                     ComputationManager computationManager,
                                                                     List<StateMonitor> monitors) {

                return CompletableFuture.supplyAsync(() -> new ShortCircuitAnalysisResult(Collections.emptyList()));
            }
        };

        ShortCircuitAnalysisResult res = provider.run(null, null, null, null, null).join();

        assertEquals(0, res.getFaultResults().size());
    }

    private static final String DEFAULT_PROVIDER_NAME = "ShortCircuitAnalysisMock";

    private Network network;
    private ComputationManager computationManager;
    private ShortCircuitParameters shortCircuitParameters;
    private List<Fault> faults;
    private List<StateMonitor> monitors;

    @Before
    public void setUp() {
        network = Mockito.mock(Network.class);
        VariantManager variantManager = Mockito.mock(VariantManager.class);
        Mockito.when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.when(variantManager.getWorkingVariantId()).thenReturn("v");
        computationManager = Mockito.mock(ComputationManager.class);
        shortCircuitParameters = Mockito.mock(ShortCircuitParameters.class);
        faults = Mockito.mock(List.class);
        monitors = Mockito.mock(List.class);
    }

    @Test
    public void testDefaultProvider() {
        ShortCircuitAnalysis.Runner defaultShortCircuitAnalysisRunner = ShortCircuitAnalysis.find();
        assertEquals(DEFAULT_PROVIDER_NAME, defaultShortCircuitAnalysisRunner.getName());
        assertEquals("1.0", defaultShortCircuitAnalysisRunner.getVersion());
    }

    @Test
    public void testAsyncDefaultProvider() throws InterruptedException, ExecutionException {
        CompletableFuture<ShortCircuitAnalysisResult> result = ShortCircuitAnalysis.runAsync(network, faults, shortCircuitParameters, computationManager, monitors);
        assertNotNull(result.get());
    }

    @Test
    public void testSyncDefaultProvider() {
        ShortCircuitAnalysisResult result = ShortCircuitAnalysis.run(network, faults, shortCircuitParameters, computationManager, monitors);
        assertNotNull(result);
    }

    @Test
    public void testSyncDefaultProviderWithoutComputationManager() {
        ShortCircuitAnalysisResult result = ShortCircuitAnalysis.run(network, faults, shortCircuitParameters, monitors);
        assertNotNull(result);
    }

    @Test
    public void testSyncDefaultProviderWithoutParameters() {
        ShortCircuitAnalysisResult result = ShortCircuitAnalysis.run(network, faults);
        assertNotNull(result);
    }

    @Test
    public void testWithReporter() {
        ReporterModel reporter = new ReporterModel("testReportShortCircuit", "Test mock short circuit");
        ShortCircuitAnalysisResult result = ShortCircuitAnalysis.run(network, faults, shortCircuitParameters, computationManager, monitors, reporter);
        assertNotNull(result);
        List<ReporterModel> subReporters = reporter.getSubReporters();
        assertEquals(1, subReporters.size());
        ReporterModel subReporter = subReporters.get(0);
        assertEquals("MockShortCircuit", subReporter.getTaskKey());
        assertEquals("Running mock short circuit", subReporter.getDefaultName());
        assertTrue(subReporter.getReports().isEmpty());
    }

    @Test
    public void testInterceptor() {
        Network network = EurostagTutorialExample1Factory.create();
        ShortCircuitAnalysisInterceptorMock interceptorMock = new ShortCircuitAnalysisInterceptorMock();
        ShortCircuitAnalysisResult result = ShortCircuitAnalysisMock.runWithNonEmptyResult();
        assertNotNull(result);

        List<FaultResult> faultResult = result.getFaultResults();
        interceptorMock.onFaultResult(network, faultResult.get(0));
        interceptorMock.onLimitViolation(network, result.getLimitViolations().get(faultResult.get(0).getFault().getId()).get(0));
        interceptorMock.onShortCircuitResult(network, result);
    }
}
