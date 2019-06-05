/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.compress;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.java.truevfs.comp.zip.ZipFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.Strings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.*;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ZipPackagerTest {

    private FileSystem fileSystem;
    private Path workingDir;

    @Before
    public void setUp() {
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
                f3Writer.write(Strings.toByteArray("hello"));
            }
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void test() throws IOException {
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
        ZipFile zipFile = new ZipFile(workingDir.resolve("res.zip"));
        assertEquals("foo", IOUtils.toString(Objects.requireNonNull(zipFile.getInputStream("f1")), StandardCharsets.UTF_8));
        assertEquals("bar", IOUtils.toString(Objects.requireNonNull(zipFile.getInputStream("f2")), StandardCharsets.UTF_8));
        assertEquals("hello", IOUtils.toString(Objects.requireNonNull(zipFile.getInputStream("f3")), StandardCharsets.UTF_8));
        assertEquals("v1", IOUtils.toString(Objects.requireNonNull(zipFile.getInputStream("k1")), StandardCharsets.UTF_8));
        assertEquals("é", IOUtils.toString(Objects.requireNonNull(zipFile.getInputStream("k2")), StandardCharsets.UTF_8));
        assertEquals("î", IOUtils.toString(Objects.requireNonNull(zipFile.getInputStream("k3")), StandardCharsets.ISO_8859_1));

        assertArrayEquals(nestedZipBytes, IOUtils.toByteArray(zipFile.getInputStream("nested.zip")));

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
        ZipFile zipFile2 = new ZipFile(workingDir.resolve("static.zip"));
        assertEquals("foo", IOUtils.toString(Objects.requireNonNull(zipFile2.getInputStream("f1")), StandardCharsets.UTF_8));
        assertEquals("bar", IOUtils.toString(Objects.requireNonNull(zipFile2.getInputStream("f2")), StandardCharsets.UTF_8));
        assertEquals("hello", IOUtils.toString(Objects.requireNonNull(zipFile2.getInputStream("f3")), StandardCharsets.UTF_8));
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

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }
}
