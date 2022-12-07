/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.local.test;

import com.powsybl.computation.local.LocalCommandExecutor;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class DockerLocalCommandExecutor implements LocalCommandExecutor {

    private final String dockerImage;
    private final Path hostVolumePath;
    private final Path containerVolumePath;
    private final Map<Path, GenericContainer<?>> containers = new ConcurrentHashMap<>();

    public DockerLocalCommandExecutor(Path hostVolumePath, Path containerVolumePath, ComputationDockerConfig config) {
        this.dockerImage = Objects.requireNonNull(config.getDockerImageId());
        this.hostVolumePath = Objects.requireNonNull(hostVolumePath);
        this.containerVolumePath = Objects.requireNonNull(containerVolumePath);
    }

    @Override
    public int execute(String program, List<String> args, Path outFile, Path errFile, Path workingDir, Map<String, String> env) throws IOException, InterruptedException {
        try (GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(dockerImage))
                .withFileSystemBind(hostVolumePath.toString(), containerVolumePath.toString(), BindMode.READ_WRITE)
                .withCreateContainerCmdModifier(cmd -> cmd.withUser("root"))
                .withCommand("sleep", "infinity")) {
            Container.ExecResult execResult;
            containers.put(workingDir, container);
            try {
                env.entrySet().stream().filter(entry -> !entry.getKey().equals("PATH"))
                        .forEach(entry -> container.withEnv(entry.getKey(), entry.getValue()));
                container.withWorkingDirectory(containerVolumePath.resolve(hostVolumePath
                        .relativize(workingDir)).toString());
                List<String> arguments = new ArrayList<>(args);
                arguments.add(0, program);
                container.start();
                execResult = container.execInContainer(arguments.toArray(new String[0]));
                container.stop();
                Files.write(errFile, execResult.getStderr().getBytes(StandardCharsets.UTF_8));
                Files.write(outFile, execResult.getStdout().getBytes(StandardCharsets.UTF_8));
            } finally {
                containers.remove(workingDir);
            }
            return execResult.getExitCode();
        }
    }

    @Override
    public void stop(Path path) {
        GenericContainer<?> container = containers.get(path);
        if (container != null && container.isRunning()) {
            container.stop();
        }
    }

    @Override
    public void stopForcibly(Path path) {
        stop(path);
    }
}
