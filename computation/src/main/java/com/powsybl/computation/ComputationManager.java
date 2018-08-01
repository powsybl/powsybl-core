/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ComputationManager extends AutoCloseable {

    String getVersion();

    OutputStream newCommonFile(String fileName) throws IOException;

    <R> CompletableFuture<R> execute(ExecutionEnvironment environment, ExecutionHandler<R> handler);

    ComputationResourcesStatus getResourcesStatus();

    Executor getExecutor();

    Path getLocalDir();

    @Override
    void close();
}
