/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class ZipArchiveDataSourceTest extends AbstractArchiveDataSourceTest {

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

    @Override
    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
        archiveWithSubfolders = "foo.iidm.zip";
        appendException = "append not supported in zip file data source";
        archiveFormat = ArchiveFormat.ZIP;
        compressionFormat = CompressionFormat.ZIP;
    }

    @Test
    @Override
    void testConstructors() {
        // Observer
        DataSourceObserver observer = new DefaultDataSourceObserver();

        // Check constructors
        checkDataSource(new ZipArchiveDataSource(testDir, "foo_bar.zip", "foo", "iidm", observer), "foo_bar.zip", "foo", "iidm", observer);
        checkDataSource(new ZipArchiveDataSource(testDir, "foo_bar.zip", "foo", "iidm"), "foo_bar.zip", "foo", "iidm", null);
        checkDataSource(new ZipArchiveDataSource(testDir, "foo", "iidm", observer), "foo.iidm.zip", "foo", "iidm", observer);
        checkDataSource(new ZipArchiveDataSource(testDir, "foo", "", observer), "foo.zip", "foo", "", observer);
        checkDataSource(new ZipArchiveDataSource(testDir, "foo", "iidm"), "foo.iidm.zip", "foo", "iidm", null);
        checkDataSource(new ZipArchiveDataSource(testDir, "foo", (String) null), "foo.zip", "foo", null, null);
        checkDataSource(new ZipArchiveDataSource(testDir, "foo", ""), "foo.zip", "foo", "", null);
        checkDataSource(new ZipArchiveDataSource(testDir, "foo", observer), "foo.zip", "foo", null, observer);
        checkDataSource(new ZipArchiveDataSource(testDir, "foo"), "foo.zip", "foo", null, null);
        checkDataSource(new ZipArchiveDataSource(testDir.resolve("foo_bar.zip")), "foo_bar.zip", "foo_bar", null, null);
    }

    private void checkDataSource(ZipArchiveDataSource dataSource, String zipFileName, String baseName, String mainExtension, DataSourceObserver observer) {
        assertEquals(testDir, dataSource.getDirectory());
        assertEquals(mainExtension, dataSource.getMainExtension());
        assertEquals(zipFileName, dataSource.getArchiveFilePath().getFileName().toString());
        assertEquals(baseName, dataSource.getBaseName());
        assertEquals(observer, dataSource.getObserver());
    }

    @Override
    protected boolean appendTest() {
        return false;
    }

    @Override
    protected DataSource createDataSource() {
        return new ZipArchiveDataSource(testDir, "foo.zip", "foo", null, null);
    }

    @Override
    protected DataSource createDataSource(DataSourceObserver observer) {
        return new ZipArchiveDataSource(testDir, "foo", "iidm", observer);
    }

    static Stream<Arguments> provideArgumentsForWriteThenReadTest() {
        return Stream.of(
            Arguments.of("foo", "iidm", CompressionFormat.ZIP),
            Arguments.of("foo", "", CompressionFormat.ZIP),
            Arguments.of("foo", "v3", CompressionFormat.ZIP)
        );
    }

    // Currently, the files are not filtered in the zip archive
    static Stream<Arguments> provideArgumentsForClassAndListingTest() {
        Set<String> listedFiles = Set.of("foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar", "bar.iidm", "bar");
        Set<String> listedBarFiles = Set.of("foo_bar.iidm", "foo_bar", "bar.iidm", "bar");
        return Stream.of(
            Arguments.of("foo", "iidm", CompressionFormat.ZIP, ZipArchiveDataSource.class,
                listedFiles,
                listedBarFiles),
            Arguments.of("foo", "", CompressionFormat.ZIP, ZipArchiveDataSource.class,
                listedFiles,
                listedBarFiles),
            Arguments.of("foo", "v3", CompressionFormat.ZIP, ZipArchiveDataSource.class,
                listedFiles,
                listedBarFiles)
        );
    }

    @Override
    protected void createFiles(String fileName) throws IOException {
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

    @Test
    void fakeZipTest() throws IOException {
        Files.createFile(testDir.resolve("fake.zip"));
        assertFalse(new ZipArchiveDataSource(testDir, "fake").exists("e"));
    }

    @Test
    void createZipDataSourceWithMoreThanOneDot() throws IOException {
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(fileSystem.getPath(ZIP_PATH)))) {
            // create an entry
            ZipEntry e = new ZipEntry(UNRELATED_FILE);
            out.putNextEntry(e);
            byte[] data = "Test String".getBytes();
            out.write(data, 0, data.length);

            e = new ZipEntry(MAIN_FILE);
            out.putNextEntry(e);
            data = "Test String 2".getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();

            e = new ZipEntry(ADDITIONAL_FILE);
            out.putNextEntry(e);
            data = "Test String 2".getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();

        }
        var workdirPath = fileSystem.getPath(WORK_DIR);
        DataSource dataSource = DataSourceUtil.createDataSource(workdirPath, ZIP_FILENAME, null);
        assertTrue(dataSource.exists(UNRELATED_FILE));
        assertFalse(dataSource.exists("not.zip"));
        assertTrue(dataSource.exists(null, MAIN_EXT));
        assertTrue(dataSource.exists(ADDITIONAL_SUFFIX, ADDITIONAL_EXT));
        assertFalse(dataSource.exists("-not", "there"));
        try (InputStream is = dataSource.newInputStream(UNRELATED_FILE)) {
            assertEquals("Test String", new String(is.readAllBytes()));
        }
    }

}
