/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractIeeeCdfWriter {

    protected static final String FILLER = " ";
    protected static final String FILLER2 = FILLER + FILLER;
    private static final DecimalFormat DF = new DecimalFormat();

    protected AbstractIeeeCdfWriter() {
        // private constructor to prevent instantiation
        DF.setGroupingUsed(false);
    }

    protected static void writeFooter(BufferedWriter writer, int footerValue) throws IOException {
        writer.write(String.valueOf(footerValue));
        writer.newLine();
    }

    protected static void writeHeader(BufferedWriter writer, String header, List<?> elements) throws IOException {
        writer.write(String.format(header, elements.size()));
        writer.newLine();
    }

    protected static String toString(int value, int index) {
        return toString(value, index, index, true);
    }

    protected static String toString(int value, int start, int end, boolean alignedToTheLeft) {
        return pad(Integer.toString(value), end - start + 1, alignedToTheLeft);
    }

    protected static String toString(double value, int start, int end, boolean alignedToTheLeft) {
        return pad(Double.toString(value), end - start + 1, alignedToTheLeft);
    }

    protected static String toString(String value, int index) {
        return toString(value, index, index, true);
    }

    protected static String toString(String value, int start, int end, boolean alignedToTheLeft) {
        return pad(value, end - start + 1, alignedToTheLeft);
    }

    private static String pad(String s, int width, boolean leftAlign) {
        int len = s.length();
        if (len == width) {
            return s; // exact fit
        }
        if (len > width) {
            return s.substring(0, width); // truncate
        }

        int pad = width - len;
        StringBuilder sb = new StringBuilder(width);

        if (leftAlign) {
            sb.append(s);
            sb.append(" ".repeat(pad));
        } else {
            sb.append(" ".repeat(pad));
            sb.append(s);
        }
        return sb.toString();
    }
}
