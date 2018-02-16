/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class UcteRecordWriter {

    private enum Alignment {
        RIGHT, LEFT
    }

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
        resizeBuffer(endIndex);
        buffer.replace(beginIndex, endIndex, value);
    }

    private String alignAndTruncate(String str, int strLen, Alignment alignment) {
        String format = String.format(Locale.US, alignment.equals(Alignment.LEFT) ? "%%-%ds" : "%%%ds", strLen);
        String formattedStr = String.format(Locale.US, format, str);
        return formattedStr.substring(0, strLen);
    }

    // floats are left aligned, zero padded to fill the field length
    void writeFloat(float value, int beginIndex, int endIndex) {
        if (Float.isNaN(value)) {
            return;
        }
        String fieldStr = alignAndTruncate(Float.toString(value), endIndex - beginIndex, Alignment.LEFT);
        writeString(fieldStr.replace(' ', '0'), beginIndex, endIndex);
    }

    // integers are right aligned
    void writeInteger(Integer value, int beginIndex, int endIndex) {
        if (value == null) {
            return;
        }
        String fieldStr = alignAndTruncate(Integer.toString(value), endIndex - beginIndex, Alignment.RIGHT);
        writeString(fieldStr, beginIndex, endIndex);
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

    private void flush() throws IOException {
        writer.write(buffer.toString());
        buffer.setLength(0); // reset buffer
    }

    void newLine() throws IOException {
        flush();
        writer.newLine();
    }

}
