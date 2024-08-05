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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
abstract class AbstractArchiveDataSourceTest extends AbstractFileSystemDataSourceTest {
    protected final Set<String> filesInArchive = Set.of(
        "foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar", "bar.iidm", "bar");
    protected String archiveWithSubfolders;
    protected String appendException;
    protected ArchiveFormat archiveFormat;

    @BeforeEach
    void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        testDir = fileSystem.getPath("/tmp");
        Files.createDirectories(testDir);
        existingFiles = Set.of(
            "foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar", "bar.iidm", "bar");
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    protected void checkDataSource(AbstractArchiveDataSource dataSource, String zipFileName, String baseName,
                                   String dataExtension, ArchiveFormat archiveFormat,
                                   CompressionFormat compressionFormat, DataSourceObserver observer) {
        assertEquals(testDir, dataSource.getDirectory());
        assertEquals(zipFileName, dataSource.getArchiveFilePath().getFileName().toString());
        assertEquals(baseName, dataSource.getBaseName());
        assertEquals(dataExtension, dataSource.getDataExtension());
        assertEquals(archiveFormat, dataSource.getArchiveFormat());
        assertEquals(compressionFormat, dataSource.getCompressionFormat());
        assertEquals(observer, dataSource.getObserver());
    }

    @Test
    void testFileInSubfolder() throws IOException {
        // File
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(archiveWithSubfolders)).getFile());
        Path path = file.toPath();

        // Create the datasource
        DataSource dataSource = DataSource.fromPath(path);

        // All the files are listed, no filter is applied
        Set<String> files = dataSource.listNames(".*");
        assertEquals(4, files.size());
        assertTrue(files.contains("foo.iidm"));
        assertTrue(files.contains("foo_bar.iidm"));
        assertFalse(files.contains("foo_baz.iidm"));
        assertTrue(files.contains("subfolder/foo_baz.iidm"));
        assertTrue(files.contains("subfolder/subsubfolder/foo.v3.iidm"));
    }

    @Test
    void testStreams() throws IOException {
        // Create file
        createFiles(archiveWithSubfolders);
        Path path = fileSystem.getPath(archiveWithSubfolders);

        // Datasource with an observer
        DataSourceObserver observer = new DefaultDataSourceObserver();
        DataSource dataSource = DataSourceUtil.createDataSource(fileSystem.getPath("."), archiveWithSubfolders, observer);
        assertInstanceOf(ObservableInputStream.class, dataSource.newInputStream("foo.iidm"));
        assertInstanceOf(ObservableOutputStream.class, dataSource.newOutputStream("test.iidm", false));

        // Datasource without an observer
        dataSource = DataSource.fromPath(path);
        try (InputStream inputStream = dataSource.newInputStream("foo.iidm");
             OutputStream outputStream = dataSource.newOutputStream("test.iidm", false)) {
            assertFalse(inputStream instanceof ObservableInputStream);
            assertFalse(outputStream instanceof ObservableOutputStream);
        }
    }

    @Test
    void testErrorOnAppend() throws IOException {
        // File
        Path path = testDir.resolve(archiveWithSubfolders);
        Files.createFile(path);

        // Create the datasource
        DataSource dataSource = DataSource.fromPath(path);

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
            try (OutputStream ignored = dataSource.newOutputStream("foo.bar", true)) {
                fail();
            }
        });
        assertEquals(appendException, exception.getMessage());
    }

    protected abstract AbstractArchiveDataSource createArchiveDataSource();

    @Test
    void testMissingArchive() throws IOException {
        AbstractArchiveDataSource dataSource = createArchiveDataSource();
        assertFalse(dataSource.exists("test.bar"));
        assertNull(dataSource.newInputStream("test.bar"));
    }

    @Test
    void testWrongTypeOfFile() throws IOException {
        Files.createFile(testDir.resolve("foo.bar"));
        AbstractArchiveDataSource dataSource = createArchiveDataSource();
        assertFalse(dataSource.exists("test.bar"));
    }
}
