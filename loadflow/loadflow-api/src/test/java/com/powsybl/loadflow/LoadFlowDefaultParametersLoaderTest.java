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
import com.powsybl.loadflow.json.JsonLoadFlowParametersTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.util.List;

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
        assertEquals("Hello", extension.getParameterString());
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
        assertEquals("Hello", beforePlatformConfig.getParameterString());

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
}
