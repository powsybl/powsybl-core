/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.writer;

import com.powsybl.ieeecdf.model.IeeeCdfBranch;
import com.powsybl.ieeecdf.model.conversion.BranchSideConversion;
import com.powsybl.ieeecdf.model.conversion.BranchTypeConversion;
import com.powsybl.ieeecdf.model.schema.IeeeCdfBranchSchema;
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
public final class IeeeCdfBranchWriter extends AbstractIeeeCdfWriter {

    private IeeeCdfBranchWriter() {
        // private constructor to prevent instantiation
    }

    public static void writeBranches(BufferedWriter writer, List<IeeeCdfBranch> branchList) throws IOException {
        writeHeader(writer, "BRANCH DATA FOLLOWS                         %d ITEMS", branchList);
        FixedWidthComposer composer = new FixedWidthComposer(writer, IeeeCdfBranchSchema.build());
        for (IeeeCdfBranch bean : branchList) {
            composer.composeLine(convertToLine(bean));
            composer.composeLineSeparator();
        }
        writeFooter(writer, -999);
    }

    private static Line convertToLine(IeeeCdfBranch branch) {
        Line line = new Line("branch");
        line.addCell(new IntegerCell("tapBusNumber", branch.getTapBusNumber()));
        line.addCell(new IntegerCell("zBusNumber", branch.getzBusNumber()));
        line.addCell(new IntegerCell("area", branch.getArea()));
        line.addCell(new IntegerCell("lossZone", branch.getLossZone()));
        line.addCell(new IntegerCell("circuit", branch.getCircuit()));
        line.addCell(new StringCell("type", BranchTypeConversion.revert(branch.getType())));
        line.addCell(new FloatCell("resistance", branch.getResistance()));
        line.addCell(new FloatCell("reactance", branch.getReactance()));
        line.addCell(new FloatCell("chargingSusceptance", branch.getChargingSusceptance()));
        line.addCell(new IntegerCell("rating1", branch.getRating1()));
        line.addCell(new IntegerCell("rating2", branch.getRating2()));
        line.addCell(new IntegerCell("rating3", branch.getRating3()));
        line.addCell(new IntegerCell("controlBusNumber", branch.getControlBusNumber()));
        line.addCell(new StringCell("side", BranchSideConversion.revert(branch.getSide())));
        line.addCell(new FloatCell("finalTurnsRatio", branch.getFinalTurnsRatio()));
        line.addCell(new FloatCell("finalAngle", branch.getFinalAngle()));
        line.addCell(new FloatCell("minTapOrPhaseShift", branch.getMinTapOrPhaseShift()));
        line.addCell(new FloatCell("maxTapOrPhaseShift", branch.getMaxTapOrPhaseShift()));
        line.addCell(new FloatCell("stepSize", branch.getStepSize()));
        line.addCell(new FloatCell("minVoltageActiveOrReactivePowerLimit", branch.getMinVoltageActiveOrReactivePowerLimit()));
        line.addCell(new FloatCell("maxVoltageActiveOrReactivePowerLimit", branch.getMaxVoltageActiveOrReactivePowerLimit()));
        return line;
    }
}
