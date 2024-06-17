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
import com.powsybl.commons.PowsyblException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class DataSourceUtilTest {
    private FileSystem fileSystem;

    @BeforeEach
    void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Files.createDirectories(fileSystem.getPath("/tmp"));
    }

    @Test
    void testGuessBaseName() {
        assertEquals("dummy", DataSourceUtil.guessBaseName("dummy.xml.gz"));
        assertEquals("dummy", DataSourceUtil.guessBaseName("dummy.gz"));
        assertEquals("dummy", DataSourceUtil.guessBaseName("dummy"));
    }

    private void checkArchiveDataSource(DataSource dataSource, String baseName, String sourceFormatExtension, DataSourceObserver dataSourceObserver) {

        // The data source should be an instance of AbstractDataSource
        assertInstanceOf(AbstractDataSource.class, dataSource);

        // Check the datasource values
        assertEquals(fileSystem.getPath("/tmp"), ((AbstractDataSource) dataSource).getDirectory());
        assertEquals(baseName, dataSource.getBaseName());
        assertEquals(sourceFormatExtension, ((AbstractDataSource) dataSource).getSourceFormatExtension());
        assertEquals(ArchiveFormat.ZIP, ((AbstractDataSource) dataSource).getArchiveFormat());
        assertEquals(CompressionFormat.ZIP, ((AbstractDataSource) dataSource).getCompressionFormat());
        if (dataSourceObserver == null) {
            assertNull(((AbstractDataSource) dataSource).getObserver());
        } else {
            assertEquals(dataSourceObserver, ((AbstractDataSource) dataSource).getObserver());
        }
    }

    @Test
    void testCreateArchiveDataSource() {
        // Observer
        DataSourceObserver dataSourceObserver = new DefaultDataSourceObserver();

        // Create a datasource
        DataSource dataSource = DataSourceUtil.createArchiveDataSource(
            fileSystem.getPath("/tmp/foo.bar.zip"));

        // Checks
        checkArchiveDataSource(dataSource, "", "", null);

        // Create a datasource
        dataSource = DataSourceUtil.createArchiveDataSource(
            fileSystem.getPath("/tmp/foo.bar.zip"),
            dataSourceObserver);

        // Checks
        checkArchiveDataSource(dataSource, "", "", dataSourceObserver);

        // Create a datasource
        dataSource = DataSourceUtil.createExtensionFilteredArchiveDataSource(
            fileSystem.getPath("/tmp/foo.bar.zip"),
            ".bar");

        // Checks
        checkArchiveDataSource(dataSource, "", ".bar", null);

        // Create a datasource
        dataSource = DataSourceUtil.createExtensionFilteredArchiveDataSource(
            fileSystem.getPath("/tmp/foo.bar.zip"),
            ".bar",
            dataSourceObserver);

        // Checks
        checkArchiveDataSource(dataSource, "", ".bar", dataSourceObserver);

        // Create a datasource
        dataSource = DataSourceUtil.createBaseNameFilteredArchiveDataSource(
            fileSystem.getPath("/tmp/foo.bar.zip"),
            "foo");

        // Checks
        checkArchiveDataSource(dataSource, "foo", "", null);

        // Create a datasource
        dataSource = DataSourceUtil.createBaseNameFilteredArchiveDataSource(
            fileSystem.getPath("/tmp/foo.bar.zip"),
            "foo",
            dataSourceObserver);

        // Checks
        checkArchiveDataSource(dataSource, "foo", "", dataSourceObserver);

        // Create a datasource
        dataSource = DataSourceUtil.createFilteredArchiveDataSource(
            fileSystem.getPath("/tmp/foo.bar.zip"),
            ArchiveFormat.ZIP,
            CompressionFormat.ZIP,
            "foo",
            ".bar",
            dataSourceObserver);

        // Checks
        checkArchiveDataSource(dataSource, "foo", ".bar", dataSourceObserver);

        // Create a datasource
        dataSource = DataSourceUtil.createFilteredArchiveDataSource(
            fileSystem.getPath("/tmp/foo.bar.zip"),
            ArchiveFormat.ZIP,
            CompressionFormat.ZIP,
            "foo",
            ".bar");

        // Checks
        checkArchiveDataSource(dataSource, "foo", ".bar", null);
    }

    private void checkDirectoryDataSource(DataSource dataSource, String baseName, String sourceFormatExtension,
                                          CompressionFormat compressionFormat,
                                          DataSourceObserver dataSourceObserver) {

        // The data source should be an instance of AbstractDataSource
        assertInstanceOf(AbstractDataSource.class, dataSource);

        // Check the datasource values
        assertEquals(fileSystem.getPath("/tmp"), ((AbstractDataSource) dataSource).getDirectory());
        assertEquals(baseName, dataSource.getBaseName());
        assertEquals(sourceFormatExtension, ((AbstractDataSource) dataSource).getSourceFormatExtension());
        if (compressionFormat == null) {
            assertNull(((AbstractDataSource) dataSource).getArchiveFormat());
        } else {
            assertEquals(compressionFormat, ((AbstractDataSource) dataSource).getCompressionFormat());
        }
        assertEquals(compressionFormat, ((AbstractDataSource) dataSource).getCompressionFormat());
        if (dataSourceObserver == null) {
            assertNull(((AbstractDataSource) dataSource).getObserver());
        } else {
            assertEquals(dataSourceObserver, ((AbstractDataSource) dataSource).getObserver());
        }
    }

    @Test
    void testCreateDirectoryDataSource() {
        // Observer
        DataSourceObserver dataSourceObserver = new DefaultDataSourceObserver();

        // Create a datasource
        DataSource dataSource = DataSourceUtil.createDirectoryDataSource(
            fileSystem.getPath("/tmp"),
            "foo");

        // Checks
        checkDirectoryDataSource(dataSource, "foo", "", null, null);

        // Create a datasource
        dataSource = DataSourceUtil.createDirectoryDataSource(
            fileSystem.getPath("/tmp"),
            "foo",
            dataSourceObserver);

        // Checks
        checkDirectoryDataSource(dataSource, "foo", "", null, dataSourceObserver);

        // Create a datasource
        dataSource = DataSourceUtil.createDirectoryDataSource(
            fileSystem.getPath("/tmp"),
            "foo",
            ".bar");

        // Checks
        checkDirectoryDataSource(dataSource, "foo", ".bar", null, null);

        // Create a datasource
        dataSource = DataSourceUtil.createDirectoryDataSource(
            fileSystem.getPath("/tmp"),
            "foo",
            ".bar",
            dataSourceObserver);

        // Checks
        checkDirectoryDataSource(dataSource, "foo", ".bar", null, dataSourceObserver);

        // Create a datasource
        dataSource = DataSourceUtil.createDirectoryDataSource(
            fileSystem.getPath("/tmp"),
            "foo",
            ".bar",
            CompressionFormat.ZIP);

        // Checks
        checkDirectoryDataSource(dataSource, "foo", ".bar", CompressionFormat.ZIP, null);

        // Create a datasource
        dataSource = DataSourceUtil.createDirectoryDataSource(
            fileSystem.getPath("/tmp"),
            "foo",
            ".bar",
            CompressionFormat.ZIP,
            dataSourceObserver);

        // Checks
        checkDirectoryDataSource(dataSource, "foo", ".bar", CompressionFormat.ZIP, dataSourceObserver);
    }

    @Test
    void testCreateDataSource() throws IOException {
        // File
        Path filePath = fileSystem.getPath("/tmp/foo.bar.zip");

        // Case where the file/folder does not exist
        PowsyblException exception = assertThrows(PowsyblException.class,
            () -> DataSourceUtil.createDataSource(filePath));
        assertEquals("Path /tmp/foo.bar.zip should exist to create a data source", exception.getMessage());

        // Create the file
        Files.createFile(filePath);

        // Observer
        DataSourceObserver dataSourceObserver = new DefaultDataSourceObserver();

        // Create a datasource
        DataSource dataSource = DataSourceUtil.createDataSource(filePath);

        // Checks
        checkArchiveDataSource(dataSource, "", "", null);

        // Create a datasource
        dataSource = DataSourceUtil.createDataSource(filePath, dataSourceObserver);

        // Checks
        checkArchiveDataSource(dataSource, "", "", dataSourceObserver);

        // Create a datasource
        dataSource = DataSourceUtil.createDataSource(fileSystem.getPath("/tmp/"), dataSourceObserver);

        // Checks
        checkDirectoryDataSource(dataSource, "tmp", "", null, dataSourceObserver);

    }
}
