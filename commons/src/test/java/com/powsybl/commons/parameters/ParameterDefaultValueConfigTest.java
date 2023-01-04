/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.parameters;

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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ParameterDefaultValueConfigTest {

    private FileSystem fileSystem;

    private MapModuleConfig moduleConfig;

    private ParameterDefaultValueConfig defaultValueConfig;

    @Before
    public void setup() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        InMemoryPlatformConfig config = new InMemoryPlatformConfig(fileSystem);
        moduleConfig = config.createModuleConfig("import-export-parameters-default-value");
        defaultValueConfig = new ParameterDefaultValueConfig(config);
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void test() {
        moduleConfig.setStringProperty("test_i", Integer.toString(3));
        Parameter parameter = new Parameter("i", ParameterType.INTEGER, "an integer", 1);
        Parameter parameter2 = new Parameter("i2", ParameterType.INTEGER, "an other integer", 2);
        assertEquals(3, defaultValueConfig.getIntegerValue("test", parameter));
        assertEquals(3, defaultValueConfig.getValue("test", parameter));
        assertEquals(2, defaultValueConfig.getIntegerValue("test", parameter2));
    }
}
