/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.compress;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.bouncycastle.util.Strings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.*;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ZipHelperTest {

    private FileSystem fileSystem;
    private Path workingDir;
    private Path resultDir;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        workingDir = fileSystem.getPath("/wd");
        resultDir = fileSystem.getPath("/result");
        Path f1 = workingDir.resolve("f1");
        Path f2 = workingDir.resolve("f2");
        Path f3 = workingDir.resolve("f3.gz");
        try {
            Files.createDirectories(workingDir);
            try (BufferedWriter writer = Files.newBufferedWriter(f1, StandardCharsets.UTF_8);
                 BufferedWriter w2 = Files.newBufferedWriter(f2, StandardCharsets.UTF_8);
                 OutputStream outStream = new GZIPOutputStream(Files.newOutputStream(f3))) {
                writer.write("foo");
                w2.write("bar");
                outStream.write(Strings.toByteArray("hello"));
            }
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void test() throws IOException {
        byte[] bytes = ZipHelper.archiveFilesToZipBytes(workingDir, "f1", "f2", "f3.gz");
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(bytes))) {
            assertEquals("f1", zis.getNextZipEntry().getName());
            assertEquals("f2", zis.getNextZipEntry().getName());
            assertEquals("f3", zis.getNextZipEntry().getName());
            assertNull(zis.getNextZipEntry());
        } catch (IOException e) {
            fail();
        }
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }
}
