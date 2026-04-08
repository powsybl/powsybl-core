/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class YamlPlatformConfigTest {

    enum E {
        E1,
        E2,
        E3
    }

    @Test
    void test() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path cfgDir = Files.createDirectory(fileSystem.getPath("config"));
            try (Writer writer = Files.newBufferedWriter(cfgDir.resolve("config.yml"), StandardCharsets.UTF_8)) {
                writer.write(String.join(System.lineSeparator(),
                        "module1:",
                        "    s: a",
                        "    sl:",
                        "        - a",
                        "        - b",
                        "        - c",
                        "    el:",
                        "        - E1",
                        "        - E2",
                        "        - E3",
                        "    i: 3",
                        "    l: 444444444",
                        "    f: 3.3",
                        "    b: true",
                        "    p: /work/a",
                        "    pl:",
                        "        - /work/a",
                        "        - /work/b",
                        "    cl: java.lang.String",
                        "    dt: 2015-01-01T00:00:00Z",
                        "    it: 2015-01-01T00:00:00Z/2015-01-01T01:45:00Z"));
            }

            PlatformConfig config = new PlatformConfig(new YamlModuleConfigRepository(cfgDir.resolve("config.yml")), cfgDir);
            assertFalse(config.getOptionalModuleConfig("module2").isPresent());
            assertFalse(config.getOptionalModuleConfig("module2").isPresent());
            Optional<ModuleConfig> optModule1 = config.getOptionalModuleConfig("module1");
            assertTrue(optModule1.isPresent());
            ModuleConfig module1 = optModule1.get();
            assertEquals("a", module1.getStringProperty("s"));
            assertEquals(Collections.singletonList("a"), module1.getStringListProperty("s"));
            assertEquals(Arrays.asList("a", "b", "c"), module1.getStringListProperty("sl"));
            assertEquals(EnumSet.of(E.E1, E.E2, E.E3), module1.getEnumSetProperty("el", E.class));
            assertEquals(3, module1.getIntProperty("i"));
            assertEquals(444444444L, module1.getLongProperty("l"));
            assertEquals(3.3f, module1.getFloatProperty("f"), 0f);
            assertEquals(3.3, module1.getDoubleProperty("f"), 0);
            assertTrue(module1.getBooleanProperty("b"));
            assertEquals(fileSystem.getPath("/work/a"), module1.getPathProperty("p"));
            assertEquals(Arrays.asList(fileSystem.getPath("/work/a"), fileSystem.getPath("/work/b")), module1.getPathListProperty("pl"));
            assertEquals(String.class, module1.getClassProperty("cl", String.class));
            assertEquals(ZonedDateTime.parse("2015-01-01T00:00:00Z"), module1.getDateTimeProperty("dt"));
        }
    }
}
