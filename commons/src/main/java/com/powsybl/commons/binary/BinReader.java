/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.binary;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataHeader;
import com.powsybl.commons.io.TreeDataReader;

import java.io.*;
import java.util.*;

import static com.powsybl.commons.binary.BinUtil.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BinReader implements TreeDataReader {

    private final DataInputStream dis;
    private final byte[] binaryMagicNumber;

    private String[] nodeNames;

    private String[] attrNames;
    private byte[] attrTypes;

    private int nextAttrIdx = END_ATTRS;

    private static final int BUFFER_SIZE = 512 * 1024;

    public BinReader(InputStream is, byte[] binaryMagicNumber) {
        this.binaryMagicNumber = binaryMagicNumber;
        this.dis = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(is), BUFFER_SIZE));
    }

    @Override
    public TreeDataHeader readHeader() {
        try {
            readMagicNumber();
            TreeDataHeader header = new TreeDataHeader(readString(), readExtensionVersions());
            readNodeDictionary();
            readAttrDictionary();
            nextAttrIdx = dis.readUnsignedByte();
            return header;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void readMagicNumber() throws IOException {
        byte[] read = dis.readNBytes(binaryMagicNumber.length);
        if (!Arrays.equals(read, binaryMagicNumber)) {
            throw new PowsyblException("Unexpected bytes at file start");
        }
    }

    public Map<String, String> readExtensionVersions() throws IOException {
        int nbVersions = dis.readShort();
        Map<String, String> versions = new HashMap<>();
        for (int i = 0; i < nbVersions; i++) {
            versions.put(readString(), readString());
        }
        return versions;
    }

    private void readNodeDictionary() throws IOException {
        int nbEntries = dis.readShort();
        nodeNames = new String[nbEntries + 1];
        for (int i = 0; i < nbEntries; i++) {
            nodeNames[i + 1] = readString();
        }
    }

    private void readAttrDictionary() throws IOException {
        int nbEntries = dis.readShort();
        attrNames = new String[nbEntries + 1];
        attrTypes = new byte[nbEntries + 1];
        for (int i = 0; i < nbEntries; i++) {
            attrNames[i + 1] = readString();
            attrTypes[i + 1] = dis.readByte();
        }
    }

    /**
     * Checks whether the next attribute matches the requested name.
     * Does NOT consume any byte from the stream: nextAttrIdx must be updated
     * by the caller after reading the value.
     */
    private boolean matchNextAttr(String name) {
        if (nextAttrIdx == END_ATTRS) {
            return false;
        }
        String attrName = attrNames[nextAttrIdx];
        if (attrName == null) {
            throw new PowsyblException("Cannot read attribute: unknown attribute name index " + nextAttrIdx);
        }
        return name.equals(attrName);
    }

    private void skipTypedValue(byte typeTag) throws IOException {
        switch (typeTag) {
            case TYPE_DOUBLE -> dis.readDouble();
            case TYPE_FLOAT -> dis.readFloat();
            case TYPE_INT -> dis.readInt();
            case TYPE_BOOLEAN -> dis.readBoolean();
            case TYPE_STRING -> readString();
            case TYPE_ENUM -> dis.readShort();
            case TYPE_INT_ARRAY -> skipIntArray();
            case TYPE_STRING_ARRAY -> skipStringArray();
            default -> throw new PowsyblException("Binary format: unknown attribute type tag " + typeTag);
        }
    }

    private void skipIntArray() throws IOException {
        int count = dis.readShort();
        dis.skipBytes(count * Integer.BYTES);
    }

    private void skipStringArray() throws IOException {
        int count = dis.readShort();
        for (int i = 0; i < count; i++) {
            readString();
        }
    }

    private void skipRemainingAttributes() throws IOException {
        while (nextAttrIdx != END_ATTRS) {
            skipTypedValue(attrTypes[nextAttrIdx]);
            nextAttrIdx = dis.readUnsignedByte();
        }
    }

    private List<Integer> readIntArrayRaw() {
        try {
            int count = dis.readShort();
            List<Integer> list = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                list.add(dis.readInt());
            }
            return list;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> readStringArrayRaw() {
        try {
            int count = dis.readShort();
            List<String> list = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                list.add(readString());
            }
            return list;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String readString() {
        try {
            return dis.readUTF();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public double readDoubleAttribute(String name) {
        if (!matchNextAttr(name)) {
            return Double.NaN;
        }
        try {
            double val = dis.readDouble();
            nextAttrIdx = dis.readUnsignedByte();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public double readDoubleAttribute(String name, double defaultValue) {
        if (!matchNextAttr(name)) {
            return defaultValue;
        }
        try {
            double val = dis.readDouble();
            nextAttrIdx = dis.readUnsignedByte();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public OptionalDouble readOptionalDoubleAttribute(String name) {
        if (!matchNextAttr(name)) {
            return OptionalDouble.empty();
        }
        try {
            double val = dis.readDouble();
            nextAttrIdx = dis.readUnsignedByte();
            return OptionalDouble.of(val);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public float readFloatAttribute(String name) {
        if (!matchNextAttr(name)) {
            return Float.NaN;
        }
        try {
            float val = dis.readFloat();
            nextAttrIdx = dis.readUnsignedByte();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public float readFloatAttribute(String name, float defaultValue) {
        if (!matchNextAttr(name)) {
            return defaultValue;
        }
        try {
            float val = dis.readFloat();
            nextAttrIdx = dis.readUnsignedByte();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String readStringAttribute(String name) {
        if (!matchNextAttr(name)) {
            return null;
        }
        String val = readString();
        try {
            nextAttrIdx = dis.readUnsignedByte();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return val;
    }

    @Override
    public int readIntAttribute(String name) {
        if (!matchNextAttr(name)) {
            return 0;
        }
        try {
            int val = dis.readInt();
            nextAttrIdx = dis.readUnsignedByte();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public int readIntAttribute(String name, int defaultValue) {
        if (!matchNextAttr(name)) {
            return defaultValue;
        }
        try {
            int val = dis.readInt();
            nextAttrIdx = dis.readUnsignedByte();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public OptionalInt readOptionalIntAttribute(String name) {
        if (!matchNextAttr(name)) {
            return OptionalInt.empty();
        }
        try {
            int val = dis.readInt();
            nextAttrIdx = dis.readUnsignedByte();
            return OptionalInt.of(val);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean readBooleanAttribute(String name) {
        if (!matchNextAttr(name)) {
            return false;
        }
        try {
            boolean val = dis.readBoolean();
            nextAttrIdx = dis.readUnsignedByte();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean readBooleanAttribute(String name, boolean defaultValue) {
        if (!matchNextAttr(name)) {
            return defaultValue;
        }
        try {
            boolean val = dis.readBoolean();
            nextAttrIdx = dis.readUnsignedByte();
            return val;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<Boolean> readOptionalBooleanAttribute(String name) {
        if (!matchNextAttr(name)) {
            return Optional.empty();
        }
        try {
            boolean val = dis.readBoolean();
            nextAttrIdx = dis.readUnsignedByte();
            return Optional.of(val);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz) {
        return readEnumAttribute(name, clazz, null);
    }

    @Override
    public <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz, T defaultValue) {
        if (!matchNextAttr(name)) {
            return defaultValue;
        }
        try {
            int ordinal = dis.readShort();
            nextAttrIdx = dis.readUnsignedByte();
            T[] constants = clazz.getEnumConstants();
            return (ordinal >= 0 && ordinal < constants.length) ? constants[ordinal] : defaultValue;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String readContent() {
        String val = readStringAttribute(BinUtil.CONTENT_ATTR_NAME);
        readEndNode();
        return val;
    }

    @Override
    public List<Integer> readIntArrayAttribute(String name) {
        if (!matchNextAttr(name)) {
            return Collections.emptyList();
        }
        List<Integer> val = readIntArrayRaw();
        try {
            nextAttrIdx = dis.readUnsignedByte();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return val;
    }

    @Override
    public List<String> readStringArrayAttribute(String name) {
        if (!matchNextAttr(name)) {
            return Collections.emptyList();
        }
        List<String> val = readStringArrayRaw();
        try {
            nextAttrIdx = dis.readUnsignedByte();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return val;
    }

    @Override
    public void skipNode() {
        readChildNodes(nodeName -> skipNode());
    }

    @Override
    public void readChildNodes(ChildNodeReader childNodeReader) {
        try {
            skipRemainingAttributes();
            int nodeNameIndex;
            while ((nodeNameIndex = dis.readShort()) != END_NODE) {
                String nodeName = nodeNames[nodeNameIndex];
                if (nodeName == null) {
                    throw new PowsyblException("Cannot read child node: unknown node name index " + nodeNameIndex);
                }
                nextAttrIdx = dis.readUnsignedByte();
                childNodeReader.onStartNode(nodeName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void readEndNode() {
        try {
            skipRemainingAttributes();
            int nextIndex = dis.readShort();
            if (nextIndex != END_NODE) {
                throw new PowsyblException("Binary parsing: expected end node but got " + nextIndex);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        try {
            dis.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
