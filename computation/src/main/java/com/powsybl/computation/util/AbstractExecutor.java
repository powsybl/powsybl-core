/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.util;

import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractExecutor {

    private final ReentrantLock runLock = new ReentrantLock();

    private boolean initialized = false;

    private boolean shutdownRequested = false;

    private int runCounter = 0;

    protected void startInit() throws Exception {
        runLock.lock();
        try {
            if (initialized) {
                throw new IllegalStateException("Module is already initialized");
            }
        } finally {
            runLock.unlock();
        }
    }

    protected void endInit() throws Exception {
        runLock.lock();
        try {
            initialized = true;
        } finally {
            runLock.unlock();
        }
    }

    protected abstract void clean() throws Exception;

    protected void startRun() throws Exception {
        runLock.lock();
        try {
            if (!initialized) {
                throw new IllegalStateException("Module is not initialized");
            }
            if (shutdownRequested) {
                throw new IllegalStateException("Module has been shutdown");
            }
            runCounter++;
        } finally {
            runLock.unlock();
        }
    }

    protected void endRun() throws Exception {
        runLock.lock();
        try {
            runCounter--;
            if (shutdownRequested && runCounter == 0) {
                clean();
            }
        } finally {
            runLock.unlock();
        }
    }

    public void close() throws Exception {
        runLock.lock();
        try {
            shutdownRequested = true;
            if (runCounter == 0) {
                clean();
            }
        } finally {
            runLock.unlock();
        }
    }
}
