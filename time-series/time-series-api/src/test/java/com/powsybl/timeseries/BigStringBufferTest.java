/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
class BigStringBufferTest {

    private static final int BUFFER_SIZE_INTS = 1 << 28;

    private int allocatorCount;

    private ByteBuffer testStringAllocator(int capacity) {
        allocatorCount++;
        ByteBuffer mockbyte = Mockito.mock(ByteBuffer.class);
        IntBuffer mockint = Mockito.mock(IntBuffer.class);
        when(mockbyte.asIntBuffer()).thenReturn(mockint);
        Map<Integer, Integer> map = new HashMap<>();
        when(mockint.put(anyInt(), anyInt())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            IntBuffer mock = (IntBuffer) invocation.getMock();
            map.put((int) args[0], (int) args[1]);
            return mock;
        });
        when(mockint.get(anyInt())).thenAnswer(invocation -> {
            return map.get(invocation.getArguments()[0]);
        });
        return mockbyte;
    }

    @BeforeEach
    void before() {
        allocatorCount = 0;
    }

    private void bufferTester(long size) {
        BigStringBuffer buffer = new BigStringBuffer(this::testStringAllocator, size);
        //Simple writes at the begining
        for (int i = 0; i < 10; i++) {
            buffer.putString(i, Integer.toString(i));
        }
        //writes around first buffer change
        if (size > BUFFER_SIZE_INTS + 10) {
            for (int i = BUFFER_SIZE_INTS - 10; i < BUFFER_SIZE_INTS + 10; i++) {
                buffer.putString(i, Integer.toString(i));
            }
        }
        //writes at the end
        for (long i = size - 10; i < size; i++) {
            buffer.putString(i, Long.toString(i));
        }

        for (int i = 0; i < 10; i++) {
            assertEquals(Integer.toString(i), buffer.getString(i));
        }
        if (size > BUFFER_SIZE_INTS + 10) {
            for (int i = BUFFER_SIZE_INTS - 10; i < BUFFER_SIZE_INTS + 10; i++) {
                assertEquals(Integer.toString(i), buffer.getString(i));
            }
        }
        for (long i = size - 10; i < size; i++) {
            assertEquals(Long.toString(i), buffer.getString(i));
        }
    }

    @Test
    void testSimple() {
        bufferTester(10);
        assertEquals(1, allocatorCount);
    }

    @Test
    void testMultipleBuffers() {
        bufferTester(400000000);
        assertEquals(2, allocatorCount);
    }

    @Test
    void testHuge() {
        bufferTester(10000000000L);
        assertEquals(38, allocatorCount);
    }

    @Test
    void testSizeBufferMinus1() {
        bufferTester(BUFFER_SIZE_INTS - 1);
        assertEquals(1, allocatorCount);
    }

    @Test
    void testSizeBufferExact() {
        bufferTester(BUFFER_SIZE_INTS);
        assertEquals(1, allocatorCount);
    }

    @Test
    void testSizeBufferPlus1() {
        bufferTester(BUFFER_SIZE_INTS + 1);
        assertEquals(2, allocatorCount);
    }
}
