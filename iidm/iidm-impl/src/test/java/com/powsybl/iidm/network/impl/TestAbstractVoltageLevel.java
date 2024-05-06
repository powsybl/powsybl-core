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
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Lucas Leblow {@literal <lucasleblow@mailbox.org>}
 */
class TestAbstractVoltageLevel {

    @Test
    void testLoadNodeIndexLimit() throws IOException {
        assertEquals(1000, AbstractVoltageLevel.NODE_INDEX_LIMIT);
        assertEquals(1000, AbstractVoltageLevel.loadNodeIndexLimit(PlatformConfig.defaultConfig()));

        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {

            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
            MapModuleConfig moduleConfig = platformConfig.createModuleConfig("iidm");
            moduleConfig.setStringProperty("node-index-limit", "5");

            assertEquals(5, AbstractVoltageLevel.loadNodeIndexLimit(platformConfig));
        }
    }
}
