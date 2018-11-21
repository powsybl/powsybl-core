/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.local;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalComputationConfigTest {

    private FileSystem fileSystem;

    private InMemoryPlatformConfig platformConfig;

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        Files.createDirectories(fileSystem.getPath("/tmp"));
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void test() {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("computation-local");
        moduleConfig.setStringProperty("tmpDir", "/tmp");
        moduleConfig.setStringProperty("availableCore", "2");
        LocalComputationConfig config = LocalComputationConfig.load(platformConfig, fileSystem);
        assertEquals(fileSystem.getPath("/tmp"), config.getLocalDir());
        assertEquals(2, config.getAvailableCore());
    }

    @Test
    public void testDefaultConfig() {
        LocalComputationConfig config = LocalComputationConfig.load(platformConfig, fileSystem);
        assertEquals(fileSystem.getPath(LocalComputationConfig.DEFAULT_LOCAL_DIR), config.getLocalDir());
        assertEquals(1, config.getAvailableCore());
    }

    @Test
    public void testAvailableCoresNegative() {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("computation-local");
        moduleConfig.setStringProperty("availableCore", "-1");
        LocalComputationConfig config = LocalComputationConfig.load(platformConfig, fileSystem);
        assertEquals(Runtime.getRuntime().availableProcessors(), config.getAvailableCore());
    }

    @Test
    public void testSnakeCase() {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("computation-local");
        moduleConfig.setStringProperty("tmp-dir", "/tmp");
        moduleConfig.setStringProperty("available-core", "99");
        LocalComputationConfig config = LocalComputationConfig.load(platformConfig, fileSystem);
        assertEquals(fileSystem.getPath("/tmp"), config.getLocalDir());
        assertEquals(99, config.getAvailableCore());
    }

    @Test
    public void testSnakeOverCamelCase() throws IOException {
        Files.createDirectories(fileSystem.getPath("/deprecated"));
        Files.createDirectories(fileSystem.getPath("/new"));
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("computation-local");
        moduleConfig.setStringProperty("tmpDir", "/deprecated");
        moduleConfig.setStringProperty("availableCore", "1");
        moduleConfig.setStringProperty("tmp-dir", "/new");
        moduleConfig.setStringProperty("available-core", "2");
        LocalComputationConfig config = LocalComputationConfig.load(platformConfig, fileSystem);
        assertEquals(fileSystem.getPath("/new"), config.getLocalDir());
        assertEquals(2, config.getAvailableCore());
    }

    @Test
    public void testTmpDirAlternatives() throws IOException  {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("computation-local");
        moduleConfig.setStringListProperty("tmp-dir", Arrays.asList("/first", "/second"));
        Files.createDirectories(fileSystem.getPath("/second"));
        LocalComputationConfig config = LocalComputationConfig.load(platformConfig, fileSystem);
        // first does not exist second is used as tmp dir
        assertEquals(fileSystem.getPath("/second"), config.getLocalDir());
        Files.createDirectories(fileSystem.getPath("/first"));
        config = LocalComputationConfig.load(platformConfig, fileSystem);
        // now fist exist and is used in priority
        assertEquals(fileSystem.getPath("/first"), config.getLocalDir());
    }
}
