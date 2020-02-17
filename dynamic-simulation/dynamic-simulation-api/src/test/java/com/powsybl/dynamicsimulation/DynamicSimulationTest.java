/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class DynamicSimulationTest {

    private Network network;

    private ComputationManager computationManager;

    @Before
    public void setUp() {
        network = Mockito.mock(Network.class);
        VariantManager variantManager = Mockito.mock(VariantManager.class);
        Mockito.when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.when(variantManager.getWorkingVariantId()).thenReturn("v");
        computationManager = Mockito.mock(ComputationManager.class);
    }

    @Test
    public void testDefaultOneProvider() {
        // case with only one provider, no need for config
        DynamicSimulation.Runner defaultDynamicSimulation = DynamicSimulation.find();
        assertEquals("DynamicSimulationMock", defaultDynamicSimulation.getName());
        DynamicSimulationResult result = defaultDynamicSimulation.run(network, computationManager, new DynamicSimulationParameters());
        assertNotNull(result);
    }

    @Test
    public void testAsyncNamedProvider()
            throws InterruptedException, ExecutionException {
        // case with only one provider, no need for config
        DynamicSimulation.Runner defaultDynamicSimulation = DynamicSimulation
                .find("DynamicSimulationMock");
        assertEquals("DynamicSimulationMock", defaultDynamicSimulation.getName());
        CompletableFuture<DynamicSimulationResult> result = defaultDynamicSimulation.runAsync(network, computationManager, new DynamicSimulationParameters());
        assertNotNull(result.get());
    }
}
