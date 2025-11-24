/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.loadflow.json.JsonLoadFlowParametersTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.powsybl.loadflow.LoadFlowParameters.load;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
class LoadFlowDefaultParametersLoaderTest {

    InMemoryPlatformConfig platformConfig;

    @BeforeEach
    void setUp() {
        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @Test
    void testLoadParametersFromClassPath() {
        LoadFlowDefaultParametersLoaderMock loader = new LoadFlowDefaultParametersLoaderMock("test");

        LoadFlowParameters parameters = new LoadFlowParameters(List.of(loader), platformConfig);

        assertFalse(parameters.isUseReactiveLimits());
        assertEquals(LoadFlowParameters.VoltageInitMode.DC_VALUES, parameters.getVoltageInitMode());
        List<Extension<LoadFlowParameters>> extensions = parameters.getExtensions().stream().toList();
        assertEquals(1, extensions.size());
        JsonLoadFlowParametersTest.DummyExtension dummyExtension = (JsonLoadFlowParametersTest.DummyExtension) extensions.get(0);
        assertEquals(5, dummyExtension.getParameterDouble());
    }

    @Test
    void testWithAModuleButNoProperty() {
        LoadFlowDefaultParametersLoaderMock loader = new LoadFlowDefaultParametersLoaderMock("test");

        platformConfig.createModuleConfig("load-flow");

        LoadFlowParameters parameters = new LoadFlowParameters(List.of(loader), platformConfig);
        JsonLoadFlowParametersTest.DummyExtension extension = parameters.getExtension(JsonLoadFlowParametersTest.DummyExtension.class);
        // The module is present in configuration, but no "default-parameters-loader" is set.
        // A unique loader is found: it is used.
        assertNotNull(extension);
        assertEquals("no", extension.getParameterString());
    }

    @Test
    void testLoaderButWrongDefault() {
        LoadFlowDefaultParametersLoaderMock loader = new LoadFlowDefaultParametersLoaderMock("test1");

        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow");
        moduleConfig.setStringProperty("default-parameters-loader", "test2");

        LoadFlowParameters parameters = new LoadFlowParameters(List.of(loader), platformConfig);
        // The loader was not used since it doesn't match the expected one. No exception is thrown.
        assertNull(parameters.getExtension(JsonLoadFlowParametersTest.DummyExtension.class));
    }

    @Test
    void testConflictBetweenDefaultParametersLoader() {
        LoadFlowDefaultParametersLoaderMock loader1 = new LoadFlowDefaultParametersLoaderMock("test1");
        LoadFlowDefaultParametersLoaderMock loader2 = new LoadFlowDefaultParametersLoaderMock("test2");
        List<LoadFlowDefaultParametersLoader> loaders = List.of(loader1, loader2);

        assertThrows(PowsyblException.class, () -> new LoadFlowParameters(loaders, platformConfig));

        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow");
        moduleConfig.setStringProperty("default-parameters-loader", "test1");

        LoadFlowParameters parameters = new LoadFlowParameters(loaders, platformConfig);
        List<Extension<LoadFlowParameters>> extensions = parameters.getExtensions().stream().toList();
        assertEquals(1, extensions.size());
    }

    @Test
    void testCorrectLoadingOrder() {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow-default-parameters");
        moduleConfig.setStringProperty("voltageInitMode", "PREVIOUS_VALUES");

        LoadFlowDefaultParametersLoaderMock loader = new LoadFlowDefaultParametersLoaderMock("test");

        LoadFlowParameters parameters = new LoadFlowParameters(List.of(loader));
        load(parameters, platformConfig);
        assertFalse(parameters.isUseReactiveLimits());
        assertEquals(LoadFlowParameters.VoltageInitMode.PREVIOUS_VALUES, parameters.getVoltageInitMode());
    }

    @Test
    void testProviderParameters() {
        LoadFlowDefaultParametersLoaderMock loader = new LoadFlowDefaultParametersLoaderMock("test");
        LoadFlowParameters parameters = new LoadFlowParameters(List.of(loader), platformConfig);

        // LoadFlowDefaultParametersLoaderMock creates provider parameters extensions
        JsonLoadFlowParametersTest.DummyExtension beforePlatformConfig = parameters.getExtension(JsonLoadFlowParametersTest.DummyExtension.class);
        assertNotNull(beforePlatformConfig);
        assertEquals(5, beforePlatformConfig.getParameterDouble());
        assertEquals("no", beforePlatformConfig.getParameterString());

        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("dummy-extension");
        moduleConfig.setStringProperty("parameterString", "modified");

        load(parameters, platformConfig);
        parameters.loadExtensions(platformConfig);

        // loadExtensions only override values that are present in PlatformConfig
        JsonLoadFlowParametersTest.DummyExtension afterPlatformConfig = parameters.getExtension(JsonLoadFlowParametersTest.DummyExtension.class);
        assertNotNull(afterPlatformConfig);
        assertEquals(5, afterPlatformConfig.getParameterDouble());
        assertEquals("modified", afterPlatformConfig.getParameterString());
    }

    private void assertSameMetaData(List<Parameter> l1, List<Parameter> l2) {
        // Everything except default value should be the same
        assertEquals(l1.size(), l2.size());
        Iterator<Parameter> it1 = l1.iterator();
        Iterator<Parameter> it2 = l2.iterator();
        for (int i = 0; i < l1.size(); i++) {
            Parameter p1 = it1.next();
            Parameter p2 = it2.next();
            assertEquals(p1.getName(), p2.getName());
            assertEquals(p1.getCategoryKey(), p2.getCategoryKey());
            assertEquals(p1.getDescription(), p2.getDescription());
            assertEquals(p1.getPossibleValues(), p2.getPossibleValues());
            assertEquals(p1.getType(), p2.getType());
            assertEquals(p1.getScope(), p2.getScope());
        }
    }

    @Test
    void testSpecificParameters() {
        LoadFlowDefaultParametersLoaderMock loader = new LoadFlowDefaultParametersLoaderMock("test");
        LoadFlowProvider provider = new LoadFlowProviderMock();
        List<Parameter> parameters = provider.getSpecificParameters();
        List<Parameter> parametersWithDefaultValue = LoadFlowProviderUtil.getSpecificParameters(provider, Optional.of(loader));

        assertSameMetaData(parameters, parametersWithDefaultValue);

        assertEquals(6.4, parameters.get(0).getDefaultValue());
        assertEquals(5.0, parametersWithDefaultValue.get(0).getDefaultValue());

        assertEquals(42, parameters.get(1).getDefaultValue());
        assertEquals(7, parametersWithDefaultValue.get(1).getDefaultValue());

        assertEquals(false, parameters.get(2).getDefaultValue());
        assertEquals(false, parametersWithDefaultValue.get(2).getDefaultValue());

        assertEquals("yes", parameters.get(3).getDefaultValue());
        assertEquals("no", parametersWithDefaultValue.get(3).getDefaultValue());

        assertNull(parameters.get(4).getDefaultValue());
        assertEquals("Hello", parametersWithDefaultValue.get(4).getDefaultValue());

        assertNull(parameters.get(5).getDefaultValue());
        assertEquals(List.of("a", "b"), parametersWithDefaultValue.get(5).getDefaultValue());

        LoadFlowDefaultParametersLoader partialDefault = new AbstractLoadFlowDefaultParametersLoader("partial", "/LoadFlowParametersPartialUpdate.json") {
        };
        parametersWithDefaultValue = LoadFlowProviderUtil.getSpecificParameters(provider, Optional.of(partialDefault));
        assertEquals(5.0, parametersWithDefaultValue.get(0).getDefaultValue());  // overridden
        assertEquals(42, parametersWithDefaultValue.get(1).getDefaultValue()); // default value
        assertEquals(false, parametersWithDefaultValue.get(2).getDefaultValue()); // default value
        assertEquals("yes", parametersWithDefaultValue.get(3).getDefaultValue()); // default value
        assertNull(parametersWithDefaultValue.get(4).getDefaultValue()); // default value
        assertNull(parametersWithDefaultValue.get(5).getDefaultValue()); // default value

    }

}
