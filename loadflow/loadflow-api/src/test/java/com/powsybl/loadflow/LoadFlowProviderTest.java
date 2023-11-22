/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.loadflow.json.JsonLoadFlowParametersTest.DummyExtension;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class LoadFlowProviderTest {

    @Test
    void findAllProvidersTest() {
        assertEquals(1, LoadFlowProvider.findAll().size());
    }

    @Test
    void testParametersExtension() throws IOException {
        LoadFlowProvider provider = new LoadFlowProviderMock();
        assertEquals(3, provider.getSpecificParameters().size());
        assertEquals(List.of("parameterDouble", "parameterBoolean", "parameterString"), provider.getSpecificParameters().stream().map(Parameter::getName).collect(Collectors.toList()));
        assertSame(DummyExtension.class, provider.getSpecificParametersClass().orElseThrow());
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
            DummyExtension parametersExtension = (DummyExtension) provider.loadSpecificParameters(platformConfig).orElseThrow();
            assertEquals(0, parametersExtension.getParameterDouble());
            assertFalse(parametersExtension.isParameterBoolean());
            MapModuleConfig moduleConfig = platformConfig.createModuleConfig("dummy-extension");
            moduleConfig.setStringProperty("parameterDouble", "3.14");
            parametersExtension = (DummyExtension) provider.loadSpecificParameters(platformConfig).orElseThrow();
            assertEquals(3.14, parametersExtension.getParameterDouble());
            parametersExtension = (DummyExtension) provider.loadSpecificParameters(Map.of("parameterBoolean", "true")).orElseThrow();
            assertTrue(parametersExtension.isParameterBoolean());
            parametersExtension.setParameterString("ok");
            assertEquals(Map.of("parameterDouble", "0.0", "parameterBoolean", "true", "parameterString", "ok"), provider.createMapFromSpecificParameters(parametersExtension));
            provider.updateSpecificParameters(parametersExtension, Map.of("parameterDouble", "666"));
            assertEquals(666, parametersExtension.getParameterDouble());
        }
    }
}
