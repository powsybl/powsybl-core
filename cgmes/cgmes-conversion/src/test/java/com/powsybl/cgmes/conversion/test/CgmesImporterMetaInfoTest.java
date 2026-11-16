/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class CgmesImporterMetaInfoTest {

    @Test
    void test() throws IOException {
        try (var fs = Jimfs.newFileSystem(Configuration.unix())) {
            var importer = new CgmesImport(new InMemoryPlatformConfig(fs));
            assertEquals("CGMES", importer.getFormat());
            assertEquals("ENTSO-E CGMES version 2.4.15", importer.getComment());
            assertEquals(List.of("xml"), importer.getSupportedExtensions());
        }
    }
}
