/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MapDbAppFileSystemConfigTest {

    private FileSystem fileSystem;

    private InMemoryPlatformConfig platformConfig;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Files.createFile(fileSystem.getPath("/db"));
        Files.createFile(fileSystem.getPath("/db0"));
        Files.createFile(fileSystem.getPath("/db2"));
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("mapdb-app-file-system");
        moduleConfig.setStringProperty("drive-name", "db");
        moduleConfig.setStringProperty("remotely-accessible", "true");
        moduleConfig.setPathProperty("db-file", fileSystem.getPath("/db"));
        moduleConfig.setStringProperty("max-additional-drive-count", "1");
        moduleConfig.setStringProperty("drive-name-0", "db0");
        moduleConfig.setPathProperty("db-file-0", fileSystem.getPath("/db0"));
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void loadTest() {
        List<MapDbAppFileSystemConfig> configs = MapDbAppFileSystemConfig.load(platformConfig);
        assertEquals(2, configs.size());
        MapDbAppFileSystemConfig config = configs.get(0);
        MapDbAppFileSystemConfig config1 = configs.get(1);
        assertEquals("db", config.getDriveName());
        assertTrue(config.isRemotelyAccessible());
        assertEquals(fileSystem.getPath("/db"), config.getDbFile());
        assertEquals("db0", config1.getDriveName());
        assertFalse(config1.isRemotelyAccessible());
        assertEquals(fileSystem.getPath("/db0"), config1.getDbFile());
        config.setDriveName("db2");
        config.setDbFile(fileSystem.getPath("/db2"));
        assertEquals("db2", config.getDriveName());
        assertEquals(fileSystem.getPath("/db2"), config.getDbFile());
        try {
            config.setDbFile(fileSystem.getPath("/"));
            fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void loadEmptyTest() {
        List<MapDbAppFileSystemConfig> configs = MapDbAppFileSystemConfig.load(new InMemoryPlatformConfig(fileSystem));
        assertNotNull(configs);
        assertTrue(configs.isEmpty());
    }
}
