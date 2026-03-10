/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.binary;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.powsybl.commons.binary.BinUtil.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BinWriter implements TreeDataWriter {

    private final String rootVersion;
    private final DataOutputStream dos;
    private final DataOutputStream tmpDos;
    private final ByteArrayOutputStream buffer;
    private final byte[] binaryMagicNumber;
    private Map<String, String> extensionVersions = Collections.emptyMap();

    private final Map<String, Integer> nodeNamesIndex = new LinkedHashMap<>();
    private final Map<AttrKey, Integer> attrNamesIndex = new LinkedHashMap<>();

    private record AttrKey(String name, byte type) { }

    private final ByteArrayOutputStream attrBuffer = new ByteArrayOutputStream();
    private final DataOutputStream attrDos = new DataOutputStream(attrBuffer);

    private boolean attrBlockTerminated = false;

    public BinWriter(OutputStream outputStream, byte[] binaryMagicNumber, String rootVersion) {
        this.binaryMagicNumber = Objects.requireNonNull(binaryMagicNumber);
        this.rootVersion = Objects.requireNonNull(rootVersion);
        this.dos = new DataOutputStream(new BufferedOutputStream(Objects.requireNonNull(outputStream)));
        this.buffer = new ByteArrayOutputStream();
        this.tmpDos = new DataOutputStream(buffer);
    }

    private static void writeIndex(int index, DataOutputStream dataOutputStream) {
        try {
            dataOutputStream.writeShort(index);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeString(String value, DataOutputStream dataOutputStream) {
        try {
            if (value == null) {
                writeIndex(-1, dataOutputStream);
            } else {
                byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                writeIndex(bytes.length, dataOutputStream);
                dataOutputStream.write(bytes);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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
        if (nodeNamesIndex.isEmpty()) {
            nodeNamesIndex.put(name, 1); // root element is not a child of another node, hence index is not expected
        } else {
            flushCurrentNodeAttrsIfNeeded();
            int index = nodeNamesIndex.computeIfAbsent(name, n -> 1 + nodeNamesIndex.size());
            writeIndex(index, tmpDos);
        }
        attrBlockTerminated = false;
    }

    @Override
    public void writeEndNode() {
        flushCurrentNodeAttrsIfNeeded();
        writeIndex(END_NODE, tmpDos);
    }

    private void flushCurrentNodeAttrsIfNeeded() {
        if (!attrBlockTerminated) {
            try {
                attrDos.flush();
                tmpDos.write(attrBuffer.toByteArray());
                tmpDos.writeByte(END_ATTRS);
                attrBuffer.reset();
                attrBlockTerminated = true;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void writeAttrIndex(String name, byte type) {
        int index = attrNamesIndex.computeIfAbsent(new AttrKey(name, type), k -> 1 + attrNamesIndex.size());
        if (index > 255) {
            throw new PowsyblException("Binary format: too many distinct attribute (name, type) pairs (max 255)");
        }
        try {
            attrDos.writeByte(index);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeNamespace(String prefix, String namespace) {
        // nothing to do
    }

    @Override
    public void writeNodeContent(String value) {
        writeAttrIndex(BinUtil.CONTENT_ATTR_NAME, TYPE_STRING);
        writeString(value, attrDos);
    }

    @Override
    public void writeStringAttribute(String name, String value) {
        writeAttrIndex(name, TYPE_STRING);
        writeString(value, attrDos);
    }

    @Override
    public void writeDoubleAttribute(String name, double value) {
        if (Double.isNaN(value)) {
            return;
        }
        writeAttrIndex(name, TYPE_DOUBLE);
        try {
            attrDos.writeDouble(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeDoubleAttribute(String name, double value, double absentValue) {
        boolean isAbsent = Double.isNaN(absentValue) ? Double.isNaN(value) : value == absentValue;
        if (isAbsent) {
            return;
        }
        writeAttrIndex(name, TYPE_DOUBLE);
        try {
            attrDos.writeDouble(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeOptionalDoubleAttribute(String name, Double value) {
        if (value == null) {
            return;
        }
        writeAttrIndex(name, TYPE_DOUBLE);
        try {
            attrDos.writeDouble(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeFloatAttribute(String name, float value) {
        if (Float.isNaN(value)) {
            return;
        }
        writeAttrIndex(name, TYPE_FLOAT);
        try {
            attrDos.writeFloat(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeFloatAttribute(String name, float value, float absentValue) {
        boolean isAbsent = Float.isNaN(absentValue) ? Float.isNaN(value) : value == absentValue;
        if (isAbsent) {
            return;
        }
        writeAttrIndex(name, TYPE_FLOAT);
        try {
            attrDos.writeFloat(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeIntAttribute(String name, int value) {
        writeAttrIndex(name, TYPE_INT);
        try {
            attrDos.writeInt(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeIntAttribute(String name, int value, int absentValue) {
        if (value == absentValue) {
            return;
        }
        writeAttrIndex(name, TYPE_INT);
        try {
            attrDos.writeInt(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeOptionalIntAttribute(String name, Integer value) {
        if (value == null) {
            return;
        }
        writeAttrIndex(name, TYPE_INT);
        try {
            attrDos.writeInt(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeBooleanAttribute(String name, boolean value) {
        writeAttrIndex(name, TYPE_BOOLEAN);
        try {
            attrDos.writeBoolean(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeBooleanAttribute(String name, boolean value, boolean absentValue) {
        if (value == absentValue) {
            return;
        }
        writeAttrIndex(name, TYPE_BOOLEAN);
        try {
            attrDos.writeBoolean(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeOptionalBooleanAttribute(String name, Boolean value) {
        if (value == null) {
            return;
        }
        writeAttrIndex(name, TYPE_BOOLEAN);
        try {
            attrDos.writeBoolean(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public <E extends Enum<E>> void writeEnumAttribute(String name, E value) {
        if (value == null) {
            return;
        }
        writeAttrIndex(name, TYPE_ENUM);
        try {
            attrDos.writeShort(value.ordinal());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeIntArrayAttribute(String name, Collection<Integer> values) {
        writeAttrIndex(name, TYPE_INT_ARRAY);
        try {
            attrDos.writeShort(values.size());
            for (int v : values) {
                attrDos.writeInt(v);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeStringArrayAttribute(String name, Collection<String> values) {
        writeAttrIndex(name, TYPE_STRING_ARRAY);
        try {
            attrDos.writeShort(values.size());
            for (String s : values) {
                writeString(s, attrDos);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        try {
            flushCurrentNodeAttrsIfNeeded();
            tmpDos.flush();
            writeHeader();
            dos.write(buffer.toByteArray());
            dos.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeHeader() throws IOException {
        // magic number ("Binary IIDM" in ASCII)
        dos.write(binaryMagicNumber);

        // iidm version
        writeString(rootVersion, dos);

        // extensions versions
        writeIndex(extensionVersions.size(), dos);
        extensionVersions.forEach((extensionName, extensionVersion) -> {
            writeString(extensionName, dos);
            writeString(extensionVersion, dos);
        });

        writeIndex(nodeNamesIndex.size(), dos);
        nodeNamesIndex.forEach((name, index) -> writeString(name, dos));

        writeIndex(attrNamesIndex.size(), dos);
        attrNamesIndex.forEach((key, index) -> {
            writeString(key.name(), dos);
            try {
                dos.writeByte(key.type());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public void setVersions(Map<String, String> extensionVersions) {
        this.extensionVersions = Objects.requireNonNull(extensionVersions);
    }
}
