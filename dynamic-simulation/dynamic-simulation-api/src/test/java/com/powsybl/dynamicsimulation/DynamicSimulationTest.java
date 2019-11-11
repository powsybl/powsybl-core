/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class DynamicSimulationTest {

    private FileSystem fileSystem;

    private InMemoryPlatformConfig platformConfig;

    private Network network;

    private VariantManager variantManager;

    private ComputationManager computationManager;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        network = Mockito.mock(Network.class);
        variantManager = Mockito.mock(VariantManager.class);
        Mockito.when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.when(variantManager.getWorkingVariantId()).thenReturn("v");
        computationManager = Mockito.mock(ComputationManager.class);
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void testDefaultOneProvider() {
        // case with only one provider, no need for config
        DynamicSimulation.Runner defaultDynamicSimulation = DynamicSimulation.find(null, ImmutableList.of(new DynamicSimulationProviderMock()), platformConfig);
        assertEquals("DynamicSimulationMock", defaultDynamicSimulation.getName());
        DynamicSimulationResult result = defaultDynamicSimulation.run(network, computationManager, new DynamicSimulationParameters());
        assertNotNull(result);
    }

    @Test
    public void testAsyncDefaultOneProvider() {
        // case with only one provider, no need for config
        DynamicSimulation.Runner defaultDynamicSimulation = DynamicSimulation.find(null, ImmutableList.of(new DynamicSimulationProviderMock()), platformConfig);
        assertEquals("DynamicSimulationMock", defaultDynamicSimulation.getName());
        CompletableFuture<DynamicSimulationResult> result = defaultDynamicSimulation.runAsync(network, computationManager, new DynamicSimulationParameters());
        try {
            assertNotNull(result.get());
        } catch (InterruptedException | ExecutionException ignored) {
        }
    }

    @Test
    public void testDefaultTwoProviders() {
        // case with 2 providers without any config, an exception is expected
        try {
            DynamicSimulation.find(null, ImmutableList.of(new DynamicSimulationProviderMock(), new AnotherDynamicSimulationProviderMock()), platformConfig);
            fail();
        } catch (PowsyblException ignored) {
        }
    }

    @Test
    public void testDefaultNoProvider() {
        // case without any provider
        try {
            DynamicSimulation.find(null, ImmutableList.of(), platformConfig);
            fail();
        } catch (PowsyblException ignored) {
        }
    }

    @Test
    public void testTwoProviders() {
        // case with 2 providers without any config but specifying which one to use programmatically
        DynamicSimulation.Runner otherDynamicSimulation = DynamicSimulation.find("AnotherDynamicSimulationMock", ImmutableList.of(new DynamicSimulationProviderMock(), new AnotherDynamicSimulationProviderMock()), platformConfig);
        assertEquals("AnotherDynamicSimulationMock", otherDynamicSimulation.getName());
    }

    @Test
    public void testDefaultTwoProvidersPlatformConfig() {
        // case with 2 providers without any config but specifying which one to use in platform config
        platformConfig.createModuleConfig("dynamic-simulation").setStringProperty("default", "AnotherDynamicSimulationMock");
        DynamicSimulation.Runner otherDynamicSimulation2 = DynamicSimulation.find(null, ImmutableList.of(new DynamicSimulationProviderMock(), new AnotherDynamicSimulationProviderMock()), platformConfig);
        assertEquals("AnotherDynamicSimulationMock", otherDynamicSimulation2.getName());
    }

    @Test(expected = PowsyblException.class)
    public void testOneProviderAndMistakeInPlatformConfig() {
        // case with 1 provider with config but with a name that is not the one of provider.
        platformConfig.createModuleConfig("dynamic-simulation").setStringProperty("default", "AnotherDynamicSimulationMock");
        DynamicSimulation.find(null, ImmutableList.of(new DynamicSimulationProviderMock()), platformConfig);
    }
}
