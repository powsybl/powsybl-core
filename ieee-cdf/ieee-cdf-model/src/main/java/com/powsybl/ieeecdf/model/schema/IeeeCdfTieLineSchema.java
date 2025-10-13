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
public final class IeeeCdfTieLineSchema extends AbstractCdfSchema {

    private IeeeCdfTieLineSchema() {
        // private constructor to prevent instantiation
    }

    public static FixedWidthSchema build() {
        FixedWidthSchemaLine line = FixedWidthSchemaLine.builder("tieLine").build();

        int fillerNumber = 1;
        line.addSchemaCell(FixedWidthSchemaCell.builder("meteredBusNumber", 4)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 1–4
        line.addSchemaCell(filler(2, fillerNumber++)); // 5–6
        line.addSchemaCell(FixedWidthSchemaCell.builder("meteredAreaNumber", 2)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 7–8
        line.addSchemaCell(filler(2, fillerNumber++)); // 9-10
        line.addSchemaCell(FixedWidthSchemaCell.builder("nonMeteredBusNumber", 4)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 11-14
        line.addSchemaCell(filler(2, fillerNumber++)); // 15-16
        line.addSchemaCell(FixedWidthSchemaCell.builder("nonMeteredAreaNumber", 2)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 17-18
        line.addSchemaCell(filler(2, fillerNumber)); // 19-20
        line.addSchemaCell(FixedWidthSchemaCell.builder("circuitNumber", 1)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 21

        return FixedWidthSchema.builder().withLine(line).build();
    }
}
