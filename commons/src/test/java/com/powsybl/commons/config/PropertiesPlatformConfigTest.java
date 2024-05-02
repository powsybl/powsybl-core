/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class PropertiesPlatformConfigTest extends AbstractMapModuleConfigTest {

    PropertiesPlatformConfigTest() {
    }

    @Test
    void test() throws IOException {
        Path cfgDir = Files.createDirectory(fileSystem.getPath("config"));
        Properties prop1 = new Properties();
        prop1.setProperty("s", "hello");
        prop1.setProperty("i", Integer.toString(3));
        prop1.setProperty("l", Long.toString(33333333333L));
        prop1.setProperty("b", Boolean.FALSE.toString());
        prop1.setProperty("d", Double.toString(2.3));
        prop1.setProperty("c", ArrayList.class.getName());
        prop1.setProperty("sl1", "a,b,c");
        prop1.setProperty("sl2", "a:b:c");
        prop1.setProperty("e", StandardOpenOption.APPEND.name());
        prop1.setProperty("el", StandardOpenOption.APPEND + "," + StandardOpenOption.CREATE);
        prop1.setProperty("dt", "2009-01-03T18:15:05Z");
        prop1.setProperty("it", "2009-01-03T18:15:05Z/2009-01-09T02:54:25Z");
        Path p = fileSystem.getPath("/tmp");
        Path p2 = fileSystem.getPath("/home");
        prop1.setProperty("p", p.toString());
        prop1.setProperty("pl", p.toString() + ":" + p2.toString());
        prop1.setProperty("pl2", p.toString() + "," + p2.toString());
        try (Writer w = Files.newBufferedWriter(cfgDir.resolve("mod.properties"), StandardCharsets.UTF_8)) {
            prop1.store(w, null);
        }
        PlatformConfig propsConfig = new PlatformConfig(new PropertiesModuleConfigRepository(cfgDir), cfgDir);
        Optional<ModuleConfig> modConfig = propsConfig.getOptionalModuleConfig("mod");
        assertTrue(modConfig.isPresent());
        assertModConfig(modConfig.get());

        assertEquals(Sets.newHashSet("p", "b", "c", "s", "d", "dt", "e", "el", "pl2", "sl2", "sl1", "i", "it", "l", "pl"),
                modConfig.get().getPropertyNames());
    }
}
