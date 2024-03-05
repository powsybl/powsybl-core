/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractTaskInterruptionTest {

    // Counters
    public CountDownLatch waitForStart;
    public CountDownLatch waitForFinish;
    public CountDownLatch waitForInterruption;

    // Booleans
    public AtomicBoolean config;
    public AtomicBoolean interrupted;

    private void assertions(CompletableFuture<Object> task) throws InterruptedException {

        // This line is used to check that the task has already started
        waitForStart.await();

        // The task should not be done at that point
        assertFalse(task.isDone());

        // Cancel the task
        boolean cancelled = task.cancel(true);

        // Check that the task is cancelled
        assertTrue(cancelled);
        assertTrue(task.isCancelled());

        // Boolean stays at false if the task is cancelled
        assertFalse(config.get());

        // This should throw an exception since the task is cancelled
        assertThrows(CancellationException.class, () -> {
            task.get();
            fail("Should not happen: task has been cancelled");
        });

        // This line should return immediately since the task has been cancelled
        waitForInterruption.await();

        // This boolean is true if the task has been interrupted
        assertTrue(interrupted.get());

        // Second call to cancel should return false
        cancelled = task.cancel(true);
        assertFalse(cancelled);
    }

    /**
     * Test the interruption of a task containing a call to a specific method.
     * @param isDelayed boolean describing if a delay must be added between the task launch and the interruption
     * @param methodCalledInTask method called in the task
     * @throws Exception Exception that should be thrown during the interruption
     */
    public void testCancelTask(boolean isDelayed, Supplier<?> methodCalledInTask) throws Exception {
        CompletableFuture<Object> task = CompletableFutureTask.runAsync(() -> {
            waitForStart.countDown();
            try {
                methodCalledInTask.get();
                config.set(true);
                waitForFinish.countDown();
            } catch (Exception e) { // Thread interrupted => good
                interrupted.set(true);
                waitForInterruption.countDown();
            }
            return null;
        }, Executors.newSingleThreadExecutor());

        // If asked, wait a bit to simulate interruption by a user
        if (isDelayed) {
            await()
                .during(800, TimeUnit.MILLISECONDS)
                .atMost(1850, TimeUnit.MILLISECONDS)
                .until(() -> true);
        }

        assertions(task);
    }
}
