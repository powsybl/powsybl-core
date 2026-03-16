/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.binary;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Clement Leclerc {@literal <clement.leclerc at rte-france.com>}
 */
class BinWriterReaderTest {

    private static final byte[] MAGIC = {0x54, 0x45, 0x53, 0x54}; // "TEST"
    private static final String ROOT_VERSION = "1.0";

    /** Write a single root node, close the writer, return an initialised reader. */
    private BinReader roundTrip(WriterAction action) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BinWriter writer = new BinWriter(baos, MAGIC, ROOT_VERSION)) {
            writer.setVersions(Collections.emptyMap());
            writer.writeStartNode(null, "root");
            action.run(writer);
            writer.writeEndNode();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        BinReader reader = new BinReader(new ByteArrayInputStream(baos.toByteArray()), MAGIC);
        reader.readHeader();
        return reader;
    }

    @FunctionalInterface
    private interface WriterAction {
        void run(BinWriter writer) throws Exception;
    }

    @Test
    void testMissingAttributesReturnDefaults() {
        BinReader reader = roundTrip(writer -> { /* write nothing */ });

        assertNull(reader.readStringAttribute("s"));

        assertTrue(Float.isNaN(reader.readFloatAttribute("f")));
        assertEquals(99f, reader.readFloatAttribute("f", 99f), 0f);

        assertTrue(Double.isNaN(reader.readDoubleAttribute("d")));
        assertEquals(99.0, reader.readDoubleAttribute("d", 99.0), 0.0);
        assertTrue(reader.readOptionalDoubleAttribute("d").isEmpty());

        assertEquals(0, reader.readIntAttribute("i"));
        assertEquals(7, reader.readIntAttribute("i", 7));
        assertTrue(reader.readOptionalIntAttribute("i").isEmpty());

        assertFalse(reader.readBooleanAttribute("b"));
        assertTrue(reader.readBooleanAttribute("b", true));
        assertTrue(reader.readOptionalBooleanAttribute("b").isEmpty());

        assertNull(reader.readEnumAttribute("e", Thread.State.class));
        assertEquals(Thread.State.NEW, reader.readEnumAttribute("e", Thread.State.class, Thread.State.NEW));

        assertTrue(reader.readIntArrayAttribute("ia").isEmpty());
        assertTrue(reader.readStringArrayAttribute("sa").isEmpty());

        reader.readEndNode();
        reader.close();
    }

    @Test
    void testAbsentAttributesReturnDefault() {
        // Writer writes only "a" and "c", skipping "b" (optional/default value)
        BinReader reader = roundTrip(writer -> {
            writer.writeIntAttribute("a", 1);
            writer.writeStringAttribute("c", "hello");
        });

        assertEquals(1, reader.readIntAttribute("a"));
        // "b" was not written: next attr is "c", name mismatch → default returned, stream not consumed
        assertNull(reader.readStringAttribute("b"));
        // "c" is still available
        assertEquals("hello", reader.readStringAttribute("c"));

        reader.readEndNode();
        reader.close();
    }
}
