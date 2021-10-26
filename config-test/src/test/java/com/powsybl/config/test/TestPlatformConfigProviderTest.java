/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.config.test;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.commons.config.PlatformConfig;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class TestPlatformConfigProviderTest {

    @Test
    public void test() throws IOException {
        PlatformConfig platformConfig = new TestPlatformConfigProvider().getPlatformConfig();
        assertEquals("/work/" + TestPlatformConfigProvider.CONFIG_DIR,
                platformConfig.getConfigDir().toString());

        Path testPath = platformConfig.getConfigDir().resolve("other.txt");
        String testContent = Files.readAllLines(testPath, StandardCharsets.UTF_8).get(0);
        assertEquals("conf", testContent);
        assertEquals("baz", platformConfig.getModuleConfig("foo").getStringProperty("bar"));
    }

    @Test
    public void testBaseVoltagesConfig() {
        BaseVoltagesConfig config = BaseVoltagesConfig.fromPlatformConfig();
        assertNotNull(config);
        assertEquals(1, config.getBaseVoltages().size());
        assertEquals("test", config.getBaseVoltages().get(0).getName());
    }
}
