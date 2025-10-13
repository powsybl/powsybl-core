/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.writer;

import com.powsybl.ieeecdf.model.IeeeCdfTieLine;
import com.powsybl.ieeecdf.model.schema.IeeeCdfTieLineSchema;
import org.jsapar.compose.fixed.FixedWidthComposer;
import org.jsapar.model.IntegerCell;
import org.jsapar.model.Line;

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
        FixedWidthComposer composer = new FixedWidthComposer(writer, IeeeCdfTieLineSchema.build());
        for (IeeeCdfTieLine bean : tieLineList) {
            composer.composeLine(convertToLine(bean));
            composer.composeLineSeparator();
        }
        writeFooter(writer, -999);
    }

    private static Line convertToLine(IeeeCdfTieLine title) {
        Line line = new Line("tieLine");
        line.addCell(new IntegerCell("meteredBusNumber", title.getMeteredBusNumber()));
        line.addCell(new IntegerCell("meteredAreaNumber", title.getMeteredAreaNumber()));
        line.addCell(new IntegerCell("nonMeteredBusNumber", title.getNonMeteredBusNumber()));
        line.addCell(new IntegerCell("nonMeteredAreaNumber", title.getNonMeteredAreaNumber()));
        line.addCell(new IntegerCell("circuitNumber", title.getCircuitNumber()));
        return line;
    }
}
