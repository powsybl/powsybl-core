/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.binary;

import com.powsybl.commons.PowsyblException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;

/**
 * Buffered reader on top of a {@link ReadableByteChannel}, backed by a direct {@link ByteBuffer}.
 *
 * @author Clement Leclerc {@literal <clement.leclerc at rte-france.com>}
 */
final class BufferedChannelReader implements AutoCloseable {

    static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

    private final ReadableByteChannel channel;
    private final ByteBuffer buffer;
    private boolean channelExhausted;

    BufferedChannelReader(InputStream inputStream) {
        this(inputStream, DEFAULT_BUFFER_SIZE);
    }

    BufferedChannelReader(InputStream inputStream, int bufferSize) {
        this.channel = Objects.requireNonNull(Channels.newChannel(inputStream));
        this.buffer = ByteBuffer.allocateDirect(bufferSize);
        this.buffer.flip();
    }

    /** Pulls bytes from the channel until the buffer holds at least {@code n}, or the channel ends. */
    private int fill(int n) {
        if (buffer.remaining() >= n) {
            return buffer.remaining();
        }
        buffer.compact();
        try {
            while (buffer.position() < n && !channelExhausted) {
                if (channel.read(buffer) == -1) {
                    channelExhausted = true;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        buffer.flip();
        return buffer.remaining();
    }

    private void require(int n) {
        if (fill(n) < n) {
            throw new PowsyblException("Unexpected end of stream: needed " + n + " bytes, got " + buffer.remaining());
        }
    }

    byte readByte() {
        require(1);
        return buffer.get();
    }

    int readUnsignedShort() {
        require(2);
        return Short.toUnsignedInt(buffer.getShort());
    }

    /**
     * Reads an unsigned short, or returns {@link BinUtil#END_OF_FILE} if the end of the stream is reached before.
     */
    int readOptionalUnsignedShort() {
        int readCount = fill(2);
        if (readCount == 0) {
            return BinUtil.END_OF_FILE;
        } else if (readCount < 2) {
            throw new PowsyblException("Unexpected end of stream: needed 2 bytes, got only 1");
        }
        return Short.toUnsignedInt(buffer.getShort());
    }

    int readInt() {
        require(4);
        return buffer.getInt();
    }

    float readFloat() {
        require(4);
        return buffer.getFloat();
    }

    double readDouble() {
        require(8);
        return buffer.getDouble();
    }

    boolean readBoolean() {
        return readByte() != 0;
    }

    byte[] readNBytes(int n) {
        byte[] out = new byte[n];
        int filled = 0;
        while (filled < n) {
            if (!buffer.hasRemaining()) {
                require(1);
            }
            int take = Math.min(buffer.remaining(), n - filled);
            buffer.get(out, filled, take);
            filled += take;
        }
        return out;
    }

    void skipNBytes(long n) {
        long remaining = n;
        while (remaining > 0) {
            if (!buffer.hasRemaining()) {
                require(1);
            }
            int skip = (int) Math.min(buffer.remaining(), remaining);
            buffer.position(buffer.position() + skip);
            remaining -= skip;
        }
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
