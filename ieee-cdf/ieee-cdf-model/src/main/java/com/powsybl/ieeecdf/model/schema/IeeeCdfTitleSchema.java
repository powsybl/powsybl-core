/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.schema;

import org.jsapar.schema.FixedWidthSchema;
import org.jsapar.schema.FixedWidthSchemaCell;
import org.jsapar.schema.FixedWidthSchemaLine;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfTitleSchema extends AbstractCdfSchema {

    private IeeeCdfTitleSchema() {
        // private constructor to prevent instantiation
    }

    public static FixedWidthSchema build() {
        FixedWidthSchemaLine line = FixedWidthSchemaLine.builder("title").build();

        int fillerNumber = 1;
        line.addSchemaCell(filler(1, fillerNumber++)); // 1
        line.addSchemaCell(FixedWidthSchemaCell.builder("date", 8)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 2-9
        line.addSchemaCell(filler(1, fillerNumber++)); // 10
        line.addSchemaCell(FixedWidthSchemaCell.builder("originatorName", 20)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT)
            .withTrimPadCharacter(false).build()); // 11-30
        line.addSchemaCell(filler(1, fillerNumber++)); // 31
        line.addSchemaCell(FixedWidthSchemaCell.builder("mvaBase", 6)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 32-37
        line.addSchemaCell(filler(1, fillerNumber++)); // 38
        line.addSchemaCell(FixedWidthSchemaCell.builder("year", 4)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 39-42
        line.addSchemaCell(filler(1, fillerNumber++)); // 43
        line.addSchemaCell(FixedWidthSchemaCell.builder("season", 1)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 44
        line.addSchemaCell(filler(1, fillerNumber)); // 45
        line.addSchemaCell(FixedWidthSchemaCell.builder("caseIdentification", 28)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 46-73

        return FixedWidthSchema.builder().withLine(line).build();
    }
}
