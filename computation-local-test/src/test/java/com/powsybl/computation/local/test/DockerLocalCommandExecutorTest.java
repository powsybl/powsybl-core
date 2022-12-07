/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.local.test;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.test.DockerTests;
import com.powsybl.computation.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Category(DockerTests.class)
public class DockerLocalCommandExecutorTest {

    private static final String COMMAND_ID = "test";

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private ComputationManager computationManager;

    @Before
    public void setup() throws IOException {
        Path localDir = tempFolder.getRoot().toPath();
        Path dockerDir = Path.of("/tmp");
        ComputationDockerConfig config = ComputationDockerConfig.load(PlatformConfig.defaultConfig());
        computationManager = new DockerLocalComputationManager(localDir, dockerDir, config);
    }

    @After
    public void tearDown() {
        computationManager.close();
    }

    @Test
    public void test() {
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
    public void testInputFile() {
        List<String> lines = computationManager.execute(ExecutionEnvironment.createDefault(), new AbstractExecutionHandler<List<String>>() {
            @Override
            public List<CommandExecution> before(Path workingDir) throws IOException {
                Files.write(workingDir.resolve("test.txt"), "hello".getBytes(StandardCharsets.UTF_8));
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
    public void testOutputFile() {
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
}
