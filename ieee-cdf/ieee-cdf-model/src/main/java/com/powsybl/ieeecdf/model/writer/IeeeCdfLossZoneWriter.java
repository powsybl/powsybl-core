/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.writer;

import com.powsybl.ieeecdf.model.elements.IeeeCdfLossZone;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfLossZoneWriter extends AbstractIeeeCdfWriter {

    private IeeeCdfLossZoneWriter() {
        // private constructor to prevent instantiation
    }

    public static void writeLossZone(BufferedWriter writer, List<IeeeCdfLossZone> lossZoneList) throws IOException {
        writeHeader(writer, "LOSS ZONES FOLLOWS                     %d ITEMS", lossZoneList);
        for (IeeeCdfLossZone bean : lossZoneList) {
            writer.write(convertLossZoneToLine(bean));
            writer.newLine();
        }
        writeFooter(writer, -99);
    }

    private static String convertLossZoneToLine(IeeeCdfLossZone lossZone) {
        return toString(lossZone.getNumber(), 1, 3, false) +
            FILLER +
            toString(lossZone.getName(), 5, 16, true);
    }
}
