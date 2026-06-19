/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.binary;

import com.powsybl.commons.PowsyblException;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Clement Leclerc {@literal <clement.leclerc at rte-france.com>}
 */
class BufferedChannelReaderTest {

    private static InputStream readerOf(byte[] data) {
        return new ByteArrayInputStream(data);
    }

    @FunctionalInterface
    private interface ByteSource {
        void write(DataOutputStream dos) throws IOException;
    }

    /** Produces bytes via {@link DataOutputStream} (big-endian) — matches the binary format wire layout. */
    private static byte[] bytes(ByteSource source) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            source.write(dos);
        }
        return baos.toByteArray();
    }

    @Test
    void endiannessIsBigEndian() {
        // format guarantee: ByteBuffer must match DataOutputStream's network byte order
        assertEquals(ByteOrder.BIG_ENDIAN, ByteBuffer.allocateDirect(8).order());
    }

    @Test
    void readsAllPrimitives() throws Exception {
        byte[] data = bytes(dos -> {
            dos.writeByte(0x7F);
            dos.writeShort(0xFEDC);
            dos.writeInt(0xDEADBEEF);
            dos.writeFloat(2.71f);
            dos.writeDouble(3.14159265358979);
            dos.writeBoolean(true);
            dos.writeBoolean(false);
            dos.write(new byte[] {1, 2, 3, 4, 5});
        });

        try (BufferedChannelReader r = new BufferedChannelReader(readerOf(data))) {
            assertEquals((byte) 0x7F, r.readByte());
            assertEquals(0xFEDC, r.readUnsignedShort());
            assertEquals(0xDEADBEEF, r.readInt());
            assertEquals(2.71f, r.readFloat(), 0f);
            assertEquals(3.14159265358979, r.readDouble(), 0d);
            assertTrue(r.readBoolean());
            assertFalse(r.readBoolean());
            assertArrayEquals(new byte[] {1, 2, 3, 4, 5}, r.readNBytes(5));
        }
    }

    @Test
    void readsCrossBufferBoundary() throws Exception {
        // small buffer forces multiple channel refills inside a single readInt
        byte[] data = bytes(dos -> {
            for (int i = 0; i < 100; i++) {
                dos.writeInt(i);
            }
        });

        try (BufferedChannelReader r = new BufferedChannelReader(readerOf(data), 16)) {
            for (int i = 0; i < 100; i++) {
                assertEquals(i, r.readInt());
            }
        }
    }

    @Test
    void readNBytesAcrossManyRefills() throws Exception {
        byte[] payload = new byte[1024];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (i & 0xFF);
        }
        try (BufferedChannelReader r = new BufferedChannelReader(readerOf(payload), 32)) {
            assertArrayEquals(payload, r.readNBytes(payload.length));
        }
    }

    @Test
    void skipNBytes() throws Exception {
        byte[] data = bytes(dos -> {
            for (int i = 0; i < 50; i++) {
                dos.writeInt(i);
            }
        });

        try (BufferedChannelReader r = new BufferedChannelReader(readerOf(data), 16)) {
            r.skipNBytes(4L * 25);
            assertEquals(25, r.readInt());
            r.skipNBytes(4L * 24);
        }
    }

    @Test
    void readUnexpectedEofThrows() throws Exception {
        try (BufferedChannelReader r = new BufferedChannelReader(readerOf(new byte[] {0x01, 0x02}))) {
            assertThrows(PowsyblException.class, r::readInt);
        }
    }
}
