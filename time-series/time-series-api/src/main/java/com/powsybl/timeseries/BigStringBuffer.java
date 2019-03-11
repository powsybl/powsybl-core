/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class BigStringBuffer {

    private static final int BUFFER_SHIFT = 28;
    private static final int BUFFER_SIZE_INTS = 1 << BUFFER_SHIFT;
    private static final int BUFFER_MASK = BUFFER_SIZE_INTS - 1;
    private CompactStringBuffer[] buffers;
    private long size;

    public BigStringBuffer(IntFunction<ByteBuffer> byteBufferAllocator, long size) {
        Objects.requireNonNull(byteBufferAllocator);
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
        buffers = new CompactStringBuffer[bufferCount];
        for (int i = 0; i < bufferCount - 1; i++) {
            buffers[i] = new CompactStringBuffer(byteBufferAllocator, BUFFER_SIZE_INTS);
        }
        if (lastBufferSizeInts > 0) {
            buffers[bufferCount - 1] = new CompactStringBuffer(byteBufferAllocator, lastBufferSizeInts);
        }
        this.size = size;
    }

    public void putString(long index, String value) {
        long computedBufferIndex = index >> BUFFER_SHIFT;
        long computedSecondIndex = index & BUFFER_MASK;
        int bufferIndex = (int) computedBufferIndex;
        int secondIndex = (int) computedSecondIndex;
        buffers[bufferIndex].putString(secondIndex, value);
    }

    public String getString(long index) {
        long computedBufferIndex = index >> BUFFER_SHIFT;
        long computedSecondIndex = index & BUFFER_MASK;
        int bufferIndex = (int) computedBufferIndex;
        int secondIndex = (int) computedSecondIndex;
        return buffers[bufferIndex].getString(secondIndex);
    }

    public long capacity() {
        return size;
    }
}
