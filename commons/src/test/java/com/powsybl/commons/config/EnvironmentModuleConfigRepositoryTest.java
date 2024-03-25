/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.powsybl.commons.config.EnvironmentModuleConfigRepository.UPPER_UNDERSCORE_FORMATTER;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
class EnvironmentModuleConfigRepositoryTest extends MapModuleConfigTest {

    @Test
    void test() {
        Map<String, String> fakeEnvMap = new HashMap<>();
        fakeEnvMap.put("MOD__S", "hello");
        fakeEnvMap.put("MOD__I", "3");
        fakeEnvMap.put("MOD__L", "33333333333");
        fakeEnvMap.put("MOD__B", "false");
        fakeEnvMap.put("MOD__D", "2.3");
        fakeEnvMap.put("MOD__C", ArrayList.class.getName());
        fakeEnvMap.put("MOD__SL1", "a,b,c");
        fakeEnvMap.put("MOD__SL2", "a:b:c");
        fakeEnvMap.put("MOD__E", StandardOpenOption.APPEND.name());
        fakeEnvMap.put("MOD__EL", StandardOpenOption.APPEND + "," + StandardOpenOption.CREATE);
        Path p = fileSystem.getPath("/tmp");
        Path p2 = fileSystem.getPath("/home");
        fakeEnvMap.put("MOD__P", p.toString());
        fakeEnvMap.put("MOD__PL", p.toString() + ":" + p2.toString());
        fakeEnvMap.put("MOD__PL2", p.toString() + "," + p2.toString());
        fakeEnvMap.put("MOD__DT", "2009-01-03T18:15:05Z");
        fakeEnvMap.put("MOD__IT", "2009-01-03T18:15:05Z/2009-01-09T02:54:25Z");
        fakeEnvMap.put("AB__CD__EF", "fio");

        fakeEnvMap.put("LOAD_FLOW_ACTION_SIMULATOR__MAX_ITERATIONS", "7");

        String lowerCamel = "lowerCamel";
        fakeEnvMap.put("LOWER_CAMEL__LOWER_CAMEL", "asfd");

        String snakeCaseMod = "a_snake_mod";
        fakeEnvMap.put("A_SNAKE_MOD__SNAKE_LENGTH", "3");

        String upperCamel = "UpperCamel";
        fakeEnvMap.put("UPPER_CAMEL__UPPER_CAMEL", "UpperCamel");

        EnvironmentModuleConfigRepository sut = new EnvironmentModuleConfigRepository(fakeEnvMap, fileSystem);
        Optional<ModuleConfig> modConfigOpt = sut.getModuleConfig("mod");
        assertTrue(modConfigOpt.isPresent());
        ModuleConfig modConfig = modConfigOpt.get();

        assertModConfig(modConfig);

        String modName = "load-flow-action-simulator";
        Optional<ModuleConfig> moduleConfigOpt = sut.getModuleConfig(modName);
        assertTrue(moduleConfigOpt.isPresent());
        ModuleConfig moduleConfig = moduleConfigOpt.get();
        assertEquals(7, moduleConfig.getIntProperty("max-iterations"));

        Optional<ModuleConfig> moduleConfig1 = sut.getModuleConfig(lowerCamel);
        assertTrue(moduleConfig1.isPresent());
        assertEquals("asfd", moduleConfig1.get().getStringProperty(lowerCamel));

        Optional<ModuleConfig> moduleConfig2 = sut.getModuleConfig(snakeCaseMod);
        assertTrue(moduleConfig2.isPresent());
        assertEquals(3, moduleConfig2.get().getIntProperty("snake_length"));

        Optional<ModuleConfig> moduleConfig3 = sut.getModuleConfig(upperCamel);
        assertTrue(moduleConfig3.isPresent());
        assertEquals("UpperCamel", moduleConfig3.get().getStringProperty("UpperCamel"));

        Optional<ModuleConfig> moduleConfig4 = sut.getModuleConfig("ab__cd");
        assertTrue(moduleConfig4.isPresent());
        assertEquals("fio", moduleConfig4.get().getStringProperty("ef"));
        assertTrue(sut.getModuleConfig("ab").isPresent());

        assertFalse(sut.getModuleConfig("").isPresent());
        try {
            modConfig.getPropertyNames();
            fail();
        } catch (UnsupportedOperationException e) {
            // ignore
        }
    }

    @Test
    void testUpperUndersocreFormatter() {
        assertEquals("NAME", UPPER_UNDERSCORE_FORMATTER.apply("name"));
        assertEquals("LOWER_HYPHEN", UPPER_UNDERSCORE_FORMATTER.apply("lower-hyphen"));
        assertEquals("LOWER_CAMEL", UPPER_UNDERSCORE_FORMATTER.apply("lowerCamel"));
        assertEquals("UPPER_CAMEL", UPPER_UNDERSCORE_FORMATTER.apply("UpperCamel"));
        assertEquals("SNAKE_CASE", UPPER_UNDERSCORE_FORMATTER.apply("snake_case"));
    }
}
