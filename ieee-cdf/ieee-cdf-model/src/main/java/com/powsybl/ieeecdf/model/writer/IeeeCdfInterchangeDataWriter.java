/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.writer;

import com.powsybl.ieeecdf.model.IeeeCdfInterchangeData;
import com.powsybl.ieeecdf.model.schema.IeeeCdfInterchangeDataSchema;
import org.jsapar.compose.fixed.FixedWidthComposer;
import org.jsapar.model.FloatCell;
import org.jsapar.model.IntegerCell;
import org.jsapar.model.Line;
import org.jsapar.model.StringCell;

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
        FixedWidthComposer composer = new FixedWidthComposer(writer, IeeeCdfInterchangeDataSchema.build());
        for (IeeeCdfInterchangeData bean : interchangeDataList) {
            composer.composeLine(convertToLine(bean));
            composer.composeLineSeparator();
        }
        writeFooter(writer, -9);
    }

    private static Line convertToLine(IeeeCdfInterchangeData branch) {
        Line line = new Line("interchangeData");
        line.addCell(new IntegerCell("areaNumber", branch.getAreaNumber()));
        line.addCell(new IntegerCell("interchangeSlackBusNumber", branch.getInterchangeSlackBusNumber()));
        line.addCell(new StringCell("alternateSwingBusName", branch.getAlternateSwingBusName()));
        line.addCell(new FloatCell("areaInterchangeExport", branch.getAreaInterchangeExport()));
        line.addCell(new FloatCell("areaInterchangeTolerance", branch.getAreaInterchangeTolerance()));
        line.addCell(new StringCell("areaCode", branch.getAreaCode()));
        line.addCell(new StringCell("areaName", branch.getAreaName()));
        return line;
    }
}
