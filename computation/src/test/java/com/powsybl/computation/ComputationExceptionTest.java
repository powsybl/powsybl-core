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

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ComputationExceptionTest {

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
            e.printStackTrace();
        }
    }

    @Test
    public void test() {
        RuntimeException runtimeException = new RuntimeException();
        ComputationException sut = new ComputationException(runtimeException);
        sut.addOutLog(f1).addErrLog(f2);
        String outLog = sut.getOutLogs().get("f1.out");
        String errLog = sut.getErrLogs().get("f2.err");
        assertEquals("foo", outLog);
        assertEquals("bar", errLog);

        sut.addOutLog("out", "outLog")
                .addErrLog("err", "errLog");
        assertEquals("outLog", sut.getOutLogs().get("out"));
        assertEquals("errLog", sut.getErrLogs().get("err"));

    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }
}
