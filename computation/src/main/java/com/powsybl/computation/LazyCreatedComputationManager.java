/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LazyCreatedComputationManager implements ComputationManager {

    private final ComputationManagerFactory factory;

    private ComputationManager delegate;

    public LazyCreatedComputationManager(ComputationManagerFactory factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    private synchronized ComputationManager getComputationManager() {
        if (delegate == null) {
            delegate = factory.create();
            Objects.requireNonNull(delegate, "Computation manager factory returned null");
        }
        return delegate;
    }

    @Override
    public String getVersion() {
        return getComputationManager().getVersion();
    }

    @Override
    public OutputStream newCommonFile(String fileName) throws IOException {
        return getComputationManager().newCommonFile(fileName);
    }

    @Override
    public <R> CompletableFuture<R> execute(ExecutionEnvironment environment, ExecutionHandler<R> handler) {
        return getComputationManager().execute(environment, handler);
    }

    @Override
    public ComputationResourcesStatus getResourcesStatus() {
        return getComputationManager().getResourcesStatus();
    }

    @Override
    public Executor getExecutor() {
        return getComputationManager().getExecutor();
    }

    @Override
    public Path getLocalDir() {
        return getComputationManager().getLocalDir();
    }

    @Override
    public synchronized void close() {
        //Close the underlying delegate only if it has been initialized.
        if (delegate != null) {
            delegate.close();
        }
    }
}
