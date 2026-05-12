/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.binary;

import com.powsybl.commons.PowsyblException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;

/**
 * Buffered reader on top of a {@link ReadableByteChannel}, backed by a direct {@link ByteBuffer}.
 * Replaces the {@code DataInputStream + BufferedInputStream} chain to avoid double indirection
 * and to leverage JVM intrinsics on aligned multi-byte reads (big-endian, network order).
 *
 * @author Clement Leclerc {@literal <clement.leclerc at rte-france.com>}
 */
final class BufferedChannelReader implements AutoCloseable {

    static final int DEFAULT_BUFFER_SIZE = 64 * 1024;

    /** Sentinel returned by {@link #tryReadUnsignedShort()} on end-of-stream. */
    static final int EOF = -1;

    private final ReadableByteChannel channel;
    private final ByteBuffer buffer;
    private boolean channelExhausted;

    BufferedChannelReader(ReadableByteChannel channel) {
        this(channel, DEFAULT_BUFFER_SIZE);
    }

    BufferedChannelReader(ReadableByteChannel channel, int bufferSize) {
        this.channel = Objects.requireNonNull(channel);
        this.buffer = ByteBuffer.allocateDirect(bufferSize);
        this.buffer.flip(); // start empty in read mode
    }

    /** Ensures at least {@code n} bytes are available in the buffer, refilling from the channel if needed. */
    private void ensureAvailable(int n) throws IOException {
        if (buffer.remaining() >= n) {
            return;
        }
        buffer.compact();
        while (buffer.position() < n) {
            int read = channel.read(buffer);
            if (read == -1) {
                channelExhausted = true;
                break;
            }
        }
        buffer.flip();
        if (buffer.remaining() < n) {
            throw new PowsyblException("Unexpected end of stream: needed " + n + " bytes, got " + buffer.remaining());
        }
    }

    byte readByte() throws IOException {
        ensureAvailable(1);
        return buffer.get();
    }

    int readUnsignedShort() throws IOException {
        ensureAvailable(2);
        return Short.toUnsignedInt(buffer.getShort());
    }

    int readInt() throws IOException {
        ensureAvailable(4);
        return buffer.getInt();
    }

    float readFloat() throws IOException {
        ensureAvailable(4);
        return buffer.getFloat();
    }

    double readDouble() throws IOException {
        ensureAvailable(8);
        return buffer.getDouble();
    }

    boolean readBoolean() throws IOException {
        return readByte() != 0;
    }

    /** Reads exactly {@code n} bytes; throws if the stream ends early. */
    byte[] readNBytes(int n) throws IOException {
        byte[] out = new byte[n];
        int filled = 0;
        while (filled < n) {
            if (!buffer.hasRemaining()) {
                ensureAvailable(1);
            }
            int take = Math.min(buffer.remaining(), n - filled);
            buffer.get(out, filled, take);
            filled += take;
        }
        return out;
    }

    void skipNBytes(long n) throws IOException {
        long remaining = n;
        while (remaining > 0) {
            if (!buffer.hasRemaining()) {
                ensureAvailable(1);
            }
            int skip = (int) Math.min(buffer.remaining(), remaining);
            buffer.position(buffer.position() + skip);
            remaining -= skip;
        }
    }

    /**
     * Attempts to read an unsigned short. Returns {@link #EOF} (-1) if the stream is exhausted
     * before any byte of the short can be read, instead of throwing.
     * Useful for end-of-stream detection in peek-style parsers.
     */
    int tryReadUnsignedShort() throws IOException {
        if (buffer.remaining() >= 2) {
            return Short.toUnsignedInt(buffer.getShort());
        }
        buffer.compact();
        while (buffer.position() < 2) {
            int read = channel.read(buffer);
            if (read == -1) {
                channelExhausted = true;
                break;
            }
        }
        buffer.flip();
        if (buffer.remaining() < 2) {
            if (buffer.remaining() == 0) {
                return EOF;
            }
            throw new PowsyblException("Unexpected end of stream: needed 2 bytes, got " + buffer.remaining());
        }
        return Short.toUnsignedInt(buffer.getShort());
    }

    /** Returns true if no more bytes are available in the buffer or the underlying channel. */
    boolean isEndOfStream() throws IOException {
        if (buffer.hasRemaining()) {
            return false;
        }
        if (channelExhausted) {
            return true;
        }
        buffer.compact();
        int read = channel.read(buffer);
        buffer.flip();
        if (read == -1) {
            channelExhausted = true;
            return !buffer.hasRemaining();
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
