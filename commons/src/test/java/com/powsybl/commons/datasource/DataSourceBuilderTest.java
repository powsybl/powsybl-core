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

import java.nio.file.FileSystem;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class DataSourceBuilderTest {
    protected FileSystem fileSystem;
    protected Path testDir;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        testDir = fileSystem.getPath("/tmp");
    }

    @Test
    void testBuilderZip() {
        // Observer
        DataSourceObserver observer = new DefaultDataSourceObserver();

        // Constant parameters
        DataSourceBuilder builder = new DataSourceBuilder()
            .withDirectory(testDir)
            .withBaseName("foo")
            .withSourceFormat(".baz")
            .withObserver(observer);

        // Directory datasource
        assertInstanceOf(DirectoryDataSource.class, builder.build());
        assertInstanceOf(GzDataSource.class, builder.withCompressionFormat(CompressionFormat.GZIP).build());
        assertInstanceOf(ZstdDataSource.class, builder.withCompressionFormat(CompressionFormat.ZSTD).build());
        assertInstanceOf(XZDataSource.class, builder.withCompressionFormat(CompressionFormat.XZ).build());
        assertInstanceOf(Bzip2DataSource.class, builder.withCompressionFormat(CompressionFormat.BZIP2).build());

        // Archive datasources
        assertInstanceOf(TarDataSource.class, builder.withArchiveFormat(ArchiveFormat.TAR).build());
        assertInstanceOf(ZipDataSource.class, builder.withArchiveFormat(ArchiveFormat.ZIP).withCompressionFormat(null).build());
        assertInstanceOf(ZipDataSource.class, builder.withCompressionFormat(CompressionFormat.ZIP).build());
        assertInstanceOf(ZipDataSource.class, builder.withArchiveFormat(null).build());
    }

    @Test
    void testBuilderErrors() {
        // Observer
        DataSourceObserver observer = new DefaultDataSourceObserver();

        // Builder
        DataSourceBuilder builder = new DataSourceBuilder()
            .withCompressionFormat(CompressionFormat.ZIP)
            .withArchiveFileName("bar.zip")
            .withSourceFormat(".baz")
            .withArchiveFormat(ArchiveFormat.ZIP)
            .withObserver(observer);

        // Directory missing
        PowsyblException exception = assertThrows(PowsyblException.class, builder::build);
        assertEquals("Datasource directory cannot be null", exception.getMessage());

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
            .withSourceFormat(".baz");

        // Wrong archive format
        builder.withCompressionFormat(CompressionFormat.ZIP).withArchiveFormat(ArchiveFormat.TAR);
        PowsyblException exception = assertThrows(PowsyblException.class, builder::build);
        assertEquals("Incoherence between compression format ZIP and archive format TAR", exception.getMessage());

        // Wrong compression format
        builder.withCompressionFormat(CompressionFormat.GZIP).withArchiveFormat(ArchiveFormat.ZIP);
        exception = assertThrows(PowsyblException.class, builder::build);
        assertEquals("Incoherence between compression format GZIP and archive format ZIP", exception.getMessage());
    }
}
