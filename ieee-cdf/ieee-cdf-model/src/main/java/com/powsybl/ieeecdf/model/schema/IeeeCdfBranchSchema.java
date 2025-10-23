/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

/*
 * CopyLEFT (c) 2025, RTE (http://www.rte-france.com)
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
public final class IeeeCdfBranchSchema extends AbstractCdfSchema {

    private IeeeCdfBranchSchema() {
        // private constructor to prevent instantiation
        super();
    }

    public static FixedWidthSchema build() {
        FixedWidthSchemaLine line = FixedWidthSchemaLine.builder("branch").build();

        int fillerNumber = 1;
        line.addSchemaCell(FixedWidthSchemaCell.builder("tapBusNumber", 4)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 1–4
        line.addSchemaCell(filler(1, fillerNumber++)); // 5
        line.addSchemaCell(FixedWidthSchemaCell.builder("zBusNumber", 4)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 6–9
        line.addSchemaCell(filler(1, fillerNumber++)); // 10
        line.addSchemaCell(FixedWidthSchemaCell.builder("area", 2)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 11–12
        line.addSchemaCell(filler(1, fillerNumber++)); // 13
        line.addSchemaCell(FixedWidthSchemaCell.builder("lossZone", 2)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 14–15
        line.addSchemaCell(filler(1, fillerNumber++)); // 16
        line.addSchemaCell(FixedWidthSchemaCell.builder("circuit", 1)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 17
        line.addSchemaCell(filler(1, fillerNumber++)); // 18
        line.addSchemaCell(FixedWidthSchemaCell.builder("type", 1)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 19
        line.addSchemaCell(FixedWidthSchemaCell.builder("resistance", 10)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 20–29
        line.addSchemaCell(FixedWidthSchemaCell.builder("reactance", 10)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 30–39
        line.addSchemaCell(filler(1, fillerNumber++)); // 40
        line.addSchemaCell(FixedWidthSchemaCell.builder("chargingSusceptance", 9)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 41–49
        line.addSchemaCell(filler(1, fillerNumber++)); // 50
        line.addSchemaCell(FixedWidthSchemaCell.builder("rating1", 5)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 51–55
        line.addSchemaCell(filler(1, fillerNumber++)); // 56
        line.addSchemaCell(FixedWidthSchemaCell.builder("rating2", 5)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 57–61
        line.addSchemaCell(filler(1, fillerNumber++)); // 62
        line.addSchemaCell(FixedWidthSchemaCell.builder("rating3", 5)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 63–67
        line.addSchemaCell(filler(1, fillerNumber++)); // 68
        line.addSchemaCell(FixedWidthSchemaCell.builder("controlBusNumber", 4)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 69–72
        line.addSchemaCell(filler(1, fillerNumber++)); // 73
        line.addSchemaCell(FixedWidthSchemaCell.builder("side", 1)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 74
        line.addSchemaCell(filler(2, fillerNumber++)); // 75-76
        line.addSchemaCell(FixedWidthSchemaCell.builder("finalTurnsRatio", 6)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 77–82
        line.addSchemaCell(filler(1, fillerNumber++)); // 83
        line.addSchemaCell(FixedWidthSchemaCell.builder("finalAngle", 7)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 84–90
        line.addSchemaCell(FixedWidthSchemaCell.builder("minTapOrPhaseShift", 7)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 91–97
        line.addSchemaCell(FixedWidthSchemaCell.builder("maxTapOrPhaseShift", 7)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 98–104
        line.addSchemaCell(FixedWidthSchemaCell.builder("stepSize", 7)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 105–111
        line.addSchemaCell(filler(1, fillerNumber++)); // 112
        line.addSchemaCell(FixedWidthSchemaCell.builder("minVoltageActiveOrReactivePowerLimit", 6)
            .withAlignment(FixedWidthSchemaCell.Alignment.LEFT).build()); // 113-119
        line.addSchemaCell(filler(1, fillerNumber)); // 119
        line.addSchemaCell(FixedWidthSchemaCell.builder("maxVoltageActiveOrReactivePowerLimit", 7)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 120-126
        line.addSchemaCell(FixedWidthSchemaCell.builder("unused", 7)
            .withAlignment(FixedWidthSchemaCell.Alignment.RIGHT).build()); // 127–133

        return FixedWidthSchema.builder().withLine(line).build();
    }
}
