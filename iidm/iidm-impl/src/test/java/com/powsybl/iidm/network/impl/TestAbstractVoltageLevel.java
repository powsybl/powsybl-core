/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.Assert.*;

/**
 * @author Lucas Leblow <lucasleblow@mailbox.org>
 */
public class TestAbstractVoltageLevel {

    @Test
    public void testLoadNodeIndexLimit() throws IOException {
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
