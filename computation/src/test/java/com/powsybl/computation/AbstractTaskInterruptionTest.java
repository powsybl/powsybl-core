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
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
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
    public CountDownLatch waitForFinish;
    public CountDownLatch waitForInterruption;

    // Booleans
    public AtomicBoolean config;
    public AtomicBoolean interrupted;

    private void assertions(CompletableFuture<Object> task) throws InterruptedException {

        // This line is used to check that the task has already started
        LOGGER.warn("assertions - waitForStart - " + ZonedDateTime.now());
        waitForStart.await();

        // The task should not be done at that point
        LOGGER.warn("assertions - isDone - " + ZonedDateTime.now());
        assertFalse(task.isDone());

        // Cancel the task
        LOGGER.warn("assertions - cancel - " + ZonedDateTime.now());
        boolean cancelled = task.cancel(true);

        // Check that the task is cancelled
        LOGGER.warn("assertions - isCancelled - " + ZonedDateTime.now());
        assertTrue(cancelled);
        assertTrue(task.isCancelled());

        // Boolean stays at false if the task is cancelled
        LOGGER.warn("assertions - get - " + ZonedDateTime.now());
        assertFalse(config.get());

        // This should throw an exception since the task is cancelled
        assertThrows(CancellationException.class, () -> {
            task.get();
            fail("Should not happen: task has been cancelled");
        });

        // This line should return immediately since the task has been cancelled
        LOGGER.warn("assertions - waitForInterruption - " + ZonedDateTime.now());
        waitForInterruption.await();

        // This boolean is true if the task has been interrupted
        assertTrue(interrupted.get());

        // Second call to cancel should return false
        cancelled = task.cancel(true);
        assertFalse(cancelled);
    }

    private CompletableFuture<Object> createTask(Supplier<?> methodCalledInTask) {
        return CompletableFutureTask.runAsync(() -> {
            LOGGER.warn("createTask - START - " + ZonedDateTime.now());
            waitForStart.countDown();
            try {
                LOGGER.warn("createTask - TRY - " + ZonedDateTime.now());
                methodCalledInTask.get();
                LOGGER.warn("createTask - CONFIG TRUE - " + ZonedDateTime.now());
                config.set(true);
                waitForFinish.countDown();
            } catch (Exception e) { // Thread interrupted => good
                LOGGER.warn("createTask - INTERRUPT - " + ZonedDateTime.now());
                interrupted.set(true);
                waitForInterruption.countDown();
            }
            LOGGER.warn("createTask - END - " + ZonedDateTime.now());
            return null;
        }, Executors.newSingleThreadExecutor());
    }

    /**
     * Test the interruption of a task containing a call to a specific method.
     * @param isDelayed boolean describing if a delay must be added between the task launch and the interruption
     * @param methodCalledInTask method called in the task
     * @throws InterruptedException Exception that should be thrown during the interruption
     */
    public void testCancelLongTask(boolean isDelayed, Supplier<?> methodCalledInTask) throws InterruptedException {

        // Counters
        waitForStart = new CountDownLatch(1);
        waitForFinish = new CountDownLatch(1);
        waitForInterruption = new CountDownLatch(1);

        // Booleans
        config = new AtomicBoolean(false);
        interrupted = new AtomicBoolean(false);

        // Task
        CompletableFuture<Object> task = createTask(methodCalledInTask);

        // If asked, wait a bit to simulate interruption by a user
        if (isDelayed) {
            Thread.sleep(800);
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

        // Counters
        waitForStart = new CountDownLatch(1);
        waitForFinish = new CountDownLatch(1);
        waitForInterruption = new CountDownLatch(1);

        // Booleans
        config = new AtomicBoolean(false);
        interrupted = new AtomicBoolean(false);

        // Task
        CompletableFuture<Object> task = createTask(methodCalledInTask);

        // If asked, wait a bit to simulate interruption by a user
        if (isDelayed) {
            Thread.sleep(800);

            // This line is used to check that the task has already started
            waitForStart.await();

            // the script was to short to be interrupted before its end so the task is done
            assertTrue(task.isDone());

            // Cancel the task
            boolean cancelled = task.cancel(true);
            assertFalse(cancelled);
        } else {
            // If it's not delayed, the script didn't have enough time to finish yet
            assertions(task);
        }
    }
}
