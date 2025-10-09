/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntConsumer;

/**
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
public class BigStringBuffer {

    private static final int BUFFER_SHIFT = 28;
    private static final int BUFFER_SIZE_INTS = 1 << BUFFER_SHIFT;
    private static final int BUFFER_MASK = BUFFER_SIZE_INTS - 1;
    private CompactStringBuffer[] buffers;
    private final long size;

    //To remove if we ever get it from somewhere else
    //package private for tests
    @FunctionalInterface interface IntIntBiConsumer { public void accept(int a, int b); }

    //using a lambda to test independently from java.nio.ByteBuffer
    //package private for tests
    static void withSizes(long size, IntConsumer bufferContainerInitializer, IntIntBiConsumer bufferInitializer) {
        if (size < 0) {
            throw new IllegalArgumentException("Invalid buffer size: " + size);
        }
        long computedBufferCount = (size + BUFFER_SIZE_INTS - 1) >> BUFFER_SHIFT;
        if (computedBufferCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("size " + size + " is bigger than max allowed value " + (long) BUFFER_SIZE_INTS * BUFFER_SIZE_INTS);
        }
        int bufferCount = (int) computedBufferCount;
        long computedLastBufferSizeInts = size & BUFFER_MASK;
        int lastBufferSizeInts = (int) computedLastBufferSizeInts;
        if (size > 0 && lastBufferSizeInts == 0) {
            lastBufferSizeInts = BUFFER_SIZE_INTS;
        }
        bufferContainerInitializer.accept(bufferCount);
        for (int i = 0; i < bufferCount - 1; i++) {
            bufferInitializer.accept(i, BUFFER_SIZE_INTS);
        }
        if (lastBufferSizeInts > 0) {
            bufferInitializer.accept(bufferCount - 1, lastBufferSizeInts);
        }
    }

    public BigStringBuffer(IntFunction<ByteBuffer> byteBufferAllocator, long size) {
        Objects.requireNonNull(byteBufferAllocator);
        withSizes(size,
            bufferCount -> buffers = new CompactStringBuffer[bufferCount],
            (i, bufferSize) -> buffers[i] = new CompactStringBuffer(byteBufferAllocator, bufferSize)
        );
        this.size = size;
    }

    //To remove if we ever get it from somewhere else
    //package private for tests
    @FunctionalInterface interface IntIntBiFunction { public String apply(int a, int b); }

    //using a lambda to test independently from java.nio.ByteBuffer
    //package private for tests
    static String withIndices(long index, IntIntBiFunction indicesBiFunction) {
        long computedBufferIndex = index >> BUFFER_SHIFT;
        long computedSecondIndex = index & BUFFER_MASK;
        int bufferIndex = (int) computedBufferIndex;
        int secondIndex = (int) computedSecondIndex;
        return indicesBiFunction.apply(bufferIndex, secondIndex);
    }

    public void putString(long index, String value) {
        withIndices(index, (bufferIndex, secondIndex) -> {
            buffers[bufferIndex].putString(secondIndex, value);
            return null; // just to reuse the withIndices code
        });
    }

    public String getString(long index) {
        return withIndices(index, (bufferIndex, secondIndex) ->
            buffers[bufferIndex].getString(secondIndex)
        );
    }

    public long capacity() {
        return size;
    }
}
