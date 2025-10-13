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
public final class IeeeCdfLossZoneSchema extends AbstractCdfSchema {

    private IeeeCdfLossZoneSchema() {
        // private constructor to prevent instantiation
    }

    public static FixedWidthSchema build() {
        FixedWidthSchemaLine line = FixedWidthSchemaLine.builder("lossZone").build();

        line.addSchemaCell(FixedWidthSchemaCell.builder("number", 3)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 1–3
        line.addSchemaCell(filler(1, 1)); // 4
        line.addSchemaCell(FixedWidthSchemaCell.builder("name", 12)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 5-16

        return FixedWidthSchema.builder().withLine(line).build();
    }
}
