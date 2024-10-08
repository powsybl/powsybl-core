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
import java.util.function.Supplier;

import static com.powsybl.commons.binary.BinUtil.END_NODE;
import static com.powsybl.commons.binary.BinUtil.NULL_ENUM;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BinReader implements TreeDataReader {

    private final DataInputStream dis;
    private final Map<Integer, String> dictionary = new HashMap<>();
    private final byte[] binaryMagicNumber;

    public BinReader(InputStream is, byte[] binaryMagicNumber) {
        this.binaryMagicNumber = binaryMagicNumber;
        this.dis = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(is)));
    }

    @Override
    public TreeDataHeader readHeader() {
        try {
            readMagicNumber();
            TreeDataHeader header = new TreeDataHeader(readString(), readExtensionVersions());
            readDictionary();
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

    private void readDictionary() throws IOException {
        int nbEntries = dis.readShort();
        for (int i = 0; i < nbEntries; i++) {
            dictionary.put(i + 1, readString());
        }
    }

    private String readString() {
        try {
            int stringNbBytes = dis.readShort();
            if (stringNbBytes == -1) {
                return null;
            }
            byte[] stringBytes = dis.readNBytes(stringNbBytes);
            if (stringBytes.length != stringNbBytes) {
                throw new PowsyblException("Cannot read the full string, bytes missing: " + (stringNbBytes - stringBytes.length)
                        + " (this may happen when the attribute wasn't written in the first place, causing string length to be an aberrant number)");
            }
            return new String(stringBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private double readDouble() {
        try {
            return dis.readDouble();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private float readFloat() {
        try {
            return dis.readFloat();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private int readInt() {
        try {
            return dis.readInt();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean readBoolean() {
        try {
            return dis.readBoolean();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private <T extends Enum<T>> T readEnum(Class<T> clazz) {
        try {
            short ordinal = dis.readShort();
            return ordinal != NULL_ENUM ? clazz.getEnumConstants()[ordinal] : null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private <T> List<T> readArray(Supplier<T> valueReader) {
        try {
            int nbValues = dis.readShort();
            List<T> values = new ArrayList<>(nbValues);
            for (int i = 0; i < nbValues; i++) {
                values.add(valueReader.get());
            }
            return values;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public double readDoubleAttribute(String name) {
        return readDouble();
    }

    @Override
    public double readDoubleAttribute(String name, double defaultValue) {
        return readDouble();
    }

    @Override
    public OptionalDouble readOptionalDoubleAttribute(String name) {
        if (!readBoolean()) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(readDouble());
    }

    @Override
    public float readFloatAttribute(String name) {
        return readFloat();
    }

    @Override
    public float readFloatAttribute(String name, float defaultValue) {
        return readFloat();
    }

    @Override
    public String readStringAttribute(String name) {
        return readString();
    }

    @Override
    public int readIntAttribute(String name) {
        return readInt();
    }

    @Override
    public OptionalInt readOptionalIntAttribute(String name) {
        if (!readBoolean()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(readInt());
    }

    @Override
    public int readIntAttribute(String name, int defaultValue) {
        return readInt();
    }

    @Override
    public boolean readBooleanAttribute(String name) {
        return readBoolean();
    }

    @Override
    public boolean readBooleanAttribute(String name, boolean defaultValue) {
        return readBoolean();
    }

    @Override
    public Optional<Boolean> readOptionalBooleanAttribute(String name) {
        if (!readBoolean()) {
            return Optional.empty();
        }
        return Optional.of(readBoolean());
    }

    @Override
    public <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz) {
        return readEnum(clazz);
    }

    @Override
    public <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz, T defaultValue) {
        return readEnum(clazz);
    }

    @Override
    public String readContent() {
        String content = readString();
        readEndNode();
        return content;
    }

    @Override
    public List<Integer> readIntArrayAttribute(String name) {
        return readArray(this::readInt);
    }

    @Override
    public List<String> readStringArrayAttribute(String name) {
        return readArray(this::readString);
    }

    @Override
    public void skipChildNodes() {
        throw new PowsyblException("Binary format does not support skipping child nodes");
    }

    @Override
    public void readChildNodes(ChildNodeReader childNodeReader) {
        try {
            int nodeNameIndex;
            while ((nodeNameIndex = dis.readShort()) != END_NODE) {
                String nodeName = dictionary.get(nodeNameIndex);
                if (nodeName == null) {
                    throw new PowsyblException("Cannot read child node: unknown element name index " + nodeNameIndex);
                }
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
