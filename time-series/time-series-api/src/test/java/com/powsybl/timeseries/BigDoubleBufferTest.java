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
import com.powsybl.timeseries.BigDoubleBuffer.IntIntBiConsumer;
import com.powsybl.timeseries.BigDoubleBuffer.IntIntToDoubleBiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
class BigDoubleBufferTest {

    private static final int BUFFER_SIZE_DOUBLES = 1 << 27;

    private int allocatorCount;

    private ByteBuffer testDoubleAllocator(int capacity) {
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
        BigDoubleBuffer buffer = new BigDoubleBuffer(this::testDoubleAllocator, size);
        assertEquals(1, allocatorCount);
        //Simple writes at the begining
        for (int i = 0; i < 10; i++) {
            buffer.put(i, i);
        }

        for (int i = 0; i < 10; i++) {
            assertEquals(i, buffer.get(i), 0);
        }
    }

    @Test
    void testWithIndices() {
        IntIntToDoubleBiFunction indicesBiFunction = Mockito.mock(IntIntToDoubleBiFunction.class);
        BigDoubleBuffer.withIndices(10, indicesBiFunction);
        verify(indicesBiFunction).applyAsDouble(0, 10);
        verifyNoMoreInteractions(indicesBiFunction);

        //writes around first buffer change
        BigDoubleBuffer.withIndices(BUFFER_SIZE_DOUBLES - 1, indicesBiFunction);
        verify(indicesBiFunction).applyAsDouble(0, BUFFER_SIZE_DOUBLES - 1);
        verifyNoMoreInteractions(indicesBiFunction);
        BigDoubleBuffer.withIndices(BUFFER_SIZE_DOUBLES, indicesBiFunction);
        verify(indicesBiFunction).applyAsDouble(1, 0);
        verifyNoMoreInteractions(indicesBiFunction);

        //writes around random buffer change
        BigDoubleBuffer.withIndices(7 * BUFFER_SIZE_DOUBLES - 1, indicesBiFunction);
        verify(indicesBiFunction).applyAsDouble(6, BUFFER_SIZE_DOUBLES - 1);
        verifyNoMoreInteractions(indicesBiFunction);
        BigDoubleBuffer.withIndices(7 * BUFFER_SIZE_DOUBLES, indicesBiFunction);
        verify(indicesBiFunction).applyAsDouble(7, 0);
        verifyNoMoreInteractions(indicesBiFunction);
    }

    void indicesTester(long size, int bufferCount) {
        IntConsumer bufferContainerInitializer = Mockito.mock(IntConsumer.class);
        IntIntBiConsumer bufferInitializer = Mockito.mock(IntIntBiConsumer.class);
        BigDoubleBuffer.withSizes(size,
                bufferContainerInitializer,
                bufferInitializer
        );
        verify(bufferContainerInitializer).accept(bufferCount);
        verifyNoMoreInteractions(bufferContainerInitializer);
        for (int i = 0; i < bufferCount - 1; i++) {
            verify(bufferInitializer).accept(i, BUFFER_SIZE_DOUBLES * Double.BYTES);
        }
        verify(bufferInitializer).accept(bufferCount - 1, (int) (size - (bufferCount - 1) * BUFFER_SIZE_DOUBLES) * Double.BYTES);
        verifyNoMoreInteractions(bufferInitializer);
    }

    @Test
    void testMultipleBuffers() {
        indicesTester(200000000, 2);
    }

    @Test
    void testHuge() {
        indicesTester(10000000000L, 75);
    }

    @Test
    void testSizeBufferMinus1() {
        indicesTester(BUFFER_SIZE_DOUBLES - 1, 1);
    }

    @Test
    void testSizeBufferExact() {
        indicesTester(BUFFER_SIZE_DOUBLES, 1);
    }

    @Test
    void testSizeBufferPlus1() {
        indicesTester(BUFFER_SIZE_DOUBLES + 1, 2);
    }
}
