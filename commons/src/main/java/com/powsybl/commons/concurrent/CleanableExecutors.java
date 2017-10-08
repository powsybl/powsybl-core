/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.concurrent;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.*;

/**
 * Thread pool executors that can execute cleaning tasks each time a thread go back to the pool.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class CleanableExecutors {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanableExecutors.class);

    private static final String DEFAULT_POOL_NAME = "POWSYBL_POOL";

    public interface ThreadCleaner {

        void clean();
    }

    public static class CleanableThreadPoolExecutor extends ThreadPoolExecutor {

        private final Iterable<ThreadCleaner> cleaners;

        public CleanableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                           BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, Iterable<ThreadCleaner> cleaners) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
            this.cleaners = Objects.requireNonNull(cleaners);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("activeCount={} ,corePoolSize={} ,largestPoolSize={} ,maximumPoolSize={}",
                        getActiveCount(), getCorePoolSize(), getLargestPoolSize(), getMaximumPoolSize());
            }
            if (t != null && LOGGER.isErrorEnabled()) {
                LOGGER.error(t.toString(), t);
            }
            for (ThreadCleaner cleaner : cleaners) {
                cleaner.clean();
            }
        }
    }

    private CleanableExecutors() {
    }

    private static ThreadFactory threadFactory(String poolName) {
        return new ThreadFactoryBuilder()
                .setNameFormat(poolName + "-%d")
                .build();
    }

    public static ExecutorService newFixedThreadPool(int nThreads) {
        return newFixedThreadPool(DEFAULT_POOL_NAME, nThreads);
    }

    public static ExecutorService newFixedThreadPool(String poolName, int nThreads) {
        return newFixedThreadPool(poolName, nThreads, ServiceLoader.load(ThreadCleaner.class));
    }

    public static ExecutorService newFixedThreadPool(String poolName, int nThreads, Iterable<ThreadCleaner> cleaners) {
        return new CleanableThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                threadFactory(poolName), cleaners);
    }

    public static ExecutorService newCachedThreadPool() {
        return newCachedThreadPool(DEFAULT_POOL_NAME);
    }

    public static ExecutorService newCachedThreadPool(String poolName) {
        return newCachedThreadPool(poolName, ServiceLoader.load(ThreadCleaner.class));
    }

    public static ExecutorService newCachedThreadPool(String poolName, Iterable<ThreadCleaner> cleaners) {
        return new CleanableThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
                threadFactory(poolName), cleaners);
    }

    public static ExecutorService newSizeLimitedThreadPool(int maxSize) {
        return newSizeLimitedThreadPool(DEFAULT_POOL_NAME, maxSize);
    }

    public static ExecutorService newSizeLimitedThreadPool(String poolName, int maxSize) {
        return newSizeLimitedThreadPool(poolName, maxSize, ServiceLoader.load(ThreadCleaner.class));
    }

    public static ExecutorService newSizeLimitedThreadPool(String poolName, int maxSize, Iterable<ThreadCleaner> cleaners) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("Invalid bounded max size");
        }
        CleanableThreadPoolExecutor threadPoolExecutor = new CleanableThreadPoolExecutor(maxSize, maxSize, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), threadFactory(poolName), cleaners);
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return threadPoolExecutor;
    }
}
