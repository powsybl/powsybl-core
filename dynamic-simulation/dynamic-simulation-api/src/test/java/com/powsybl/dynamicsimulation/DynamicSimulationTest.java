/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.powsybl.commons.report.ReportNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class DynamicSimulationTest {

    private Network network;

    private ComputationManager computationManager;

    @BeforeEach
    void setUp() {
        network = Mockito.mock(Network.class);
        VariantManager variantManager = Mockito.mock(VariantManager.class);
        Mockito.when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.when(variantManager.getWorkingVariantId()).thenReturn("v");
        computationManager = Mockito.mock(ComputationManager.class);
    }

    @Test
    void testDefaultOneProvider() {
        // case with only one provider, no need for config
        DynamicSimulation.Runner defaultDynamicSimulation = DynamicSimulation.find();
        assertEquals("DynamicSimulationMock", defaultDynamicSimulation.getName());
        assertEquals("1.0", defaultDynamicSimulation.getVersion());
        DynamicSimulationResult result = defaultDynamicSimulation.run(network, DynamicModelsSupplierMock.empty(), new DynamicSimulationParameters());
        assertNotNull(result);
    }

    @Test
    void testAsyncNamedProvider()
            throws InterruptedException, ExecutionException {
        // case with only one provider, no need for config
        DynamicSimulation.Runner defaultDynamicSimulation = DynamicSimulation
                .find("DynamicSimulationMock");
        assertEquals("DynamicSimulationMock", defaultDynamicSimulation.getName());
        CompletableFuture<DynamicSimulationResult> result = defaultDynamicSimulation.runAsync(network, DynamicModelsSupplierMock.empty(), new DynamicSimulationParameters());
        assertNotNull(result.get());
    }

    @Test
    void testProviderRunCombinations() {
        // case with only one provider, no need for config
        DynamicSimulationParameters parameters = new DynamicSimulationParameters();
        assertNotNull(DynamicSimulation.run(network, DynamicModelsSupplierMock.empty()));
        assertNotNull(DynamicSimulation.run(network, DynamicModelsSupplierMock.empty(), CurvesSupplier.empty()));
        assertNotNull(DynamicSimulation.run(network, DynamicModelsSupplierMock.empty(), EventModelsSupplier.empty()));
        assertNotNull(DynamicSimulation.run(network, DynamicModelsSupplierMock.empty(), parameters));
        assertNotNull(DynamicSimulation.run(network, DynamicModelsSupplierMock.empty(), CurvesSupplier.empty(), parameters));
        assertNotNull(DynamicSimulation.run(network, DynamicModelsSupplierMock.empty(), EventModelsSupplier.empty(), parameters));
        assertNotNull(DynamicSimulation.run(network, DynamicModelsSupplierMock.empty(), EventModelsSupplier.empty(), CurvesSupplier.empty(), parameters));
        assertNotNull(DynamicSimulation.run(network, DynamicModelsSupplierMock.empty(), EventModelsSupplier.empty(), CurvesSupplier.empty(), network.getVariantManager().getWorkingVariantId(), parameters));
        assertNotNull(DynamicSimulation.run(network, DynamicModelsSupplierMock.empty(), EventModelsSupplier.empty(), CurvesSupplier.empty(), network.getVariantManager().getWorkingVariantId(), computationManager, parameters, ReportNode.NO_OP));
    }

    @Test
    void testProviderAsyncCombinations() {
        // case with only one provider, no need for config
        DynamicSimulationParameters parameters = new DynamicSimulationParameters();
        assertNotNull(DynamicSimulation.runAsync(network, DynamicModelsSupplierMock.empty()));
        assertNotNull(DynamicSimulation.runAsync(network, DynamicModelsSupplierMock.empty(), CurvesSupplier.empty()));
        assertNotNull(DynamicSimulation.runAsync(network, DynamicModelsSupplierMock.empty(), EventModelsSupplier.empty()));
        assertNotNull(DynamicSimulation.runAsync(network, DynamicModelsSupplierMock.empty(), parameters));
        assertNotNull(DynamicSimulation.runAsync(network, DynamicModelsSupplierMock.empty(), CurvesSupplier.empty(), parameters));
        assertNotNull(DynamicSimulation.runAsync(network, DynamicModelsSupplierMock.empty(), EventModelsSupplier.empty(), parameters));
        assertNotNull(DynamicSimulation.runAsync(network, DynamicModelsSupplierMock.empty(), EventModelsSupplier.empty(), CurvesSupplier.empty(), parameters));
        assertNotNull(DynamicSimulation.runAsync(network, DynamicModelsSupplierMock.empty(), EventModelsSupplier.empty(), CurvesSupplier.empty(), network.getVariantManager().getWorkingVariantId(), parameters));
        assertNotNull(DynamicSimulation.runAsync(network, DynamicModelsSupplierMock.empty(), EventModelsSupplier.empty(), CurvesSupplier.empty(), network.getVariantManager().getWorkingVariantId(), computationManager, parameters, ReportNode.NO_OP));
    }
}
