/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import eu.itesla_project.iidm.network.impl.ThreadLocalMultiStateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Thread pool executors that reset the current network state each time a thread go back to the pool.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MultiStateNetworkAwareExecutors {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiStateNetworkAwareExecutors.class);

    private static final String DEFAULT_POOL_NAME = "COMPUTATION_POOL";

    private static ThreadFactory threadFactory(String poolName) {
        return new ThreadFactoryBuilder()
                .setNameFormat(poolName + "-%d")
                .build();
    }

    public static ExecutorService newFixedThreadPool(int nThreads) {
        return newFixedThreadPool(DEFAULT_POOL_NAME, nThreads);
    }

    public static ExecutorService newFixedThreadPool(String poolName, int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), threadFactory(poolName)) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                ThreadLocalMultiStateContext.INSTANCE.reset();
            }
        };
    }

    public static ExecutorService newCachedThreadPool() {
        return newCachedThreadPool(DEFAULT_POOL_NAME);
    }

    public static ExecutorService newCachedThreadPool(String poolName) {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), threadFactory(poolName)) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                ThreadLocalMultiStateContext.INSTANCE.reset();
            }
        };
    }

    public static ExecutorService newSizeLimitedThreadPool(int maxSize) {
        return newSizeLimitedThreadPool(DEFAULT_POOL_NAME, maxSize);
    }

    public static ExecutorService newSizeLimitedThreadPool(String poolName, int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("Invalid bounded max size");
        }
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(maxSize, maxSize, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), threadFactory(poolName)) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
//                LOGGER.info("activeCount=" + getActiveCount() + " ,corePoolSize=" + getCorePoolSize()
//                        + " ,largestPoolSize=" + getLargestPoolSize() + " ,maximumPoolSize=" + getMaximumPoolSize());
                ThreadLocalMultiStateContext.INSTANCE.reset();
                if (t != null) {
                    LOGGER.error(t.toString(), t);
                }
            }
        };
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return threadPoolExecutor;
    }
}
