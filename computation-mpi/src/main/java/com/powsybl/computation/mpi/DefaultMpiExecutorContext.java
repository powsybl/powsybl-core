/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultMpiExecutorContext implements MpiExecutorContext {

    @Override
    public ScheduledExecutorService getMonitorExecutor() {
        return null;
    }

    @Override
    public ExecutorService getSchedulerExecutor() {
        return ForkJoinPool.commonPool();
    }

    @Override
    public ExecutorService getAfterExecutor() {
        return ForkJoinPool.commonPool();
    }

    @Override
    public ExecutorService getApplicationExecutor() {
        return ForkJoinPool.commonPool();
    }

    @Override
    public ExecutorService getBeforeExecutor() {
        return ForkJoinPool.commonPool();
    }

    @Override
    public ExecutorService getCommandExecutor() {
        return ForkJoinPool.commonPool();
    }

    @Override
    public void shutdown() throws InterruptedException {
    }
}
