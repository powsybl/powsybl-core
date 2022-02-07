/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.shortcircuit.interceptors.ShortCircuitAnalysisInterceptor;
import com.powsybl.shortcircuit.interceptors.ShortCircuitAnalysisInterceptorMock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
            public CompletableFuture<ShortCircuitAnalysisResult> run(Network network, ShortCircuitParameters parameters,
                                                                     ComputationManager computationManager) {

                return CompletableFuture.supplyAsync(() -> new ShortCircuitAnalysisResult(Collections.emptyList(), Collections.emptyList()));
            }
        };

        ShortCircuitAnalysisResult res = provider.run(null, null, null).join();

        assertEquals(0, res.getFaultResults().size());
        assertEquals(0, res.getLimitViolations().size());
    }

    private static final String DEFAULT_PROVIDER_NAME = "ShortCircuitAnalysisMock";

    private Network network;
    private ComputationManager computationManager;
    private ShortCircuitParameters shortCircuitParameters;

    @Before
    public void setUp() {
        network = Mockito.mock(Network.class);
        VariantManager variantManager = Mockito.mock(VariantManager.class);
        Mockito.when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.when(variantManager.getWorkingVariantId()).thenReturn("v");
        computationManager = Mockito.mock(ComputationManager.class);
        shortCircuitParameters = Mockito.mock(ShortCircuitParameters.class);
    }

    @Test
    public void testDefaultProvider() {
        ShortCircuitAnalysis.Runner defaultShortCircuitAnalysisRunner = ShortCircuitAnalysis.find();
        assertEquals(DEFAULT_PROVIDER_NAME, defaultShortCircuitAnalysisRunner.getName());
        assertEquals("1.0", defaultShortCircuitAnalysisRunner.getVersion());
    }

    @Test
    public void testAsyncDefaultProvider() throws InterruptedException, ExecutionException {
        CompletableFuture<ShortCircuitAnalysisResult> result = ShortCircuitAnalysis.runAsync(network, new ShortCircuitParameters(), computationManager);
        assertNotNull(result.get());
    }

    @Test
    public void testSyncDefaultProvider() {
        ShortCircuitAnalysisResult result = ShortCircuitAnalysis.run(network, new ShortCircuitParameters(), computationManager);
        assertNotNull(result);
    }

    @Test
    public void testSyncDefaultProviderWithoutComputationManager() {
        ShortCircuitAnalysisResult result = ShortCircuitAnalysis.run(network, new ShortCircuitParameters());
        assertNotNull(result);
    }

    @Test
    public void testSyncDefaultProviderWithoutParameters() {
        ShortCircuitAnalysisResult result = ShortCircuitAnalysis.run(network);
        assertNotNull(result);
    }

    @Test
    public void testInterceptor() {
        Network network = EurostagTutorialExample1Factory.create();
        ShortCircuitAnalysisInterceptorMock interceptorMock = new ShortCircuitAnalysisInterceptorMock();
        ShortCircuitAnalysisResult result = ShortCircuitAnalysisMock.runAsync(network);
        assertNotNull(result);

        List<FaultResult> faultResult = result.getFaultResults();
        interceptorMock.onFaultResult(network, faultResult.get(0));
        interceptorMock.onLimitViolation(network, result.getLimitViolations().get(0));
        interceptorMock.onShortCircuitResult(network, result);
    }
}
