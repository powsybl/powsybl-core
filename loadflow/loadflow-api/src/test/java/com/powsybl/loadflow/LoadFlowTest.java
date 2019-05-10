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
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.iidm.network.Network;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import sun.nio.ch.Net;

import java.nio.file.FileSystem;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadFlowTest {

    private FileSystem fileSystem;

    private InMemoryPlatformConfig platformConfig;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void test() {
        List<LoadFlowProvider> providers = ImmutableList.of(new FakeLoadFlowProvider());
        Network network = Mockito.mock(Network.class);
        LoadFlowResult result = LoadFlow.on(network, providers, platformConfig).run();
    }
}