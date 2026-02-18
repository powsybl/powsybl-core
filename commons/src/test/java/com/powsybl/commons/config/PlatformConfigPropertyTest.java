/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.*;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
class PlatformConfigPropertyTest {

    private static final String MODULE_NAME = "module";
    private static final String WORK_FOLDER = "/work/";

    private static class TestClass {
    }

    private static final class DefaultTestSubClass extends TestClass {
    }

    private static final class TestSubClass extends TestClass {
    }

    private enum TestEnum {
        FIRST,
        SECOND
    }

    private final ZonedDateTime defaultZonedDateTime = ZonedDateTime.parse("2026-01-01T00:00Z");

    private FileSystem fileSystem;
    private Path defaultPath;
    private InMemoryPlatformConfig platformConfig;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        defaultPath = fileSystem.getPath(WORK_FOLDER);
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    void testConfiguredValues() {
        // Given
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig(MODULE_NAME);
        moduleConfig.setStringProperty("stringProperty", "value");
        moduleConfig.setStringProperty("booleanProperty", "true");
        moduleConfig.setStringProperty("intProperty", "22");
        moduleConfig.setStringProperty("enumProperty", "FIRST");
        moduleConfig.setStringListProperty("stringsProperty", List.of("value1"));
        moduleConfig.setStringProperty("floatProperty", "1");
        moduleConfig.setStringProperty("doubleProperty", "1.00");
        moduleConfig.setStringProperty("longProperty", "1");
        moduleConfig.setStringProperty("enumValuesProperty", "FIRST");
        moduleConfig.setPathProperty("pathProperty", fileSystem.getPath("path1"));
        moduleConfig.setPathsProperty("pathsProperty", List.of(fileSystem.getPath("path1"), fileSystem.getPath("path2")));
        moduleConfig.setClassProperty("classProperty", TestSubClass.class);
        moduleConfig.setStringProperty("zonedDateTimeProperty", ZonedDateTime.parse("2026-01-02T00:00Z").toString());
        // When
        String stringValue = platformConfig.getStringProperty(MODULE_NAME, "stringProperty", "default-string");
        boolean booleanValue = platformConfig.getBooleanProperty(MODULE_NAME, "booleanProperty", false);
        int intValue = platformConfig.getIntProperty(MODULE_NAME, "intProperty", 23);
        TestEnum enumValue = platformConfig.getEnumProperty(MODULE_NAME, "enumProperty", TestEnum.SECOND, TestEnum.class);
        List<String> stringValues = platformConfig.getStringListProperty(MODULE_NAME, "stringsProperty", List.of("value1", "value2"));
        float floatValue = platformConfig.getFloatProperty(MODULE_NAME, "floatProperty", 2);
        double doubleValue = platformConfig.getDoubleProperty(MODULE_NAME, "doubleProperty", 2.00);
        long longValue = platformConfig.getLongProperty(MODULE_NAME, "longProperty", 2);
        Set<TestEnum> enumValues = platformConfig.getEnumProperty(MODULE_NAME, "enumValuesProperty", Set.of(TestEnum.SECOND), TestEnum.class);
        Path pathValue = platformConfig.getPathProperty(MODULE_NAME, "pathProperty", defaultPath);
        List<Path> pathsValue = platformConfig.getPathListProperty(MODULE_NAME, "pathsProperty", List.of(defaultPath));
        Class<? extends TestClass> classValue = platformConfig.getClassProperty(MODULE_NAME, "classProperty", DefaultTestSubClass.class, TestClass.class);
        ZonedDateTime zonedDateTimeValue = platformConfig.getZonedDateTimeProperty(MODULE_NAME, "zonedDateTimeProperty", defaultZonedDateTime);
        // Then
        assertEquals("value", stringValue);
        assertTrue(booleanValue);
        assertEquals(22, intValue);
        assertEquals(TestEnum.FIRST, enumValue);
        assertEquals(1, floatValue, 0.001);
        assertEquals(1.00, doubleValue, 0.001);
        assertEquals(1, longValue);
        assertEquals(List.of("value1"), stringValues);
        assertEquals(Set.of(TestEnum.FIRST), enumValues);
        assertEquals(fileSystem.getPath(WORK_FOLDER + "/path1"), pathValue);
        assertEquals(List.of(fileSystem.getPath(WORK_FOLDER + "path1"), fileSystem.getPath(WORK_FOLDER + "path2")), pathsValue);
        assertEquals(TestSubClass.class, classValue);
        assertEquals(ZonedDateTime.parse("2026-01-02T00:00Z"), zonedDateTimeValue);
    }

    @ParameterizedTest(name = "with module creation : {0}")
    @ValueSource(booleans = {false, true})
    void testDefaultValuesWhenModuleOrPropertyMissing(boolean moduleToCreate) {
        // Given
        if (moduleToCreate) {
            platformConfig.createModuleConfig(MODULE_NAME);
        }
        // When
        String stringValue = platformConfig.getStringProperty(MODULE_NAME, "stringProperty", "default-string");
        List<String> stringValues = platformConfig.getStringListProperty(MODULE_NAME, "stringsProperty", List.of("value1", "value2"));
        float floatValue = platformConfig.getFloatProperty(MODULE_NAME, "floatProperty", 2);
        double doubleValue = platformConfig.getDoubleProperty(MODULE_NAME, "doubleProperty", 2.00);
        long longValue = platformConfig.getLongProperty(MODULE_NAME, "longProperty", 2);
        boolean booleanValue = platformConfig.getBooleanProperty(MODULE_NAME, "booleanProperty", false);
        int intValue = platformConfig.getIntProperty(MODULE_NAME, "intProperty", 23);
        TestEnum enumValue = platformConfig.getEnumProperty(MODULE_NAME, "enumProperty", TestEnum.SECOND, TestEnum.class);
        Set<TestEnum> enumValues = platformConfig.getEnumProperty(MODULE_NAME, "enumValuesProperty", Set.of(TestEnum.SECOND), TestEnum.class);
        Path pathValue = platformConfig.getPathProperty(MODULE_NAME, "pathProperty", defaultPath);
        List<Path> pathsValue = platformConfig.getPathListProperty(MODULE_NAME, "pathsProperty", List.of(defaultPath));
        Class<? extends TestClass> classValue = platformConfig.getClassProperty(MODULE_NAME, "classProperty", DefaultTestSubClass.class, TestClass.class);
        ZonedDateTime zonedDateTimeValue = platformConfig.getZonedDateTimeProperty(MODULE_NAME, "zonedDateTimeProperty", defaultZonedDateTime);
        // Then
        assertEquals("default-string", stringValue);
        assertFalse(booleanValue);
        assertEquals(23, intValue);
        assertEquals(TestEnum.SECOND, enumValue);
        assertEquals(2, floatValue, 0.001);
        assertEquals(2.00, doubleValue, 0.001);
        assertEquals(2, longValue);
        assertEquals(Set.of(TestEnum.SECOND), enumValues);
        assertEquals(List.of("value1", "value2"), stringValues);
        assertEquals(defaultPath, pathValue);
        assertEquals(List.of(defaultPath), pathsValue);
        assertEquals(DefaultTestSubClass.class, classValue);
        assertEquals(defaultZonedDateTime, zonedDateTimeValue);
    }
}
