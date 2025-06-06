/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation.local;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.computation.*;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class LocalComputationManagerTest {

    private static final String PREFIX = "test_";

    private FileSystem fileSystem;

    private Path localDir;

    private static final String DEBUG_DIR = "/tmp/debugDir";

    private LocalComputationConfig config;

    @BeforeEach
    void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        localDir = fileSystem.getPath("/tmp");
        config = new LocalComputationConfig(localDir, 1);
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    void testVersion() throws Exception {
        assertEquals("none (local mode)", new LocalComputationManager(config).getVersion());
    }

    @Test
    void testLocalDir() throws Exception {
        assertEquals(localDir, new LocalComputationManager(config).getLocalDir());
    }

    @Test
    void test1() throws Exception {
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
            computationManager.execute(new ExecutionEnvironment(ImmutableMap.of("var1", "val1"), PREFIX, false, DEBUG_DIR),
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
                            assertTrue(Files.exists(config.getLocalDir().getFileSystem().getPath((DEBUG_DIR + workingDir.getFileName()))));
                            return null;
                        }
                    }).join();
        }
    }

    @Test
    void test2() throws Exception {
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

    @Test
    void hangingIssue() throws Exception {
        LocalCommandExecutor localCommandExecutor = new AbstractLocalCommandExecutor() {
            @Override
            void nonZeroLog(List<String> cmdLs, int exitCode) {
            }

            @Override
            public int execute(String program, List<String> args, Path outFile, Path errFile, Path workingDir, Map<String, String> env) throws IOException, InterruptedException {
                return 0;
            }
        };
        try (ComputationManager computationManager = new LocalComputationManager(config, localCommandExecutor, ForkJoinPool.commonPool())) {
            CompletableFuture<Object> result = computationManager.execute(ExecutionEnvironment.createDefault(), new AbstractExecutionHandler<Object>() {
                @Override
                public List<CommandExecution> before(Path workingDir) {
                    throw new IllegalStateException("Oups");
                }
            });
            // check that code is not hanging anymore when a java.lang.Error is thrown inside before
            assertThrows(ExecutionException.class, () -> result.get(100, TimeUnit.MILLISECONDS));
        }
    }

    private static List<CommandExecution> dummyExecutions() {
        // run the command
        Command command = new SimpleCommandBuilder()
                .id("prog1_cmd")
                .program("prog1")
                .args("arg1", "arg2")
                .build();
        return Collections.singletonList(new CommandExecution(command, 1));
    }

    @Test
    void cancelBeforeExecutionShouldThrowAndNotExecute() throws Exception {
        LocalCommandExecutor localCommandExecutor = new AbstractLocalCommandExecutor() {
            @Override
            public void nonZeroLog(List<String> cmdLs, int exitCode) {
            }

            @Override
            public int execute(String program, List<String> args, Path outFile, Path errFile, Path workingDir, Map<String, String> env) throws IOException, InterruptedException {
                fail();
                return 0;
            }
        };

        CountDownLatch waitForBefore = new CountDownLatch(1);
        CountDownLatch waitForCancel = new CountDownLatch(1);

        try (ComputationManager computationManager = new LocalComputationManager(config, localCommandExecutor, ForkJoinPool.commonPool())) {

            Lock lock = new ReentrantLock();
            lock.lock();

            CompletableFuture<Object> result = computationManager.execute(ExecutionEnvironment.createDefault(), new AbstractExecutionHandler<Object>() {
                @Override
                public List<CommandExecution> before(Path workingDir) {
                    waitForBefore.countDown();
                    awaitUninterruptibly(waitForCancel);
                    return dummyExecutions();
                }

                @Override
                public Object after(Path workingDir, ExecutionReport report) throws IOException {
                    fail();
                    return super.after(workingDir, report);
                }
            });

            waitForBefore.await();
            result.cancel(true);
            waitForCancel.countDown();

            assertThrows(CancellationException.class, result::get);
        }
    }

    @Test
    void cancelDuringExecutionShouldThrowAndEventuallyStopExecution() throws InterruptedException, ExecutionException, IOException {

        CountDownLatch waitForExecution = new CountDownLatch(1);
        CountDownLatch execution = new CountDownLatch(1); // Will be interrupted, not decremented
        CountDownLatch waitForInterruption = new CountDownLatch(1);

        MutableBoolean stopped = new MutableBoolean(false);
        LocalCommandExecutor localCommandExecutor = new AbstractLocalCommandExecutor() {
            @Override
            void nonZeroLog(List<String> cmdLs, int exitCode) {
            }

            @Override
            public int execute(String program, List<String> args, Path outFile, Path errFile, Path workingDir, Map<String, String> env) throws IOException, InterruptedException {
                waitForExecution.countDown();
                execution.await(); //Simulates process running
                return 0;
            }

            @Override
            public void stop(Path workingDir) {
                stopped.setTrue();
                waitForInterruption.countDown();
            }

            @Override
            public void stopForcibly(Path workingDir) {
                stopped.setTrue();
                waitForInterruption.countDown();
            }
        };

        try (ComputationManager computationManager = new LocalComputationManager(config, localCommandExecutor, ForkJoinPool.commonPool())) {

            CompletableFuture<Object> result = computationManager.execute(ExecutionEnvironment.createDefault(), new AbstractExecutionHandler<Object>() {
                @Override
                public List<CommandExecution> before(Path workingDir) {
                    return dummyExecutions();
                }

                @Override
                public Object after(Path workingDir, ExecutionReport report) throws IOException {
                    fail();
                    return super.after(workingDir, report);
                }
            });

            waitForExecution.await();
            result.cancel(true);
            result.get();
            fail("Should not happen: result has been cancelled");
        } catch (CancellationException exc) {
            //OK
        }

        waitForInterruption.await(10, TimeUnit.SECONDS);
        assertTrue(stopped.isTrue());
    }
}
