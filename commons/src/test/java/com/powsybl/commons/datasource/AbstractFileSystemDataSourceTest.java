/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
abstract class AbstractFileSystemDataSourceTest {
    protected FileSystem fileSystem;
    protected Path testDir;
    protected Set<String> unlistedFiles;
    protected Set<String> existingFiles;
    protected ArchiveFormat archiveFormat;
    protected CompressionFormat compressionFormat;

    @Test
    abstract void testConstructors();

    protected boolean appendTest() {
        return true;
    }

    protected abstract DataSource createDataSource();

    protected abstract DataSource createDataSource(DataSourceObserver observer);

    protected String getFileName(String baseName, String mainExtension, ArchiveFormat archiveFormat,
                                 CompressionFormat compressionFormat) {
        return testDir + "/" + baseName + mainExtension
            + (archiveFormat == null ? "" : "." + archiveFormat.getExtension())
            + (compressionFormat == null ? "" : "." + compressionFormat.getExtension());
    }

    protected void createArchiveAndFiles(String fileName) throws IOException {
        // Do nothing
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForWriteThenReadTest")
    void writeThenReadTest(String baseName, String mainExtension, CompressionFormat compressionFormat) throws IOException {
        // Compute the full filename
        String fileName = getFileName(baseName, mainExtension, archiveFormat, compressionFormat);

        // Create an archive when needed
        if (archiveFormat != null) {
            createArchiveAndFiles(fileName);
        }

        // Create the datasource
        DataSource dataSource = DataSource.fromPath(fileSystem.getPath(fileName));

        writeThenReadTest(dataSource);
    }

    void writeThenReadTest(DataSource dataSource) throws IOException {
        writeThenReadTest(dataSource, null, "bar");
        writeThenReadTest(dataSource, "_baz", "bar");
        writeThenReadTest(dataSource, "_baz", null);
    }

    private void writeThenReadTest(DataSource dataSource, String suffix, String ext) throws IOException {
        // check file does not exist
        assertFalse(dataSource.exists(suffix, ext));

        // write file
        try (OutputStream os = dataSource.newOutputStream(suffix, ext, false)) {
            os.write("line1".getBytes(StandardCharsets.UTF_8));
        }
        if (appendTest()) {
            // write file in append mode
            try (OutputStream os = dataSource.newOutputStream(suffix, ext, true)) {
                os.write((System.lineSeparator() + "line2").getBytes(StandardCharsets.UTF_8));
            }
        }

        // write another file
        try (OutputStream os = dataSource.newOutputStream("dummy.txt", false)) {
            os.write("otherline1".getBytes(StandardCharsets.UTF_8));
        }

        // check files exists
        assertTrue(dataSource.exists(suffix, ext));
        assertTrue(dataSource.exists("dummy.txt"));

        // check content exists and is ok
        try (InputStream is = dataSource.newInputStream(suffix, ext)) {
            assertEquals("line1" + (appendTest() ? System.lineSeparator() + "line2" : ""),
                new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        } catch (IOException x) {
            fail();
        }
        try (InputStream is = dataSource.newInputStream("dummy.txt")) {
            assertEquals("otherline1", new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        } catch (IOException x) {
            fail();
        }
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForClassAndListingTest")
    void testClassAndListing(String baseName, String mainExtension,
                             CompressionFormat compressionFormat, Class<? extends AbstractFileSystemDataSource> dataSourceClass,
                             Set<String> listedFiles, Set<String> listedBarFiles) throws IOException {
        // Compute the full filename
        String fileName = getFileName(baseName, mainExtension, archiveFormat, compressionFormat);

        // Update the list of unlisted files
        unlistedFiles = existingFiles.stream().filter(name -> !listedFiles.contains(name)).collect(Collectors.toSet());

        // Create archive when needed
        if (archiveFormat != null) {
            createArchiveAndFiles(fileName);
        }

        // Create the datasource
        DataSource dataSource = DataSource.fromPath(fileSystem.getPath(fileName));

        // Check the class
        assertInstanceOf(dataSourceClass, dataSource);

        // List all the files in the datasource
        assertEquals(listedFiles, dataSource.listNames(".*"));
        assertEquals(listedBarFiles, dataSource.listNames(".*bar.*"));
    }

    @Test
    void testGetters() {
        // Observer
        DataSourceObserver observer = new DefaultDataSourceObserver();

        // Create the datasource
        DataSource dataSourceWithObserver = createDataSource(observer);

        // Checks
        assertInstanceOf(AbstractFileSystemDataSource.class, dataSourceWithObserver);
        assertEquals(testDir, ((AbstractFileSystemDataSource) dataSourceWithObserver).getDirectory());
        assertEquals("foo", dataSourceWithObserver.getBaseName());
        assertEquals(".iidm", ((AbstractFileSystemDataSource) dataSourceWithObserver).getMainExtension());
        assertEquals(compressionFormat, ((AbstractFileSystemDataSource) dataSourceWithObserver).getCompressionFormat());
        assertEquals(observer, ((AbstractFileSystemDataSource) dataSourceWithObserver).getObserver());

        // Create the datasource
        DataSource dataSourceWithoutObserver = createDataSource();

        // Checks
        assertInstanceOf(AbstractFileSystemDataSource.class, dataSourceWithoutObserver);
        assertEquals(testDir, ((AbstractFileSystemDataSource) dataSourceWithoutObserver).getDirectory());
        assertEquals("foo", dataSourceWithoutObserver.getBaseName());
        assertNull(((AbstractFileSystemDataSource) dataSourceWithoutObserver).getMainExtension());
        assertEquals(compressionFormat, ((AbstractFileSystemDataSource) dataSourceWithoutObserver).getCompressionFormat());
        assertNull(((AbstractFileSystemDataSource) dataSourceWithoutObserver).getObserver());
    }
}
