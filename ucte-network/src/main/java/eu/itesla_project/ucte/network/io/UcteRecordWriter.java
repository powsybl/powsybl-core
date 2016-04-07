/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.ucte.network.io;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class UcteRecordWriter {

    private final BufferedWriter writer;

    private final StringBuilder buffer = new StringBuilder();

    UcteRecordWriter(BufferedWriter writer) {
        this.writer = writer;
    }

    private void resizeBuffer(int length) {
        int n = length - buffer.length();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                buffer.append(" ");
            }
        }
    }

    void writeChar(Character value, int index) {
        if (value == null) {
            return;
        }
        resizeBuffer(index + 1);
        buffer.setCharAt(index, value);
    }

    void writeString(String value, int beginIndex, int endIndex) {
        if (value == null) {
            return;
        }
        resizeBuffer(endIndex + 1);
        buffer.replace(beginIndex, endIndex, value);
    }

    void writeFloat(float value, int beginIndex, int endIndex) {
        if (Float.isNaN(value)) {
            return;
        }
        writeString(Float.toString(value), beginIndex, endIndex);
    }

    void writeInteger(Integer value, int beginIndex, int endIndex) {
        if (value == null) {
            return;
        }
        writeString(Integer.toString(value), beginIndex, endIndex);
    }

    void writeInteger(Integer value, int index) {
        if (value == null) {
            return;
        }
        if (value < 0 || value > 9) {
            throw new UcteIoException("Integer value does not fit into one character");
        }
        writeChar(Integer.toString(value).charAt(0), index);
    }

    void writeEnumOrdinal(Enum<?> value, int index) {
        if (value == null) {
            return;
        }
        if (value.ordinal() > 9) {
            throw new UcteIoException("Enum ordinal cannot fit into one character");
        }
        writeChar(Integer.toString(value.ordinal()).charAt(0), index);
    }

    void writeEnumValue(Enum<?> value, int beginIndex, int endIndex) {
        if (value == null) {
            return;
        }
        if (value.name().length() > (endIndex - beginIndex)) {
            throw new UcteIoException("Enum value cannot fit into " + (endIndex - beginIndex) + " characters");
        }
        writeString(value.name(), beginIndex, endIndex);
    }

    void writeEnumValue(Enum<?> value, int index) {
        if (value == null) {
            return;
        }
        if (value.name().length() > 1) {
            throw new UcteIoException("Enum value cannot fit into one characters");
        }
        writeChar(value.name().charAt(0), index);
    }

    void flush() throws IOException {
        writer.write(buffer.toString());
        buffer.setLength(0); // reset buffer
    }

    void newLine() throws IOException {
        flush();
        writer.newLine();
    }

}
