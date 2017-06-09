/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.action.util;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import eu.itesla_project.commons.config.InMemoryPlatformConfig;
import eu.itesla_project.commons.config.MapModuleConfig;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.loadflow.api.mock.LoadFlowFactoryMock;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class LoadFlowBasesPhaseShifterOptimizerConfigTest {

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
            MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow-based-phase-shifter-optimizer");
            moduleConfig.setClassProperty("load-flow-factory", LoadFlowFactoryMock.class);

            LoadFlowBasedPhaseShifterOptimizerConfig config = LoadFlowBasedPhaseShifterOptimizerConfig.load(platformConfig);
            assertEquals(LoadFlowFactoryMock.class, config.getLoadFlowFactoryClass());
            config.setLoadFlowFactoryClass(AnotherLoadFlowFactoryMock.class);
            assertEquals(AnotherLoadFlowFactoryMock.class, config.getLoadFlowFactoryClass());
        }
    }

}
