/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.binary;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Segmented heap buffer used to stage binary payloads in memory before writing them to a stream.
 *
 * @author Clement Leclerc {@literal <clement.leclerc at rte-france.com>}
 */
final class SegmentedByteBuffer {

    private static final int BLOCK_SIZE = 64 * 1024;

    private final int blockSize;
    private final List<ByteBuffer> filledBlocks = new ArrayList<>();
    private ByteBuffer current;

    SegmentedByteBuffer() {
        this(BLOCK_SIZE);
    }

    SegmentedByteBuffer(int blockSize) {
        this.blockSize = blockSize;
        this.current = ByteBuffer.allocate(blockSize);
    }

    /** Rolls to a fresh block if the current one cannot hold {@code n} more bytes. */
    private void ensureSpace(int n) {
        if (current.remaining() < n) {
            current.flip();
            filledBlocks.add(current);
            current = ByteBuffer.allocate(blockSize);
        }
    }

    void writeByte(int b) {
        ensureSpace(1);
        current.put((byte) b);
    }

    void writeShort(int s) {
        ensureSpace(2);
        current.putShort((short) s);
    }

    void writeInt(int i) {
        ensureSpace(4);
        current.putInt(i);
    }

    void writeFloat(float f) {
        ensureSpace(4);
        current.putFloat(f);
    }

    void writeDouble(double d) {
        ensureSpace(8);
        current.putDouble(d);
    }

    void writeBoolean(boolean b) {
        writeByte(b ? 1 : 0);
    }

    void writeBytes(byte[] bytes) {
        int offset = 0;
        while (offset < bytes.length) {
            if (current.remaining() == 0) {
                ensureSpace(1);
            }
            int n = Math.min(current.remaining(), bytes.length - offset);
            current.put(bytes, offset, n);
            offset += n;
        }
    }

    void writeTo(OutputStream os) throws IOException {
        current.flip();
        filledBlocks.add(current);
        for (ByteBuffer block : filledBlocks) {
            os.write(block.array(), block.arrayOffset() + block.position(), block.remaining());
        }
    }
}
