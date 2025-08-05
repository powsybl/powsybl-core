/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.loadflow;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class LoadFlowTest {

    private static final String DEFAULT_PROVIDER_NAME = "LoadFlowMock";

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
    void testDefaultProvider() {
        // case with only one provider, no need for config
        LoadFlow.Runner defaultLoadFlow = LoadFlow.find();
        assertEquals(DEFAULT_PROVIDER_NAME, defaultLoadFlow.getName());
        LoadFlowResult result = defaultLoadFlow.run(network, computationManager,
                new LoadFlowParameters());
        assertNotNull(result);
    }

    @Test
    void testAsyncNamedProvider() throws InterruptedException, ExecutionException {
        // named provider
        LoadFlow.Runner defaultLoadFlow = LoadFlow.find(DEFAULT_PROVIDER_NAME);
        CompletableFuture<LoadFlowResult> result = defaultLoadFlow.runAsync(network,
                computationManager, new LoadFlowParameters());
        assertNotNull(result.get());
    }

    @Test
    void testRunWithDefaultParameters() {
        LoadFlow.Runner defaultLoadFlow = LoadFlow.find();
        LoadFlowResult result = defaultLoadFlow.setNetwork(network).run();
        assertNotNull(result);
    }

    @Test
    void testRunWithFluentSetters() {
        LoadFlow.Runner defaultLoadFlow = LoadFlow.find();
        LoadFlowResult result = defaultLoadFlow.setNetwork(network)
                .setComputationManager(computationManager)
                .setParameters(new LoadFlowParameters())
                .setVariantId("VariantId")
                .setReportNode(ReportNode.NO_OP)
                .run();
        assertNotNull(result);
    }

    @Test
    void testRunAsyncWithDefaultParameters() throws InterruptedException, ExecutionException {
        LoadFlow.Runner defaultLoadFlow = LoadFlow.find();
        CompletableFuture<LoadFlowResult> result = defaultLoadFlow.setNetwork(network).runAsync();
        assertNotNull(result.get());
    }

    @Test
    void testRunAsyncWithFluentSetters() throws InterruptedException, ExecutionException {
        LoadFlow.Runner defaultLoadFlow = LoadFlow.find();
        CompletableFuture<LoadFlowResult> result = defaultLoadFlow.setNetwork(network)
                .setComputationManager(computationManager)
                .setParameters(new LoadFlowParameters())
                .setVariantId("VariantId")
                .setReportNode(ReportNode.NO_OP)
                .runAsync();
        assertNotNull(result.get());
    }

}
