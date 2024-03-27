/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation.local.test;

import com.powsybl.computation.local.LocalComputationConfig;
import com.powsybl.computation.local.LocalComputationManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DockerLocalComputationManager extends LocalComputationManager {

    public DockerLocalComputationManager(Path localDir, Path dockerDir, ComputationDockerConfig dockerConfig) throws IOException {
        this(localDir, dockerDir, dockerConfig, ForkJoinPool.commonPool());
    }

    public DockerLocalComputationManager(Path localDir, Path dockerDir, ComputationDockerConfig dockerConfig, Executor executor) throws IOException {
        this(new LocalComputationConfig(localDir), dockerDir, dockerConfig, executor);
    }

    public DockerLocalComputationManager(LocalComputationConfig computationConfig, Path dockerDir,
                                         ComputationDockerConfig dockerConfig, Executor executor) throws IOException {
        super(computationConfig, createCommandExecutor(computationConfig, dockerDir, dockerConfig), executor);
    }

    private static DockerLocalCommandExecutor createCommandExecutor(LocalComputationConfig computationConfig, Path dockerDir,
                                                                    ComputationDockerConfig dockerConfig) {
        Objects.requireNonNull(computationConfig);
        return new DockerLocalCommandExecutor(computationConfig.getLocalDir(), dockerDir, dockerConfig);
    }
}
