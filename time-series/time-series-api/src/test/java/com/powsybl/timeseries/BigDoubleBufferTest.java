/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class BigDoubleBufferTest {

    private static final int BUFFER_SIZE_DOUBLES = 1 << 27;

    private int allocatorCount;

    private ByteBuffer testDoubleAllocator(int capacity) {
        allocatorCount++;
        ByteBuffer mockbyte = Mockito.mock(ByteBuffer.class);
        DoubleBuffer mockdouble = Mockito.mock(DoubleBuffer.class);
        when(mockbyte.asDoubleBuffer()).thenReturn(mockdouble);
        Map<Integer, Double> map = new HashMap<>();
        when(mockdouble.put(anyInt(), anyDouble())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            DoubleBuffer mock = (DoubleBuffer) invocation.getMock();
            map.put((int) args[0], (double) args[1]);
            return mock;
        });
        when(mockdouble.get(anyInt())).thenAnswer(invocation -> {
            return map.get(invocation.getArguments()[0]);
        });
        return mockbyte;
    }

    @Before
    public void before() {
        allocatorCount = 0;
    }

    private void bufferTester(long size) {
        BigDoubleBuffer buffer = new BigDoubleBuffer(this::testDoubleAllocator, size);
        //Simple writes at the begining
        for (int i = 0; i < 10; i++) {
            buffer.put(i, i);
        }
        //writes around first buffer change
        if (size > BUFFER_SIZE_DOUBLES + 10) {
            for (int i = BUFFER_SIZE_DOUBLES - 10; i < BUFFER_SIZE_DOUBLES + 10; i++) {
                buffer.put(i, i);
            }
        }
        //writes at the end
        for (long i = size - 10; i < size; i++) {
            buffer.put(i, i);
        }

        for (int i = 0; i < 10; i++) {
            assertEquals(i, buffer.get(i), 0);
        }
        if (size > BUFFER_SIZE_DOUBLES + 10) {
            for (int i = BUFFER_SIZE_DOUBLES - 10; i < BUFFER_SIZE_DOUBLES + 10; i++) {
                assertEquals(i, buffer.get(i), 0);
            }
        }
        for (long i = size - 10; i < size; i++) {
            assertEquals(i, buffer.get(i), 0);
        }
    }

    @Test
    public void testSimple() {
        bufferTester(10);
        assertEquals(1, allocatorCount);
    }

    @Test
    public void testMultipleBuffers() {
        bufferTester(200000000);
        assertEquals(2, allocatorCount);
    }

    @Test
    public void testHuge() {
        bufferTester(10000000000L);
        assertEquals(75, allocatorCount);
    }

    @Test
    public void testSizeBufferMinus1() {
        bufferTester(BUFFER_SIZE_DOUBLES - 1);
        assertEquals(1, allocatorCount);
    }

    @Test
    public void testSizeBufferExact() {
        bufferTester(BUFFER_SIZE_DOUBLES);
        assertEquals(1, allocatorCount);
    }

    @Test
    public void testSizeBufferPlus1() {
        bufferTester(BUFFER_SIZE_DOUBLES + 1);
        assertEquals(2, allocatorCount);
    }
}
