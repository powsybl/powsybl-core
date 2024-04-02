/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Hugo Kulesza <hugo.kulesza at rte-france.com>
 */
public class OdreGeoDataAdderPostProcessorTest {

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
        OdreGeoDataAdderPostProcessor.DEFAULT_FILE_NAMES.forEach(
                (name, fileName) -> {
                    Path path = platformConfig.getConfigDir().map(p -> p.resolve(fileName)).orElse(null);
                    assertNotNull(path);
                    try {
                        Files.copy(getClass().getResourceAsStream("/" + fileName), path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

    }

}
