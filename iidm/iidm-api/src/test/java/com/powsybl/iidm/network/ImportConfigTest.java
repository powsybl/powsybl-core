/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ImportConfigTest {

    @Test
    void test() throws IOException {
        var importConfig = new ImportConfig();
        assertTrue(importConfig.getPostProcessors().isEmpty());
        importConfig = new ImportConfig("p1", "p2");
        var postProcessors = importConfig.getPostProcessors();
        assertEquals(List.of("p1", "p2"), postProcessors);
        // assert the returned list is immutable
        assertThrows(UnsupportedOperationException.class, () -> postProcessors.add("p3"));
        importConfig.addPostProcessors(List.of("p3"));
        assertEquals(List.of("p1", "p2", "p3"), postProcessors);
        try (var fs = Jimfs.newFileSystem(Configuration.unix())) {
            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
            importConfig = ImportConfig.load(platformConfig);
            assertTrue(importConfig.getPostProcessors().isEmpty());
            var module = platformConfig.createModuleConfig("import");
            module.setStringListProperty("postProcessors", List.of("p4"));
            importConfig = ImportConfig.load(platformConfig);
            assertEquals(List.of("p4"), importConfig.getPostProcessors());
        }
    }
}
