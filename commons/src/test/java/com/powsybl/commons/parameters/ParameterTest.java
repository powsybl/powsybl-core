/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.parameters;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ParameterTest {

    private FileSystem fileSystem;

    private InMemoryPlatformConfig config;

    @BeforeEach
    void setup() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        config = new InMemoryPlatformConfig(fileSystem);
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void testConversionParameters() {
        Properties properties = new Properties();
        properties.put("test-param-boolean", "true");
        Parameter paramBoolean = new Parameter("test-param-boolean", ParameterType.BOOLEAN, "", Boolean.FALSE);
        Parameter paramBoolean2 = new Parameter("test-param-boolean2", ParameterType.BOOLEAN, "", Boolean.FALSE);
        assertTrue(Parameter.readBoolean("TEST", properties, paramBoolean));
        assertTrue(Parameter.readBoolean("TEST", properties, paramBoolean, ParameterDefaultValueConfig.INSTANCE));
        assertFalse(Parameter.readBoolean("TEST", properties, paramBoolean2, ParameterDefaultValueConfig.INSTANCE));

        properties.put("test-param-string", "TestProperty");
        Parameter paramString = new Parameter("test-param-string", ParameterType.STRING, "", "TestParam");
        Parameter paramString2 = new Parameter("test-param-string2", ParameterType.STRING, "", "TestParam");
        assertEquals("TestProperty", Parameter.readString("TEST", properties, paramString));
        assertEquals("TestProperty", Parameter.readString("TEST", properties, paramString, ParameterDefaultValueConfig.INSTANCE));
        assertEquals("TestParam", Parameter.readString("TEST", properties, paramString2, ParameterDefaultValueConfig.INSTANCE));

        List<String> stringList = new ArrayList<>();
        stringList.add("Test1Property");
        stringList.add("Test2Property");
        List<String> stringList2 = new ArrayList<>();
        stringList2.add("Test1Param");
        stringList2.add("Test2Param");
        properties.put("test-param-string-list", stringList);
        Parameter paramStringList = new Parameter("test-param-string-list", ParameterType.STRING_LIST, "", stringList2);
        Parameter paramStringList2 = new Parameter("test-param-string-list2", ParameterType.STRING_LIST, "", stringList2);
        assertEquals("Test2Property", Parameter.readStringList("TEST", properties, paramStringList).get(1));
        assertEquals("Test2Property", Parameter.readStringList("TEST", properties, paramStringList, ParameterDefaultValueConfig.INSTANCE).get(1));
        assertEquals("Test1Param", Parameter.readStringList("TEST", properties, paramStringList2, ParameterDefaultValueConfig.INSTANCE).get(0));

        properties.put("test-param-double", 0.06);
        Parameter paramDouble = new Parameter("test-param-double", ParameterType.DOUBLE, "", 0.08);
        Parameter paramDouble2 = new Parameter("test-param-double2", ParameterType.DOUBLE, "", 0.08);
        assertEquals(0.06, Parameter.readDouble("TEST", properties, paramDouble), 1e-8);
        assertEquals(0.06, Parameter.readDouble("TEST", properties, paramDouble, ParameterDefaultValueConfig.INSTANCE), 1e-8);
        assertEquals(0.08, Parameter.readDouble("TEST", properties, paramDouble2, ParameterDefaultValueConfig.INSTANCE), 1e-8);

        config.createModuleConfig("import-export-parameters-default-value").setStringProperty("test-param-double", "0.06");
        assertEquals(0.06, Parameter.readDouble("TEST", new Properties(), paramDouble, new ParameterDefaultValueConfig(config)), 1e-8);

        properties.put("test-param-int", 666);
        Parameter paramInt = new Parameter("test-param-int", ParameterType.INTEGER, "", 999);
        Parameter paramInt2 = new Parameter("test-param-int2", ParameterType.INTEGER, "", 888);
        assertEquals(666, Parameter.readInteger("TEST", properties, paramInt, ParameterDefaultValueConfig.INSTANCE));
        assertEquals(888, Parameter.readInteger("TEST", properties, paramInt2, ParameterDefaultValueConfig.INSTANCE));
    }

    @Test
    void testWithPossibleValues() {
        Properties properties = new Properties();
        properties.put("p1", "d");
        Parameter p1 = new Parameter("p1", ParameterType.STRING, "a param", "a", List.of("a", "b", "c"));
        var defaultValueConfig = new ParameterDefaultValueConfig(config);
        var e = assertThrows(IllegalArgumentException.class, () -> Parameter.readString("TEST", properties, p1, defaultValueConfig));
        assertEquals("Value d of parameter p1 is not contained in possible values [a, b, c]", e.getMessage());
    }

    @Test
    void possibleValuesTest() {
        Parameter p1 = new Parameter("p1", ParameterType.STRING, "a param", "a", List.of("a", "b", "c"));
        p1.addAdditionalNames("pone");
        assertEquals("p1", p1.getName());
        assertEquals(List.of("p1", "pone"), p1.getNames());
        assertEquals(ParameterType.STRING, p1.getType());
        assertEquals("a param", p1.getDescription());
        assertEquals("a", p1.getDefaultValue());
        assertEquals(List.of("a", "b", "c"), p1.getPossibleValues());
    }

    @Test
    void defaultValueNotInPossibleValuesTest() {
        List<Object> possibleValues = List.of("a", "b", "c");
        var e = assertThrows(IllegalArgumentException.class, () -> new Parameter("p1", ParameterType.STRING, "a param", "d", possibleValues));
        assertEquals("Parameter possible values [a, b, c] should contain default value d", e.getMessage());
    }

    @Test
    void defaultValueWithStringListParamTest() {
        List<Object> possibleValues = List.of("a", "b", "c");
        assertDoesNotThrow(() -> new Parameter("p1", ParameterType.STRING_LIST, "a str list param", List.of("a", "c"), possibleValues));
    }

    @Test
    void getScopeTest() {
        Parameter param = new Parameter("test-param", ParameterType.STRING, "", "yes");
        assertEquals(ParameterScope.FUNCTIONAL, param.getScope());
        Parameter param2 = new Parameter("test-param2", ParameterType.STRING, "", "yes", null, ParameterScope.TECHNICAL);
        assertEquals(ParameterScope.TECHNICAL, param2.getScope());
    }

    @Test
    void intParameterNullDefaultValueErrorTest() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> new Parameter("i", ParameterType.INTEGER, "an integer", null));
        assertEquals("With Integer parameter you are not allowed to pass a null default value", e.getMessage());
    }

    @Test
    void getCategoryKeyTest() {
        Parameter param0 = new Parameter("p0", ParameterType.BOOLEAN, "param0", Boolean.FALSE);
        Parameter param1 = new Parameter("p1", ParameterType.BOOLEAN, "another param", Boolean.FALSE,
                ParameterScope.FUNCTIONAL, "Category");
        assertNull(param0.getCategoryKey());
        assertEquals("Category", param1.getCategoryKey());
    }
}
