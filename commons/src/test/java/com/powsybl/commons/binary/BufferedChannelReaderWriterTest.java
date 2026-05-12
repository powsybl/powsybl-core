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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Clement Leclerc {@literal <clement.leclerc at rte-france.com>}
 */
class BufferedChannelReaderWriterTest {

    private static ReadableByteChannel readerOf(byte[] data) {
        return Channels.newChannel(new ByteArrayInputStream(data));
    }

    @Test
    void endiannessIsBigEndian() {
        // Format compatibility guarantee: ByteBuffer must match DataInputStream's network byte order
        assertEquals(ByteOrder.BIG_ENDIAN, ByteBuffer.allocateDirect(8).order());
    }

    @Test
    void roundTripAllPrimitives() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WritableByteChannel out = Channels.newChannel(baos);
        try (BufferedChannelWriter w = new BufferedChannelWriter(out)) {
            w.writeByte(0x7F);
            w.writeShort(0xFEDC);
            w.writeInt(0xDEADBEEF);
            w.writeFloat(2.71f);
            w.writeDouble(3.14159265358979);
            w.writeBoolean(true);
            w.writeBoolean(false);
            w.writeBytes(new byte[] {1, 2, 3, 4, 5});
        }

        try (BufferedChannelReader r = new BufferedChannelReader(readerOf(baos.toByteArray()))) {
            assertEquals((byte) 0x7F, r.readByte());
            assertEquals(0xFEDC, r.readUnsignedShort());
            assertEquals(0xDEADBEEF, r.readInt());
            assertEquals(2.71f, r.readFloat(), 0f);
            assertEquals(3.14159265358979, r.readDouble(), 0d);
            assertTrue(r.readBoolean());
            assertFalse(r.readBoolean());
            assertArrayEquals(new byte[] {1, 2, 3, 4, 5}, r.readNBytes(5));
            assertTrue(r.isEndOfStream());
        }
    }

    @Test
    void parityWithDataStream() throws Exception {
        // Same bytes produced as DataOutputStream/DataInputStream → format compatibility
        ByteArrayOutputStream dosOut = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(dosOut)) {
            dos.writeByte(0x42);
            dos.writeShort(0xABCD);
            dos.writeInt(123456789);
            dos.writeFloat(1.5f);
            dos.writeDouble(-9.875);
            dos.writeBoolean(true);
        }

        ByteArrayOutputStream channelOut = new ByteArrayOutputStream();
        try (BufferedChannelWriter w = new BufferedChannelWriter(Channels.newChannel(channelOut))) {
            w.writeByte(0x42);
            w.writeShort(0xABCD);
            w.writeInt(123456789);
            w.writeFloat(1.5f);
            w.writeDouble(-9.875);
            w.writeBoolean(true);
        }

        assertArrayEquals(dosOut.toByteArray(), channelOut.toByteArray());

        // Channel reader reads bytes written by DataOutputStream identically
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dosOut.toByteArray()));
             BufferedChannelReader r = new BufferedChannelReader(readerOf(dosOut.toByteArray()))) {
            assertEquals(dis.readByte(), r.readByte());
            assertEquals(dis.readUnsignedShort(), r.readUnsignedShort());
            assertEquals(dis.readInt(), r.readInt());
            assertEquals(dis.readFloat(), r.readFloat(), 0f);
            assertEquals(dis.readDouble(), r.readDouble(), 0d);
            assertEquals(dis.readBoolean(), r.readBoolean());
        }
    }

    @Test
    void readsCrossBufferBoundary() throws Exception {
        // Small buffer (16 B) forces multiple refills within a single readNBytes / readInt
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BufferedChannelWriter w = new BufferedChannelWriter(Channels.newChannel(baos), 16)) {
            for (int i = 0; i < 100; i++) {
                w.writeInt(i);
            }
        }

        try (BufferedChannelReader r = new BufferedChannelReader(readerOf(baos.toByteArray()), 16)) {
            for (int i = 0; i < 100; i++) {
                assertEquals(i, r.readInt());
            }
            assertTrue(r.isEndOfStream());
        }
    }

    @Test
    void readNBytesAcrossManyRefills() throws Exception {
        byte[] payload = new byte[1024];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (i & 0xFF);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BufferedChannelWriter w = new BufferedChannelWriter(Channels.newChannel(baos), 32)) {
            w.writeBytes(payload);
        }

        try (BufferedChannelReader r = new BufferedChannelReader(readerOf(baos.toByteArray()), 32)) {
            assertArrayEquals(payload, r.readNBytes(payload.length));
        }
    }

    @Test
    void writeBytesLargerThanBufferGoesDirect() throws Exception {
        byte[] big = new byte[200];
        for (int i = 0; i < big.length; i++) {
            big[i] = (byte) i;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BufferedChannelWriter w = new BufferedChannelWriter(Channels.newChannel(baos), 64)) {
            w.writeByte(0xAA);
            w.writeBytes(big);
            w.writeByte(0xBB);
        }

        try (BufferedChannelReader r = new BufferedChannelReader(readerOf(baos.toByteArray()), 64)) {
            assertEquals((byte) 0xAA, r.readByte());
            assertArrayEquals(big, r.readNBytes(big.length));
            assertEquals((byte) 0xBB, r.readByte());
            assertTrue(r.isEndOfStream());
        }
    }

    @Test
    void skipNBytes() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BufferedChannelWriter w = new BufferedChannelWriter(Channels.newChannel(baos), 16)) {
            for (int i = 0; i < 50; i++) {
                w.writeInt(i);
            }
        }

        try (BufferedChannelReader r = new BufferedChannelReader(readerOf(baos.toByteArray()), 16)) {
            r.skipNBytes(4L * 25);
            assertEquals(25, r.readInt());
            r.skipNBytes(4L * 24);
            assertTrue(r.isEndOfStream());
        }
    }

    @Test
    void tryReadUnsignedShortReturnsEofWhenEmpty() throws Exception {
        try (BufferedChannelReader r = new BufferedChannelReader(readerOf(new byte[0]))) {
            assertEquals(BufferedChannelReader.EOF, r.tryReadUnsignedShort());
        }
    }

    @Test
    void tryReadUnsignedShortReturnsValueWhenAvailable() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BufferedChannelWriter w = new BufferedChannelWriter(Channels.newChannel(baos))) {
            w.writeShort(0x1234);
            w.writeShort(0xCAFE);
        }
        try (BufferedChannelReader r = new BufferedChannelReader(readerOf(baos.toByteArray()))) {
            assertEquals(0x1234, r.tryReadUnsignedShort());
            assertEquals(0xCAFE, r.tryReadUnsignedShort());
            assertEquals(BufferedChannelReader.EOF, r.tryReadUnsignedShort());
        }
    }

    @Test
    void tryReadUnsignedShortThrowsOnPartialShort() throws Exception {
        // Single trailing byte: cannot read 2-byte short, must throw rather than silently return EOF
        try (BufferedChannelReader r = new BufferedChannelReader(readerOf(new byte[] {0x42}))) {
            assertThrows(PowsyblException.class, r::tryReadUnsignedShort);
        }
    }

    @Test
    void isEndOfStreamWorksWithoutRead() throws Exception {
        try (BufferedChannelReader r = new BufferedChannelReader(readerOf(new byte[0]))) {
            assertTrue(r.isEndOfStream());
        }
    }

    @Test
    void readUnexpectedEofThrows() throws Exception {
        try (BufferedChannelReader r = new BufferedChannelReader(readerOf(new byte[] {0x01, 0x02}))) {
            assertThrows(PowsyblException.class, r::readInt);
        }
    }
}
