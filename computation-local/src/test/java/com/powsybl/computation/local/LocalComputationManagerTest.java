/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.local;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.computation.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalComputationManagerTest {

    private static final String PREFIX = "test_";

    private FileSystem fileSystem;

    private Path localDir;

    private LocalComputationConfig config;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        localDir = fileSystem.getPath("/tmp");
        config = new LocalComputationConfig(localDir, 1);
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void testVersion() throws Exception {
        assertEquals("none (local mode)", new LocalComputationManager(config).getVersion());
    }

    @Test
    public void testLocalDir() throws Exception {
        assertEquals(localDir, new LocalComputationManager(config).getLocalDir());
    }

    @Test
    public void test1() throws Exception {
        LocalCommandExecutor localCommandExecutor = new AbstractLocalCommandExecutor() {
            @Override
            void nonZeroLog(List<String> cmdLs, int exitCode) {

            }

            @Override
            public int execute(String program, List<String> args, Path outFile, Path errFile, Path workingDir, Map<String, String> env) throws IOException, InterruptedException {
                // check command line is correct
                assertEquals("prog1", program);
                assertEquals(ImmutableList.of("file1", "file2", "file3"), args);
                assertEquals(ImmutableMap.of("var1", "val1"), env);

                // check working directory exists, contains inout files and standard output file
                assertTrue(Files.exists(workingDir));
                assertEquals(workingDir.resolve("prog1_cmd_0.out").toString(), outFile.toString());
                assertEquals(workingDir.resolve("prog1_cmd_0.err").toString(), errFile.toString());
                assertTrue(Files.exists(workingDir.resolve("file1")));
                assertTrue(Files.exists(workingDir.resolve("file2")));
                assertTrue(Files.exists(workingDir.resolve("file3")));

                // command exits badly
                return 1;
            }
        };
        try (ComputationManager computationManager = new LocalComputationManager(config, localCommandExecutor, ForkJoinPool.commonPool())) {
            computationManager.execute(new ExecutionEnvironment(ImmutableMap.of("var1", "val1"), PREFIX, false),
                    new AbstractExecutionHandler<Object>() {
                        @Override
                        public List<CommandExecution> before(Path workingDir) throws IOException {
                            // create files in the working directory
                            Files.createFile(workingDir.resolve("file1"));
                            try (OutputStream os = new GZIPOutputStream(Files.newOutputStream(workingDir.resolve("file2.gz")))) {
                            }
                            try (ZipOutputStream os = new ZipOutputStream(Files.newOutputStream(workingDir.resolve("file3.zip")))) {
                                os.putNextEntry(new ZipEntry("file3"));
                                os.closeEntry();
                            }

                            // run the command
                            Command command = new SimpleCommandBuilder()
                                    .id("prog1_cmd")
                                    .program("prog1")
                                    .args("file1", "file2", "file3")
                                    .timeout(60)
                                    .inputFiles(new InputFile("file1"),
                                                new InputFile("file2.gz", FilePreProcessor.FILE_GUNZIP),
                                                new InputFile("file3.zip", FilePreProcessor.ARCHIVE_UNZIP))
                                    .build();
                            return Collections.singletonList(new CommandExecution(command, 1));
                        }

                        @Override
                        public Object after(Path workingDir, ExecutionReport report) throws IOException {
                            // check command exits with an error
                            assertEquals(1, report.getErrors().size());
                            assertEquals("prog1_cmd", report.getErrors().get(0).getCommand().getId());
                            assertEquals(0, report.getErrors().get(0).getIndex());
                            assertEquals(1, report.getErrors().get(0).getExitCode());

                            return null;
                        }
                    }).join();
        }
    }

    @Test
    public void test2() throws Exception {
        LocalCommandExecutor localCommandExecutor = new AbstractLocalCommandExecutor() {
            @Override
            void nonZeroLog(List<String> cmdLs, int exitCode) {

            }

            @Override
            public int execute(String program, List<String> args, Path outFile, Path errFile, Path workingDir, Map<String, String> env) throws IOException, InterruptedException {
                // check working directory exists and standard output file
                assertTrue(Files.exists(workingDir));
                assertEquals(workingDir.resolve("prog2_cmd_0.out").toString(), outFile.toString());
                assertEquals(workingDir.resolve("prog2_cmd_0.err").toString(), errFile.toString());

                switch (program) {
                    case "prog2_1":
                        // check command line is correct
                        assertEquals(ImmutableList.of(), args);
                        break;

                    case "prog2_2":
                        // check command line is correct
                        assertEquals(ImmutableList.of("file1"), args);

                        // check input file exists
                        assertTrue(Files.exists(workingDir.resolve("file1")));

                        // create output files
                        Files.createFile(workingDir.resolve("outFile1"));
                        Files.createFile(workingDir.resolve("outFile2"));
                        break;

                    default:
                        fail();
                }

                // command is ok
                return 0;
            }
        };
        try (ComputationManager computationManager = new LocalComputationManager(config, localCommandExecutor, ForkJoinPool.commonPool())) {

            // create file1 as a common file
            computationManager.newCommonFile("file1").close();

            computationManager.execute(new ExecutionEnvironment(ImmutableMap.of(), PREFIX, false),
                    new AbstractExecutionHandler<Object>() {
                        @Override
                        public List<CommandExecution> before(Path workingDir) throws IOException {
                            // run the group command
                            Command command = new GroupCommandBuilder()
                                    .id("prog2_cmd")
                                    .subCommand()
                                        .program("prog2_1")
                                        .timeout(60)
                                    .add()
                                    .subCommand()
                                        .program("prog2_2")
                                        .args("file1")
                                    .add()
                                    .inputFiles(new InputFile("file1"))
                                    .outputFiles(new OutputFile("outFile1"),
                                                 new OutputFile("outFile2", FilePostProcessor.FILE_GZIP))
                                    .build();
                            return Collections.singletonList(new CommandExecution(command, 1));
                        }

                        @Override
                        public Object after(Path workingDir, ExecutionReport report) throws IOException {
                            // check command exits correctly
                            assertTrue(report.getErrors().isEmpty());

                            assertTrue(Files.exists(workingDir.resolve("outFile1")));
                            assertTrue(Files.exists(workingDir.resolve("outFile2.gz")));

                            return null;
                        }
                    }).join();
        }
    }

}
