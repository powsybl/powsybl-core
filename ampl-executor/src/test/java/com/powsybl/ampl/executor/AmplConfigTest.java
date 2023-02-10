/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.executor;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.FileSystem;

/**
 * @author Nicolas Pierre <nicolas.pierre@artelys.com>
 */
public class AmplConfigTest {
    @Test
    public void test() {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("ampl");
        moduleConfig.setStringProperty("homeDir", "/home/test/ampl");
        AmplConfig cfg = AmplConfig.getConfig(platformConfig);
        Assert.assertEquals("Error parsing Ampl Home", "/home/test/ampl", cfg.getAmplHome());
    }
}
