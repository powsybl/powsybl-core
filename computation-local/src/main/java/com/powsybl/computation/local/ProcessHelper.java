/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation.local;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
public final class ProcessHelper {

    private static final long DEFAULT_FAST_POLLING = 500_000_000; // 0.5 seconds
    private static final long BEGINING_NANO = 3_000_000_000L; // 3 seconds

    /**
     * It requests for exit code by {@link java.lang.Process#exitValue()} every half second at the begining of 3 seconds.
     * Then it will no more request for exit code until 80% of {@code timeoutInSeconds}.
     * @param process The process to run
     * @return Returns exit code for the process. Returns 124 if process timeout.
     * @throws InterruptedException
     * @throws IOException
     */
    static int runWithTimeout(long timeoutInSeconds, Process process) throws InterruptedException, IOException {
        Preconditions.checkArgument(timeoutInSeconds > 0, "negative timeout: %s", timeoutInSeconds);
        int exitCode;
        long startTimeNano = System.nanoTime();
        while (true) {
            try {
                exitCode = process.exitValue();
                closeStream(process);
                return exitCode;
            } catch (IllegalThreadStateException ex) {
                long running = System.nanoTime() - startTimeNano;
                if (running > TimeUnit.SECONDS.toNanos(timeoutInSeconds)) {
                    break;
                }
                TimeUnit.NANOSECONDS.sleep(smartWait(running, startTimeNano, timeoutInSeconds));
            }
        }
        process.destroy();
        exitCode = 124;

        closeStream(process);
        return exitCode;
    }

    private static long smartWait(long running, long startTime, long timeout) {
        if (running < BEGINING_NANO || running > 0.8 * TimeUnit.SECONDS.toNanos(timeout)) {
            return DEFAULT_FAST_POLLING;
        }
        return (long) (TimeUnit.SECONDS.toNanos(timeout) * 0.8) - running;
    }

    private static void closeStream(Process process) throws IOException {
        // to avoid 'too many open files' exception
        process.getInputStream().close();
        process.getOutputStream().close();
        process.getErrorStream().close();
    }

    private ProcessHelper() {
    }
}
