/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class ThreadInterruptedCompletableFutureTest {

    @Test
    public void test() throws Exception {
        ThreadInterruptedCompletableFuture<?> foo = new ThreadInterruptedCompletableFuture<>();
        boolean[] result = new boolean[2];
        Thread t = new Thread() {
            @Override
            public void run() {
                result[0] = foo.cancel(true);
                result[1] = Thread.currentThread().isInterrupted();
            }
        };
        t.start();
        t.interrupt();
        t.join(100);
        assertFalse(result[0]);
        assertTrue(result[1]);
    }
}
