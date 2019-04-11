/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.local;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.*;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ProcessHelperTest {

    private InputStream in;
    private OutputStream out;
    private InputStream err;
    private Process process;

    @Before
    public void setup() {
        in = mock(InputStream.class);
        out = mock(OutputStream.class);
        err = mock(InputStream.class);
        process = mock(Process.class);
        when(process.getErrorStream()).thenReturn(err);
        when(process.getInputStream()).thenReturn(in);
        when(process.getOutputStream()).thenReturn(out);
    }

    @Test
    public void testIllegalTimeout() {
        try {
            int exitCode = ProcessHelper.runWithTimeout(-1, process);
            fail();
        } catch (Exception e) {
            assertEquals("negative timeout: -1", e.getMessage());
        }
    }

    @Test
    public void testWithTimeout() {
        try {
            // process finishes in 1 second
            when(process.exitValue()).thenThrow(new IllegalThreadStateException())
                    .thenThrow(new IllegalThreadStateException())
                    .thenReturn(0);

            int exitCode = ProcessHelper.runWithTimeout(10, process);

            assertEquals(0, exitCode);
            verify(process, never()).waitFor();
            verify(process, times(3)).exitValue();
            verify(process, never()).destroy();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testTimeouted() {
        try {
            // process never finish
            when(process.exitValue()).thenThrow(new IllegalThreadStateException());

            int exitCode = ProcessHelper.runWithTimeout(2, process);

            assertEquals(124, exitCode);
            verify(process, never()).waitFor();
            verify(process, times(1)).destroy();
        } catch (Exception e) {
            fail();
        }
    }
}
