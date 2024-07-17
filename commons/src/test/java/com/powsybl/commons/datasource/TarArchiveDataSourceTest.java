/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
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

    @Override
    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
        archiveWithSubfolders = "foo.iidm.tar.gz";
        appendException = "append not supported in tar file data source";
        archiveFormat = ArchiveFormat.TAR;
        compressionFormat = CompressionFormat.GZIP;
    }

    @Test
    @Override
    void testConstructors() {
        // Observer
        DataSourceObserver observer = new DefaultDataSourceObserver();

        // Check constructors
        checkDataSource(new TarArchiveDataSource(testDir, "foo_bar.tar.gz", "foo", "iidm", compressionFormat, observer), "foo_bar.tar.gz", "foo", "iidm", archiveFormat, compressionFormat, observer);
        checkDataSource(new TarArchiveDataSource(testDir, "foo_bar.tar.gz", "foo", "iidm", compressionFormat), "foo_bar.tar.gz", "foo", "iidm", archiveFormat, compressionFormat, null);
        checkDataSource(new TarArchiveDataSource(testDir, "foo", "iidm", compressionFormat, observer), "foo.iidm.tar.gz", "foo", "iidm", archiveFormat, compressionFormat, observer);
        checkDataSource(new TarArchiveDataSource(testDir, "foo", "", compressionFormat, observer), "foo.tar.gz", "foo", "", archiveFormat, compressionFormat, observer);
        checkDataSource(new TarArchiveDataSource(testDir, "foo", "iidm", compressionFormat), "foo.iidm.tar.gz", "foo", "iidm", archiveFormat, compressionFormat, null);
        checkDataSource(new TarArchiveDataSource(testDir, "foo", null, compressionFormat), "foo.tar.gz", "foo", null, archiveFormat, compressionFormat, null);
        checkDataSource(new TarArchiveDataSource(testDir, "foo", "", compressionFormat), "foo.tar.gz", "foo", "", archiveFormat, compressionFormat, null);
        checkDataSource(new TarArchiveDataSource(testDir, "foo", compressionFormat, observer), "foo.tar.gz", "foo", null, archiveFormat, compressionFormat, observer);
        checkDataSource(new TarArchiveDataSource(testDir, "foo", compressionFormat), "foo.tar.gz", "foo", null, archiveFormat, compressionFormat, null);
        checkDataSource(new TarArchiveDataSource(testDir, "foo"), "foo.tar", "foo", null, archiveFormat, null, null);
        checkDataSource(new TarArchiveDataSource(testDir.resolve("foo_bar.tar.gz")), "foo_bar.tar.gz", "foo_bar", "", archiveFormat, compressionFormat, null);
    }

    @Override
    protected boolean appendTest() {
        // append does not work with tar files
        return false;
    }

    @Override
    protected DataSource createDataSource() {
        return new TarArchiveDataSource(testDir, "foo.tar.gz", "foo", null, compressionFormat, null);
    }

    @Override
    protected DataSource createDataSource(DataSourceObserver observer) {
        return new TarArchiveDataSource(testDir, "foo", "iidm", compressionFormat, observer);
    }

    @Override
    protected AbstractArchiveDataSource createArchiveDataSource() {
        return new TarArchiveDataSource(testDir, "foo.bar", "foo", null, compressionFormat, null);
    }

    static Stream<Arguments> provideArgumentsForWriteThenReadTest() {
        return Stream.of(
            Arguments.of("foo", "iidm", CompressionFormat.GZIP),
            Arguments.of("foo", "", CompressionFormat.XZ),
            Arguments.of("foo", "v3", CompressionFormat.ZSTD),
            Arguments.of("foo", "v3", CompressionFormat.BZIP2),
            Arguments.of("foo", "v3", null)
        );
    }

    static Stream<Arguments> provideArgumentsForClassAndListingTest() {
        return Stream.of(
            Arguments.of("foo", "iidm", CompressionFormat.GZIP, TarArchiveDataSource.class,
                Set.of("foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar", "bar.iidm", "bar"),
                Set.of("foo_bar.iidm", "foo_bar", "bar.iidm", "bar")),
            Arguments.of("foo", "", CompressionFormat.BZIP2, TarArchiveDataSource.class,
                Set.of("foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar", "bar.iidm", "bar"),
                Set.of("foo_bar.iidm", "foo_bar", "bar.iidm", "bar")),
            Arguments.of("foo", "v3", CompressionFormat.ZSTD, TarArchiveDataSource.class,
                Set.of("foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar", "bar.iidm", "bar"),
                Set.of("foo_bar.iidm", "foo_bar", "bar.iidm", "bar"))
        );
    }

    @Override
    protected void createArchiveAndFiles(String fileName) throws IOException {

        // File information
        FileInformation fileInformation = new FileInformation(fileName);

        // Create the Tar archive and add the files
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

    @Test
    void fakeTarTest() throws IOException {
        Files.createFile(testDir.resolve("fake.tar"));
        assertFalse(new TarArchiveDataSource(testDir, "fake", null).exists("e"));
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
        DataSource dataSource = DataSourceUtil.createDataSource(workdirPath, TAR_FILENAME, null);

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

    private OutputStream getCompressedOutputStream(OutputStream os, CompressionFormat compressionFormat) throws IOException {
        return compressionFormat == null ? os : switch (compressionFormat) {
            case GZIP -> new GzipCompressorOutputStream(os);
            case BZIP2 -> new BZip2CompressorOutputStream(os);
            case XZ -> new XZCompressorOutputStream(os);
            case ZSTD -> new ZstdCompressorOutputStream(os);
            default -> os;
        };
    }
}
