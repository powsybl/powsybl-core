/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.util;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class GraphWriter extends StringWriter {
    private StringWriter writer;
    private StringBuffer stringBuffer;

    public GraphWriter(StringWriter writer) {
        this.writer = writer;
        this.stringBuffer = new StringBuffer();
    }

    @Override
    public void write(char[] chars, int i, int i1) {
        String s = new String(chars);
        this.write(s);

    }

    @Override
    public void write(@NonNull String s) {
        if (!s.contains("\n")) {
            stringBuffer.append(s);
        } else if (s.equals("\n")) {
            this.stringBuffer.append(s);
            this.writeBuf();
        } else {
            // done to properly control the last loop
            boolean endsWithNewline = s.endsWith("\n");
            // escape the \ in the regex
            String[] parts = s.split("\n");
            //cut the string at all the `\n` and write the different parts one after the other
            for (int i = 0; i < parts.length - 1; ++i) {
                String part = parts[i];
                stringBuffer.append(part);
                stringBuffer.append("\n");
                this.writeBuf();
            }
            stringBuffer.append(parts[parts.length - 1]);
            if (endsWithNewline) {
                stringBuffer.append("\n");
                this.writeBuf();
            }
        }
    }

    @Override
    public String toString() {
        return this.writer.toString();
    }

    @Override
    public void flush() {
        this.writer.flush();
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }

    protected void writeBuf() {
        String s = new String(this.stringBuffer);
        s = s.replace("->", "--");
        s = s.replace("[", "");
        s = s.replace("]", "");
        s = s.replace("digraph", "graph");
        s = s.replaceAll("\\t\\w* ;", "");
        s = s.replaceAll("\\t[a-zA-Z]{2,}.*;", "");
        if (!s.isBlank()) {
            writer.write(s);
        }
        // reset the stringBuffer as empty
        this.stringBuffer.setLength(0);
    }
}
