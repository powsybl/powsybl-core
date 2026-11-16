/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.writer;

import com.powsybl.ieeecdf.model.elements.IeeeCdfInterchangeData;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfInterchangeDataWriter extends AbstractIeeeCdfWriter {

    private IeeeCdfInterchangeDataWriter() {
        // private constructor to prevent instantiation
    }

    public static void writeInterchangeData(BufferedWriter writer, List<IeeeCdfInterchangeData> interchangeDataList) throws IOException {
        writeHeader(writer, "INTERCHANGE DATA FOLLOWS                 %d ITEMS", interchangeDataList);
        for (IeeeCdfInterchangeData bean : interchangeDataList) {
            writer.write(convertInterchangeDataToLine(bean));
            writer.newLine();
        }
        writeFooter(writer, -9);
    }

    private static String convertInterchangeDataToLine(IeeeCdfInterchangeData interchangeData) {
        return toString(interchangeData.getAreaNumber(), 1, 2, false) +
            FILLER +
            toString(interchangeData.getInterchangeSlackBusNumber(), 4, 7, true) +
            FILLER +
            toString(interchangeData.getAlternateSwingBusName(), 9, 20, true) +
            toString(interchangeData.getAreaInterchangeExport(), 21, 28, true) +
            toString(interchangeData.getAreaInterchangeTolerance(), 29, 35, true) +
            FILLER2 +
            toString(interchangeData.getAreaCode(), 38, 43, true) +
            FILLER2 +
            toString(interchangeData.getAreaName(), 46, 75, true);
    }
}
