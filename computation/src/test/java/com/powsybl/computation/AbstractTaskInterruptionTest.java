/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractTaskInterruptionTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTaskInterruptionTest.class);

    // Counters
    public CountDownLatch waitForStart;
    public CountDownLatch waitForEnd;

    // Booleans
    public AtomicBoolean config;
    public AtomicBoolean interrupted;

    private void assertions(CompletableFuture<Object> task) throws InterruptedException {

        // This line is used to check that the task has already started
        LOGGER.info("assertions - waitForStart - {}", ZonedDateTime.now());
        waitForStart.await();

        // The task should not be done at that point
        assertFalse(task.isDone());

        // Cancel the task
        LOGGER.info("assertions - cancel - {}", ZonedDateTime.now());
        boolean cancelled = task.cancel(true);

        // Check that the task is cancelled
        LOGGER.info("assertions - isCancelled - {}", ZonedDateTime.now());
        assertTrue(cancelled);
        assertTrue(task.isCancelled());

        // Boolean stays at false if the task is cancelled
        LOGGER.info("assertions - get - {}", ZonedDateTime.now());
        assertFalse(config.get());

        // This should throw an exception since the task is cancelled
        assertThrows(CancellationException.class, () -> {
            task.get();
            fail("Should not happen: task has been cancelled");
        });

        // This line should return immediately since the task has been cancelled
        LOGGER.info("assertions - waitForEnd - {}", ZonedDateTime.now());
        waitUntilTaskEnd(task);
        LOGGER.info("assertions - ended - {}", ZonedDateTime.now());

        // This boolean is true if the task has been interrupted
        assertTrue(interrupted.get());

        // Second call to cancel should return false
        cancelled = task.cancel(true);
        assertFalse(cancelled);
    }

    private CompletableFuture<Object> createTask(Supplier<?> methodCalledInTask) {
        return CompletableFutureTask.runAsync(() -> {
            LOGGER.info("createTask - START - {}", ZonedDateTime.now());
            waitForStart.countDown();
            try {
                LOGGER.info("createTask - TRY - {}", ZonedDateTime.now());
                methodCalledInTask.get();
                LOGGER.info("createTask - FINISHED - {}", ZonedDateTime.now());
                config.set(true);
            } catch (Exception e) { // Thread interrupted => good
                LOGGER.info("createTask - INTERRUPTED - {}", ZonedDateTime.now());
                interrupted.set(true);
            } finally {
                waitForEnd.countDown();
            }
            return Boolean.TRUE;
        }, Executors.newSingleThreadExecutor());
    }

    /**
     * Test the interruption of a task containing a call to a specific method.
     * @param isDelayed boolean describing if a delay must be added between the task launch and the interruption
     * @param methodCalledInTask method called in the task
     * @throws InterruptedException Exception that should be thrown during the interruption
     */
    public void testCancelLongTask(boolean isDelayed, Supplier<?> methodCalledInTask) throws InterruptedException {
        testCancelLongTask(isDelayed ? 2000 : 0, methodCalledInTask);
    }

    /**
     * Test the interruption of a task containing a call to a specific method.
     * @param delayBeforeInterruption delay (in ms) between the task launch and the interruption
     * @param methodCalledInTask method called in the task
     * @throws InterruptedException Exception that should be thrown during the interruption
     */
    public void testCancelLongTask(int delayBeforeInterruption, Supplier<?> methodCalledInTask) throws InterruptedException {

        // Counters
        waitForStart = new CountDownLatch(1);
        waitForEnd = new CountDownLatch(1);

        // Booleans
        config = new AtomicBoolean(false);
        interrupted = new AtomicBoolean(false);

        // Task
        CompletableFuture<Object> task = createTask(methodCalledInTask);

        // If asked, wait a bit to simulate interruption by a user
        if (delayBeforeInterruption > 0) {
            Thread.sleep(delayBeforeInterruption);
        }

        assertions(task);
    }

    /**
     * Test the interruption of a task containing a call to a specific method.
     * @param isDelayed boolean describing if a delay must be added between the task launch and the interruption
     * @param methodCalledInTask method called in the task
     * @throws InterruptedException Exception that should be thrown during the interruption
     */
    public void testCancelShortTask(boolean isDelayed, Supplier<?> methodCalledInTask) throws InterruptedException {
        // When "isDelayed" is true, the test checks that interrupting a script after its execution (it's a *short* one) doesn't generate problems.
        //
        // When "isDelayed" is false,  the script should be interrupted before its execution's end. This could be either during the preparation phase
        // or during its execution. In both cases, the script should be interrupted and end gracefully...
        // ... but it is difficult to test both cases since it depends on the test machine's performances,
        // and we don't want to add a specific mechanism in the production code (which may slow the execution) only for test purposes.

        // Counters
        waitForStart = new CountDownLatch(1);
        waitForEnd = new CountDownLatch(1);

        // Booleans
        config = new AtomicBoolean(false);
        interrupted = new AtomicBoolean(false);

        // Task
        CompletableFuture<Object> task = createTask(methodCalledInTask);

        // If asked, wait a bit to simulate interruption by a user
        if (isDelayed) {
            // For short tasks, the cancellation may be done after the task's end.
            // We test here that it doesn't raise any problems.
            LOGGER.info("shortTask - waitForEnd - {}", ZonedDateTime.now());

            waitUntilTaskEnd(task);
            LOGGER.info("shortTask - ended - {}", ZonedDateTime.now());

            // Cancel the task (after its end)
            LOGGER.info("shortTask - cancel task - {}", ZonedDateTime.now());
            boolean cancelled = task.cancel(true);
            assertFalse(cancelled);
        } else {
            // If it's not delayed, the script didn't have enough time to finish yet
            assertions(task);
        }
    }

    private void waitUntilTaskEnd(CompletableFuture<Object> task) throws InterruptedException {
        // When the task is not interrupted, the latch is released before the task is properly ended.
        // We thus wait for the CompletableFuture's end in order to have the right CompletableFuture's status
        // when we will test it.
        // Note that when the task is interrupted, "task.join()" throws an Exception before the task is totally
        // finished. This is why we also use the latch.
        waitForEnd.await();
        try {
            task.join();
        } catch (CancellationException | CompletionException e) {
            LOGGER.info("Task ended with exception - {}", ZonedDateTime.now());
            LOGGER.info("{}", e.getMessage());
        }
    }
}
