/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation.mpi.util;

import eu.itesla_project.computation.mpi.MpiExecutorContext;
import eu.itesla_project.iidm.network.impl.util.MultiStateNetworkAwareExecutors;

import java.util.concurrent.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MultiStateNetworkAwareMpiExecutorContext implements MpiExecutorContext {

    private final ScheduledExecutorService monitorExecutorService;
    private final ExecutorService schedulerExecutorService;
    private final ExecutorService computationExecutorService;
    private final ExecutorService applicationExecutorService;

    public MultiStateNetworkAwareMpiExecutorContext() {
        monitorExecutorService = Executors.newScheduledThreadPool(1);
        schedulerExecutorService = Executors.newCachedThreadPool();
        computationExecutorService = MultiStateNetworkAwareExecutors.newSizeLimitedThreadPool("COMPUTATION_POOL", 100);
        applicationExecutorService = MultiStateNetworkAwareExecutors.newSizeLimitedThreadPool("APPLICATION_POOL", 100);
    }

    @Override
    public ScheduledExecutorService getMonitorExecutor() {
        return monitorExecutorService;
    }

    @Override
    public ExecutorService getSchedulerExecutor() {
        return schedulerExecutorService;
    }

    @Override
    public ExecutorService getBeforeExecutor() {
        return computationExecutorService;
    }

    @Override
    public ExecutorService getCommandExecutor() {
        return computationExecutorService;
    }

    @Override
    public ExecutorService getAfterExecutor() {
        return computationExecutorService;
    }

    @Override
    public ExecutorService getApplicationExecutor() {
        return applicationExecutorService;
    }

    @Override
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
