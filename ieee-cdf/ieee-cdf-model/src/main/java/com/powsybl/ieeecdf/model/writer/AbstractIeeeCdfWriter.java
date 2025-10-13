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
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractIeeeCdfWriter {

    protected AbstractIeeeCdfWriter() {
        // private constructor to prevent instantiation
    }

    protected static void writeFooter(BufferedWriter writer, int footerValue) throws IOException {
        writer.write(String.valueOf(footerValue));
        writer.newLine();
    }

    protected static void writeHeader(BufferedWriter writer, String header, List<?> elements) throws IOException {
        writer.write(String.format(header, elements.size()));
        writer.newLine();
    }
}
