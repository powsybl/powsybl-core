/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class BigDoubleBuffer {

    private static final int BUFFER_SHIFT = 27;
    private static final int BUFFER_SIZE_DOUBLES = 1 << BUFFER_SHIFT;
    private static final int BUFFER_MASK = BUFFER_SIZE_DOUBLES - 1;
    private static final int BUFFER_SIZE_BYTES = BUFFER_SIZE_DOUBLES * Double.BYTES;
    private DoubleBuffer[] buffers;
    private long size;

    public BigDoubleBuffer(IntFunction<ByteBuffer> byteBufferAllocator, long size) {
        Objects.requireNonNull(byteBufferAllocator);
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
        buffers = new DoubleBuffer[bufferCount];
        for (int i = 0; i < bufferCount - 1; i++) {
            buffers[i] = byteBufferAllocator.apply(BUFFER_SIZE_BYTES).asDoubleBuffer();
        }
        if (lastBufferSizeBytes > 0) {
            buffers[bufferCount - 1] = byteBufferAllocator.apply(lastBufferSizeBytes).asDoubleBuffer();
        }
        this.size = size;
    }

    public void put(long index, double value) {
        long computedBufferIndex = index >> BUFFER_SHIFT;
        long computedSecondIndex = index & BUFFER_MASK;
        int bufferIndex = (int) computedBufferIndex;
        int secondIndex = (int) computedSecondIndex;
        buffers[bufferIndex].put(secondIndex, value);
    }

    public double get(long index) {
        long computedBufferIndex = index >> BUFFER_SHIFT;
        long computedSecondIndex = index & BUFFER_MASK;
        int bufferIndex = (int) computedBufferIndex;
        int secondIndex = (int) computedSecondIndex;
        return buffers[bufferIndex].get(secondIndex);
    }

    public long capacity() {
        return size;
    }
}
