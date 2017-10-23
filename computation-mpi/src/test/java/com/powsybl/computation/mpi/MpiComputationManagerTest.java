/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;


import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.*;
import com.powsybl.computation.mpi.generated.Messages;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MpiComputationManagerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MpiComputationManagerTest.class);

    private static final String ID_CMD_1 = "cmd1";
    private static final String INPUT_FILE_NAME_1 = "in1.txt";
    private static final String INPUT_FILE_CONTENT_1 = "inputcontent1";
    private static final String OUTPUT_FILE_NAME_1 = "out1.txt";
    private static final String OUTPUT_FILE_CONTENT_1 = "outputcontent1";
    private static final String ID_CMD_2 = "cmd2";
    private static final String OUTPUT_FILE_NAME_2 = "out2.txt";
    private static final String OUTPUT_FILE_CONTENT_2 = "outputcontent2";

    static class MpiNativeServicesMock implements MpiNativeServices {

        private static Messages.TaskResult createResultMessage(String fileName, String fileContent) {
            return Messages.TaskResult.newBuilder()
                    .setExitCode(0)
                    .setTaskDuration(0)
                    .addCommandDuration(0)
                    .setWorkingDataSize(0)
                    .addOutputFile(Messages.TaskResult.OutputFile.newBuilder()
                            .setName(fileName)
                            .setData(ByteString.copyFromUtf8(fileContent))
                            .build())
                    .build();
        }

        @Override
        public void checkTasksCompletion(List<MpiTask> runningTasks, List<MpiTask> completedTasks) {
            for (MpiTask runningTask : runningTasks) {
                try {
                    Messages.Task message = Messages.Task.parseFrom(runningTask.getMessage());
                    switch (message.getCmdId()) {
                        case ID_CMD_1:
                            runningTask.setResultMessage(createResultMessage(OUTPUT_FILE_NAME_1, OUTPUT_FILE_CONTENT_1).toByteArray());
                            completedTasks.add(runningTask);
                            break;
                        case ID_CMD_2:
                            runningTask.setResultMessage(createResultMessage(OUTPUT_FILE_NAME_2, OUTPUT_FILE_CONTENT_2).toByteArray());
                            completedTasks.add(runningTask);
                            break;
                    }
                } catch (InvalidProtocolBufferException e) {
                    LOGGER.error(e.toString(), e);
                }
            }
        }

        @Override
        public void initMpi(int coresPerRank, boolean verbose) {
        }

        @Override
        public void terminateMpi() {
        }

        @Override
        public String getMpiVersion() {
            return "Mpi mock";
        }

        @Override
        public int getMpiCommSize() {
            return 2;
        }

        @Override
        public void sendCommonFile(byte[] message) {
        }

        @Override
        public void startTasks(List<MpiTask> tasks) {
        }
    }

    private ComputationManager cm;
    private FileSystem fileSystem;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Path tmpDir = Files.createDirectory(fileSystem.getPath("/tmp"));
        cm = new MpiComputationManager(tmpDir, new MpiNativeServicesMock());
    }

    @After
    public void tearDown() throws Exception {
        cm.close();
        cm = null;
        fileSystem.close();
        fileSystem = null;
    }

    private static CommandExecution createParams1() {
        return new CommandExecution(new SimpleCommandBuilder()
                .id(ID_CMD_1)
                .program("exec1")
                .inputFiles(new InputFile(INPUT_FILE_NAME_1))
                .args(INPUT_FILE_NAME_1, OUTPUT_FILE_NAME_1)
                .outputFiles(new OutputFile(OUTPUT_FILE_NAME_1))
                .build(),
                1);
    }

    private static CommandExecution createParams2() {
        return new CommandExecution(new SimpleCommandBuilder()
                .id(ID_CMD_2)
                .program("exec2")
                .inputFiles(new InputFile(OUTPUT_FILE_NAME_1))
                .args(OUTPUT_FILE_NAME_1, OUTPUT_FILE_NAME_2)
                .outputFiles(new OutputFile(OUTPUT_FILE_NAME_2))
                .build(),
                1);
    }

    private static void writeInput1(Path workingDir) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(workingDir.resolve(INPUT_FILE_NAME_1), StandardCharsets.UTF_8)) {
            writer.write(INPUT_FILE_CONTENT_1);
            writer.newLine();
        }
    }

    private static String readOutput1(Path workingDir) throws IOException {
        return new String(Files.readAllBytes(workingDir.resolve(OUTPUT_FILE_NAME_1)), StandardCharsets.UTF_8);
    }

    private static String readOutput2(Path workingDir) throws IOException {
        return new String(Files.readAllBytes(workingDir.resolve(OUTPUT_FILE_NAME_2)), StandardCharsets.UTF_8);
    }

    private class ExecutionHandlerTest1 extends AbstractExecutionHandler<String> {
        @Override
        public List<CommandExecution> before(Path workingDir) throws IOException {
            writeInput1(workingDir);
            return Collections.singletonList(createParams1());
        }

        @Override
        public String after(Path workingDir, ExecutionReport report) throws IOException {
            report.log();
            return readOutput1(workingDir);
        }
    }

    private class ExecutionHandlerTest2 extends AbstractExecutionHandler<String> {
        @Override
        public List<CommandExecution> before(Path workingDir) throws IOException {
            writeInput1(workingDir);
            return Arrays.asList(createParams1(),
                    createParams2());
        }

        @Override
        public String after(Path workingDir, ExecutionReport report) throws IOException {
            report.log();
            return readOutput2(workingDir);
        }
    }

    @Test
    public void testExecute() throws Exception {
        final Path[] workingDirSav = new Path[1];
        String result = cm.execute(ExecutionEnvironment.createDefault(), new ExecutionHandlerTest1() {
                @Override
                public List<CommandExecution> before(Path workingDir) throws IOException {
                    workingDirSav[0] = workingDir;
                    return super.before(workingDir);
                }
            }).join();
        assertTrue(OUTPUT_FILE_CONTENT_1.equals(result));
        assertTrue(Files.notExists(workingDirSav[0]));
    }

    @Test
    public void testExecute2() throws Exception {
        final Path[] workingDirSav = new Path[1];
        try {
            cm.execute(ExecutionEnvironment.createDefault(), new ExecutionHandlerTest1() {
                    @Override
                    public List<CommandExecution> before(Path workingDir) throws IOException {
                        workingDirSav[0] = workingDir;
                        throw new PowsyblException("test error");
                    }
                }).join();
            fail();
        } catch (Exception ignored) {
        }
        assertTrue(Files.notExists(workingDirSav[0]));
    }

    @Test
    public void testExecute3() throws Exception {
        final Path[] workingDirSav = new Path[1];
        try {
            cm.execute(ExecutionEnvironment.createDefault(), new ExecutionHandlerTest1() {
                @Override
                public List<CommandExecution> before(Path workingDir) throws IOException {
                    workingDirSav[0] = workingDir;
                    return super.before(workingDir);
                }

                @Override
                public String after(Path workingDir, ExecutionReport report) throws IOException {
                    throw new PowsyblException("test error");
                }
            }).join();
            fail();
        } catch (Exception ignored) {
        }
        assertTrue(Files.notExists(workingDirSav[0]));
    }

    @Test
    public void testExecute4() throws Exception {
        final Path[] workingDirSav = new Path[1];
        String result = cm.execute(ExecutionEnvironment.createDefault(), new ExecutionHandlerTest2() {
                @Override
                public List<CommandExecution> before(Path workingDir) throws IOException {
                    workingDirSav[0] = workingDir;
                    return super.before(workingDir);
                }

                @Override
                public String after(Path workingDir, ExecutionReport report) throws IOException {
                    String result = super.after(workingDir, report);
                    assertTrue(Files.exists(workingDir.resolve(OUTPUT_FILE_NAME_1)));
                    assertTrue(Files.exists(workingDir.resolve(OUTPUT_FILE_NAME_2)));
                    return result;
                }
            }).join();
        assertTrue(OUTPUT_FILE_CONTENT_2.equals(result));
        assertTrue(Files.notExists(workingDirSav[0]));
    }

}
