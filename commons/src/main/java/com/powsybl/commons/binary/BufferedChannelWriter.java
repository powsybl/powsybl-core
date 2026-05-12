/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.binary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

/**
 * Buffered writer on top of a {@link WritableByteChannel}, backed by a direct {@link ByteBuffer}.
 * Replaces the {@code DataOutputStream + BufferedOutputStream} chain.
 * Payloads larger than the buffer capacity are written directly to the channel after a flush,
 * avoiding pointless intermediate copies.
 *
 * @author Clement Leclerc {@literal <clement.leclerc at rte-france.com>}
 */
final class BufferedChannelWriter implements AutoCloseable {

    static final int DEFAULT_BUFFER_SIZE = 256 * 1024;

    private final WritableByteChannel channel;
    private final ByteBuffer buffer;

    BufferedChannelWriter(WritableByteChannel channel) {
        this(channel, DEFAULT_BUFFER_SIZE);
    }

    BufferedChannelWriter(WritableByteChannel channel, int bufferSize) {
        this.channel = Objects.requireNonNull(channel);
        this.buffer = ByteBuffer.allocateDirect(bufferSize);
    }

    private void ensureSpace(int n) throws IOException {
        if (buffer.remaining() < n) {
            flush();
        }
    }

    void writeByte(int b) throws IOException {
        ensureSpace(1);
        buffer.put((byte) b);
    }

    void writeShort(int s) throws IOException {
        ensureSpace(2);
        buffer.putShort((short) s);
    }

    void writeInt(int i) throws IOException {
        ensureSpace(4);
        buffer.putInt(i);
    }

    void writeFloat(float f) throws IOException {
        ensureSpace(4);
        buffer.putFloat(f);
    }

    void writeDouble(double d) throws IOException {
        ensureSpace(8);
        buffer.putDouble(d);
    }

    void writeBoolean(boolean b) throws IOException {
        writeByte(b ? 1 : 0);
    }

    void writeBytes(byte[] bytes) throws IOException {
        if (bytes.length > buffer.capacity()) {
            // payload exceeds buffer capacity: flush then write directly
            flush();
            ByteBuffer wrap = ByteBuffer.wrap(bytes);
            while (wrap.hasRemaining()) {
                channel.write(wrap);
            }
            return;
        }
        ensureSpace(bytes.length);
        buffer.put(bytes);
    }

    /** Drains a fully-prepared (already flipped) ByteBuffer to the channel after flushing internal buffer. */
    void writeFully(ByteBuffer src) throws IOException {
        flush();
        while (src.hasRemaining()) {
            channel.write(src);
        }
    }

    void flush() throws IOException {
        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        buffer.clear();
    }

    @Override
    public void close() throws IOException {
        flush();
        channel.close();
    }
}
