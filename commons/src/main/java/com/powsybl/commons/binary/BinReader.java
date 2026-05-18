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
import java.io.UncheckedIOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static com.powsybl.commons.binary.BinUtil.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 * @author Clement Leclerc {@literal <clement.leclerc at rte-france.com>}
 */
public class BinReader extends AbstractTreeDataReader {

    private final BufferedChannelReader in;
    private final byte[] binaryMagicNumber;

    private String[] names;
    private byte[] types;

    private int nextNameIdx = END_NODE;
    private byte nextType;

    public BinReader(ReadableByteChannel channel, byte[] binaryMagicNumber) {
        this.binaryMagicNumber = Objects.requireNonNull(binaryMagicNumber);
        this.in = new BufferedChannelReader(Objects.requireNonNull(channel));
    }

    public BinReader(Path path, byte[] binaryMagicNumber) throws IOException {
        this(Files.newByteChannel(Objects.requireNonNull(path), StandardOpenOption.READ), binaryMagicNumber);
    }

    @Override
    public TreeDataHeader readHeader() {
        TreeDataHeader header = super.readHeader();
        readNamesDictionary();
        peekNextEntry();
        return header;
    }

    @Override
    protected String readRootVersion() {
        byte[] magic = in.readNBytes(binaryMagicNumber.length);
        if (!Arrays.equals(magic, binaryMagicNumber)) {
            throw new PowsyblException("Unexpected bytes at file start");
        }
        return readString();
    }

    @Override
    protected Map<String, String> readExtensionVersions() {
        int nbVersions = in.readUnsignedShort();
        Map<String, String> versions = new HashMap<>();
        for (int i = 0; i < nbVersions; i++) {
            versions.put(readString(), readString());
        }
        return versions;
    }

    private void readNamesDictionary() {
        int nbEntries = in.readUnsignedShort();
        names = new String[nbEntries + 1];
        types = new byte[nbEntries + 1];
        for (int i = 0; i < nbEntries; i++) {
            names[i + 1] = readString();
            types[i + 1] = in.readByte();
        }
    }

    private void peekNextEntry() {
        if (in.isEndOfStream()) {
            nextNameIdx = END_NODE;
            return;
        }
        nextNameIdx = in.readUnsignedShort();
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

    private void skipRemainingAttributes() {
        while (nextNameIdx != END_NODE && nextType != TYPE_OBJECT) {
            skipTypedValue(nextType);
            peekNextEntry();
        }
    }

    private void skipTypedValue(byte typeTag) {
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

    private void skipString() {
        int len = in.readUnsignedShort();
        if (len != NULL_STRING_SENTINEL) {
            in.skipNBytes(len);
        }
    }

    private void skipIntArray() {
        int count = in.readUnsignedShort();
        if (count > 0) {
            in.skipNBytes(4L * count);
        }
    }

    private void skipStringArray() {
        int count = in.readUnsignedShort();
        for (int i = 0; i < count; i++) {
            skipString();
        }
    }

    private String readString() {
        int len = in.readUnsignedShort();
        if (len == NULL_STRING_SENTINEL) {
            return null;
        }
        return new String(in.readNBytes(len), StandardCharsets.UTF_8);
    }

    @Override
    public double readDoubleAttribute(String name, double defaultValue) {
        if (isAttrAbsent(name)) {
            return defaultValue;
        }
        double val = in.readDouble();
        peekNextEntry();
        return val;
    }

    @Override
    public OptionalDouble readOptionalDoubleAttribute(String name) {
        if (isAttrAbsent(name)) {
            return OptionalDouble.empty();
        }
        OptionalDouble val = OptionalDouble.of(in.readDouble());
        peekNextEntry();
        return val;
    }

    @Override
    public float readFloatAttribute(String name, float defaultValue) {
        if (isAttrAbsent(name)) {
            return defaultValue;
        }
        float val = in.readFloat();
        peekNextEntry();
        return val;
    }

    @Override
    public String readStringAttribute(String name) {
        if (isAttrAbsent(name)) {
            return null;
        }
        String val = readString();
        peekNextEntry();
        return val;
    }

    @Override
    public int readIntAttribute(String name) {
        if (isAttrAbsent(name)) {
            throw new PowsyblException("Missing required int attribute: " + name);
        }
        int val = in.readInt();
        peekNextEntry();
        return val;
    }

    @Override
    public int readIntAttribute(String name, int defaultValue) {
        if (isAttrAbsent(name)) {
            return defaultValue;
        }
        int val = in.readInt();
        peekNextEntry();
        return val;
    }

    @Override
    public OptionalInt readOptionalIntAttribute(String name) {
        if (isAttrAbsent(name)) {
            return OptionalInt.empty();
        }
        OptionalInt val = OptionalInt.of(in.readInt());
        peekNextEntry();
        return val;
    }

    @Override
    public boolean readBooleanAttribute(String name) {
        if (isAttrAbsent(name)) {
            throw new PowsyblException("Missing required boolean attribute: " + name);
        }
        boolean val = in.readBoolean();
        peekNextEntry();
        return val;
    }

    @Override
    public boolean readBooleanAttribute(String name, boolean defaultValue) {
        if (isAttrAbsent(name)) {
            return defaultValue;
        }
        boolean val = in.readBoolean();
        peekNextEntry();
        return val;
    }

    @Override
    public Optional<Boolean> readOptionalBooleanAttribute(String name) {
        if (isAttrAbsent(name)) {
            return Optional.empty();
        }
        Optional<Boolean> val = Optional.of(in.readBoolean());
        peekNextEntry();
        return val;
    }

    @Override
    public <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz, T defaultValue) {
        if (isAttrAbsent(name)) {
            return defaultValue;
        }
        int ordinal = in.readUnsignedShort();
        peekNextEntry();
        T[] constants = clazz.getEnumConstants();
        return ordinal < constants.length ? constants[ordinal] : defaultValue;
    }

    @Override
    public String readContent() {
        if (nextNameIdx == END_NODE || nextType != TYPE_STRING_CONTENT) {
            readEndNode();
            return null;
        }
        String val = readString();
        peekNextEntry();
        readEndNode();
        return val;
    }

    @Override
    public List<Integer> readIntArrayAttribute(String name) {
        if (isAttrAbsent(name)) {
            return Collections.emptyList();
        }
        int count = in.readUnsignedShort();
        List<Integer> val = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            val.add(in.readInt());
        }
        peekNextEntry();
        return val;
    }

    @Override
    public List<String> readStringArrayAttribute(String name) {
        if (isAttrAbsent(name)) {
            return Collections.emptyList();
        }
        int count = in.readUnsignedShort();
        List<String> val = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            val.add(readString());
        }
        peekNextEntry();
        return val;
    }

    @Override
    public void skipNode() {
        readChildNodes(nodeName -> skipNode());
    }

    @Override
    public void readChildNodes(ChildNodeReader childNodeReader) {
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
    }

    @Override
    public void readEndNode() {
        skipRemainingAttributes();
        if (nextNameIdx != END_NODE) {
            throw new PowsyblException("Binary parsing: expected end node but got name index " + nextNameIdx);
        }
        peekNextEntry();
    }

    boolean readEndOfStream() {
        return in.isEndOfStream();
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
