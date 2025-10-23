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
public final class IeeeCdfBusSchema extends AbstractCdfSchema {

    private IeeeCdfBusSchema() {
        // private constructor to prevent instantiation
    }

    public static FixedWidthSchema build() {
        FixedWidthSchemaLine line = FixedWidthSchemaLine.builder("bus").build();

        int fillerNumber = 1;
        line.addSchemaCell(FixedWidthSchemaCell.builder("number", 4)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 1–4
        line.addSchemaCell(filler(1, fillerNumber++)); // 5
        line.addSchemaCell(FixedWidthSchemaCell.builder("name", 12)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 6–17
        line.addSchemaCell(filler(1, fillerNumber++)); // 18
        line.addSchemaCell(FixedWidthSchemaCell.builder("areaNumber", 2)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 19–20
        line.addSchemaCell(FixedWidthSchemaCell.builder("lossZoneNumber", 3)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 21–23
        line.addSchemaCell(filler(1, fillerNumber++)); // 24
        line.addSchemaCell(FixedWidthSchemaCell.builder("type", 2)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 25–26
        line.addSchemaCell(filler(1, fillerNumber++)); // 27
        line.addSchemaCell(FixedWidthSchemaCell.builder("finalVoltage", 6)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 28–33
        line.addSchemaCell(FixedWidthSchemaCell.builder("finalAngle", 7)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 34–40
        line.addSchemaCell(FixedWidthSchemaCell.builder("activeLoad", 9)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 41–49
        line.addSchemaCell(FixedWidthSchemaCell.builder("reactiveLoad", 9)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 50–58
        line.addSchemaCell(FixedWidthSchemaCell.builder("activeGeneration", 9)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 59–67
        line.addSchemaCell(FixedWidthSchemaCell.builder("reactiveGeneration", 8)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 68–75
        line.addSchemaCell(filler(1, fillerNumber++)); // 76
        line.addSchemaCell(FixedWidthSchemaCell.builder("baseVoltage", 7)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 77–83
        line.addSchemaCell(filler(1, fillerNumber++)); // 84
        line.addSchemaCell(FixedWidthSchemaCell.builder("desiredVoltage", 6)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 85–90
        line.addSchemaCell(FixedWidthSchemaCell.builder("maxReactivePowerOrVoltageLimit", 8)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 91–98
        line.addSchemaCell(FixedWidthSchemaCell.builder("minReactivePowerOrVoltageLimit", 8)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 99–106
        line.addSchemaCell(FixedWidthSchemaCell.builder("shuntConductance", 8)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 107–114
        line.addSchemaCell(FixedWidthSchemaCell.builder("shuntSusceptance", 8)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 115–122
        line.addSchemaCell(filler(1, fillerNumber)); // 123
        line.addSchemaCell(FixedWidthSchemaCell.builder("remoteControlledBusNumber", 4)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 124–127
        line.addSchemaCell(FixedWidthSchemaCell.builder("unused", 6)
            .build()); // 128–133

        return FixedWidthSchema.builder().withLine(line).build();
    }
}
