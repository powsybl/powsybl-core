/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.Assert.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StackedModuleConfigRepositoryTest {

    private FileSystem fileSystem;

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    private ModuleConfigRepository createRepository1() {
        InMemoryModuleConfigRepository repository1 = new InMemoryModuleConfigRepository(fileSystem);
        MapModuleConfig config1 = repository1.createModuleConfig("config1");
        config1.setStringProperty("key1", "value1");
        config1.setStringProperty("key2", "value2");
        MapModuleConfig config2 = repository1.createModuleConfig("config2");
        config2.setStringProperty("key3", "value3");
        return repository1;
    }

    private ModuleConfigRepository createRepository2() {
        InMemoryModuleConfigRepository repository2 = new InMemoryModuleConfigRepository(fileSystem);
        MapModuleConfig config1 = repository2.createModuleConfig("config1");
        config1.setStringProperty("key1", "newValue1");
        MapModuleConfig config3 = repository2.createModuleConfig("config3");
        config3.setStringProperty("key4", "value4");
        return repository2;
    }

    @Test
    public void test() {
        StackedModuleConfigRepository stackedRepository = new StackedModuleConfigRepository(createRepository2(), createRepository1());
        ModuleConfig config1 = stackedRepository.getModuleConfig("config1").orElse(null);
        assertNotNull(config1);
        assertEquals("newValue1", config1.getStringProperty("key1"));
        assertEquals("value2", config1.getStringProperty("key2"));
    }
}
