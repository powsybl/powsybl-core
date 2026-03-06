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
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.powsybl.commons.binary.BinUtil.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BinReader implements TreeDataReader {

    private final DataInputStream dis;
    private final byte[] binaryMagicNumber;

    private final Map<Integer, String> nodeDictionary = new HashMap<>();

    private final Map<Integer, String> attrDictionary = new HashMap<>();

    private Map<String, Object> currentAttrs = Collections.emptyMap();

    public BinReader(InputStream is, byte[] binaryMagicNumber) {
        this.binaryMagicNumber = binaryMagicNumber;
        this.dis = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(is)));
    }

    @Override
    public TreeDataHeader readHeader() {
        try {
            readMagicNumber();
            TreeDataHeader header = new TreeDataHeader(readString(), readExtensionVersions());
            readNodeDictionary();
            readAttrDictionary();
            preReadCurrentNodeAttributes();
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
        for (int i = 0; i < nbEntries; i++) {
            nodeDictionary.put(i + 1, readString());
        }
    }

    private void readAttrDictionary() throws IOException {
        int nbEntries = dis.readShort();
        for (int i = 0; i < nbEntries; i++) {
            attrDictionary.put(i + 1, readString());
        }
    }

    private void preReadCurrentNodeAttributes() throws IOException {
        currentAttrs = new HashMap<>();
        int attrIdx;
        while ((attrIdx = dis.readUnsignedByte()) != END_ATTRS) {
            String attrName = attrDictionary.get(attrIdx);
            if (attrName == null) {
                throw new PowsyblException("Binary format: unknown attribute index " + attrIdx);
            }
            byte typeTag = dis.readByte();
            Object value = readTypedValue(typeTag);
            currentAttrs.put(attrName, value);
        }
    }

    private Object readTypedValue(byte typeTag) throws IOException {
        return switch (typeTag) {
            case TYPE_DOUBLE -> dis.readDouble();
            case TYPE_FLOAT -> dis.readFloat();
            case TYPE_INT -> dis.readInt();
            case TYPE_BOOLEAN -> dis.readBoolean();
            case TYPE_STRING -> readString();
            case TYPE_ENUM -> (int) dis.readShort();
            case TYPE_INT_ARRAY -> readIntArrayRaw();
            case TYPE_STRING_ARRAY -> readStringArrayRaw();
            default -> throw new PowsyblException("Binary format: unknown attribute type tag " + typeTag);
        };
    }

    private List<Integer> readIntArrayRaw() throws IOException {
        int count = dis.readShort();
        List<Integer> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(dis.readInt());
        }
        return list;
    }

    private List<String> readStringArrayRaw() throws IOException {
        int count = dis.readShort();
        List<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(readString());
        }
        return list;
    }

    private String readString() {
        try {
            int stringNbBytes = dis.readShort();
            if (stringNbBytes == -1) {
                return null;
            }
            byte[] stringBytes = dis.readNBytes(stringNbBytes);
            if (stringBytes.length != stringNbBytes) {
                throw new PowsyblException("Cannot read the full string, bytes missing: " + (stringNbBytes - stringBytes.length));
            }
            return new String(stringBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public double readDoubleAttribute(String name) {
        Object val = currentAttrs.get(name);
        return val instanceof Double d ? d : Double.NaN;
    }

    @Override
    public double readDoubleAttribute(String name, double defaultValue) {
        Object val = currentAttrs.get(name);
        return val instanceof Double d ? d : defaultValue;
    }

    @Override
    public OptionalDouble readOptionalDoubleAttribute(String name) {
        Object val = currentAttrs.get(name);
        return val instanceof Double d ? OptionalDouble.of(d) : OptionalDouble.empty();
    }

    @Override
    public float readFloatAttribute(String name) {
        Object val = currentAttrs.get(name);
        return val instanceof Float f ? f : Float.NaN;
    }

    @Override
    public float readFloatAttribute(String name, float defaultValue) {
        Object val = currentAttrs.get(name);
        return val instanceof Float f ? f : defaultValue;
    }

    @Override
    public String readStringAttribute(String name) {
        Object val = currentAttrs.get(name);
        return val instanceof String s ? s : null;
    }

    @Override
    public int readIntAttribute(String name) {
        Object val = currentAttrs.get(name);
        return val instanceof Integer i ? i : 0;
    }

    @Override
    public int readIntAttribute(String name, int defaultValue) {
        Object val = currentAttrs.get(name);
        return val instanceof Integer i ? i : defaultValue;
    }

    @Override
    public OptionalInt readOptionalIntAttribute(String name) {
        Object val = currentAttrs.get(name);
        return val instanceof Integer i ? OptionalInt.of(i) : OptionalInt.empty();
    }

    @Override
    public boolean readBooleanAttribute(String name) {
        Object val = currentAttrs.get(name);
        return val instanceof Boolean b ? b : false;
    }

    @Override
    public boolean readBooleanAttribute(String name, boolean defaultValue) {
        Object val = currentAttrs.get(name);
        return val instanceof Boolean b ? b : defaultValue;
    }

    @Override
    public Optional<Boolean> readOptionalBooleanAttribute(String name) {
        Object val = currentAttrs.get(name);
        return val instanceof Boolean b ? Optional.of(b) : Optional.empty();
    }

    @Override
    public <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz) {
        return readEnumAttribute(name, clazz, null);
    }

    @Override
    public <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz, T defaultValue) {
        Object val = currentAttrs.get(name);
        if (val instanceof Integer ordinal) {
            T[] constants = clazz.getEnumConstants();
            if (ordinal >= 0 && ordinal < constants.length) {
                return constants[ordinal];
            }
        }
        return defaultValue;
    }

    @Override
    public String readContent() {
        String content = (String) currentAttrs.get(BinUtil.CONTENT_ATTR_NAME);
        readEndNode();
        return content;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Integer> readIntArrayAttribute(String name) {
        Object val = currentAttrs.get(name);
        return val instanceof List<?> list ? (List<Integer>) list : Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> readStringArrayAttribute(String name) {
        Object val = currentAttrs.get(name);
        return val instanceof List<?> list ? (List<String>) list : Collections.emptyList();
    }

    @Override
    public void skipNode() {
        readChildNodes(nodeName -> skipNode());
    }

    @Override
    public void readChildNodes(ChildNodeReader childNodeReader) {
        try {
            int nodeNameIndex;
            while ((nodeNameIndex = dis.readShort()) != END_NODE) {
                String nodeName = nodeDictionary.get(nodeNameIndex);
                if (nodeName == null) {
                    throw new PowsyblException("Cannot read child node: unknown node name index " + nodeNameIndex);
                }
                preReadCurrentNodeAttributes();
                childNodeReader.onStartNode(nodeName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void readEndNode() {
        try {
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
