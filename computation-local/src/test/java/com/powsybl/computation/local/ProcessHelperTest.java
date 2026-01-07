/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation.local;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
class ProcessHelperTest {

    private InputStream in;
    private OutputStream out;
    private InputStream err;
    private Process process;

    @BeforeEach
    void setup() {
        in = mock(InputStream.class);
        out = mock(OutputStream.class);
        err = mock(InputStream.class);
        process = mock(Process.class);
        when(process.getErrorStream()).thenReturn(err);
        when(process.getInputStream()).thenReturn(in);
        when(process.getOutputStream()).thenReturn(out);
    }

    @Test
    void testIllegalTimeout() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> ProcessHelper.runWithTimeout(-1, process));
        assertEquals("negative timeout: -1", exception.getMessage());
    }

    @Test
    void testProcessFinishedBeforeTimeout() {
        try {
            when(process.waitFor(10, TimeUnit.SECONDS)).thenReturn(true);
            when(process.exitValue()).thenReturn(0);

            int exitCode = ProcessHelper.runWithTimeout(10, process);

            assertEquals(0, exitCode);
            verify(process, times(1)).waitFor(10, TimeUnit.SECONDS);
            verify(process, times(1)).exitValue();
            verify(in, times(1)).close();
            verify(out, times(1)).close();
            verify(err, times(1)).close();
            verify(process, never()).destroy();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void testTimeoutBeforeProcessFinishes() {
        try {
            // process never finish
            when(process.waitFor(2, TimeUnit.SECONDS)).thenReturn(false);

            int exitCode = ProcessHelper.runWithTimeout(2, process);

            assertEquals(124, exitCode);
            verify(process, times(1)).waitFor(2, TimeUnit.SECONDS);
            verify(process, never()).exitValue();
            verify(in, times(1)).close();
            verify(out, times(1)).close();
            verify(err, times(1)).close();
            verify(process, times(1)).destroy();
        } catch (Exception e) {
            fail();
        }
    }
}
