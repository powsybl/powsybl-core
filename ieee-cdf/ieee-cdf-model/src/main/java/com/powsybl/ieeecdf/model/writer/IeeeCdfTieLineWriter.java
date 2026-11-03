/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.writer;

import com.powsybl.ieeecdf.model.elements.IeeeCdfTieLine;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfTieLineWriter extends AbstractIeeeCdfWriter {

    private IeeeCdfTieLineWriter() {
        // private constructor to prevent instantiation
    }

    public static void writeTieLines(BufferedWriter writer, List<IeeeCdfTieLine> tieLineList) throws IOException {
        writeHeader(writer, "TIE LINES FOLLOWS                     %d ITEMS", tieLineList);
        for (IeeeCdfTieLine bean : tieLineList) {
            writer.write(convertTieLineToLine(bean));
            writer.newLine();
        }
        writeFooter(writer, -999);
    }

    private static String convertTieLineToLine(IeeeCdfTieLine tieLine) {
        return toString(tieLine.getMeteredBusNumber(), 1, 4, false) +
            FILLER2 +
            toString(tieLine.getMeteredAreaNumber(), 7, 8, true) +
            FILLER2 +
            toString(tieLine.getNonMeteredBusNumber(), 11, 14, true) +
            FILLER2 +
            toString(tieLine.getNonMeteredAreaNumber(), 17, 18, true) +
            FILLER2 +
            toString(tieLine.getCircuitNumber(), 21);

    }
}
