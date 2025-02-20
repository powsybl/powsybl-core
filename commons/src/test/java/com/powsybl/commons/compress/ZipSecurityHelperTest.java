/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.compress;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.datasource.DirectoryDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;

/**
 * @author Giovanni Ferrari {@literal <giovanni.ferrari at soft.it>}
 */
class ZipSecurityHelperTest {
    private FileSystem fileSystem;
    private Path workingDir;
    ReadOnlyDataSource dataSource;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        workingDir = fileSystem.getPath("/wd");
        try {
            Files.createDirectories(workingDir);
            Path zipFile = workingDir.resolve("test.zip");
            try (ZipOutputStream zipWriter = new ZipOutputStream(Files.newOutputStream(zipFile))) {
                zipWriter.putNextEntry(new ZipEntry("foo"));
                zipWriter.write("foo".getBytes(StandardCharsets.UTF_8));
                zipWriter.closeEntry();
                zipWriter.putNextEntry(new ZipEntry("bar"));
                zipWriter.write("bar".getBytes(StandardCharsets.UTF_8));
                zipWriter.closeEntry();
                zipWriter.putNextEntry(new ZipEntry("hello"));
                zipWriter.write("helloworksjshsiejskwiodndycv9emimcyejeiwniojiifornfioeriroefioriormiormfiormfomormcinrujaaaaaaaaaaaaaaaaaaaaa".getBytes(StandardCharsets.UTF_8));
                zipWriter.closeEntry();
            }
            dataSource = new DirectoryDataSource(workingDir, "");
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void testIsZipFileSafe() throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(dataSource.newInputStream("test.zip"))) {
            assertTrue(ZipSecurityHelper.isZipFileSafe(zipInputStream));
        }
    }

    @Test
    void testZipFileEntries() throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(dataSource.newInputStream("test.zip"))) {
            boolean result = ZipSecurityHelper.isZipFileSafe(zipInputStream, 2, ZipSecurityHelper.THRESHOLD_SIZE, ZipSecurityHelper.THRESHOLD_RATIO);
            assertFalse(result);
        }
    }

    @Test
    void testZipFileSize() throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(dataSource.newInputStream("test.zip"))) {
            assertFalse(ZipSecurityHelper.isZipFileSafe(zipInputStream, ZipSecurityHelper.THRESHOLD_ENTRIES, 1, ZipSecurityHelper.THRESHOLD_RATIO));
        }
    }

    @Test
    void testZipFileCompressRatio() throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(dataSource.newInputStream("test.zip"))) {
            assertFalse(ZipSecurityHelper.isZipFileSafe(zipInputStream, ZipSecurityHelper.THRESHOLD_ENTRIES, ZipSecurityHelper.THRESHOLD_SIZE, 0.1d));

        }
    }

    @Test
    void testCheckIfZipExtractionIsSafe() {
        ZipSecurityHelper.checkIfZipExtractionIsSafe(dataSource, "test.zip");
    }

    @Test
    void testCheckIfZipExtractionIsNotSafe() {
        String message = assertThrows(UncheckedIOException.class,
                () -> ZipSecurityHelper.checkIfZipExtractionIsSafe(dataSource, "test.zip", 2, ZipSecurityHelper.THRESHOLD_SIZE, ZipSecurityHelper.THRESHOLD_RATIO))
                .getMessage();
        Assertions.assertEquals("Zip file extraction is not safe", message);
    }

}
