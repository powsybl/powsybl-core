/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

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
    public void test() {
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
                .addZipFileIfExists(workingDir.resolve("notExists"))
                .addZipFileIfExists(f1)
                .addException(runtimeException);
        ComputationException computationException2 = ceb2.build();
        assertEquals("outLog", computationException2.getOutLogs().get("out"));
        assertEquals("errLog", computationException2.getErrLogs().get("err"));
        assertEquals(runtimeException, computationException2.getExceptions().get(0));
        assertEquals("foo", new String(computationException2.getZipBytes().get("f1.out")));

    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }
}
