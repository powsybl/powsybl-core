/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.binary;

import java.nio.ByteBuffer;

final class GrowingByteBuffer {

    private static final int DEFAULT_INITIAL_CAPACITY = 16 * 1024;

    private ByteBuffer buffer;

    GrowingByteBuffer() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    GrowingByteBuffer(int initialCapacity) {
        this.buffer = ByteBuffer.allocate(initialCapacity);
    }

    private void ensureSpace(int n) {
        if (buffer.remaining() < n) {
            int needed = buffer.position() + n;
            int newCapacity = buffer.capacity() * 2;
            while (newCapacity < needed) {
                newCapacity *= 2;
            }
            ByteBuffer next = ByteBuffer.allocate(newCapacity);
            buffer.flip();
            next.put(buffer);
            buffer = next;
        }
    }

    void writeByte(int b) {
        ensureSpace(1);
        buffer.put((byte) b);
    }

    void writeShort(int s) {
        ensureSpace(2);
        buffer.putShort((short) s);
    }

    void writeInt(int i) {
        ensureSpace(4);
        buffer.putInt(i);
    }

    void writeFloat(float f) {
        ensureSpace(4);
        buffer.putFloat(f);
    }

    void writeDouble(double d) {
        ensureSpace(8);
        buffer.putDouble(d);
    }

    void writeBoolean(boolean b) {
        writeByte(b ? 1 : 0);
    }

    void writeBytes(byte[] bytes) {
        ensureSpace(bytes.length);
        buffer.put(bytes);
    }

    /** Returns a read-only view positioned at 0 with limit at current size, ready for channel.write(). */
    ByteBuffer toReadBuffer() {
        ByteBuffer view = buffer.duplicate();
        view.flip();
        return view;
    }
}
