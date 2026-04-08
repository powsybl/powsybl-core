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

    public static final int TIMEOUT_EXIT_CODE = 124;

    /**
     * This method blocks the current thread until either:
     * <ul>
     *     <li>The process terminates, in which case it returns the exit code from {@link Process#exitValue()}</li>
     *     <li>The timeout is reached, in which case the process is destroyed and the exit code {@value #TIMEOUT_EXIT_CODE} is returned</li>
     * </ul>
     * @param process The process to run
     * @return Returns exit code for the process, or {@value #TIMEOUT_EXIT_CODE} if the process timeouts.
     * @throws InterruptedException
     * @throws IOException
     */
    static int runWithTimeout(long timeoutInSeconds, Process process) throws InterruptedException, IOException {
        Preconditions.checkArgument(timeoutInSeconds > 0, "negative timeout: %s", timeoutInSeconds);
        if (process.waitFor(timeoutInSeconds, TimeUnit.SECONDS)) {
            closeStream(process);
            return process.exitValue();
        }
        process.destroy();
        closeStream(process);
        return TIMEOUT_EXIT_CODE;
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
