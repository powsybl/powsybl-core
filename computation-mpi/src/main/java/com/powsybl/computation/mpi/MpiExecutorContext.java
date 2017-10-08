/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import com.powsybl.commons.concurrent.CleanableExecutors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MpiExecutorContext {

    private final ScheduledExecutorService monitorExecutorService;
    private final ExecutorService schedulerExecutorService;
    private final ExecutorService computationExecutorService;
    private final ExecutorService applicationExecutorService;

    public MpiExecutorContext() {
        this(100);
    }

    public MpiExecutorContext(int poolSize) {
        monitorExecutorService = Executors.newScheduledThreadPool(1);
        schedulerExecutorService = Executors.newCachedThreadPool();
        computationExecutorService = CleanableExecutors.newSizeLimitedThreadPool("COMPUTATION_POOL", poolSize);
        applicationExecutorService = CleanableExecutors.newSizeLimitedThreadPool("APPLICATION_POOL", poolSize);
    }

    public ScheduledExecutorService getMonitorExecutor() {
        return monitorExecutorService;
    }

    public ExecutorService getSchedulerExecutor() {
        return schedulerExecutorService;
    }

    public ExecutorService getComputationExecutor() {
        return computationExecutorService;
    }

    public ExecutorService getApplicationExecutor() {
        return applicationExecutorService;
    }

    public void shutdown() throws InterruptedException {
        monitorExecutorService.shutdown();
        schedulerExecutorService.shutdown();
        computationExecutorService.shutdown();
        applicationExecutorService.shutdown();
        monitorExecutorService.awaitTermination(15, TimeUnit.MINUTES);
        schedulerExecutorService.awaitTermination(15, TimeUnit.MINUTES);
        computationExecutorService.awaitTermination(15, TimeUnit.MINUTES);
        applicationExecutorService.awaitTermination(15, TimeUnit.MINUTES);
    }
}
