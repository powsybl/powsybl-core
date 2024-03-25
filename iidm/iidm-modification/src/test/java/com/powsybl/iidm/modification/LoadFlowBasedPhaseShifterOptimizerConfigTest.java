/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class LoadFlowBasedPhaseShifterOptimizerConfigTest {

    @Test
    void test() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
            MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow-based-phase-shifter-optimizer");
            moduleConfig.setStringProperty("load-flow-name", "LoadFlowMock");

            LoadFlowBasedPhaseShifterOptimizerConfig config = LoadFlowBasedPhaseShifterOptimizerConfig.load(platformConfig);
            assertEquals("LoadFlowMock", config.getLoadFlowName().orElseThrow(IllegalStateException::new));
            config.setLoadFlowName("LoadFlowMock2");
            assertEquals("LoadFlowMock2", config.getLoadFlowName().orElseThrow(IllegalStateException::new));
        }
    }

}
