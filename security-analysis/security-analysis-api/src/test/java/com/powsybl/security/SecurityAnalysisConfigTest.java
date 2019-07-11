/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisConfigTest {

    private FileSystem fileSystem;
    private InMemoryPlatformConfig platformConfig;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void checkDefaultConfig() {
        SecurityAnalysisConfig config = new SecurityAnalysisConfig();
        assertFalse(config.getPreprocessorName().isPresent());
    }

    @Test
    public void fromPlatformConfig() {
        MapModuleConfig module = platformConfig.createModuleConfig("security-analysis");
        module.setStringProperty("preprocessor", "myProcessor");

        SecurityAnalysisConfig config = SecurityAnalysisConfig.load(platformConfig);
        assertTrue(config.getPreprocessorName().isPresent());
        assertEquals("myProcessor", config.getPreprocessorName().get());
    }

    @Test
    public void fromEmptyPlatformConfig() {
        SecurityAnalysisConfig config = SecurityAnalysisConfig.load(platformConfig);
        assertFalse(config.getPreprocessorName().isPresent());
    }
}
