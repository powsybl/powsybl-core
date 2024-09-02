/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.powsybl.commons.datasource.DataSourceUtil.createDataSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class DataSourceUtilTest {
    protected FileSystem fileSystem;
    protected Path testDir;

    @BeforeEach
    void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        testDir = fileSystem.getPath("/tmp");
        Files.createDirectories(testDir);
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    void testGetBaseName() {
        assertEquals("dummy", DataSourceUtil.getBaseName("dummy.xml.gz"));
        assertEquals("dummy", DataSourceUtil.getBaseName("dummy.gz"));
        assertEquals("dummy", DataSourceUtil.getBaseName("dummy"));
    }

    @Test
    void testCreateDataSource() {
        assertInstanceOf(DirectoryDataSource.class, createDataSource(testDir, "dummy", null, null));
        assertInstanceOf(Bzip2DirectoryDataSource.class, createDataSource(testDir, "dummy", CompressionFormat.BZIP2, null));
        assertInstanceOf(GzDirectoryDataSource.class, createDataSource(testDir, "dummy", CompressionFormat.GZIP, null));
        assertInstanceOf(XZDirectoryDataSource.class, createDataSource(testDir, "dummy", CompressionFormat.XZ, null));
        assertInstanceOf(ZipArchiveDataSource.class, createDataSource(testDir, "dummy", CompressionFormat.ZIP, null));
        assertInstanceOf(ZstdDirectoryDataSource.class, createDataSource(testDir, "dummy", CompressionFormat.ZSTD, null));
    }
}
