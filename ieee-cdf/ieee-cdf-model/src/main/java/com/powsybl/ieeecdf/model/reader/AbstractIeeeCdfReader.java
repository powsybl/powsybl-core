/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.reader;

import org.jsapar.model.Line;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractIeeeCdfReader {

    protected AbstractIeeeCdfReader() {
    }

    protected static StringReader readLines(BufferedReader reader, int footerValue) throws IOException {
        List<String> lines = new ArrayList<>();
        String currentLine;
        while ((currentLine = reader.readLine()) != null) {
            if (currentLine.startsWith(String.valueOf(footerValue))) {
                break; // fin de section
            }
            lines.add(currentLine);
        }
        return new StringReader(String.join(System.lineSeparator(), lines));
    }

    protected static void readInteger(Line line, String name, IntConsumer consumer) {
        if (line.containsNonEmptyCell(name)) {
            consumer.accept(Integer.parseInt(line.getExistingCell(name).getStringValue().trim()));
        }
    }

    protected static void readDouble(Line line, String name, DoubleConsumer consumer) {
        if (line.containsNonEmptyCell(name)) {
            consumer.accept(Double.parseDouble(line.getExistingCell(name).getStringValue().trim()));
        }
    }

    protected static void readString(Line line, String name, Consumer<String> consumer) {
        if (line.containsNonEmptyCell(name)) {
            consumer.accept(line.getExistingCell(name).getStringValue().trim());
        }
    }
}
