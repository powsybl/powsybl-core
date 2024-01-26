/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.binary;

import com.powsybl.commons.io.TreeDataWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.powsybl.commons.binary.BinUtil.END_NODE;
import static com.powsybl.commons.binary.BinUtil.NULL_ENUM;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BinWriter implements TreeDataWriter {

    private final String rootVersion;
    private final DataOutputStream dos;
    private final DataOutputStream tmpDos;
    private final ByteArrayOutputStream buffer;
    private final Map<String, Integer> nodeNamesIndex = new LinkedHashMap<>();
    private Map<String, String> extensionVersions;
    private final byte[] binaryMagicNumber;

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

    private void writeDouble(double value) {
        try {
            tmpDos.writeDouble(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeInt(int value) {
        try {
            tmpDos.writeInt(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private <T> void writeArray(Collection<T> values, Consumer<T> valueWriter) {
        try {
            tmpDos.writeShort(values.size());
            for (T value : values) {
                valueWriter.accept(value);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeBoolean(boolean value) {
        try {
            tmpDos.writeBoolean(value);
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
            int index = nodeNamesIndex.computeIfAbsent(name, n -> 1 + nodeNamesIndex.size());
            writeIndex(index, tmpDos);
        }
    }

    @Override
    public void writeEndNode() {
        writeIndex(END_NODE, tmpDos);
    }

    @Override
    public void writeNamespace(String prefix, String namespace) {
        // nothing to do
    }

    @Override
    public void writeNodeContent(String value) {
        writeString(value, tmpDos);
    }

    @Override
    public void writeStringAttribute(String name, String value) {
        writeString(value, tmpDos);
    }

    @Override
    public void writeFloatAttribute(String name, float value) {
        try {
            tmpDos.writeFloat(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeDoubleAttribute(String name, double value) {
        writeDouble(value);
    }

    @Override
    public void writeDoubleAttribute(String name, double value, double absentValue) {
        writeDouble(value);
    }

    @Override
    public void writeOptionalDoubleAttribute(String name, Double value) {
        try {
            tmpDos.writeBoolean(value != null);
            if (value != null) {
                writeDouble(value);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeIntAttribute(String name, int value) {
        writeInt(value);
    }

    @Override
    public void writeIntAttribute(String name, int value, int absentValue) {
        writeInt(value);
    }

    @Override
    public void writeOptionalIntAttribute(String name, Integer value) {
        try {
            tmpDos.writeBoolean(value != null);
            if (value != null) {
                writeInt(value);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeIntArrayAttribute(String name, Collection<Integer> values) {
        writeArray(values, this::writeInt);
    }

    @Override
    public void writeStringArrayAttribute(String name, Collection<String> values) {
        writeArray(values, s -> writeString(s, tmpDos));
    }

    @Override
    public <E extends Enum<E>> void writeEnumAttribute(String name, E value) {
        writeIndex(value != null ? value.ordinal() : NULL_ENUM, tmpDos);
    }

    @Override
    public void writeBooleanAttribute(String name, boolean value) {
        writeBoolean(value);
    }

    @Override
    public void writeBooleanAttribute(String name, boolean value, boolean absentValue) {
        writeBoolean(value);
    }

    @Override
    public void writeOptionalBooleanAttribute(String name, Boolean value) {
        try {
            tmpDos.writeBoolean(value != null);
            if (value != null) {
                writeBoolean(value);
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

        // dictionary
        writeIndex(nodeNamesIndex.size(), dos);
        nodeNamesIndex.forEach((name, index) -> writeString(name, dos));
    }

    @Override
    public void setVersions(Map<String, String> extensionVersions) {
        this.extensionVersions = Objects.requireNonNull(extensionVersions);
    }
}
