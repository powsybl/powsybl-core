/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.hybrid.security.analysis;

import com.powsybl.commons.config.ModuleConfigRepository;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.hybrid.security.analysis.parameters.HybridSecurityAnalysisParameters;
import com.powsybl.tools.PowsyblCoreVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** @author Riad Benradi {@literal <riad.benradi_externe at rte-france.com>}*/

class HybridSecurityAnalysisHandlerProviderTest {
    private HybridSecurityAnalysisProvider provider;

    @BeforeEach
    void setUp() {
        provider = new HybridSecurityAnalysisProvider();
    }

    @Test
    void testProviderName() {
        assertEquals("HybridSecurityAnalysisHandler", provider.getName());
    }

    @Test
    void testProviderVersion() {
        assertEquals(new PowsyblCoreVersion().toString(), provider.getVersion());
    }

    @Test
    void testLoadSpecificParametersWithYamlConfigFile() {
        Path testConfigDir = Paths.get("src/test/resources");
        ModuleConfigRepository repository = PlatformConfig.loadModuleRepository(testConfigDir, "config");
        PlatformConfig config = new PlatformConfig(repository, testConfigDir);

        var extension = provider.loadSpecificParameters(config);

        assertTrue(extension.isPresent(), "Extension should be present");
        HybridSecurityAnalysisParameters params = (HybridSecurityAnalysisParameters) extension.get();

        assertEquals("LoadFlow", params.getFirstProviderName());
        assertEquals("DynaFlow", params.getSecondProviderName());
    }
}
