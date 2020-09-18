/*
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
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
public class SensitivityComputationTest {

    private static final String DEFAULT_PROVIDER_NAME = "SensitivityComputationMock";

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
        SensitivityComputation.Runner defaultSensitivityComputation = SensitivityComputation.find();
        assertEquals(DEFAULT_PROVIDER_NAME, defaultSensitivityComputation.getName());
        SensitivityComputationResults results = defaultSensitivityComputation.run(network, sensitivityFactorsProvider, contingenciesProvider);
        assertNotNull(results);
        assertTrue(results.isOk());
    }

    @Test
    public void testAsyncNamedProvider() throws InterruptedException, ExecutionException {
        // named provider
        SensitivityComputation.Runner defaultSensitivityComputation = SensitivityComputation.find(DEFAULT_PROVIDER_NAME);
        CompletableFuture<SensitivityComputationResults> result = defaultSensitivityComputation.runAsync(network, "v", sensitivityFactorsProvider,
                contingenciesProvider, new SensitivityComputationParameters(), computationManager);
        assertNotNull(result.get());
    }

    @Test
    public void testDefaultProviderNoContingencies() {
        SensitivityComputation.Runner defaultSensitivityComputation = SensitivityComputation.find();
        assertEquals(DEFAULT_PROVIDER_NAME, defaultSensitivityComputation.getName());
        SensitivityComputationResults results = defaultSensitivityComputation.run(network, sensitivityFactorsProvider);
        assertNotNull(results);
        assertTrue(results.isOk());
    }

    @Test
    public void testAsyncNamedProviderNoContingencies() throws InterruptedException, ExecutionException {
        // named provider
        SensitivityComputation.Runner defaultSensitivityComputation = SensitivityComputation.find(DEFAULT_PROVIDER_NAME);
        CompletableFuture<SensitivityComputationResults> result = defaultSensitivityComputation.runAsync(network, "v", sensitivityFactorsProvider,
                new SensitivityComputationParameters(), computationManager);
        assertNotNull(result.get());
    }

}
