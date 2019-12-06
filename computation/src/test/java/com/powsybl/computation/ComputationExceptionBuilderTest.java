/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.java.truevfs.comp.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ComputationExceptionBuilderTest {

    private FileSystem fileSystem;
    private Path workingDir;
    private Path f1;
    private Path f2;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        workingDir = fileSystem.getPath("/wd");
        f1 = workingDir.resolve("f1.out");
        f2 = workingDir.resolve("f2.err");
        try {
            Files.createDirectories(workingDir);
            try (BufferedWriter writer = Files.newBufferedWriter(f1, StandardCharsets.UTF_8);
                 BufferedWriter w2 = Files.newBufferedWriter(f2, StandardCharsets.UTF_8);) {
                writer.write("foo");
                w2.write("bar");
            }
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void readLogFromFiles() {
        ComputationException computationException = new ComputationExceptionBuilder()
                .addOutLogIfExists(f1)
                .addErrLogIfExists(f2)
                .build();
        String outLog = computationException.getOutLogs().get("f1.out");
        String errLog = computationException.getErrLogs().get("f2.err");
        assertEquals("foo", outLog);
        assertEquals("bar", errLog);
    }

    @Test
    public void withCause() {
        Exception cause = new RuntimeException("oops");
        ComputationException computationException = new ComputationExceptionBuilder(cause)
                .build();
        assertThat(computationException)
                .hasCause(cause)
                .hasMessageContaining("oops")
                .hasMessageContaining("RuntimeException")
                .hasStackTraceContaining("Caused by")
                .hasStackTraceContaining("oops");
    }

    @Test
    public void withMessage() {
        ComputationException computationException = new ComputationExceptionBuilder()
                .message("outch")
                .build();
        assertTrue(computationException.getMessage().contains("outch"));
        assertThat(computationException)
                .hasNoCause()
                .hasMessageContaining("outch")
                .hasStackTraceContaining("outch");
    }

    @Test
    public void withMessageAndCause() {
        Exception cause = new RuntimeException("oops");
        ComputationException computationException = new ComputationExceptionBuilder(cause)
                .message("outch")
                .build();
        assertFalse(computationException.getMessage().contains("oops"));
        assertThat(computationException)
                .hasCause(cause)
                .hasMessageContaining("outch")
                .hasStackTraceContaining("Caused by")
                .hasStackTraceContaining("oops");
    }

    @Test
    public void readAdditionalFiles() throws IOException {
        ComputationException computationException = new ComputationExceptionBuilder()
                .addFileIfExists(workingDir.resolve("notExists"))
                .addFileIfExists(f1)
                .addFileIfExists(null)
                .build();
        byte[] bytes = computationException.toZipBytes();
        IOUtils.copy(new ByteArrayInputStream(bytes), Files.newOutputStream(workingDir.resolve("test.zip")));
        ZipFile zip = new ZipFile(workingDir.resolve("test.zip"));

        assertEquals("foo", new String(computationException.getFileBytes().get("f1.out")));
        assertEquals("foo", IOUtils.toString(Objects.requireNonNull(zip.getInputStream("f1.out")), StandardCharsets.UTF_8));
    }

    @Test
    public void logsFromString() throws IOException {
        ComputationException computationException = new ComputationExceptionBuilder()
                .addOutLog("out", "outLog")
                .addErrLog("err", "errLog")
                .build();
        assertEquals("outLog", computationException.getOutLogs().get("out"));
        assertEquals("errLog", computationException.getErrLogs().get("err"));

        byte[] bytes = computationException.toZipBytes();
        IOUtils.copy(new ByteArrayInputStream(bytes), Files.newOutputStream(workingDir.resolve("test.zip")));
        ZipFile zip = new ZipFile(workingDir.resolve("test.zip"));
        assertEquals("outLog", IOUtils.toString(Objects.requireNonNull(zip.getInputStream("out")), StandardCharsets.UTF_8));
        assertEquals("errLog", IOUtils.toString(Objects.requireNonNull(zip.getInputStream("err")), StandardCharsets.UTF_8));
    }

    @Test
    public void addRawBytes() throws IOException {
        String key = "bytesKey";
        ComputationException computationException =  new ComputationExceptionBuilder()
                .addBytes(key, "someBytes".getBytes())
               .build();
        byte[] bytes1 = computationException.getFileBytes().get(key);
        assertEquals("someBytes", new String(bytes1));

        // test after serialized
        IOUtils.copy(new ByteArrayInputStream(computationException.toZipBytes()), Files.newOutputStream(workingDir.resolve("test3.zip")));
        ZipFile zip = new ZipFile(workingDir.resolve("test3.zip"));
        assertEquals("someBytes", IOUtils.toString(zip.getInputStream(key), StandardCharsets.UTF_8));
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }
}
