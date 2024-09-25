/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.parameters;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Antoine Bouhours {@literal <antoine.bouhours at rte-france.com>}
 */
class DynamicValueParameterTest {

    private FileSystem fileSystem;

    private InMemoryPlatformConfig config;

    private ParameterDefaultValueConfig defaultValueConfig;

    private List<Parameter> parameters;

    @BeforeEach
    void setup() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        config = new InMemoryPlatformConfig(fileSystem);
        defaultValueConfig = new ParameterDefaultValueConfig(config);
        Parameter paramBoolean = new Parameter("test-param-boolean", ParameterType.BOOLEAN, "", Boolean.FALSE);
        Parameter paramBoolean2 = new Parameter("test-param-boolean2", ParameterType.BOOLEAN, "", Boolean.FALSE);
        Parameter paramString = new Parameter("test-param-string", ParameterType.STRING, "", "TestParam");
        Parameter paramString2 = new Parameter("test-param-string2", ParameterType.STRING, "", "TestParam2");
        Parameter paramInt = new Parameter("test-param-int", ParameterType.INTEGER, "", 999);
        Parameter paramInt2 = new Parameter("test-param-int2", ParameterType.INTEGER, "", 111);
        Parameter paramStringList = new Parameter("test-param-string-list", ParameterType.STRING_LIST, "", List.of("a", "b"));
        Parameter paramStringList2 = new Parameter("test-param-string-list-2", ParameterType.STRING_LIST, "", List.of("a", "b", "c"));
        Parameter paramDouble = new Parameter("test-param-double", ParameterType.DOUBLE, "", 0.06);
        Parameter paramDouble2 = new Parameter("test-param-double2", ParameterType.DOUBLE, "", 0.08);
        parameters = List.of(paramBoolean, paramBoolean2, paramString, paramString2, paramInt, paramInt2, paramStringList, paramStringList2, paramDouble, paramDouble2);
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void testDynamicParametersModuleConfig() {
        MapModuleConfig moduleConfig = config.createModuleConfig("import-export-parameters-default-value");
        moduleConfig.setStringProperty("test-param-boolean", "true");
        moduleConfig.setStringProperty("test-param-string", "TestDynamicParam");
        moduleConfig.setStringProperty("test-param-int", "888");
        moduleConfig.setStringListProperty("test-param-string-list", List.of("e", "b"));
        moduleConfig.setStringProperty("test-param-double", "0.07");

        List<Parameter> loadedParams = DynamicValueParameter.load(parameters, moduleConfig);
        assertValuesModifiedWithConfig(loadedParams);
    }

    private static void assertValuesModifiedWithConfig(List<Parameter> loadedParams) {
        assertEquals(true, loadedParams.get(0).getDefaultValue());
        assertEquals(false, ((DynamicValueParameter) loadedParams.get(0)).getStaticDefaultValue());
        assertEquals(false, loadedParams.get(1).getDefaultValue());
        assertEquals("TestDynamicParam", loadedParams.get(2).getDefaultValue());
        assertEquals("TestParam2", loadedParams.get(3).getDefaultValue());
        assertEquals(888, loadedParams.get(4).getDefaultValue());
        assertEquals(111, loadedParams.get(5).getDefaultValue());
        assertEquals(List.of("e", "b"), loadedParams.get(6).getDefaultValue());
        assertEquals(List.of("a", "b", "c"), loadedParams.get(7).getDefaultValue());
        assertEquals(0.07, (double) loadedParams.get(8).getDefaultValue(), 1e-3);
        assertEquals(0.08, (double) loadedParams.get(9).getDefaultValue(), 1e-3);
    }

    @Test
    void testDynamicParametersNullModuleConfig() {
        List<Parameter> loadedParams = DynamicValueParameter.load(parameters, null);
        assertDefaultValues(loadedParams);
    }

    private static void assertDefaultValues(List<Parameter> loadedParams) {
        assertEquals(false, loadedParams.get(0).getDefaultValue());
        assertEquals(false, loadedParams.get(1).getDefaultValue());
        assertEquals("TestParam", loadedParams.get(2).getDefaultValue());
        assertEquals("TestParam2", loadedParams.get(3).getDefaultValue());
        assertEquals(999, loadedParams.get(4).getDefaultValue());
        assertEquals(111, loadedParams.get(5).getDefaultValue());
        assertEquals(List.of("a", "b"), loadedParams.get(6).getDefaultValue());
        assertEquals(List.of("a", "b", "c"), loadedParams.get(7).getDefaultValue());
        assertEquals(0.06, (double) loadedParams.get(8).getDefaultValue(), 1e-3);
        assertEquals(0.08, (double) loadedParams.get(9).getDefaultValue(), 1e-3);
    }

    @Test
    void testDynamicParametersDefaultValueConfig() {
        MapModuleConfig moduleConfig = config.createModuleConfig("import-export-parameters-default-value");
        moduleConfig.setStringProperty("prefix_test-param-boolean", "true");
        moduleConfig.setStringProperty("prefix_test-param-string", "TestDynamicParam");
        moduleConfig.setStringProperty("prefix_test-param-int", "888");
        moduleConfig.setStringListProperty("prefix_test-param-string-list", List.of("e", "b"));
        moduleConfig.setStringProperty("prefix_test-param-double", "0.07");

        List<Parameter> loadedParams = DynamicValueParameter.load(parameters, "prefix", defaultValueConfig);
        assertValuesModifiedWithConfig(loadedParams);
    }

    @Test
    void testDynamicParametersNullDefaultValueConfig() {
        List<Parameter> loadedParams = DynamicValueParameter.load(parameters, "", null);
        assertDefaultValues(loadedParams);
    }

    @Test
    void testDefaultValueSource() {
        MapModuleConfig moduleConfig = config.createModuleConfig("import-export-parameters-default-value");
        moduleConfig.setStringProperty("test-param-boolean", "true");
        moduleConfig.setStringProperty("test-param-string", "TestDynamicParam");
        moduleConfig.setStringProperty("test-param-int", "888");
        moduleConfig.setStringListProperty("test-param-string-list", List.of("e", "b"));
        moduleConfig.setStringProperty("test-param-double", "0.07");

        List<Parameter> loadedParams = DynamicValueParameter.load(parameters, moduleConfig);
        assertEquals(DefaultValueSource.CONFIGURATION, loadedParams.get(0).getDefaultValueSource());
        assertEquals(DefaultValueSource.CODE, loadedParams.get(1).getDefaultValueSource());
    }
}
