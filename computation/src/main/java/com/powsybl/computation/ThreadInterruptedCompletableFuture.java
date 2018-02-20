/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ThreadInterruptedCompletableFuture<R> extends CompletableFuture<R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadInterruptedCompletableFuture.class);

    protected volatile boolean cancel = false;
    protected Thread thread;

    public void setThread(Thread t) {
        thread = Objects.requireNonNull(t);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        cancel = true;

        if (this.isDone() || this.isCompletedExceptionally()) {
            LOGGER.warn("Can not be canceled because the caller future isDone or isCompletedExceptionally");
            return false;
        }

        while (thread == null) {
            try {
                LOGGER.warn("Waiting 1s for taskThread to be set...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.warn(e.toString(), e);
                Thread.currentThread().interrupt();
            }
        }

        thread.interrupt();
        return true;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }
}
