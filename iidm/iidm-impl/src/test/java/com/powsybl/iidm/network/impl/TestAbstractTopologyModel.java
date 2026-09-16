/**
 * Copyright (c) 2022, Lucas Leblow
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Lucas Leblow {@literal <lucasleblow@mailbox.org>}
 */
class TestAbstractTopologyModel {

    @Test
    void testLoadNodeIndexLimit() throws IOException {
        assertEquals(1000, AbstractTopologyModel.NODE_INDEX_LIMIT);
        assertEquals(1000, AbstractTopologyModel.loadNodeIndexLimit(PlatformConfig.defaultConfig()));

        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {

            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
            MapModuleConfig moduleConfig = platformConfig.createModuleConfig("iidm");
            moduleConfig.setStringProperty("node-index-limit", "5");

            assertEquals(5, AbstractTopologyModel.loadNodeIndexLimit(platformConfig));
        }
    }

    @Test
    void updateBusIdThrowsWhenBusDoesNotExist() {
        Network network = EurostagTutorialExample1Factory.create();
        TopologyModel topologyModel = ((VoltageLevelExt) network.getVoltageLevel("VLGEN")).getTopologyModel();

        PowsyblException e = assertThrows(PowsyblException.class,
                () -> topologyModel.updateBusId("UNKNOWN_BUS", "NEW_ID"));
        assertTrue(e.getMessage().contains("UNKNOWN_BUS"));
    }

    @Test
    void updateSwitchIdThrowsWhenSwitchDoesNotExist() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        TopologyModel topologyModel = ((VoltageLevelExt) network.getVoltageLevel("S1VL1")).getTopologyModel();

        PowsyblException e = assertThrows(PowsyblException.class,
                () -> topologyModel.updateSwitchId("UNKNOWN_SWITCH", "NEW_ID"));
        assertTrue(e.getMessage().contains("UNKNOWN_SWITCH"));
    }
}
