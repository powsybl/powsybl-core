/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.parameters.Parameter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ShortCircuitAnalysisProviderTest {

    @Test
    void findAllProvidersTest() {
        assertEquals(1, ShortCircuitAnalysisProvider.findAll().size());
    }

    @Test
    void testConfiguredParameters() throws IOException {
        ShortCircuitAnalysisProvider provider = new ShortCircuitAnalysisMock();
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
            List<Parameter> baseSpecificParameters = provider.getSpecificParameters();
            assertEquals(6.4, baseSpecificParameters.get(0).getDefaultValue());
            assertEquals(false, baseSpecificParameters.get(1).getDefaultValue());
            assertEquals("yes", baseSpecificParameters.get(2).getDefaultValue());
            MapModuleConfig moduleConfig = platformConfig.createModuleConfig("dummy-sc-extension");
            moduleConfig.setStringProperty("parameterDouble", "3.14");
            List<Parameter> configuredSpecificParameters = provider.getSpecificParameters(platformConfig);
            assertEquals(3.14, configuredSpecificParameters.get(0).getDefaultValue());
            assertEquals(false, configuredSpecificParameters.get(1).getDefaultValue());
            assertEquals("yes", configuredSpecificParameters.get(2).getDefaultValue());
        }
    }
}
