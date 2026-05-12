/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.binary;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.AbstractTreeDataReader;
import com.powsybl.commons.io.TreeDataHeader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static com.powsybl.commons.binary.BinUtil.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BinReader extends AbstractTreeDataReader {

    private final BufferedChannelReader in;
    private final byte[] binaryMagicNumber;

    private String[] names;
    private byte[] types;

    private int nextNameIdx = END_NODE;
    private byte nextType;

    /** Preferred constructor: direct channel access avoids the InputStream re-buffering layer. */
    public BinReader(ReadableByteChannel channel, byte[] binaryMagicNumber) {
        this.binaryMagicNumber = binaryMagicNumber;
        this.in = new BufferedChannelReader(Objects.requireNonNull(channel));
    }

    /** Opens a file directly as a byte channel — the fastest path for file inputs. */
    public BinReader(Path path, byte[] binaryMagicNumber) throws IOException {
        this(Files.newByteChannel(Objects.requireNonNull(path), StandardOpenOption.READ), binaryMagicNumber);
    }

    /**
     * Compatibility constructor for callers holding an {@link InputStream}.
     * Note: wrapping an {@code InputStream} via {@link Channels#newChannel} gives no perf gain
     * over the previous {@code DataInputStream} chain — prefer the {@link ReadableByteChannel}
     * or {@link Path} constructors for real I/O speedup.
     *
     * @deprecated use {@link #BinReader(ReadableByteChannel, byte[])} or {@link #BinReader(Path, byte[])}
     */
    @Deprecated(since = "6.10.0")
    public BinReader(InputStream is, byte[] binaryMagicNumber) {
        this(Channels.newChannel(Objects.requireNonNull(is)), binaryMagicNumber);
    }

    @Override
    public TreeDataHeader readHeader() {
        TreeDataHeader header = super.readHeader();
        try {
            readNamesDictionary();
            peekNextEntry();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return header;
    }

    @Override
    protected String readRootVersion() {
        try {
            readMagicNumber();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return readString();
    }

    private void readMagicNumber() throws IOException {
        byte[] read = in.readNBytes(binaryMagicNumber.length);
        if (!Arrays.equals(read, binaryMagicNumber)) {
            throw new PowsyblException("Unexpected bytes at file start");
        }
    }

    @Override
    protected Map<String, String> readExtensionVersions() {
        try {
            int nbVersions = in.readUnsignedShort();
            Map<String, String> versions = new HashMap<>();
            for (int i = 0; i < nbVersions; i++) {
                versions.put(readString(), readString());
            }
            return versions;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void readNamesDictionary() throws IOException {
        int nbEntries = in.readUnsignedShort();
        names = new String[nbEntries + 1];
        types = new byte[nbEntries + 1];
        for (int i = 0; i < nbEntries; i++) {
            names[i + 1] = readString();
            types[i + 1] = in.readByte();
        }
    }

    private void peekNextEntry() throws IOException {
        int idx = in.tryReadUnsignedShort();
        if (idx == BufferedChannelReader.EOF) {
            nextNameIdx = END_NODE;
            return;
        }
        nextNameIdx = idx;
        if (nextNameIdx != END_NODE) {
            nextType = types[nextNameIdx];
        }
    }

    private boolean isAttrAbsent(String name) {
        if (nextNameIdx == END_NODE || nextType == TYPE_OBJECT) {
            return true;
        }
        String entryName = names[nextNameIdx];
        if (entryName == null) {
            throw new PowsyblException("Cannot read attribute: unknown name index " + nextNameIdx);
        }
        return !name.equals(entryName);
    }

    private void skipRemainingAttributes() throws IOException {
        while (nextNameIdx != END_NODE && nextType != TYPE_OBJECT) {
            skipTypedValue(nextType);
            peekNextEntry();
        }
    }

    private void skipTypedValue(byte typeTag) throws IOException {
        switch (typeTag) {
            case TYPE_DOUBLE -> in.skipNBytes(8);
            case TYPE_FLOAT, TYPE_INT -> in.skipNBytes(4);
            case TYPE_BOOLEAN -> in.skipNBytes(1);
            case TYPE_STRING, TYPE_STRING_CONTENT -> skipString();
            case TYPE_ENUM -> in.skipNBytes(2);
            case TYPE_INT_ARRAY -> skipIntArray();
            case TYPE_STRING_ARRAY -> skipStringArray();
            default -> throw new PowsyblException("Binary format: unknown type tag " + typeTag);
        }
    }

    private void skipString() throws IOException {
        int len = in.readUnsignedShort();
        if (len != NULL_STRING_SENTINEL) {
            in.skipNBytes(len);
        }
    }

    private void skipIntArray() throws IOException {
        int count = in.readUnsignedShort();
        if (count > 0) {
            in.skipNBytes(4L * count);
        }
    }

    private void skipStringArray() throws IOException {
        int count = in.readUnsignedShort();
        for (int i = 0; i < count; i++) {
            skipString();
        }
    }

    private List<Integer> readIntArrayRaw() throws IOException {
        int count = in.readUnsignedShort();
        List<Integer> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(in.readInt());
        }
        return list;
    }

    private List<String> readStringArrayRaw() throws IOException {
        int count = in.readUnsignedShort();
        List<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(readString());
        }
        return list;
    }

    private String readString() {
        try {
            int stringNbBytes = in.readUnsignedShort();
            if (stringNbBytes == NULL_STRING_SENTINEL) {
                return null;
            }
            byte[] stringBytes = in.readNBytes(stringNbBytes);
            return new String(stringBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public double readDoubleAttribute(String name, double defaultValue) {
        try {
            if (isAttrAbsent(name)) {
                return defaultValue;
            }
            double val = in.readDouble();
            peekNextEntry();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public OptionalDouble readOptionalDoubleAttribute(String name) {
        try {
            if (isAttrAbsent(name)) {
                return OptionalDouble.empty();
            }
            OptionalDouble val = OptionalDouble.of(in.readDouble());
            peekNextEntry();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public float readFloatAttribute(String name, float defaultValue) {
        try {
            if (isAttrAbsent(name)) {
                return defaultValue;
            }
            float val = in.readFloat();
            peekNextEntry();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String readStringAttribute(String name) {
        try {
            if (isAttrAbsent(name)) {
                return null;
            }
            String val = readString();
            peekNextEntry();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public int readIntAttribute(String name) {
        if (isAttrAbsent(name)) {
            throw new PowsyblException("Missing required int attribute: " + name);
        }
        try {
            int val = in.readInt();
            peekNextEntry();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public int readIntAttribute(String name, int defaultValue) {
        try {
            if (isAttrAbsent(name)) {
                return defaultValue;
            }
            int val = in.readInt();
            peekNextEntry();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public OptionalInt readOptionalIntAttribute(String name) {
        try {
            if (isAttrAbsent(name)) {
                return OptionalInt.empty();
            }
            OptionalInt val = OptionalInt.of(in.readInt());
            peekNextEntry();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean readBooleanAttribute(String name) {
        if (isAttrAbsent(name)) {
            throw new PowsyblException("Missing required boolean attribute: " + name);
        }
        try {
            boolean val = in.readBoolean();
            peekNextEntry();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean readBooleanAttribute(String name, boolean defaultValue) {
        try {
            if (isAttrAbsent(name)) {
                return defaultValue;
            }
            boolean val = in.readBoolean();
            peekNextEntry();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<Boolean> readOptionalBooleanAttribute(String name) {
        try {
            if (isAttrAbsent(name)) {
                return Optional.empty();
            }
            Optional<Boolean> val = Optional.of(in.readBoolean());
            peekNextEntry();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz, T defaultValue) {
        try {
            if (isAttrAbsent(name)) {
                return defaultValue;
            }
            int ordinal = in.readUnsignedShort();
            peekNextEntry();
            T[] constants = clazz.getEnumConstants();
            return ordinal < constants.length ? constants[ordinal] : defaultValue;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String readContent() {
        try {
            if (nextNameIdx == END_NODE || nextType != TYPE_STRING_CONTENT) {
                readEndNode();
                return null;
            }
            String val = readString();
            peekNextEntry();
            readEndNode();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Integer> readIntArrayAttribute(String name) {
        try {
            if (isAttrAbsent(name)) {
                return Collections.emptyList();
            }
            List<Integer> val = readIntArrayRaw();
            peekNextEntry();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<String> readStringArrayAttribute(String name) {
        try {
            if (isAttrAbsent(name)) {
                return Collections.emptyList();
            }
            List<String> val = readStringArrayRaw();
            peekNextEntry();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void skipNode() {
        readChildNodes(nodeName -> skipNode());
    }

    @Override
    public void readChildNodes(ChildNodeReader childNodeReader) {
        try {
            skipRemainingAttributes();
            while (nextNameIdx != END_NODE) {
                String nodeName = names[nextNameIdx];
                if (nodeName == null) {
                    throw new PowsyblException("Cannot read child node: unknown name index " + nextNameIdx);
                }
                peekNextEntry();
                childNodeReader.onStartNode(nodeName);
            }
            peekNextEntry();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void readEndNode() {
        try {
            skipRemainingAttributes();
            if (nextNameIdx != END_NODE) {
                throw new PowsyblException("Binary parsing: expected end node but got name index " + nextNameIdx);
            }
            peekNextEntry();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    boolean readEndOfStream() {
        try {
            return in.isEndOfStream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
