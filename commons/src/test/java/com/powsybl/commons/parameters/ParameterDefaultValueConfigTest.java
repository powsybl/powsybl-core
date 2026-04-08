/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.parameters;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ParameterDefaultValueConfigTest {

    private FileSystem fileSystem;

    private MapModuleConfig moduleConfig;

    private ParameterDefaultValueConfig defaultValueConfig;

    @BeforeEach
    void setup() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        InMemoryPlatformConfig config = new InMemoryPlatformConfig(fileSystem);
        moduleConfig = config.createModuleConfig("import-export-parameters-default-value");
        defaultValueConfig = new ParameterDefaultValueConfig(config);
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void test() {
        moduleConfig.setStringProperty("test_i", Integer.toString(3));
        Parameter parameter = new Parameter("i", ParameterType.INTEGER, "an integer", 1);
        Parameter parameter2 = new Parameter("i2", ParameterType.INTEGER, "an other integer", 2);
        assertEquals(3, defaultValueConfig.getIntegerValue("test", parameter));
        assertEquals(3, defaultValueConfig.getValue("test", parameter));
        assertEquals(2, defaultValueConfig.getIntegerValue("test", parameter2));
    }
}
