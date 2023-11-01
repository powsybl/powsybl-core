/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.scripting;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class JavaScriptPostProcessorTest {

    private FileSystem fileSystem;

    @BeforeEach
    void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void test() throws IOException {
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        Path script = platformConfig.getConfigDir().map(p -> p.resolve(JavaScriptPostProcessor.SCRIPT_NAME)).orElse(null);
        assertNotNull(script);
        Files.copy(getClass().getResourceAsStream("/import-post-processor.js"), script);
        test(platformConfig);

        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("javaScriptPostProcessor");
        moduleConfig.setStringProperty("printToStdOut", "false");
        test(platformConfig);

        // Test with a custom script name
        script = platformConfig.getConfigDir().map(p -> p.resolve("custom-script.js")).orElse(null);
        assertNotNull(script);
        Files.copy(getClass().getResourceAsStream("/import-post-processor.js"), script);
        moduleConfig.setStringProperty("script", script.toAbsolutePath().toString());
        test(platformConfig);
    }

    private void test(PlatformConfig platformConfig) {
        JavaScriptPostProcessor processor = new JavaScriptPostProcessor(platformConfig);
        assertEquals("javaScript", processor.getName());

        try {
            processor.process(null, null);
            fail();
        } catch (Exception ignored) {
        }
    }
}
