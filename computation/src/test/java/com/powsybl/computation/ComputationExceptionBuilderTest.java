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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
    public void test() throws IOException {
        RuntimeException runtimeException = new RuntimeException();
        ComputationExceptionBuilder ceb = new ComputationExceptionBuilder(runtimeException);
        ceb.addOutLogIfExists(f1).addErrLogIfExists(f2);
        ComputationException computationException = ceb.build();
        String outLog = computationException.getOutLogs().get("f1.out");
        String errLog = computationException.getErrLogs().get("f2.err");
        assertEquals("foo", outLog);
        assertEquals("bar", errLog);

        ComputationExceptionBuilder ceb2 = new ComputationExceptionBuilder(runtimeException);
        ceb2.addOutLog("out", "outLog")
                .addErrLog("err", "errLog")
                .addFileIfExists(workingDir.resolve("notExists"))
                .addFileIfExists(f1)
                .addFileIfExists(null)
                .addException(null)
                .addException(runtimeException);
        ComputationException computationException2 = ceb2.build();
        assertEquals("outLog", computationException2.getOutLogs().get("out"));
        assertEquals("errLog", computationException2.getErrLogs().get("err"));
        assertEquals(runtimeException, computationException2.getExceptions().get(0));
        assertEquals("foo", new String(computationException2.getFileBytes().get("f1.out")));

        byte[] bytes = computationException2.toZipBytes();
        IOUtils.copy(new ByteArrayInputStream(bytes), Files.newOutputStream(workingDir.resolve("test.zip")));
        ZipFile strZipFile = new ZipFile(workingDir.resolve("test.zip"));
        assertEquals("outLog", IOUtils.toString(Objects.requireNonNull(strZipFile.getInputStream("out")), StandardCharsets.UTF_8));
        assertEquals("errLog", IOUtils.toString(Objects.requireNonNull(strZipFile.getInputStream("err")), StandardCharsets.UTF_8));
        assertEquals("foo", IOUtils.toString(Objects.requireNonNull(strZipFile.getInputStream("f1.out")), StandardCharsets.UTF_8));

        ComputationExceptionBuilder ceb3 = new ComputationExceptionBuilder(runtimeException);
        String key = "bytesKey";
        ceb3.addBytes(key, "someBytes".getBytes());
        ComputationException computationException3 = ceb3.build();
        byte[] bytes1 = computationException3.getFileBytes().get(key);
        assertEquals("someBytes", new String(bytes1));

        // test after serialized
        IOUtils.copy(new ByteArrayInputStream(computationException3.toZipBytes()), Files.newOutputStream(workingDir.resolve("test3.zip")));
        ZipFile test3 = new ZipFile(workingDir.resolve("test3.zip"));
        assertEquals("someBytes", IOUtils.toString(test3.getInputStream(key), StandardCharsets.UTF_8));
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }
}
