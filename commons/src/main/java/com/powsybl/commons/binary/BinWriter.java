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

import java.io.*;
import java.util.*;

import static com.powsybl.commons.binary.BinUtil.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BinWriter extends AbstractTreeDataWriter {

    private final String rootVersion;
    private final DataOutputStream dos;
    private final DataOutputStream tmpDos;
    private final ByteArrayOutputStream buffer;
    private final byte[] binaryMagicNumber;
    private Map<String, String> extensionVersions = Collections.emptyMap();

    private final Map<TypedName, Integer> namesIndex = new LinkedHashMap<>();

    private record TypedName(String name, byte type) { }

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
                writeIndex(NULL_STRING_SENTINEL, dataOutputStream);
            } else {
                byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                if (bytes.length >= NULL_STRING_SENTINEL) {
                    throw new PowsyblException("Binary format: string too long (max " + (NULL_STRING_SENTINEL - 1) + " bytes)");
                }
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
        if (namesIndex.isEmpty()) {
            namesIndex.put(new TypedName(name, TYPE_OBJECT), 1); // root element is not a child of another node, hence index is not expected
        } else {
            writeEntry(name, TYPE_OBJECT);
        }
    }

    @Override
    public void writeEndNode() {
        writeIndex(END_NODE, tmpDos);
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
        writeIndex(index, tmpDos);
    }

    @Override
    public void writeNamespace(String prefix, String namespace) {
        // nothing to do
    }

    @Override
    public void writeNodeContent(String value) {
        writeEntry("", TYPE_STRING_CONTENT);
        writeString(value, tmpDos);
    }

    @Override
    public void writeStringAttribute(String name, String value) {
        writeEntry(name, TYPE_STRING);
        writeString(value, tmpDos);
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
        try {
            tmpDos.writeDouble(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeFloatAttribute(String name, float value) {
        if (Float.isNaN(value)) {
            return;
        }
        writeEntry(name, TYPE_FLOAT);
        try {
            tmpDos.writeFloat(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeIntAttribute(String name, int value) {
        writeEntry(name, TYPE_INT);
        try {
            tmpDos.writeInt(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
        try {
            tmpDos.writeBoolean(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
        try {
            tmpDos.writeShort(value.ordinal());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeIntArrayAttribute(String name, Collection<Integer> values) {
        writeEntry(name, TYPE_INT_ARRAY);
        try {
            tmpDos.writeShort(values.size());
            for (int v : values) {
                tmpDos.writeInt(v);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeStringArrayAttribute(String name, Collection<String> values) {
        writeEntry(name, TYPE_STRING_ARRAY);
        try {
            tmpDos.writeShort(values.size());
            for (String s : values) {
                writeString(s, tmpDos);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        try {
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

        writeIndex(namesIndex.size(), dos);
        namesIndex.keySet().forEach(nameTypeKey -> {
            writeString(nameTypeKey.name(), dos);
            try {
                dos.writeByte(nameTypeKey.type());
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
