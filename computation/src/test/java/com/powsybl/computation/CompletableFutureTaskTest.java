/**
 * Copyright (c) 2019, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
class CompletableFutureTaskTest {

    /**
     * Check the behaviour is the same with different types of executors.
     * In particular, {@link ThreadPoolExecutor} and {@link ForkJoinPool}
     * have some behaviour discrepancies, which should be hidden by our implementation.
     */
    static Stream<Arguments> parameters() {
        return executors().stream()
                .map(Arguments::of);
    }

    private static List<Executor> executors() {
        return ImmutableList.of(
                Executors.newSingleThreadExecutor(),
                Executors.newCachedThreadPool(),
                Executors.newWorkStealingPool(),
                ForkJoinPool.commonPool()
        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void whenSupplyObjectThenReturnIt(Executor executor) throws Exception {

        Object res = new Object();
        CompletableFutureTask<Object> task = CompletableFutureTask.runAsync(() -> res, executor);
        assertSame(res, task.get());
    }

    private static class MyException extends RuntimeException {
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void whenTaskThrowsThenThrowExecutionException(Executor executor) throws InterruptedException {

        CompletableFutureTask<Integer> task = CompletableFutureTask.runAsync(() -> {
            throw new MyException();
        }, executor);

        try {
            task.get();
            fail();
        } catch (ExecutionException exc) {
            assertTrue(exc.getCause() instanceof MyException);
        }
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void whenCancelBeforeExecutionThenThrowAndDontExecute(Executor executor) throws Exception {

        CompletableFutureTask<Integer> task = new CompletableFutureTask<>(() -> {
            fail();
            return null;
        });
        boolean cancelled = task.cancel(true);
        assertTrue(cancelled);
        task.runAsync(executor);
        assertThrows(CancellationException.class, task::get);
    }

    private void testEffectiveInterrupt(boolean addDependant, Executor executor) throws Exception {
        CountDownLatch waitForStart = new CountDownLatch(1);
        CountDownLatch waitIndefinitely = new CountDownLatch(1);
        CountDownLatch waitForInterruption = new CountDownLatch(1);

        AtomicBoolean interrupted = new AtomicBoolean(false);
        CompletableFuture<Integer> task = CompletableFutureTask.runAsync(() -> {
            waitForStart.countDown();
            try {
                waitIndefinitely.await();
                fail();
            } catch (InterruptedException exc) {
                interrupted.set(true);
                waitForInterruption.countDown();
            }
            return null;
        }, executor);
        if (addDependant) {
            task = task.thenApply(Function.identity());
        }

        //Cancel after task has actually started
        waitForStart.await();
        boolean cancelled = task.cancel(true);
        assertTrue(cancelled);

        try {
            task.get();
            fail("Should not happen: task has been cancelled");
        } catch (CancellationException exc) {
            //ignored
        }

        waitForInterruption.await();
        assertTrue(interrupted.get());

        //Second call to cancel should return false
        cancelled = task.cancel(true);
        assertFalse(cancelled);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void whenCancelDuringExecutionThenThrowAndInterruptDirect(Executor executor) throws Exception {
        testEffectiveInterrupt(false, executor);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void whenCancelDuringExecutionThenThrowAndInterruptDependant(Executor executor) throws Exception {
        testEffectiveInterrupt(true, executor);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void cancelAfterExecutionShouldDoNothing(Executor executor) throws Exception {

        Object res = new Object();
        CompletableFutureTask<Object> task = CompletableFutureTask.runAsync(() -> res, executor);

        assertSame(res, task.get());

        boolean cancelled = task.cancel(true);
        assertFalse(cancelled);
        assertSame(res, task.get());
    }

}
