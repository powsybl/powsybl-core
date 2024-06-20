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
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class TarArchiveDataSourceTest extends AbstractArchiveDataSourceTest {
    private static final String WORK_DIR = "/work/";
    private static final String MAIN_EXT = "xml";
    private static final String BASENAME = "network";
    private static final String MAIN_FILE = BASENAME + "." + MAIN_EXT;
    private static final String TAR_FILENAME = MAIN_FILE + ".tar.gz";
    private static final String TAR_PATH = WORK_DIR + TAR_FILENAME;
    private static final String ADDITIONAL_SUFFIX = "_mapping";
    private static final String ADDITIONAL_EXT = "csv";
    private static final String ADDITIONAL_FILE = BASENAME + ADDITIONAL_SUFFIX + "." + ADDITIONAL_EXT;
    private static final String UNRELATED_FILE = "other.de";

    @BeforeEach
    void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        testDir = fileSystem.getPath("/tmp");
        Files.createDirectories(testDir);
        archiveWithSubfolders = "foo.iidm.tar.gz";
        appendException = "append not supported in tar file data source";
        throwExceptionOnConsistency = true;
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Override
    protected boolean appendTest() {
        return false; // FIXME append does not work with tar files
    }

    @Test
    void fakeTarTest() throws IOException {
        Files.createFile(testDir.resolve("fake.tar"));
        assertFalse(new TarArchiveDataSource(testDir, "fake", null, "", null).exists("e"));
    }

    @Test
    void testTarDataSourceWithMoreThanOneDot() throws IOException {
        // File information
        FileInformation fileInformation = new FileInformation(TAR_PATH);

        // Create the Tar archive
        try (OutputStream fOut = Files.newOutputStream(fileSystem.getPath(TAR_PATH));
             BufferedOutputStream buffOut = new BufferedOutputStream(fOut);
             OutputStream gzOut = getCompressedOutputStream(buffOut, fileInformation.getCompressionFormat());
             TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut)) {
            // First entry
            TarArchiveEntry e = new TarArchiveEntry(UNRELATED_FILE);
            e.setSize(11);
            tOut.putArchiveEntry(e);
            byte[] data = "Test String".getBytes();
            tOut.write(data, 0, data.length);
            tOut.closeArchiveEntry();

            // Another entry
            e = new TarArchiveEntry(MAIN_FILE);
            e.setSize(13);
            tOut.putArchiveEntry(e);
            data = "Test String 2".getBytes();
            tOut.write(data, 0, data.length);
            tOut.closeArchiveEntry();

            // A third one
            e = new TarArchiveEntry(ADDITIONAL_FILE);
            e.setSize(13);
            tOut.putArchiveEntry(e);
            data = "Test String 2".getBytes();
            tOut.write(data, 0, data.length);
            tOut.closeArchiveEntry();
        }

        // Create the datasource
        var workdirPath = fileSystem.getPath(WORK_DIR);
        DataSource dataSource = DataSourceUtil.createBaseNameFilteredArchiveDataSource(workdirPath.resolve(TAR_FILENAME), BASENAME);

        // Assertions on the files in the archive
        assertTrue(dataSource.exists(UNRELATED_FILE));
        assertFalse(dataSource.exists("not.tar.gz"));
        assertTrue(dataSource.exists(null, MAIN_EXT));
        assertTrue(dataSource.exists(ADDITIONAL_SUFFIX, ADDITIONAL_EXT));
        assertFalse(dataSource.exists("-not", "there"));
        try (InputStream is = dataSource.newInputStream(UNRELATED_FILE)) {
            assertEquals("Test String", new String(is.readAllBytes()));
        }
    }

    static Stream<Arguments> provideArgumentsForClassAndListingTest() {
        return Stream.of(
            Arguments.of("foo", ".iidm", ArchiveFormat.TAR, CompressionFormat.GZIP, TarArchiveDataSource.class,
                Set.of("foo.iidm", "foo_bar.iidm", "foo.v3.iidm"),
                Set.of("foo_bar.iidm")),
            Arguments.of("foo", "", ArchiveFormat.TAR, CompressionFormat.BZIP2, TarArchiveDataSource.class,
                Set.of("foo.txt", "foo.iidm", "foo.xiidm", "foo", "foo_bar.iidm", "foo.v3.iidm", "foo.v3", "foo_bar"),
                Set.of("foo_bar.iidm", "foo_bar")),
            Arguments.of("foo", ".v3", ArchiveFormat.TAR, CompressionFormat.ZSTD, TarArchiveDataSource.class,
                Set.of("foo.v3"),
                Set.of())
        );
    }

    protected String getFileName(String baseName, String sourceFormat, ArchiveFormat archiveFormat,
                                 CompressionFormat compressionFormat) {
        return testDir + "/" + baseName + sourceFormat
            + (archiveFormat == null ? "" : "." + archiveFormat.getExtension())
            + (compressionFormat == null ? "" : "." + compressionFormat.getExtension());
    }

    private OutputStream getCompressedOutputStream(OutputStream os, CompressionFormat compressionFormat) throws IOException {
        return switch (compressionFormat) {
            case GZIP -> new GzipCompressorOutputStream(os);
            case BZIP2 -> new BZip2CompressorOutputStream(os);
            case XZ -> new XZCompressorOutputStream(os);
            case ZSTD -> new ZstdCompressorOutputStream(os);
            default -> os;
        };
    }

    protected void createArchiveAndFiles(String fileName) throws IOException {

        // File information
        FileInformation fileInformation = new FileInformation(fileName);

        // Create the Zip archive and add the files
        try (OutputStream fOut = Files.newOutputStream(fileSystem.getPath(fileName));
             BufferedOutputStream buffOut = new BufferedOutputStream(fOut);
             OutputStream gzOut = getCompressedOutputStream(buffOut, fileInformation.getCompressionFormat());
             TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut)) {
            filesInArchive.forEach(fileInArchive -> {
                try {
                    TarArchiveEntry e = new TarArchiveEntry(fileInArchive);
                    e.setSize(11);
                    tOut.putArchiveEntry(e);
                    byte[] data = "Test String".getBytes();
                    tOut.write(data, 0, data.length);
                    tOut.closeArchiveEntry();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
            tOut.finish();
        }
    }

    private static Stream<Arguments> provideArgumentsForWriteThenReadTest() {
        return Stream.of(
            Arguments.of("foo", ".iidm", ArchiveFormat.TAR, CompressionFormat.GZIP),
            Arguments.of("foo", "", ArchiveFormat.TAR, CompressionFormat.XZ),
            Arguments.of("foo", ".v3", ArchiveFormat.TAR, CompressionFormat.ZSTD)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForWriteThenReadTest")
    void writeThenReadTest(String baseName, String sourceFormat, ArchiveFormat archiveFormat,
                           CompressionFormat compressionFormat) throws IOException {
        // Compute the full filename
        String fileName = getFileName(baseName, sourceFormat, archiveFormat, compressionFormat);

        // Create the Tar archive and add the files
        createArchiveAndFiles(fileName);

        // Create the datasource
        DataSource dataSource = DataSourceUtil.createBaseNameFilteredArchiveDataSource(fileSystem.getPath(fileName), baseName);

        writeThenReadTest(dataSource);
    }

    @Test
    void testErrorOnInputStreamForMissingFile() throws IOException {
        // File
        Path file = testDir.resolve("fake.tar");
        Files.createFile(file);

        // Create the datasource
        DataSource dataSource = DataSourceUtil.createBaseNameFilteredArchiveDataSource(
            file,
            "foo");

        PowsyblException exception = assertThrows(PowsyblException.class, () -> {
            try (InputStream ignored = dataSource.newInputStream("foo.bar")) {
                fail();
            }
        });
        assertEquals("File foo.bar does not seem to exist in archive fake.tar", exception.getMessage());

        file = testDir.resolve("/missing.file.tar.gz");
        DataSource newDataSource = DataSourceUtil.createBaseNameFilteredArchiveDataSource(
            file,
            "foo");
        exception = assertThrows(PowsyblException.class, () -> {
            try (InputStream ignored = newDataSource.newInputStream("foo.bar")) {
                fail();
            }
        });
        assertEquals("Tar file missing.file.tar.gz does not seem to exist", exception.getMessage());
    }
}
