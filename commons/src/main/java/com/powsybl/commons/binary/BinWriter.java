/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.binary;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.AbstractTreeDataWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static com.powsybl.commons.binary.BinUtil.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BinWriter extends AbstractTreeDataWriter {

    private final String rootVersion;
    private final BufferedChannelWriter out;
    private final GrowingByteBuffer body;
    private final byte[] binaryMagicNumber;
    private Map<String, String> extensionVersions = Collections.emptyMap();

    private final Map<TypedName, Integer> namesIndex = new LinkedHashMap<>();

    private record TypedName(String name, byte type) { }

    /** Preferred constructor: direct channel access avoids the OutputStream re-buffering layer. */
    public BinWriter(WritableByteChannel channel, byte[] binaryMagicNumber, String rootVersion) {
        this.binaryMagicNumber = Objects.requireNonNull(binaryMagicNumber);
        this.rootVersion = Objects.requireNonNull(rootVersion);
        this.out = new BufferedChannelWriter(Objects.requireNonNull(channel));
        this.body = new GrowingByteBuffer();
    }

    /** Opens a file directly as a byte channel — the fastest path for file outputs. */
    public BinWriter(Path path, byte[] binaryMagicNumber, String rootVersion) throws IOException {
        this(Files.newByteChannel(Objects.requireNonNull(path),
                StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
            binaryMagicNumber, rootVersion);
    }

    /**
     * Compatibility constructor for callers holding an {@link OutputStream}.
     * Wrapping an {@code OutputStream} via {@link Channels#newChannel} gives no perf gain over
     * the previous {@code DataOutputStream} chain — prefer the {@link WritableByteChannel} or
     * {@link Path} constructors for real I/O speedup.
     *
     * @deprecated use {@link #BinWriter(WritableByteChannel, byte[], String)} or {@link #BinWriter(Path, byte[], String)}
     */
    @Deprecated(since = "6.10.0")
    public BinWriter(OutputStream outputStream, byte[] binaryMagicNumber, String rootVersion) {
        this(Channels.newChannel(Objects.requireNonNull(outputStream)), binaryMagicNumber, rootVersion);
    }

    private static void writeStringToBody(String value, GrowingByteBuffer buf) {
        if (value == null) {
            buf.writeShort(NULL_STRING_SENTINEL);
        } else {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            if (bytes.length >= NULL_STRING_SENTINEL) {
                throw new PowsyblException("Binary format: string too long (max " + (NULL_STRING_SENTINEL - 1) + " bytes)");
            }
            buf.writeShort(bytes.length);
            buf.writeBytes(bytes);
        }
    }

    private static void writeStringToHeader(String value, BufferedChannelWriter w) throws IOException {
        if (value == null) {
            w.writeShort(NULL_STRING_SENTINEL);
        } else {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            if (bytes.length >= NULL_STRING_SENTINEL) {
                throw new PowsyblException("Binary format: string too long (max " + (NULL_STRING_SENTINEL - 1) + " bytes)");
            }
            w.writeShort(bytes.length);
            w.writeBytes(bytes);
        }
    }

    @Override
    public void writeStartNodes() {
        // nothing to do
    }

    @Override
    public void writeEndNodes() {
        // nothing to do
    }

    @Override
    public void writeStartNode(String namespace, String name) {
        if (namesIndex.isEmpty()) {
            namesIndex.put(new TypedName(name, TYPE_OBJECT), 1); // root element is not a child of another node, hence index is not expected
        } else {
            writeEntry(name, TYPE_OBJECT);
        }
    }

    @Override
    public void writeEndNode() {
        body.writeShort(END_NODE);
    }

    private void writeEntry(String name, byte type) {
        TypedName key = new TypedName(name, type);
        Integer index = namesIndex.get(key);
        if (index == null) {
            int newIndex = namesIndex.size() + 1;
            if (newIndex > MAX_NAME_IDX) {
                throw new PowsyblException("Binary format: too many distinct names (max " + MAX_NAME_IDX + ")");
            }
            namesIndex.put(key, newIndex);
            index = newIndex;
        }
        body.writeShort(index);
    }

    @Override
    public void writeNamespace(String prefix, String namespace) {
        // nothing to do
    }

    @Override
    public void writeNodeContent(String value) {
        writeEntry("", TYPE_STRING_CONTENT);
        writeStringToBody(value, body);
    }

    @Override
    public void writeStringAttribute(String name, String value) {
        writeEntry(name, TYPE_STRING);
        writeStringToBody(value, body);
    }

    @Override
    public void writeDoubleAttribute(String name, double value) {
        writeDoubleAttribute(name, value, Double.NaN);
    }

    @Override
    public void writeDoubleAttribute(String name, double value, double absentValue) {
        boolean isAbsent = Double.isNaN(absentValue) ? Double.isNaN(value) : value == absentValue;
        if (isAbsent) {
            return;
        }
        writeEntry(name, TYPE_DOUBLE);
        body.writeDouble(value);
    }

    @Override
    public void writeFloatAttribute(String name, float value) {
        if (Float.isNaN(value)) {
            return;
        }
        writeEntry(name, TYPE_FLOAT);
        body.writeFloat(value);
    }

    @Override
    public void writeIntAttribute(String name, int value) {
        writeEntry(name, TYPE_INT);
        body.writeInt(value);
    }

    @Override
    public void writeIntAttribute(String name, int value, int absentValue) {
        if (value == absentValue) {
            return;
        }
        writeIntAttribute(name, value);
    }

    @Override
    public void writeBooleanAttribute(String name, boolean value) {
        writeEntry(name, TYPE_BOOLEAN);
        body.writeBoolean(value);
    }

    @Override
    public void writeBooleanAttribute(String name, boolean value, boolean absentValue) {
        if (value == absentValue) {
            return;
        }
        writeBooleanAttribute(name, value);
    }

    @Override
    public <E extends Enum<E>> void writeEnumAttribute(String name, E value) {
        if (value == null) {
            return;
        }
        writeEntry(name, TYPE_ENUM);
        body.writeShort(value.ordinal());
    }

    @Override
    public void writeIntArrayAttribute(String name, Collection<Integer> values) {
        writeEntry(name, TYPE_INT_ARRAY);
        body.writeShort(values.size());
        for (int v : values) {
            body.writeInt(v);
        }
    }

    @Override
    public void writeStringArrayAttribute(String name, Collection<String> values) {
        writeEntry(name, TYPE_STRING_ARRAY);
        body.writeShort(values.size());
        for (String s : values) {
            writeStringToBody(s, body);
        }
    }

    @Override
    public void close() {
        try {
            writeHeader();
            out.writeFully(body.toReadBuffer());
            out.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeHeader() throws IOException {
        // magic number
        out.writeBytes(binaryMagicNumber);

        // iidm version
        writeStringToHeader(rootVersion, out);

        // extensions versions
        out.writeShort(extensionVersions.size());
        for (var entry : extensionVersions.entrySet()) {
            writeStringToHeader(entry.getKey(), out);
            writeStringToHeader(entry.getValue(), out);
        }

        // names dictionary
        out.writeShort(namesIndex.size());
        for (TypedName key : namesIndex.keySet()) {
            writeStringToHeader(key.name(), out);
            out.writeByte(key.type());
        }
    }

    @Override
    public void setVersions(Map<String, String> extensionVersions) {
        this.extensionVersions = Objects.requireNonNull(extensionVersions);
    }
}
