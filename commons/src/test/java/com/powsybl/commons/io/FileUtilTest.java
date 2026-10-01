/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.io;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilTest {

    FileSystem fsFoo;
    FileSystem fsBar;

    @BeforeEach
    void setUp() {
        fsFoo = Jimfs.newFileSystem(Configuration.unix());
        fsBar = Jimfs.newFileSystem(Configuration.unix());
    }

    @Test
    void testCopyDir() throws IOException {
        Path initPaths = fsFoo.getPath("/tmp/a/b/c");
        Files.createDirectories(initPaths);

        Path dest = fsFoo.getPath("/dest/a");
        Files.createDirectories(dest);
        Path remoteDest = fsBar.getPath("/dest/a");
        Files.createDirectories(remoteDest);

        Path source = initPaths.getParent().getParent(); // /tmp/a
        FileUtil.copyDir(source, dest);
        FileUtil.copyDir(source, remoteDest);

        assertTrue(Files.exists(fsFoo.getPath("/dest/a/b/c")));
        assertTrue(Files.exists(fsBar.getPath("/dest/a/b/c")));
    }

    @Test
    void testUnzip() throws Exception {
        Path workingDir = fsFoo.getPath("/tmp/workingDir");
        Files.createDirectories(workingDir);

        // create a zip file
        Path zipPath = workingDir.resolve("archive.zip");
        try (ZipOutputStream os = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            os.putNextEntry(new ZipEntry("validFile1"));
            os.putNextEntry(new ZipEntry("validFile2"));
            os.closeEntry();
        }
        FileUtil.unzipArchive(workingDir, zipPath);
        assertTrue(Files.exists(workingDir));
        assertTrue(Files.exists(workingDir.resolve("validFile1")));
        assertTrue(Files.exists(workingDir.resolve("validFile2")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"../invalidFile", "/invalidFile"})
    void testUnzipNoZipSlipVulnerability(String invalidFile) throws Exception {
        Path workingDir = fsFoo.getPath("/tmp/workingDir");
        Files.createDirectories(workingDir);

        // create a malicious zip file
        Path maliciousZipPath = workingDir.resolve("malicious.zip");
        try (ZipOutputStream os = new ZipOutputStream(Files.newOutputStream(maliciousZipPath))) {
            os.putNextEntry(new ZipEntry("validFile1"));
            os.putNextEntry(new ZipEntry(invalidFile));
            os.putNextEntry(new ZipEntry("validFile2"));
            os.closeEntry();
        }

        IOException exception = assertThrows(IOException.class, () -> FileUtil.unzipArchive(workingDir, maliciousZipPath));
        assertEquals("Archive entry '" + invalidFile + "' would extract outside of the working directory", exception.getMessage());

        assertTrue(Files.exists(workingDir));
        // Check that no file is extracted when an invalid file is present
        assertFalse(Files.exists(workingDir.resolve("validFile1")));
        assertFalse(Files.exists(workingDir.resolve(invalidFile)));
        assertFalse(Files.exists(workingDir.resolve("validFile2")));
    }
}
