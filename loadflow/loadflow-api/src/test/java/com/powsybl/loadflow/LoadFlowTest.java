/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.file.FileSystem;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadFlowTest {

    private FileSystem fileSystem;

    private InMemoryPlatformConfig platformConfig;

    private Network network;

    private VariantManager variantManager;

    private ComputationManager computationManager;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        network = Mockito.mock(Network.class);
        variantManager = Mockito.mock(VariantManager.class);
        Mockito.when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.when(variantManager.getWorkingVariantId()).thenReturn("v");
        computationManager = Mockito.mock(ComputationManager.class);
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void testDefaultOneProvider() {
        // case with only one provider, no need for config
        LoadFlow.Runner defaultLoadFlow = LoadFlow.find(null, ImmutableList.of(new LoadFlowProviderMock()), platformConfig);
        assertEquals("LoadFlowMock", defaultLoadFlow.getName());
        LoadFlowResult result = defaultLoadFlow.run(network, computationManager, new LoadFlowParameters());
        assertNotNull(result);
    }

    @Test
    public void testDefaultTwoProviders() {
        // case with 2 providers without any config, an exception is expected
        try {
            LoadFlow.find(null, ImmutableList.of(new LoadFlowProviderMock(), new AnotherLoadFlowProviderMock()), platformConfig);
            fail();
        } catch (PowsyblException ignored) {
        }
    }

    @Test
    public void testDefaultNoProvider() {
        // case without any provider
        try {
            LoadFlow.find(null, ImmutableList.of(), platformConfig);
            fail();
        } catch (PowsyblException ignored) {
        }
    }

    @Test
    public void testTwoProviders() {
        // case with 2 providers without any config but specifying which one to use programmatically
        LoadFlow.Runner otherLoadFlow = LoadFlow.find("AnotherLoadFlowMock", ImmutableList.of(new LoadFlowProviderMock(), new AnotherLoadFlowProviderMock()), platformConfig);
        assertEquals("AnotherLoadFlowMock", otherLoadFlow.getName());
    }

    @Test
    public void testDefaultTwoProvidersPlatformConfig() {
        // case with 2 providers without any config but specifying which one to use in platform config
        platformConfig.createModuleConfig("load-flow").setStringProperty("default", "AnotherLoadFlowMock");
        LoadFlow.Runner otherLoadFlow2 = LoadFlow.find(null, ImmutableList.of(new LoadFlowProviderMock(), new AnotherLoadFlowProviderMock()), platformConfig);
        assertEquals("AnotherLoadFlowMock", otherLoadFlow2.getName());
    }

    @Test(expected = PowsyblException.class)
    public void testOneProviderAndMistakeInPlatformConfig() {
        // case with 1 provider with config but with a name that is not the one of provider.
        platformConfig.createModuleConfig("load-flow").setStringProperty("default", "AnotherLoadFlowMock");
        LoadFlow.find(null, ImmutableList.of(new LoadFlowProviderMock()), platformConfig);
    }
}
