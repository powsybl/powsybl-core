/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.executor;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nicolas Pierre {@literal <nicolas.pierre@artelys.com>}
 */
class AmplConfigTest {
    @Test
    void test() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
            MapModuleConfig moduleConfig = platformConfig.createModuleConfig("ampl");
            moduleConfig.setStringProperty("homeDir", "/home/test/ampl");
            AmplConfig cfg = AmplConfig.load(platformConfig);
            assertEquals("/home/test/ampl", cfg.getAmplHome(), "Error parsing Ampl Home");
        }
    }
}
