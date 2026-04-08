/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.util.function.IntConsumer;
import com.powsybl.timeseries.BigStringBuffer.IntIntBiConsumer;
import com.powsybl.timeseries.BigStringBuffer.IntIntBiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
class BigStringBufferTest {

    private static final int BUFFER_SIZE_INTS = 1 << 28;

    private int allocatorCount;

    private ByteBuffer testStringAllocator(int capacity) {
        try {
            ByteBuffer bytebuffer = ByteBuffer.allocate(capacity);
            allocatorCount++;
            return bytebuffer;
        } catch (Exception e) {
            throw new RuntimeException("error in allocator test", e);
        }
    }

    @BeforeEach
    void before() {
        allocatorCount = 0;
    }

    @Test
    void testSimple() {
        long size = 10L;
        BigStringBuffer buffer = new BigStringBuffer(this::testStringAllocator, size);
        assertEquals(1, allocatorCount);
        //Simple writes at the begining
        for (int i = 0; i < 10; i++) {
            buffer.putString(i, Integer.toString(i));
        }

        for (long i = size - 10; i < size; i++) {
            assertEquals(Long.toString(i), buffer.getString(i));
        }
    }

    @Test
    void testWithIndices() {
        IntIntBiFunction indicesBiFunction = Mockito.mock(IntIntBiFunction.class);
        BigStringBuffer.withIndices(10, indicesBiFunction);
        verify(indicesBiFunction).apply(0, 10);
        verifyNoMoreInteractions(indicesBiFunction);

        //writes around first buffer change
        BigStringBuffer.withIndices(BUFFER_SIZE_INTS - 1, indicesBiFunction);
        verify(indicesBiFunction).apply(0, BUFFER_SIZE_INTS - 1);
        verifyNoMoreInteractions(indicesBiFunction);
        BigStringBuffer.withIndices(BUFFER_SIZE_INTS, indicesBiFunction);
        verify(indicesBiFunction).apply(1, 0);
        verifyNoMoreInteractions(indicesBiFunction);

        //writes around random buffer change
        BigStringBuffer.withIndices(7 * BUFFER_SIZE_INTS - 1, indicesBiFunction);
        verify(indicesBiFunction).apply(6, BUFFER_SIZE_INTS - 1);
        verifyNoMoreInteractions(indicesBiFunction);
        BigStringBuffer.withIndices(7 * BUFFER_SIZE_INTS, indicesBiFunction);
        verify(indicesBiFunction).apply(7, 0);
        verifyNoMoreInteractions(indicesBiFunction);
    }

    void indicesTester(long size, int bufferCount) {
        IntConsumer bufferContainerInitializer = Mockito.mock(IntConsumer.class);
        IntIntBiConsumer bufferInitializer = Mockito.mock(IntIntBiConsumer.class);
        BigStringBuffer.withSizes(size,
                bufferContainerInitializer,
                bufferInitializer
        );
        verify(bufferContainerInitializer).accept(bufferCount);
        verifyNoMoreInteractions(bufferContainerInitializer);
        for (int i = 0; i < bufferCount - 1; i++) {
            verify(bufferInitializer).accept(i, BUFFER_SIZE_INTS);
        }
        verify(bufferInitializer).accept(bufferCount - 1, (int) (size - (bufferCount - 1) * BUFFER_SIZE_INTS));
        verifyNoMoreInteractions(bufferInitializer);
    }

    @Test
    void testMultipleBuffers() {
        indicesTester(400000000, 2);
    }

    @Test
    void testHuge() {
        indicesTester(10000000000L, 38);
    }

    @Test
    void testSizeBufferMinus1() {
        indicesTester(BUFFER_SIZE_INTS - 1, 1);
    }

    @Test
    void testSizeBufferExact() {
        indicesTester(BUFFER_SIZE_INTS, 1);
    }

    @Test
    void testSizeBufferPlus1() {
        indicesTester(BUFFER_SIZE_INTS + 1, 2);
    }
}
