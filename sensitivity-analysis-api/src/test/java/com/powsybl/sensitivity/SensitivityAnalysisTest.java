/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
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
    private List<Contingency> contingencies;
    private List<SensitivityVariableSet> variableSets;
    private SensitivityAnalysisParameters parameters;

    @Before
    public void setUp() {
        network = Mockito.mock(Network.class);
        VariantManager variantManager = Mockito.mock(VariantManager.class);
        Mockito.when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.when(variantManager.getWorkingVariantId()).thenReturn("v");
        computationManager = Mockito.mock(ComputationManager.class);
        sensitivityFactorsProvider = Mockito.mock(SensitivityFactorsProvider.class);
        contingencies = Collections.emptyList();
        variableSets = Collections.emptyList();
        parameters = Mockito.mock(SensitivityAnalysisParameters.class);
    }

    @Test
    public void testDefaultProvider() {
        SensitivityAnalysis.Runner defaultSensitivityAnalysisRunner = SensitivityAnalysis.find();
        assertEquals(DEFAULT_PROVIDER_NAME, defaultSensitivityAnalysisRunner.getName());
        assertEquals("1.0", defaultSensitivityAnalysisRunner.getVersion());
    }

    @Test
    public void testAsyncDefaultProvider() throws InterruptedException, ExecutionException {
        CompletableFuture<SensitivityAnalysisResult> result = SensitivityAnalysis.runAsync(network, "v", sensitivityFactorsProvider,
                contingencies, variableSets, new SensitivityAnalysisParameters(), computationManager);
        assertNotNull(result.get());
    }

    @Test
    public void testAsyncDefaultProviderWithoutContingencies() throws InterruptedException, ExecutionException {
        CompletableFuture<SensitivityAnalysisResult> result = SensitivityAnalysis.runAsync(network, "v", sensitivityFactorsProvider,
                new SensitivityAnalysisParameters(), computationManager);
        assertNotNull(result.get());
    }

    @Test
    public void testAsyncDefaultProviderWithMinimumArgumentsWithContingencies() throws InterruptedException, ExecutionException {
        CompletableFuture<SensitivityAnalysisResult> result = SensitivityAnalysis.runAsync(network,
                sensitivityFactorsProvider, contingencies, variableSets);
        assertNotNull(result.get());
    }

    @Test
    public void testAsyncDefaultProviderWithMinimumArgumentsWithoutContingencies() throws InterruptedException, ExecutionException {
        CompletableFuture<SensitivityAnalysisResult> result = SensitivityAnalysis.runAsync(network,
                sensitivityFactorsProvider);
        assertNotNull(result.get());
    }

    @Test
    public void testSyncDefaultProvider() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, "v", sensitivityFactorsProvider,
                contingencies, variableSets, new SensitivityAnalysisParameters(), computationManager);
        assertNotNull(result);
    }

    @Test
    public void testSyncDefaultProviderWithoutContingencies() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, "v", sensitivityFactorsProvider,
                new SensitivityAnalysisParameters(), computationManager);
        assertNotNull(result);
    }

    @Test
    public void testSyncDefaultProviderWithMinimumArgumentsWithContingencies() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, sensitivityFactorsProvider, contingencies, variableSets);
        assertNotNull(result);
    }

    @Test
    public void testSyncDefaultProviderWithMinimumArgumentsWithoutContingencies() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, sensitivityFactorsProvider);
        assertNotNull(result);
    }

    @Test
    public void testStaticRunMethodWithContingencies() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network,
                network.getVariantManager().getWorkingVariantId(), sensitivityFactorsProvider, contingencies, variableSets, parameters);
        assertNotNull(result);
    }

    @Test
    public void testStaticRunMethodWithoutContingencies() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network,
                        network.getVariantManager().getWorkingVariantId(), sensitivityFactorsProvider, parameters);
        assertNotNull(result);
    }

    @Test
    public void testStaticSimpleRunMethodWithParameters() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, sensitivityFactorsProvider, contingencies, variableSets, parameters);
        assertNotNull(result);
    }

    @Test
    public void testStaticSimpleRunMethodWithParametersWithoutContingencies() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, sensitivityFactorsProvider, parameters);
        assertNotNull(result);
    }

    @Test
    public void testStaticSimpleRunMethod() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, sensitivityFactorsProvider, contingencies, variableSets);
        assertNotNull(result);
    }

    @Test
    public void testStaticSimpleRunMethodWithNoContingencies() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, sensitivityFactorsProvider);
        assertNotNull(result);
    }
}
