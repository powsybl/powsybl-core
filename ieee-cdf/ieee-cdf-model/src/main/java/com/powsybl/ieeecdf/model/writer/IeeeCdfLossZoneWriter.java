/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.writer;

import com.powsybl.ieeecdf.model.IeeeCdfLossZone;
import com.powsybl.ieeecdf.model.schema.IeeeCdfLossZoneSchema;
import org.jsapar.compose.fixed.FixedWidthComposer;
import org.jsapar.model.IntegerCell;
import org.jsapar.model.Line;
import org.jsapar.model.StringCell;

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
        FixedWidthComposer composer = new FixedWidthComposer(writer, IeeeCdfLossZoneSchema.build());
        for (IeeeCdfLossZone bean : lossZoneList) {
            composer.composeLine(convertToLine(bean));
            composer.composeLineSeparator();
        }
        writeFooter(writer, -99);
    }

    private static Line convertToLine(IeeeCdfLossZone lossZone) {
        Line line = new Line("lossZone");
        line.addCell(new IntegerCell("number", lossZone.getNumber()));
        line.addCell(new StringCell("name", lossZone.getName()));
        return line;
    }
}
