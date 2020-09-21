/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class SensitivityAnalysisTest {

    private static final String DEFAULT_PROVIDER_NAME = "SensitivityAnalysisMock";

    private Network network;
    private ComputationManager computationManager;
    private SensitivityFactorsProvider sensitivityFactorsProvider;
    private ContingenciesProvider contingenciesProvider;

    @Before
    public void setUp() {
        network = Mockito.mock(Network.class);
        VariantManager variantManager = Mockito.mock(VariantManager.class);
        Mockito.when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.when(variantManager.getWorkingVariantId()).thenReturn("v");
        computationManager = Mockito.mock(ComputationManager.class);
        sensitivityFactorsProvider = Mockito.mock(SensitivityFactorsProvider.class);
        contingenciesProvider = Mockito.mock(ContingenciesProvider.class);
    }

    @Test
    public void testDefaultProvider() {
        SensitivityAnalysis.Runner defaultSensitivityAnalysisRunner = SensitivityAnalysis.find();
        assertEquals(DEFAULT_PROVIDER_NAME, defaultSensitivityAnalysisRunner.getName());
        SensitivityAnalysisResults results = defaultSensitivityAnalysisRunner.run(network, sensitivityFactorsProvider, contingenciesProvider);
        assertNotNull(results);
        assertTrue(results.isOk());
        assertEquals("1.0", defaultSensitivityAnalysisRunner.getVersion());
    }

    @Test
    public void testAsyncNamedProvider() throws InterruptedException, ExecutionException {
        // named provider
        SensitivityAnalysis.Runner defaultSensitivityAnalysisRunner = SensitivityAnalysis.find(DEFAULT_PROVIDER_NAME);
        CompletableFuture<SensitivityAnalysisResults> result = defaultSensitivityAnalysisRunner.runAsync(network, "v", sensitivityFactorsProvider,
                contingenciesProvider, new SensitivityAnalysisParameters(), computationManager);
        assertNotNull(result.get());
    }

    @Test
    public void testAsyncNamedProviderWithMinimumArgumentsWithContingencies() throws InterruptedException, ExecutionException {
        // named provider
        SensitivityAnalysis.Runner defaultSensitivityAnalysisRunner = SensitivityAnalysis.find(DEFAULT_PROVIDER_NAME);
        CompletableFuture<SensitivityAnalysisResults> result = defaultSensitivityAnalysisRunner.runAsync(network,
            sensitivityFactorsProvider, contingenciesProvider);
        assertNotNull(result.get());
    }

    @Test
    public void testAsyncNamedProviderWithMinimumArgumentsNoContingencies() throws InterruptedException, ExecutionException {
        // named provider
        SensitivityAnalysis.Runner defaultSensitivityAnalysisRunner = SensitivityAnalysis.find(DEFAULT_PROVIDER_NAME);
        CompletableFuture<SensitivityAnalysisResults> result = defaultSensitivityAnalysisRunner.runAsync(network,
            sensitivityFactorsProvider);
        assertNotNull(result.get());
    }

    @Test
    public void testDefaultProviderNoContingencies() {
        SensitivityAnalysis.Runner defaultSensitivityAnalysisRunner = SensitivityAnalysis.find();
        assertEquals(DEFAULT_PROVIDER_NAME, defaultSensitivityAnalysisRunner.getName());
        SensitivityAnalysisResults results = defaultSensitivityAnalysisRunner.run(network, sensitivityFactorsProvider);
        assertNotNull(results);
        assertTrue(results.isOk());
    }

    @Test
    public void testAsyncNamedProviderNoContingencies() throws InterruptedException, ExecutionException {
        // named provider
        SensitivityAnalysis.Runner defaultSensitivityAnalysisRunner = SensitivityAnalysis.find(DEFAULT_PROVIDER_NAME);
        CompletableFuture<SensitivityAnalysisResults> result = defaultSensitivityAnalysisRunner.runAsync(network, "v", sensitivityFactorsProvider,
                new SensitivityAnalysisParameters(), computationManager);
        assertNotNull(result.get());
    }

}
