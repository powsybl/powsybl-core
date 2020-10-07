/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.computation;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class DefaultExecutionReportTest {

    @Test
    public void test() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = fs.getPath("/work");

            Files.write(tmpDir.resolve("command_0.out"), "stdout".getBytes());
            Files.write(tmpDir.resolve("command_0.err"), "stderr".getBytes());

            DefaultExecutionReport report = new DefaultExecutionReport(tmpDir);

            Command command = Mockito.mock(Command.class);
            Mockito.when(command.getId()).thenReturn("command");

            Optional<InputStream> stdout = report.getStdOut(command, 0);
            Assert.assertTrue(stdout.isPresent());
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stdout.get()))) {
                Assert.assertEquals("stdout", reader.readLine());
            }

            Optional<InputStream> stderr = report.getStdErr(command, 0);
            Assert.assertTrue(stderr.isPresent());
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stderr.get()))) {
                Assert.assertEquals("stderr", reader.readLine());
            }
        }
    }
}
