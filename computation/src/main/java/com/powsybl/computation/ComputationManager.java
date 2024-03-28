/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 *
 * Computation managers have the ability to execute commands provided through an {@link ExecutionHandler}.
 *
 * <p>Typically, they are used to execute expensive computations through external processes.
 * According to the computation manager implementation, those processes may then be executed on the local host
 * or on a dedicated computation infrastructure which may have better performance or scaling capabilities.
 * Commands will be executed asynchronously and results accessible through the {@link CompletableFuture}
 * returned by one of the {@link #execute} method variants. If {@link CompletableFuture#cancel} is called,
 * the computation manager should try as much as possible to free computation resources used for the
 * underlying commands execution.
 *
 * <p>The computation manager must provide a temporary working directory for each submitted execution,
 * where data may be written to (in particular before the execution) and read from (in particular after the execution).
 * A prefix for this directory may be provided through the {@link ExecutionEnvironment}.
 * Typically, the prefix will be appended with a UUID to ensure working directory uniqueness.
 * If {@link ExecutionEnvironment#isDebug()} is {@literal true}, that working directory will not be discarded,
 * otherwise it may be discarded to ensure a sustainable use of the execution environment.
 *
 * <p>Execution handlers define a list of {@link CommandExecution}s to be executed.
 * Those command executions must be executed sequentially. However, when a command execution
 * has an execution number greater than one, the corresponding executions may be executed in parallel.
 *
 * <p>The interface extends {@link AutoCloseable}, since it may require some resource cleanup on closing,
 * for instance deleting working directories or closing connections to remote infrastructure.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ComputationManager extends AutoCloseable {

    String getVersion();

    OutputStream newCommonFile(String fileName) throws IOException;

    /**
     * Submits for execution commands defined by the specified {@link ExecutionHandler},
     * using the specified {@link ExecutionEnvironment}.
     *
     * <p>The result of type {@link R} will be provided asynchronously through the returned {@link CompletableFuture}.
     *
     * @param environment specifies environment details such as the working directory prefix and environment variables.
     * @param handler     defines the commands to be executed together with preprocessing and postprocessing.
     * @param <R>         the type of the result expected from the commands execution.
     * @return            the result of the commands execution, as provided by the execution handler.
     */
    <R> CompletableFuture<R> execute(ExecutionEnvironment environment, ExecutionHandler<R> handler);

    /**
     * Submits for execution commands defined by the specified {@link ExecutionHandler},
     * using the specified {@link ExecutionEnvironment}.
     *
     * <p>The result of type {@link R} will be provided asynchronously through the returned {@link CompletableFuture}.
     *
     * <p>Additional technical parameters may be provided through the {@link ComputationParameters}.
     *
     * @param environment specifies environment details such as the working directory prefix and environment variables.
     * @param handler     defines the commands to be executed together with preprocessing and postprocessing.
     * @param parameters  defines additional technical parameters
     * @param <R>         the type of the result expected from the commands execution.
     * @return            the result of the commands execution, as provided by the execution handler.
     */
    default <R> CompletableFuture<R> execute(ExecutionEnvironment environment, ExecutionHandler<R> handler, ComputationParameters parameters) {
        return execute(environment, handler);
    }

    ComputationResourcesStatus getResourcesStatus();

    /**
     * An executor which may be used to perform expensive processing inside this JVM.
     *
     * @return an executor which may be used to perform expensive processing inside this JVM.
     */
    Executor getExecutor();

    Path getLocalDir();

    @Override
    void close();
}
