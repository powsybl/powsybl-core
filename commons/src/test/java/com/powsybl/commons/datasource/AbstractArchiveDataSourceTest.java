/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
abstract class AbstractArchiveDataSourceTest extends AbstractFileSystemDataSourceTest {
    protected final Set<String> filesInArchive = Set.of(
        "foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar", "bar.iidm", "bar");
    protected Set<String> unlistedFiles;
    protected String archiveWithSubfolders;
    protected String appendException;

    static Stream<Arguments> provideArgumentsForClassAndListingTest() {
        return null;
    }

    protected abstract String getFileName(String baseName, String mainExtension, ArchiveFormat archiveFormat,
                                          CompressionFormat compressionFormat);

    protected abstract void createArchiveAndFiles(String fileName) throws IOException;

    @ParameterizedTest
    @MethodSource("provideArgumentsForClassAndListingTest")
    void testClassAndListing(String baseName, String mainExtension, ArchiveFormat archiveFormat,
                             CompressionFormat compressionFormat, Class<? extends AbstractFileSystemDataSource> dataSourceClass,
                             Set<String> listedFiles, Set<String> listedBarFiles) throws IOException {
        // Compute the full filename
        String fileName = getFileName(baseName, mainExtension, archiveFormat, compressionFormat);

        // Create the Zip archive and add the files
        createArchiveAndFiles(fileName);

        // Update the list of unlisted files
        unlistedFiles = filesInArchive.stream().filter(name -> !listedFiles.contains(name)).collect(Collectors.toSet());

        // Create the datasource
        DataSource dataSource = DataSourceUtil.createFilteredArchiveDataSource(testDir.resolve(fileName), baseName, mainExtension);

        // Check the class
        assertInstanceOf(dataSourceClass, dataSource);

        // List all the files in the datasource
        assertEquals(listedFiles, dataSource.listNames(".*"));
        assertEquals(listedBarFiles, dataSource.listNames(".*bar.*"));
    }

    @Test
    void testFileInSubfolder() throws IOException {
        // File
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(archiveWithSubfolders)).getFile());
        Path path = file.toPath();

        // Create the datasource
        DataSource dataSource = DataSourceUtil.createFilteredArchiveDataSource(
            path,
            "foo",
            "iidm");

        // Assertions
        Set<String> files = dataSource.listNames(".*");
        assertEquals(2, files.size());
        assertTrue(files.contains("foo.iidm"));
        assertTrue(files.contains("foo_bar.iidm"));
        assertFalse(files.contains("foo_baz.iidm"));
        assertFalse(files.contains("subfolder/foo_baz.iidm"));
    }

    @Test
    void testErrorOnAppend() throws IOException {
        // File
        Path file = testDir.resolve(archiveWithSubfolders);
        Files.createFile(file);

        // Create the datasource
        DataSource dataSource = DataSourceUtil.createFilteredArchiveDataSource(
            file,
            "foo",
            "bar");

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
            try (OutputStream ignored = dataSource.newOutputStream("foo.bar", true)) {
                fail();
            }
        });
        assertEquals(appendException, exception.getMessage());
    }

    @Test
    void testConsistencyWithDataSource() {
        // File
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(archiveWithSubfolders)).getFile());
        Path path = file.toPath();

        // Create the datasource
        DataSource dataSource = DataSourceUtil.createFilteredArchiveDataSource(
            path,
            "foo",
            "iidm");

        // Test the consistency
        if (dataSource instanceof AbstractArchiveDataSource archiveDataSource) {
            assertFalse(archiveDataSource.isConsistentWithDataSource("bar.test"));
            assertFalse(archiveDataSource.isConsistentWithDataSource("foo"));
            assertFalse(archiveDataSource.isConsistentWithDataSource("foo.bar"));
            assertTrue(archiveDataSource.isConsistentWithDataSource("foo.iidm"));
        } else {
            fail();
        }

        // Create the datasource
        DataSource dataSourceNoExtension = DataSourceUtil.createFilteredArchiveDataSource(
            path,
            "foo",
            "");

        // Test the consistency
        if (dataSourceNoExtension instanceof AbstractArchiveDataSource archiveDataSource) {
            assertFalse(archiveDataSource.isConsistentWithDataSource("bar.test"));
            assertTrue(archiveDataSource.isConsistentWithDataSource("foo.bar"));
            assertTrue(archiveDataSource.isConsistentWithDataSource("foo"));
        } else {
            fail();
        }
    }
}
