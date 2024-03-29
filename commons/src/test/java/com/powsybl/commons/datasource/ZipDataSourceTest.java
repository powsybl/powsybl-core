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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class ZipDataSourceTest extends AbstractArchiveDataSourceTest {
    private static final String WORK_DIR = "/work/";
    private static final String MAIN_EXT = "xml";
    private static final String BASENAME = "network";
    private static final String MAIN_FILE = BASENAME + "." + MAIN_EXT;
    private static final String ZIP_FILENAME = MAIN_FILE + ".zip";
    private static final String ZIP_PATH = WORK_DIR + ZIP_FILENAME;
    private static final String ADDITIONAL_SUFFIX = "_mapping";
    private static final String ADDITIONAL_EXT = "csv";
    private static final String ADDITIONAL_FILE = BASENAME + ADDITIONAL_SUFFIX + "." + ADDITIONAL_EXT;
    private static final String UNRELATED_FILE = "other.de";

    @BeforeEach
    void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        testDir = fileSystem.getPath("/tmp");
        Files.createDirectories(testDir);
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Override
    protected boolean appendTest() {
        return false; // FIXME append does not work with zip files compression
    }

    @Test
    void fakeZipTest() throws IOException {
        Files.createFile(testDir.resolve("fake.zip"));
        assertFalse(new ZipDataSource(testDir, "fake", "", null).exists("e"));
    }

    @Test
    void createZipDataSourceWithMoreThanOneDot() throws IOException {
        // Create the zip file
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(fileSystem.getPath(ZIP_PATH)))) {
            // create an entry
            ZipEntry e = new ZipEntry(UNRELATED_FILE);
            out.putNextEntry(e);
            byte[] data = "Test String".getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();

            // Another entry
            e = new ZipEntry(MAIN_FILE);
            out.putNextEntry(e);
            data = "Test String 2".getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();

            // A third one
            e = new ZipEntry(ADDITIONAL_FILE);
            out.putNextEntry(e);
            data = "Test String 2".getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
        }

        // Create the datasource
        var workdirPath = fileSystem.getPath(WORK_DIR);
        DataSource dataSource = DataSourceUtil.createDataSource(workdirPath, ZIP_FILENAME, BASENAME);

        // Assertions on the files in the archive
        assertTrue(dataSource.exists(UNRELATED_FILE));
        assertFalse(dataSource.exists("not.zip"));
        assertTrue(dataSource.exists(null, MAIN_EXT));
        assertTrue(dataSource.exists(ADDITIONAL_SUFFIX, ADDITIONAL_EXT));
        assertFalse(dataSource.exists("-not", "there"));
        try (InputStream is = dataSource.newInputStream(UNRELATED_FILE)) {
            assertEquals("Test String", new String(is.readAllBytes()));
        }
    }

    static Stream<Arguments> provideArgumentsForClassAndListingTest() {
        return Stream.of(
            Arguments.of("foo", ".iidm", ArchiveFormat.ZIP, CompressionFormat.ZIP, ZipDataSource.class,
                Set.of("foo.iidm", "foo_bar.iidm", "foo.v3.iidm"),
                Set.of("foo_bar.iidm")),
            Arguments.of("foo", "", ArchiveFormat.ZIP, CompressionFormat.ZIP, ZipDataSource.class,
                Set.of("foo.txt", "foo.iidm", "foo.xiidm", "foo", "foo_bar.iidm", "foo.v3.iidm", "foo.v3", "foo_bar"),
                Set.of("foo_bar.iidm", "foo_bar")),
            Arguments.of("foo", ".v3", ArchiveFormat.ZIP, CompressionFormat.ZIP, ZipDataSource.class,
                Set.of("foo.v3"),
                Set.of())
        );
    }

    protected String getFileName(String baseName, String sourceFormat, ArchiveFormat archiveFormat,
                                 CompressionFormat compressionFormat) {
        return testDir + "/" + baseName + sourceFormat
            + (compressionFormat == null ? "" : "." + compressionFormat.getExtension());
    }

    protected void createArchiveAndFiles(String fileName) throws IOException {
        // Create the Zip archive and add the files
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(fileSystem.getPath(fileName)))) {
            filesInArchive.forEach(fileInArchive -> {
                try {
                    ZipEntry e = new ZipEntry(fileInArchive);
                    out.putNextEntry(e);
                    byte[] data = "Test String".getBytes();
                    out.write(data, 0, data.length);
                    out.closeEntry();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    private static Stream<Arguments> provideArgumentsForWriteThenReadTest() {
        return Stream.of(
            Arguments.of("foo", ".iidm", null, CompressionFormat.ZIP),
            Arguments.of("foo", "", null, CompressionFormat.ZIP),
            Arguments.of("foo", ".v3", null, CompressionFormat.ZIP)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForWriteThenReadTest")
    void writeThenReadTest(String baseName, String sourceFormat, ArchiveFormat archiveFormat,
                           CompressionFormat compressionFormat) throws IOException {
        // Compute the full filename
        String fileName = getFileName(baseName, sourceFormat, archiveFormat, compressionFormat);

        // Create the Zip archive and add the files
        createArchiveAndFiles(fileName);

        // Create the datasource
        DataSource dataSource = DataSource.fromPath(fileSystem.getPath(fileName));

        writeThenReadTest(dataSource);
    }
}
