/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local;

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
public class LocalAppFileSystemConfigTest {

    private FileSystem fileSystem;

    private InMemoryPlatformConfig platformConfig;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Files.createDirectories(fileSystem.getPath("/tmp"));
        Files.createFile(fileSystem.getPath("/test"));
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("local-app-file-system");
        moduleConfig.setStringProperty("drive-name", "local");
        moduleConfig.setPathProperty("root-dir", fileSystem.getPath("/work"));
        moduleConfig.setStringProperty("max-additional-drive-count", "2");
        moduleConfig.setStringProperty("drive-name-1", "local1");
        moduleConfig.setStringProperty("remotely-accessible-1", "true");
        moduleConfig.setPathProperty("root-dir-1", fileSystem.getPath("/work"));
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void loadTest() {
        List<LocalAppFileSystemConfig> configs = LocalAppFileSystemConfig.load(platformConfig);
        assertEquals(2, configs.size());
        LocalAppFileSystemConfig config = configs.get(0);
        LocalAppFileSystemConfig config1 = configs.get(1);
        assertEquals("local", config.getDriveName());
        assertFalse(config.isRemotelyAccessible());
        assertEquals(fileSystem.getPath("/work"), config.getRootDir());
        assertEquals("local1", config1.getDriveName());
        assertTrue(config1.isRemotelyAccessible());
        assertEquals(fileSystem.getPath("/work"), config1.getRootDir());
        config.setDriveName("local2");
        config.setRootDir(fileSystem.getPath("/tmp"));
        assertEquals("local2", config.getDriveName());
        assertEquals(fileSystem.getPath("/tmp"), config.getRootDir());
        try {
            config.setRootDir(fileSystem.getPath("/test"));
            fail();
        } catch (Exception ignored) {
        }
    }
}
