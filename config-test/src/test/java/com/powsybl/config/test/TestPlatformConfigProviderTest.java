/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.config.test;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.commons.config.PlatformConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
class TestPlatformConfigProviderTest {

    @Test
    void test() throws IOException {
        PlatformConfig platformConfig = PlatformConfig.defaultConfig();
        assertEquals("/work/" + TestPlatformConfigProvider.CONFIG_DIR,
                platformConfig.getConfigDir().map(Path::toString).orElse(null));

        checkFileContent(platformConfig, "other.txt", "conf");
        checkFileContent(platformConfig, "subfolder/subfolder_file.txt", "subfile content");
        assertEquals("baz", platformConfig.getOptionalModuleConfig("foo").flatMap(c -> c.getOptionalStringProperty("bar")).orElse(""));
    }

    private void checkFileContent(PlatformConfig platformConfig, String file, String content) throws IOException {
        Path testPath = platformConfig.getConfigDir().map(p -> p.resolve(file)).orElse(null);
        assertNotNull(testPath);
        String testContent = Files.readAllLines(testPath, StandardCharsets.UTF_8).get(0);
        assertEquals(content, testContent);
    }

    @Test
    void testBaseVoltagesConfig() {
        BaseVoltagesConfig config = BaseVoltagesConfig.fromPlatformConfig();
        assertNotNull(config);
        assertEquals(1, config.getBaseVoltages().size());
        assertEquals("test", config.getBaseVoltages().get(0).getName());
    }
}
