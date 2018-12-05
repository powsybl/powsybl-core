/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StackedModuleConfigRepositoryTest extends MapModuleConfigTest {

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
        testEnvConfig();

        StackedModuleConfigRepository stackedRepository = new StackedModuleConfigRepository(createRepository2(), createRepository1());
        ModuleConfig config1 = stackedRepository.getModuleConfig("config1").orElse(null);
        assertNotNull(config1);
        assertEquals("newValue1", config1.getStringProperty("key1"));
        assertEquals("value2", config1.getStringProperty("key2"));
    }

    private void testEnvConfig() {
        Map<String, String> fakeEnvMap = new HashMap<>();
        fakeEnvMap.put("MOD_S", "hello");
        fakeEnvMap.put("MOD_I", "3");
        fakeEnvMap.put("MOD_L", "33333333333");
        fakeEnvMap.put("MOD_B", "false");
        fakeEnvMap.put("MOD_D", "2.3");
        fakeEnvMap.put("MOD_C", ArrayList.class.getName());
        fakeEnvMap.put("MOD_SL1", "a,b,c");
        fakeEnvMap.put("MOD_SL2", "a:b:c");
        fakeEnvMap.put("MOD_E", StandardOpenOption.APPEND.name());
        fakeEnvMap.put("MOD_EL", StandardOpenOption.APPEND + "," + StandardOpenOption.CREATE);
        Path p = fileSystem.getPath("/tmp");
        Path p2 = fileSystem.getPath("/home");
        fakeEnvMap.put("MOD_P", p.toString());
        fakeEnvMap.put("MOD_PL", p.toString() + ":" + p2.toString());
        fakeEnvMap.put("MOD_PL2", p.toString() + "," + p2.toString());
        fakeEnvMap.put("MOD_DT", "2009-01-03T18:15:05Z");
        fakeEnvMap.put("MOD_IT", "2009-01-03T18:15:05Z/2009-01-09T02:54:25Z");

        fakeEnvMap.put("LOAD_FLOW_ACTION_SIMULATOR_MAX_ITERATIONS", "7");

        EnvironmentModuleConfigRepository sut = EnvironmentModuleConfigRepository.getInstanceForTest(fakeEnvMap, fileSystem);
        assertTrue(sut.moduleExists("mod"));
        Optional<ModuleConfig> modConfigOpt = sut.getModuleConfig("mod");
        assertTrue(modConfigOpt.isPresent());
        ModuleConfig modConfig = modConfigOpt.get();

        assertModConfig(modConfig);

        String modName = "load-flow-action-simulator";
        Optional<ModuleConfig> moduleConfigOpt = sut.getModuleConfig(modName);
        assertTrue(moduleConfigOpt.isPresent());
        ModuleConfig moduleConfig = moduleConfigOpt.get();
        assertEquals(7, moduleConfig.getIntProperty("max-iterations"));

        try {
            modConfig.getPropertyNames();
            fail();
        } catch (Exception e) {
            // ignore
        }
    }
}
