/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
class BigStringBufferTest extends AbstractBigBufferTest {

    private static final int BUFFER_SIZE_INTS = 1 << 28;

    private void bufferTester(long size) {
        BigStringBuffer buffer = new BigStringBuffer(this::testAllocator, size);
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
        assertEquals(1, channels.size());
    }

    @Test
    void testMultipleBuffers() {
        bufferTester(400000000);
        assertEquals(2, channels.size());
    }

    @Test
    void testHuge() {
        bufferTester(10000000000L);
        assertEquals(38, channels.size());
    }

    @Test
    void testSizeBufferMinus1() {
        bufferTester(BUFFER_SIZE_INTS - 1);
        assertEquals(1, channels.size());
    }

    @Test
    void testSizeBufferExact() {
        bufferTester(BUFFER_SIZE_INTS);
        assertEquals(1, channels.size());
    }

    @Test
    void testSizeBufferPlus1() {
        bufferTester(BUFFER_SIZE_INTS + 1);
        assertEquals(2, channels.size());
    }
}
