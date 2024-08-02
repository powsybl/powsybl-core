/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class DataSourceBuilderTest {
    protected FileSystem fileSystem;
    protected Path testDir;

    @BeforeEach
    void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        testDir = fileSystem.getPath("/tmp");
        Files.createDirectories(testDir);
    }

    @Test
    void testBuilder() {
        // Observer
        DataSourceObserver observer = new DefaultDataSourceObserver();

        // Constant parameters
        DataSourceBuilder builder = new DataSourceBuilder()
            .withDirectory(testDir)
            .withBaseName("foo")
            .withDataExtension(".baz")
            .withObserver(observer);

        // Directory datasource
        assertInstanceOf(DirectoryDataSource.class, builder.build());
        assertInstanceOf(GzDirectoryDataSource.class, builder.withCompressionFormat(CompressionFormat.GZIP).build());
        assertInstanceOf(ZstdDirectoryDataSource.class, builder.withCompressionFormat(CompressionFormat.ZSTD).build());
        assertInstanceOf(XZDirectoryDataSource.class, builder.withCompressionFormat(CompressionFormat.XZ).build());
        assertInstanceOf(Bzip2DirectoryDataSource.class, builder.withCompressionFormat(CompressionFormat.BZIP2).build());

        // Archive datasources
        assertInstanceOf(ZipArchiveDataSource.class, builder.withArchiveFormat(ArchiveFormat.ZIP).withCompressionFormat(null).build());
        assertInstanceOf(ZipArchiveDataSource.class, builder.withCompressionFormat(CompressionFormat.ZIP).build());
        assertInstanceOf(ZipArchiveDataSource.class, builder.withArchiveFormat(null).build());
    }

    @Test
    void testBuilderErrors() {
        // Observer
        DataSourceObserver observer = new DefaultDataSourceObserver();

        // Builder
        DataSourceBuilder builder = new DataSourceBuilder()
            .withCompressionFormat(CompressionFormat.ZIP)
            .withArchiveFileName("bar.zip")
            .withDataExtension(".baz")
            .withArchiveFormat(ArchiveFormat.ZIP)
            .withObserver(observer);

        // Directory missing
        PowsyblException exception = assertThrows(PowsyblException.class, builder::build);
        assertEquals("Datasource directory cannot be null", exception.getMessage());

        // Directory parameter is not a directory but a file
        Path file = testDir.resolve("fake.zip");
        builder.withDirectory(file);
        exception = assertThrows(PowsyblException.class, builder::build);
        assertEquals("Datasource directory has to be a directory", exception.getMessage());

        // Base name missing
        builder.withDirectory(testDir);
        exception = assertThrows(PowsyblException.class, builder::build);
        assertEquals("Datasource baseName cannot be null", exception.getMessage());
    }

    @Test
    void testBuilderErrorsZip() {
        DataSourceBuilder builder = new DataSourceBuilder()
            .withDirectory(testDir)
            .withBaseName("foo")
            .withArchiveFileName("bar.zip")
            .withDataExtension(".baz");

        // Wrong compression format
        builder.withCompressionFormat(CompressionFormat.GZIP).withArchiveFormat(ArchiveFormat.ZIP);
        PowsyblException exception = assertThrows(PowsyblException.class, builder::build);
        assertEquals("Incoherence between compression format GZIP and archive format ZIP", exception.getMessage());
    }
}
