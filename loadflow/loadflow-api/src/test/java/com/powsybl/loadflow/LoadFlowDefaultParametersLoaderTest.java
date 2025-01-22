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
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.loadflow.json.JsonLoadFlowParametersTest;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.util.List;

import static com.powsybl.loadflow.LoadFlowParameters.load;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
class LoadFlowDefaultParametersLoaderTest {

    @Test
    void testLoadParametersFromClassPath() {
        LoadFlowDefaultParametersLoaderMock loader = new LoadFlowDefaultParametersLoaderMock("test");

        LoadFlowParameters parameters = new LoadFlowParameters(List.of(loader));

        assertFalse(parameters.isUseReactiveLimits());
        assertEquals(LoadFlowParameters.VoltageInitMode.DC_VALUES, parameters.getVoltageInitMode());
        List<Extension<LoadFlowParameters>> extensions = parameters.getExtensions().stream().toList();
        assertEquals(1, extensions.size());
        JsonLoadFlowParametersTest.DummyExtension dummyExtension = (JsonLoadFlowParametersTest.DummyExtension) extensions.get(0);
        assertEquals(5, dummyExtension.getParameterDouble());
    }

    @Test
    void testConflictBetweenDefaultParametersLoader() {
        LoadFlowDefaultParametersLoaderMock loader1 = new LoadFlowDefaultParametersLoaderMock("test1");
        LoadFlowDefaultParametersLoaderMock loader2 = new LoadFlowDefaultParametersLoaderMock("test2");

        LoadFlowParameters parameters = new LoadFlowParameters(List.of(loader1, loader2));
        List<Extension<LoadFlowParameters>> extensions = parameters.getExtensions().stream().toList();
        assertEquals(0, extensions.size());
    }

    @Test
    void testCorrectLoadingOrder() {
        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
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
        LoadFlowParameters parameters = new LoadFlowParameters(List.of(loader));
        load(parameters);
        parameters.loadExtensions(PlatformConfig.defaultConfig());

        JsonLoadFlowParametersTest.DummyExtension extension = parameters.getExtension(JsonLoadFlowParametersTest.DummyExtension.class);
        assertNotNull(extension);
        assertEquals(5, extension.getParameterDouble());

    }
}
