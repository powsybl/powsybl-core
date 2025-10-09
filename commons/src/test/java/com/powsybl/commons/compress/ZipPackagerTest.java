/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.compress;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
class ZipPackagerTest {

    private FileSystem fileSystem;
    private Path workingDir;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        workingDir = fileSystem.getPath("/wd");
        Path f1 = workingDir.resolve("f1");
        Path f2 = workingDir.resolve("f2");
        Path f3 = workingDir.resolve("f3.gz");
        try {
            Files.createDirectories(workingDir);
            try (BufferedWriter f1Writer = Files.newBufferedWriter(f1, StandardCharsets.UTF_8);
                 BufferedWriter f2Writer = Files.newBufferedWriter(f2, StandardCharsets.UTF_8);
                 OutputStream f3Writer = new GZIPOutputStream(Files.newOutputStream(f3))) {
                f1Writer.write("foo");
                f2Writer.write("bar");
                f3Writer.write("hello".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void test() throws IOException {
        ZipPackager emptyZipPackager = new ZipPackager();
        emptyZipPackager.addPath(null)
                .addPath(workingDir.resolve("missing"));
        assertArrayEquals(emptyZipBytes(), emptyZipPackager.toZipBytes());

        ZipPackager nested = new ZipPackager();
        nested.addString("nestedKey", "nestedValue");
        byte[] nestedZipBytes = nested.toZipBytes();

        ZipPackager zipPackager = new ZipPackager();
        zipPackager.addPaths(workingDir, "f1", "f2", "f3.gz")
                .addString("k1", "v1")
                .addString("k2", "é")
                .addString("k3", "î", StandardCharsets.ISO_8859_1)
                .addBytes("nested.zip", nestedZipBytes);
        byte[] zipBytes = zipPackager.toZipBytes();

        IOUtils.copy(new ByteArrayInputStream(zipBytes), Files.newOutputStream(workingDir.resolve("res.zip")));
        try (ZipFile zipFile = new ZipFile(Files.newByteChannel(workingDir.resolve("res.zip")))) {
            assertEquals("foo", IOUtils.toString(Objects.requireNonNull(zipFile.getInputStream(zipFile.getEntry("f1"))), StandardCharsets.UTF_8));
            assertEquals("bar", IOUtils.toString(Objects.requireNonNull(zipFile.getInputStream(zipFile.getEntry("f2"))), StandardCharsets.UTF_8));
            assertEquals("hello", IOUtils.toString(Objects.requireNonNull(zipFile.getInputStream(zipFile.getEntry("f3"))), StandardCharsets.UTF_8));
            assertEquals("v1", IOUtils.toString(Objects.requireNonNull(zipFile.getInputStream(zipFile.getEntry("k1"))), StandardCharsets.UTF_8));
            assertEquals("é", IOUtils.toString(Objects.requireNonNull(zipFile.getInputStream(zipFile.getEntry("k2"))), StandardCharsets.UTF_8));
            assertEquals("î", IOUtils.toString(Objects.requireNonNull(zipFile.getInputStream(zipFile.getEntry("k3"))), StandardCharsets.ISO_8859_1));

            assertArrayEquals(nestedZipBytes, IOUtils.toByteArray(zipFile.getInputStream(zipFile.getEntry("nested.zip"))));
        }

        ZipPackager zipPackager1 = new ZipPackager();
        try {
            zipPackager1.addString("k1", null);
            fail();
        } catch (NullPointerException e) {
            // ignored
        }

        // static methods
        byte[] bytes = ZipPackager.archiveFilesToZipBytes(workingDir, "f1", "f2", "f3.gz");

        IOUtils.copy(new ByteArrayInputStream(zipBytes), Files.newOutputStream(workingDir.resolve("static.zip")));
        try (ZipFile zipFile2 = new ZipFile(Files.newByteChannel(workingDir.resolve("static.zip")))) {
            assertEquals("foo", IOUtils.toString(Objects.requireNonNull(zipFile2.getInputStream(zipFile2.getEntry("f1"))), StandardCharsets.UTF_8));
            assertEquals("bar", IOUtils.toString(Objects.requireNonNull(zipFile2.getInputStream(zipFile2.getEntry("f2"))), StandardCharsets.UTF_8));
            assertEquals("hello", IOUtils.toString(Objects.requireNonNull(zipFile2.getInputStream(zipFile2.getEntry("f3"))), StandardCharsets.UTF_8));
        }

        HashMap<String, byte[]> stringHashMap = new HashMap<>();
        stringHashMap.put("k1", "foo".getBytes(StandardCharsets.UTF_8));
        stringHashMap.put("k2", "bar".getBytes(StandardCharsets.UTF_8));
        byte[] strMapBytes = ZipPackager.archiveBytesByNameToZipBytes(stringHashMap);
        IOUtils.copy(new ByteArrayInputStream(strMapBytes), Files.newOutputStream(workingDir.resolve("str.zip")));
        try (ZipFile strZipFile = new ZipFile(Files.newByteChannel(workingDir.resolve("str.zip")))) {
            assertEquals("foo", IOUtils.toString(Objects.requireNonNull(strZipFile.getInputStream(strZipFile.getEntry("k1"))), StandardCharsets.UTF_8));
            assertEquals("bar", IOUtils.toString(Objects.requireNonNull(strZipFile.getInputStream(strZipFile.getEntry("k2"))), StandardCharsets.UTF_8));
        }
    }

    private byte[] emptyZipBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        try {
            baos.close();
            zos.close();
        } catch (IOException e) {
            fail();
        }
        return baos.toByteArray();
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }
}
