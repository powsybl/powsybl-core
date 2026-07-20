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
import java.util.*;

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
            throw new PowsyblException(e);
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

        assertThrows(PowsyblException.class, () -> reader.readIntAttribute("i"));
        assertEquals(7, reader.readIntAttribute("i", 7));
        assertTrue(reader.readOptionalIntAttribute("i").isEmpty());

        assertThrows(PowsyblException.class, () -> reader.readBooleanAttribute("b"));
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

    @Test
    void testAllTypesRoundTrip() {
        BinReader reader = roundTrip(writer -> {
            writer.writeDoubleAttribute("d", 3.14);
            writer.writeDoubleAttribute("d2", 1.0, 0.0);
            writer.writeFloatAttribute("f", 2.71f);
            writer.writeIntAttribute("i", 42);
            writer.writeIntAttribute("i2", 5, 0);
            writer.writeBooleanAttribute("b", true);
            writer.writeBooleanAttribute("b2", false, true);
            writer.writeStringAttribute("s", "hello");
            writer.writeEnumAttribute("e", Thread.State.BLOCKED);
            writer.writeIntArrayAttribute("ia", List.of(1, 2, 3));
            writer.writeStringArrayAttribute("sa", List.of("x", "y"));
        });

        assertEquals(3.14, reader.readDoubleAttribute("d"), 1e-9);
        assertEquals(1.0, reader.readDoubleAttribute("d2", 0.0), 1e-9);
        assertEquals(2.71f, reader.readFloatAttribute("f"), 1e-6f);
        assertEquals(42, reader.readIntAttribute("i"));
        assertEquals(5, reader.readIntAttribute("i2", 0));
        assertTrue(reader.readBooleanAttribute("b"));
        assertFalse(reader.readBooleanAttribute("b2", true));
        assertEquals("hello", reader.readStringAttribute("s"));
        assertEquals(Thread.State.BLOCKED, reader.readEnumAttribute("e", Thread.State.class));
        assertEquals(List.of(1, 2, 3), reader.readIntArrayAttribute("ia"));
        assertEquals(List.of("x", "y"), reader.readStringArrayAttribute("sa"));

        reader.readEndNode();
        reader.close();
    }

    @Test
    void testAbsentValuesAreSkipped() {
        BinReader reader = roundTrip(writer -> {
            writer.writeDoubleAttribute("d", Double.NaN);
            writer.writeDoubleAttribute("d2", 5.0, 5.0);
            writer.writeFloatAttribute("f", Float.NaN);
            writer.writeIntAttribute("i", 7, 7);
            writer.writeBooleanAttribute("b", true, true);
            writer.writeEnumAttribute("e", (Thread.State) null);
            writer.writeIntAttribute("real", 1);
        });
        assertEquals(1, reader.readIntAttribute("real"));
        reader.readEndNode();
        reader.close();
    }

    @Test
    void testOptionalTypesRoundTrip() {
        BinReader reader = roundTrip(writer -> {
            writer.writeOptionalDoubleAttribute("d", 3.14);
            writer.writeOptionalIntAttribute("i", 42);
            writer.writeOptionalBooleanAttribute("b", true);
        });

        OptionalDouble d = reader.readOptionalDoubleAttribute("d");
        assertTrue(d.isPresent());
        assertEquals(3.14, d.getAsDouble(), 1e-9);

        OptionalInt i = reader.readOptionalIntAttribute("i");
        assertTrue(i.isPresent());
        assertEquals(42, i.getAsInt());

        Optional<Boolean> b = reader.readOptionalBooleanAttribute("b");
        assertTrue(b.isPresent());
        assertTrue(b.get());

        reader.readEndNode();
        reader.close();
    }

    @Test
    void testReadEndNodeWithRemainingAttributesThrows() {
        BinReader reader = roundTrip(writer -> {
            writer.writeIntAttribute("a", 1);
            writer.writeDoubleAttribute("b", 2.0);
        });

        assertEquals(1, reader.readIntAttribute("a"));
        // Remaining attribute "b" not read → readEndNode must throw
        assertThrows(PowsyblException.class, reader::readEndNode);
        reader.close();
    }

    @Test
    void testSkipNodeSkipsAllAttributeTypes() {
        BinReader reader = roundTrip(writer -> {
            writer.writeStartNode(null, "child");
            writer.writeIntAttribute("a", 1);
            writer.writeDoubleAttribute("b", 2.0);
            writer.writeFloatAttribute("c", 3.0f);
            writer.writeBooleanAttribute("d", true);
            writer.writeStringAttribute("e", "skip");
            writer.writeEnumAttribute("f", Thread.State.RUNNABLE);
            writer.writeIntArrayAttribute("g", List.of(1, 2));
            writer.writeStringArrayAttribute("h", List.of("x", "y"));
            writer.writeEndNode();
            writer.writeStartNode(null, "sibling");
            writer.writeIntAttribute("z", 99);
            writer.writeEndNode();
        });

        List<String> visited = new ArrayList<>();
        reader.readChildNodes(nodeName -> {
            visited.add(nodeName);
            reader.skipNode();
        });
        // reaching "sibling" proves every attribute type in "child" was skipped with the exact byte count
        assertEquals(List.of("child", "sibling"), visited);
        reader.close();
    }

    @Test
    void testReadChildNodes() {
        BinReader reader = roundTrip(writer -> {
            writer.writeStartNode(null, "child1");
            writer.writeIntAttribute("x", 10);
            writer.writeEndNode();
            writer.writeStartNode(null, "child2");
            writer.writeStringAttribute("y", "hello");
            writer.writeEndNode();
        });

        List<String> visited = new ArrayList<>();
        reader.readChildNodes(nodeName -> {
            visited.add(nodeName);
            reader.skipNode();
        });
        assertEquals(List.of("child1", "child2"), visited);
        reader.close();
    }

    @Test
    void testSkipNode() {
        BinReader reader = roundTrip(writer -> {
            writer.writeStartNode(null, "child1");
            writer.writeIntAttribute("x", 42);
            writer.writeStartNode(null, "grandchild");
            writer.writeBooleanAttribute("flag", true);
            writer.writeEndNode();
            writer.writeEndNode();
            writer.writeStartNode(null, "child2");
            writer.writeStringAttribute("y", "test");
            writer.writeEndNode();

        });

        List<String> nodesNameSkipped = new ArrayList<>();
        reader.readChildNodes(nodeName -> {
            reader.skipNode();
            nodesNameSkipped.add(nodeName);
        });

        assertEquals(List.of("child1", "child2"), nodesNameSkipped);

        reader.close();
    }

    @Test
    void testNodeContent() {
        BinReader reader = roundTrip(writer -> writer.writeNodeContent("hello content"));
        assertEquals("hello content", reader.readContent());
        reader.close();
    }

    @Test
    void testAbsentNodeContent() {
        BinReader reader = roundTrip(writer -> { /* no content written */ });
        assertNull(reader.readContent());
        reader.close();
    }

    @Test
    void testInvalidMagicNumber() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BinWriter writer = new BinWriter(baos, MAGIC, ROOT_VERSION)) {
            writer.setVersions(Collections.emptyMap());
            writer.writeStartNode(null, "root");
            writer.writeEndNode();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        byte[] wrongMagic = {0x00, 0x00, 0x00, 0x00};
        BinReader reader = new BinReader(new ByteArrayInputStream(baos.toByteArray()), wrongMagic);
        assertThrows(PowsyblException.class, reader::readHeader);
        reader.close();
    }
}
