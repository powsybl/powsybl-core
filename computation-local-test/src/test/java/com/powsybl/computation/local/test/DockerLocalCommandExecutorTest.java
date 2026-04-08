/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation.local.test;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.computation.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@Tag("dockerTests")
class DockerLocalCommandExecutorTest {

    private static final String COMMAND_ID = "test";
    private static final String COMMAND_ID_2 = "test2";

    @TempDir
    Path localDir;

    private ComputationManager computationManager;

    @BeforeEach
    void setup() throws IOException {
        Path dockerDir = Path.of("/tmp");
        ComputationDockerConfig config = ComputationDockerConfig.load(PlatformConfig.defaultConfig());
        computationManager = new DockerLocalComputationManager(localDir, dockerDir, config);
    }

    @AfterEach
    void tearDown() {
        computationManager.close();
    }

    @Test
    void test() {
        List<String> lines = computationManager.execute(ExecutionEnvironment.createDefault(), new AbstractExecutionHandler<List<String>>() {
            @Override
            public List<CommandExecution> before(Path workingDir) {
                return List.of(new CommandExecution(new SimpleCommandBuilder()
                        .id(COMMAND_ID)
                        .program("ls")
                        .arg("/")
                        .build(), 1));
            }

            @Override
            public List<String> after(Path workingDir, ExecutionReport report) throws IOException {
                return Files.readAllLines(workingDir.resolve(COMMAND_ID + "_0.out"));
            }
        }).join();
        assertEquals(List.of("bin", "dev", "etc", "home", "lib", "media", "mnt", "opt", "proc", "root", "run", "sbin", "srv", "sys", "tmp", "usr", "var"), lines);
    }

    @Test
    void testInputFile() {
        List<String> lines = computationManager.execute(ExecutionEnvironment.createDefault(), new AbstractExecutionHandler<List<String>>() {
            @Override
            public List<CommandExecution> before(Path workingDir) throws IOException {
                Files.writeString(workingDir.resolve("test.txt"), "hello");
                return List.of(new CommandExecution(new SimpleCommandBuilder()
                        .id(COMMAND_ID)
                        .program("cat")
                        .arg("test.txt")
                        .inputFiles(new InputFile("test.txt"))
                        .build(), 1));
            }

            @Override
            public List<String> after(Path workingDir, ExecutionReport report) throws IOException {
                return Files.readAllLines(workingDir.resolve(COMMAND_ID + "_0.out"));
            }
        }).join();
        assertEquals(List.of("hello"), lines);
    }

    @Test
    void testOutputFile() {
        Boolean exists = computationManager.execute(ExecutionEnvironment.createDefault(), new AbstractExecutionHandler<Boolean>() {
            @Override
            public List<CommandExecution> before(Path workingDir) {
                return List.of(new CommandExecution(new SimpleCommandBuilder()
                        .id(COMMAND_ID)
                        .program("touch")
                        .args("result.txt")
                        .outputFiles(new OutputFile("result.txt"))
                        .build(), 1));
            }

            @Override
            public Boolean after(Path workingDir, ExecutionReport report) {
                return Files.exists(workingDir.resolve("result.txt"));
            }
        }).join();
        assertTrue(exists);
    }

    @Test
    void testOutputDir() {
        Boolean exists = computationManager.execute(ExecutionEnvironment.createDefault(), new AbstractExecutionHandler<Boolean>() {
            @Override
            public List<CommandExecution> before(Path workingDir) {
                var cmd1 = new CommandExecution(new SimpleCommandBuilder()
                        .id(COMMAND_ID)
                        .program("mkdir")
                        .args("mydir")
                        .build(), 1);
                var cmd2 = new CommandExecution(new SimpleCommandBuilder()
                        .id(COMMAND_ID_2)
                        .program("touch")
                        .args("mydir/result.txt")
                        .outputFiles(new OutputFile("mydir/result.txt"))
                        .build(), 1);
                return List.of(cmd1, cmd2);
            }

            @Override
            public Boolean after(Path workingDir, ExecutionReport report) {
                return Files.exists(workingDir.resolve("mydir/result.txt"));
            }
        }).join();
        assertTrue(exists);
    }

    @Test
    void testEnvVar() {
        ExecutionEnvironment env = ExecutionEnvironment.createDefault()
                .setVariables(Map.of("FOO", "BAR"));
        List<String> lines = computationManager.execute(env, new AbstractExecutionHandler<List<String>>() {
            @Override
            public List<CommandExecution> before(Path workingDir) {
                return List.of(new CommandExecution(new SimpleCommandBuilder()
                        .id(COMMAND_ID)
                        .program("printenv")
                        .args("FOO")
                        .build(), 1));
            }

            @Override
            public List<String> after(Path workingDir, ExecutionReport report) throws IOException {
                return Files.readAllLines(workingDir.resolve(COMMAND_ID + "_0.out"));
            }
        }).join();
        assertEquals(List.of("BAR"), lines);
    }
}
