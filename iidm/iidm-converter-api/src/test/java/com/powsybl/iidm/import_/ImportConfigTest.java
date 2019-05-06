/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ImportConfigTest {

    private FileSystem fileSystem;
    private InMemoryPlatformConfig platformConfig;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem();
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void createEmpty() {
        ImportConfig importConfig = new ImportConfig();
        assertTrue(importConfig.getPostProcessors().isEmpty());
    }

    private static void checkImportConfig(ImportConfig importConfig, String... postProcessors) {
        assertFalse(importConfig.getPostProcessors().isEmpty());
        assertEquals(postProcessors.length, importConfig.getPostProcessors().size());
        for (String postProc : postProcessors) {
            assertTrue(importConfig.getPostProcessors().contains(postProc));
        }
    }

    @Test
    public void createNotEmpty() {
        checkImportConfig(new ImportConfig(Arrays.asList("foo", "bar")), "foo", "bar");
        checkImportConfig(new ImportConfig("foo", "bar"), "foo", "bar");
    }

    @Test
    public void load() {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("import");
        moduleConfig.setStringListProperty("postProcessors", Arrays.asList("foo", "bar"));
        checkImportConfig(ImportConfig.load(platformConfig), "foo", "bar");
    }

    @Test
    public void setPostProcessors() {
        checkImportConfig(new ImportConfig().setPostProcessors(Arrays.asList("foo", "bar")), "foo", "bar");
        checkImportConfig(new ImportConfig().setPostProcessors("foo", "bar"), "foo", "bar");
    }

    @Test
    public void addPostProcessor() {
        ImportConfig importConfig = new ImportConfig("foo");
        checkImportConfig(importConfig.addPostProcessor("bar"), "foo", "bar");
    }

    @Test
    public void removePostProcessor() {
        ImportConfig importConfig = new ImportConfig("foo", "bar");
        checkImportConfig(importConfig.removePostProcessor("bar"), "foo");
    }
}
