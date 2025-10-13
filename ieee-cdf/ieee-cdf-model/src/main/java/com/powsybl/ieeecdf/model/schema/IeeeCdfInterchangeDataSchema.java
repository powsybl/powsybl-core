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
public final class IeeeCdfInterchangeDataSchema extends AbstractCdfSchema {

    private IeeeCdfInterchangeDataSchema() {
        // private constructor to prevent instantiation
    }

    public static FixedWidthSchema build() {
        FixedWidthSchemaLine line = FixedWidthSchemaLine.builder("interchangeData").build();

        int fillerNumber = 1;
        line.addSchemaCell(FixedWidthSchemaCell.builder("areaNumber", 2)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 1–2
        line.addSchemaCell(filler(1, fillerNumber++)); // 3
        line.addSchemaCell(FixedWidthSchemaCell.builder("interchangeSlackBusNumber", 4)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 4-7
        line.addSchemaCell(filler(1, fillerNumber++)); // 8
        line.addSchemaCell(FixedWidthSchemaCell.builder("alternateSwingBusName", 12)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 9-20
        line.addSchemaCell(FixedWidthSchemaCell.builder("areaInterchangeExport", 8)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 21-28
        line.addSchemaCell(FixedWidthSchemaCell.builder("areaInterchangeTolerance", 7)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 29-35
        line.addSchemaCell(filler(2, fillerNumber++)); // 36-37
        line.addSchemaCell(FixedWidthSchemaCell.builder("areaCode", 6)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 38-43
        line.addSchemaCell(filler(2, fillerNumber)); // 44-45
        line.addSchemaCell(FixedWidthSchemaCell.builder("areaName", 30)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 46-75

        return FixedWidthSchema.builder().withLine(line).build();
    }
}
