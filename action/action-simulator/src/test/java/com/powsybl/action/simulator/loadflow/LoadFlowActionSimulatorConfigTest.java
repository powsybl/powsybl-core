/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowFactory;
import com.powsybl.loadflow.mock.LoadFlowFactoryMock;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static com.powsybl.commons.config.ConfigVersion.DEFAULT_CONFIG_VERSION;
import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class LoadFlowActionSimulatorConfigTest {

    private static class AnotherLoadFlowFactoryMock implements LoadFlowFactory {

        @Override
        public LoadFlow create(Network network, ComputationManager computationManager, int priority) {
            return null;
        }
    }

    @Test
    public void test() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
            MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow-action-simulator");
            moduleConfig.setClassProperty("load-flow-factory", LoadFlowFactoryMock.class);
            moduleConfig.setStringProperty("max-iterations", "15");
            moduleConfig.setStringProperty("ignore-pre-contingency-violations", "true");
            moduleConfig.setStringProperty("copy-strategy", CopyStrategy.DEEP.name());

            LoadFlowActionSimulatorConfig config = LoadFlowActionSimulatorConfig.load(platformConfig);

            assertEquals(DEFAULT_CONFIG_VERSION, config.getVersion());

            assertEquals(LoadFlowFactoryMock.class, config.getLoadFlowFactoryClass());
            config.setLoadFlowFactoryClass(AnotherLoadFlowFactoryMock.class);
            assertEquals(AnotherLoadFlowFactoryMock.class, config.getLoadFlowFactoryClass());

            assertEquals(15, config.getMaxIterations());
            config.setMaxIterations(10);
            assertEquals(10, config.getMaxIterations());

            assertTrue(config.isIgnorePreContingencyViolations());
            config.setIgnorePreContingencyViolations(false);
            assertFalse(config.isIgnorePreContingencyViolations());
            assertEquals(CopyStrategy.DEEP, config.getCopyStrategy());
            config.setCopyStrategy(CopyStrategy.STATE);
            assertEquals(CopyStrategy.STATE, config.getCopyStrategy());
            assertFalse(config.isDebug());
            config.setDebug(true);
            assertTrue(config.isDebug());

            moduleConfig.setStringProperty("version", "1.1");
            config = LoadFlowActionSimulatorConfig.load(platformConfig);
            assertEquals("1.1", config.getVersion());
        }
    }
}
