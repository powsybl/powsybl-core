/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.util.concurrent.*;

/**
 *
 * A {@link CompletableFuture} which embeds an actual task, unlike the default implementation.
 * In particular, the behaviour of {@link #cancel(boolean)} is modified in order to actually
 * interrupt the thread executing the bound task.
 *
 * <p>Similarly to {@link FutureTask}, this class implements {@link Runnable} and
 * can therefore be submitted to an {@link java.util.concurrent.Executor} for execution.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class CompletableFutureTask<R> extends CompletableFuture<R> implements Runnable {

    private final FutureTask<R> future;

    /**
     * Creates a completable future bound to the specified task. This task will be executed
     * on a call to {@link #run()}, for instance by submitting this object to
     * an {@link java.util.concurrent.Executor}.
     *
     * @param task The task to be bound to this future.
     */
    public CompletableFutureTask(Callable<R> task) {
        future = new FutureTask<>(task);
    }

    /**
     * Executes the wrapped callable. On execution end, this future will complete with the callable result,
     * or complete exceptionally in case of exception.
     */
    @Override
    public void run() {
        future.run();
        try {
            complete(future.get());
        } catch (ExecutionException exc) {
            completeExceptionally(exc.getCause());
        } catch (InterruptedException exc) {
            Thread.currentThread().interrupt();
            completeExceptionally(exc);
        } catch (Throwable exc) {
            completeExceptionally(exc);
        }
    }

    /**
     * Submits this task to the specified executor.
     * @param executor The executor which will execute this task.
     * @return         This.
     */
    public CompletableFutureTask<R> runAsync(Executor executor) {
        executor.execute(this);
        return this;
    }

    /**
     * Creates a completable future task and submits it to the specified executor.
     * @param task     The task to be executed.
     * @param executor The executor which will execute the task.
     * @return         A {@link CompletableFutureTask} bound to the specified task.
     */
    public static <T> CompletableFutureTask<T> runAsync(Callable<T> task, Executor executor) {
        return new CompletableFutureTask<>(task).runAsync(executor);
    }

    /**
     * Cancels this completable future and, if requested and if the bound task is under execution,
     * interrupts the thread executing the task.
     *
     * @param mayInterruptIfRunning if {@code true}, and if the bound task is under execution,
     *                              the thread executing the task will be interrupted
     * @return {@code false} if the task could not be cancelled,
     *         typically because it has already completed;
     *         {@code true} otherwise
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return super.cancel(mayInterruptIfRunning) && future.cancel(mayInterruptIfRunning);
    }

}
