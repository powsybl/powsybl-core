/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntConsumer;

/**
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
public class BigDoubleBuffer {

    private static final int BUFFER_SHIFT = 27;
    private static final int BUFFER_SIZE_DOUBLES = 1 << BUFFER_SHIFT;
    private static final int BUFFER_MASK = BUFFER_SIZE_DOUBLES - 1;
    private static final int BUFFER_SIZE_BYTES = BUFFER_SIZE_DOUBLES * Double.BYTES;
    private DoubleBuffer[] buffers;
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
        long computedBufferCount = (size + BUFFER_SIZE_DOUBLES - 1) >> BUFFER_SHIFT;
        if (computedBufferCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("size " + size + " is bigger than max allowed value " + (long) BUFFER_SIZE_DOUBLES * BUFFER_SIZE_DOUBLES);
        }
        int bufferCount = (int) computedBufferCount;
        long computedLastBufferSizeBytes = (size & BUFFER_MASK) * Double.BYTES;
        int lastBufferSizeBytes = (int) computedLastBufferSizeBytes;
        if (size > 0 && lastBufferSizeBytes == 0) {
            lastBufferSizeBytes = BUFFER_SIZE_BYTES;
        }
        bufferContainerInitializer.accept(bufferCount);
        for (int i = 0; i < bufferCount - 1; i++) {
            bufferInitializer.accept(i, BUFFER_SIZE_BYTES);
        }
        if (lastBufferSizeBytes > 0) {
            bufferInitializer.accept(bufferCount - 1, lastBufferSizeBytes);
        }
    }

    public BigDoubleBuffer(IntFunction<ByteBuffer> byteBufferAllocator, long size) {
        Objects.requireNonNull(byteBufferAllocator);
        withSizes(size,
            bufferCount -> buffers = new DoubleBuffer[bufferCount],
            (i, bufferSize) -> buffers[i] = byteBufferAllocator.apply(bufferSize).asDoubleBuffer()
        );
        this.size = size;
    }

    //To remove if we ever get it from somewhere else
    //package private for tests
    @FunctionalInterface interface IntIntToDoubleBiFunction { public double applyAsDouble(int a, int b); }

    //using a lambda to test independently from java.nio.ByteBuffer
    //package private for tests
    static double withIndices(long index, IntIntToDoubleBiFunction indicesBiFunction) {
        long computedBufferIndex = index >> BUFFER_SHIFT;
        long computedSecondIndex = index & BUFFER_MASK;
        int bufferIndex = (int) computedBufferIndex;
        int secondIndex = (int) computedSecondIndex;
        return indicesBiFunction.applyAsDouble(bufferIndex, secondIndex);
    }

    public void put(long index, double value) {
        withIndices(index, (bufferIndex, secondIndex) -> {
            buffers[bufferIndex].put(secondIndex, value);
            return Double.NaN; // just to reuse the withIndices code
        });
    }

    public double get(long index) {
        return withIndices(index, (bufferIndex, secondIndex) ->
            buffers[bufferIndex].get(secondIndex)
        );
    }

    public long capacity() {
        return size;
    }
}
