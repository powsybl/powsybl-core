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
import com.powsybl.computation.local.LocalComputationConfig;
import com.powsybl.computation.local.LocalComputationManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Category(DockerTests.class)
public class DockerLocalCommandExecutorTest {

    private static final String COMMAND_ID = "test";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private ComputationManager computationManager;

    @Before
    public void setup() throws IOException {
        Path localDir = tempFolder.getRoot().toPath();
        Path dockerDir = Path.of("/tmp");
        ComputationDockerConfig config = ComputationDockerConfig.load(PlatformConfig.defaultConfig());
        computationManager = new LocalComputationManager(new LocalComputationConfig(localDir),
                new DockerLocalCommandExecutor(localDir, dockerDir, config), Executors.newCachedThreadPool());
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
}
