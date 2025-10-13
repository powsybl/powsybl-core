/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.writer;

import com.powsybl.ieeecdf.model.IeeeCdfTitle;
import com.powsybl.ieeecdf.model.conversion.LocalDateConversion;
import com.powsybl.ieeecdf.model.conversion.SeasonConversion;
import com.powsybl.ieeecdf.model.schema.IeeeCdfTitleSchema;
import org.jsapar.compose.fixed.FixedWidthComposer;
import org.jsapar.model.FloatCell;
import org.jsapar.model.IntegerCell;
import org.jsapar.model.Line;
import org.jsapar.model.StringCell;

import java.io.BufferedWriter;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfTitleWriter extends AbstractIeeeCdfWriter {

    private IeeeCdfTitleWriter() {
        // private constructor to prevent instantiation
    }

    public static void writeTitle(BufferedWriter writer, IeeeCdfTitle title) {
        FixedWidthComposer composer = new FixedWidthComposer(writer, IeeeCdfTitleSchema.build());
        composer.composeLine(convertToLine(title));
        composer.composeLineSeparator();
    }

    private static Line convertToLine(IeeeCdfTitle title) {
        Line line = new Line("title");
        line.addCell(new StringCell("date", LocalDateConversion.revert(title.getDate())));
        line.addCell(new StringCell("originatorName", title.getOriginatorName()));
        line.addCell(new FloatCell("mvaBase", title.getMvaBase()));
        line.addCell(new IntegerCell("year", title.getYear()));
        line.addCell(new StringCell("season", SeasonConversion.revert(title.getSeason())));
        line.addCell(new StringCell("caseIdentification", title.getCaseIdentification()));
        return line;
    }
}
