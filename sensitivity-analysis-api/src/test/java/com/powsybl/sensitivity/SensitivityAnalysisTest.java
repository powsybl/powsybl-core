/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class SensitivityAnalysisTest {

    private static final String DEFAULT_PROVIDER_NAME = "SensitivityAnalysisMock";

    private Network network;
    private ComputationManager computationManager;
    private SensitivityFactor factor;
    private SensitivityFactorReader factorReader;
    private SensitivityValueModelWriter valueWriter;
    private List<Contingency> contingencies;
    private List<SensitivityVariableSet> variableSets;
    private SensitivityAnalysisParameters parameters;

    @Before
    public void setUp() {
        network = EurostagTutorialExample1Factory.create();
        computationManager = Mockito.mock(ComputationManager.class);
        factor = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER,
                "NHV1_NHV2_1",
                SensitivityVariableType.INJECTION_ACTIVE_POWER,
                "GEN",
                false,
                ContingencyContext.none());
        factorReader = handler -> handler.onFactor(factor.getFunctionType(), factor.getFunctionId(), factor.getVariableType(), factor.getVariableId(), factor.isVariableSet(), factor.getContingencyContext());
        valueWriter = new SensitivityValueModelWriter();
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
    public void testAsyncDefaultProvider() {
        CompletableFuture<Void> job = SensitivityAnalysis.runAsync(network, "v", factorReader, valueWriter,
                contingencies, variableSets, new SensitivityAnalysisParameters(), computationManager);
        job.join();
        assertEquals(1, valueWriter.getValues().size());
    }

    @Test
    public void testAsyncDefaultProviderWithoutContingencies() {
        CompletableFuture<Void> job = SensitivityAnalysis.runAsync(network, "v", factorReader, valueWriter,
                new SensitivityAnalysisParameters(), computationManager);
        job.join();
        assertEquals(1, valueWriter.getValues().size());
    }

    @Test
    public void testAsyncDefaultProviderWithMinimumArgumentsWithContingencies() {
        CompletableFuture<Void> job = SensitivityAnalysis.runAsync(network,
                factorReader, valueWriter, contingencies, variableSets);
        job.join();
        assertEquals(1, valueWriter.getValues().size());
    }

    @Test
    public void testAsyncDefaultProviderWithMinimumArgumentsWithoutContingencies() {
        CompletableFuture<Void> job = SensitivityAnalysis.runAsync(network,
                factorReader, valueWriter);
        job.join();
        assertEquals(1, valueWriter.getValues().size());
    }

    @Test
    public void testSyncDefaultProvider() {
        SensitivityAnalysis.run(network, "v", factorReader, valueWriter,
                contingencies, variableSets, new SensitivityAnalysisParameters(), computationManager);
        assertEquals(1, valueWriter.getValues().size());
    }

    @Test
    public void testSyncDefaultProviderWithoutContingencies() {
        SensitivityAnalysis.run(network, "v", factorReader, valueWriter,
                new SensitivityAnalysisParameters(), computationManager);
        assertEquals(1, valueWriter.getValues().size());
    }

    @Test
    public void testSyncDefaultProviderWithMinimumArgumentsWithContingencies() {
        SensitivityAnalysis.run(network, factorReader, valueWriter, contingencies, variableSets);
        assertEquals(1, valueWriter.getValues().size());
    }

    @Test
    public void testSyncDefaultProviderWithMinimumArgumentsWithoutContingencies() {
        SensitivityAnalysis.run(network, factorReader, valueWriter);
        assertEquals(1, valueWriter.getValues().size());
    }

    @Test
    public void testStaticRunMethodWithContingencies() {
        SensitivityAnalysis.run(network,
                network.getVariantManager().getWorkingVariantId(), factorReader, valueWriter, contingencies, variableSets, parameters);
        assertEquals(1, valueWriter.getValues().size());
    }

    @Test
    public void testStaticRunMethodWithoutContingencies() {
        SensitivityAnalysis.run(network,
                        network.getVariantManager().getWorkingVariantId(), factorReader, valueWriter, parameters);
        assertEquals(1, valueWriter.getValues().size());
    }

    @Test
    public void testStaticSimpleRunMethodWithParameters() {
        SensitivityAnalysis.run(network, factorReader, valueWriter, contingencies, variableSets, parameters);
        assertEquals(1, valueWriter.getValues().size());
    }

    @Test
    public void testStaticSimpleRunMethodWithParametersWithoutContingencies() {
        SensitivityAnalysis.run(network, factorReader, valueWriter, parameters);
        assertEquals(1, valueWriter.getValues().size());
    }

    @Test
    public void testStaticSimpleRunMethod() {
        SensitivityAnalysis.run(network, factorReader, valueWriter, contingencies, variableSets);
        assertEquals(1, valueWriter.getValues().size());
    }

    @Test
    public void testStaticSimpleRunMethodWithNoContingencies() {
        SensitivityAnalysis.run(network, factorReader, valueWriter);
        assertEquals(1, valueWriter.getValues().size());
    }

    @Test
    public void testSimpleRunWithSensitivityFactorList() {
        SensitivityAnalysisResult res = SensitivityAnalysis.run(network, "v", List.of(factor),
                contingencies, variableSets, new SensitivityAnalysisParameters(), computationManager);
        assertNotNull(res);
    }
}
