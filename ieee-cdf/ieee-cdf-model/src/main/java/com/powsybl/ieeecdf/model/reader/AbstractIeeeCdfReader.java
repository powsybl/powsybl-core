/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.reader;

import com.powsybl.ieeecdf.model.elements.AbstractIeeeElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractIeeeCdfReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIeeeCdfReader.class);

    protected AbstractIeeeCdfReader() {
    }

    protected static <A extends AbstractIeeeElement> List<A> readLines(BufferedReader reader, int footerValue,
                                                                       Function<String, A> constructor,
                                                                       int expectedItemsNumber) throws IOException {
        List<A> ieeeElements = new ArrayList<>();
        String currentLine;
        while ((currentLine = reader.readLine()) != null) {
            if (currentLine.startsWith(String.valueOf(footerValue))) {
                break; // fin de section
            }
            ieeeElements.add(constructor.apply(currentLine));
        }
        if (ieeeElements.size() != expectedItemsNumber) {
            LOGGER.warn("Wrong number of elements parsed in the IEEE-CDF file: {} (expected {})", ieeeElements.size(), expectedItemsNumber);
        }
        return ieeeElements;
    }

    protected static void readInteger(String line, int start, int end, IntConsumer consumer) {
        if (start < line.length() + 1) {
            String value = line.substring(start - 1, Math.min(end, line.length())).trim();
            if (!value.isEmpty()) {
                consumer.accept(Integer.parseInt(value));
            }
        }
    }

    protected static void readInteger(String line, int index, IntConsumer consumer) {
        readInteger(line, index, index, consumer);
    }

    protected static void readDouble(String line, int start, int end, DoubleConsumer consumer) {
        if (start < line.length() + 1) {
            String value = line.substring(start - 1, Math.min(end, line.length())).trim();
            if (!value.isEmpty()) {
                consumer.accept(Double.parseDouble(value));
            }
        }
    }

    protected static void readString(String line, int start, int end, Consumer<String> consumer) {
        if (start < line.length() + 1) {
            String value = line.substring(start - 1, Math.min(end, line.length())).trim();
            if (!value.isEmpty()) {
                consumer.accept(value.trim());
            }
        }
    }

    protected static void readString(String line, int index, Consumer<String> consumer) {
        readString(line, index, index, consumer);
    }
}
