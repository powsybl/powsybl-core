/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.google.common.collect.Sets;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.Test;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static org.junit.Assert.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PropertiesPlatformConfigTest {

    public PropertiesPlatformConfigTest() {
    }

    @Test
    public void test() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path cfgDir = Files.createDirectory(fileSystem.getPath("config"));
            Properties prop1 = new Properties();
            prop1.setProperty("s", "hello");
            prop1.setProperty("i", Integer.toString(3));
            prop1.setProperty("b", Boolean.FALSE.toString());
            prop1.setProperty("d", Double.toString(2.3));
            prop1.setProperty("c", ArrayList.class.getName());
            prop1.setProperty("sl1", "a,b,c");
            prop1.setProperty("sl2", "a:b:c");
            prop1.setProperty("e", StandardOpenOption.APPEND.name());
            prop1.setProperty("el", StandardOpenOption.APPEND + "," + StandardOpenOption.CREATE);
            Path p = fileSystem.getPath("/tmp");
            Path p2 = fileSystem.getPath("/home");
            prop1.setProperty("p", p.toString());
            prop1.setProperty("pl", p.toString() + ":" + p2.toString());
            prop1.setProperty("pl2", p.toString() + "," + p2.toString());
            try (Writer w = Files.newBufferedWriter(cfgDir.resolve("mod.properties"), StandardCharsets.UTF_8)) {
                prop1.store(w, null);
            }
            PropertiesPlatformConfig propsConfig = new PropertiesPlatformConfig(cfgDir, fileSystem);
            ModuleConfig modConfig = propsConfig.getModuleConfig("mod");
            assertEquals("hello", modConfig.getStringProperty("s"));
            assertEquals("oups", modConfig.getStringProperty("s2", "oups"));
            try {
                modConfig.getStringProperty("s2");
                fail();
            } catch (Exception ignored) {
            }
            assertEquals(3, modConfig.getIntProperty("i"));
            try {
                modConfig.getIntProperty("i2");
                fail();
            } catch (Exception ignored) {
            }
            assertNull(modConfig.getOptionalIntProperty("i2"));
            assertFalse(modConfig.getOptionalIntegerProperty("i2").isPresent());
            assertEquals(4, modConfig.getIntProperty("i2", 4));
            assertFalse(modConfig.getBooleanProperty("b"));
            try {
                modConfig.getBooleanProperty("b2");
                fail();
            } catch (Exception ignored) {
            }
            assertNull(modConfig.getOptinalBooleanProperty("b2"));
            assertFalse(modConfig.getOptionalBooleanProperty("b2").isPresent());
            assertTrue(modConfig.getBooleanProperty("b2", true));
            assertFalse(modConfig.getOptionalFloatProperty("f").isPresent());
            assertEquals(1.5f, modConfig.getFloatProperty("f", 1.5f), 0d);
            assertEquals(2.3f, modConfig.getFloatProperty("d"), 0d);
            assertEquals(2.3d, modConfig.getDoubleProperty("d"), 0d);
            try {
                modConfig.getDoubleProperty("d2");
                fail();
            } catch (Exception ignored) {
            }
            assertEquals(4.5d, modConfig.getDoubleProperty("d2", 4.5d), 0d);
            assertEquals(ArrayList.class, modConfig.getClassProperty("c", List.class));
            assertEquals(Arrays.asList("a", "b", "c"), modConfig.getStringListProperty("sl1"));
            assertEquals(Arrays.asList("a", "b", "c"), modConfig.getStringListProperty("sl2"));
            try {
                modConfig.getStringListProperty("sl3");
                fail();
            } catch (Exception ignored) {
            }
            assertEquals(StandardOpenOption.APPEND, modConfig.getEnumProperty("e", StandardOpenOption.class));
            try {
                modConfig.getEnumProperty("e2", StandardOpenOption.class);
                fail();
            } catch (Exception ignored) {
            }
            assertEquals(EnumSet.of(StandardOpenOption.APPEND, StandardOpenOption.CREATE), modConfig.getEnumSetProperty("el", StandardOpenOption.class));
            try {
                modConfig.getEnumSetProperty("el2", StandardOpenOption.class);
                fail();
            } catch (Exception ignored) {
            }
            assertEquals(p, modConfig.getPathProperty("p"));
            try {
                modConfig.getPathProperty("p2");
                fail();
            } catch (Exception ignored) {
            }
            assertEquals(Arrays.asList(p, p2), modConfig.getPathListProperty("pl"));
            assertEquals(Arrays.asList(p, p2), modConfig.getPathListProperty("pl2"));
            try {
                modConfig.getPathListProperty("pl3");
                fail();
            } catch (Exception ignored) {
            }
            assertEquals(Sets.newHashSet("p", "b", "c", "s", "d", "e", "el", "pl2", "sl2", "sl1", "i", "pl"), modConfig.getPropertyNames());
            assertTrue(modConfig.hasProperty("p"));
        }
    }
}
