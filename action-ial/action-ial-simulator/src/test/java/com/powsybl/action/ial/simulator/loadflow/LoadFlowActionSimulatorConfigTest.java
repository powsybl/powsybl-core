/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.simulator.loadflow;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
class LoadFlowActionSimulatorConfigTest {

    @Test
    void test() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
            MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow-action-simulator");
            moduleConfig.setStringProperty("load-flow-name", "LoadFlowMock");
            moduleConfig.setStringProperty("max-iterations", "15");
            moduleConfig.setStringProperty("ignore-pre-contingency-violations", "true");
            moduleConfig.setStringProperty("copy-strategy", CopyStrategy.DEEP.name());

            LoadFlowActionSimulatorConfig config = LoadFlowActionSimulatorConfig.load(platformConfig);

            assertEquals("LoadFlowMock", config.getLoadFlowName().orElseThrow(IllegalStateException::new));
            config.setLoadFlowName("AnotherLoadFlowMock");
            assertEquals("AnotherLoadFlowMock", config.getLoadFlowName().orElseThrow(IllegalStateException::new));

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
        }
    }
}
