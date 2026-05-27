/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.writer;

import com.powsybl.ieeecdf.model.conversion.BranchSideConversion;
import com.powsybl.ieeecdf.model.conversion.BranchTypeConversion;
import com.powsybl.ieeecdf.model.elements.IeeeCdfBranch;

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
        for (IeeeCdfBranch bean : branchList) {
            writer.write(convertBranchToLine(bean));
            writer.newLine();
        }
        writeFooter(writer, -999);
    }

    private static String convertBranchToLine(IeeeCdfBranch branch) {
        return toString(branch.getTapBusNumber(), 1, 4, false) +
            FILLER +
            toString(branch.getzBusNumber(), 6, 9, false) +
            FILLER +
            toString(branch.getArea(), 11, 12, false) +
            FILLER +
            toString(branch.getLossZone(), 14, 15, false) +
            FILLER +
            toString(branch.getCircuit(), 17) +
            FILLER +
            toString(BranchTypeConversion.revert(branch.getType()), 19) +
            toString(branch.getResistance(), 20, 29, true) +
            toString(branch.getReactance(), 30, 39, true) +
            FILLER +
            toString(branch.getChargingSusceptance(), 41, 49, true) +
            FILLER +
            toString(branch.getRating1(), 51, 55, false) +
            FILLER +
            toString(branch.getRating2(), 57, 61, false) +
            FILLER +
            toString(branch.getRating3(), 63, 67, false) +
            FILLER +
            toString(branch.getControlBusNumber(), 69, 72, false) +
            FILLER +
            toString(BranchSideConversion.revert(branch.getSide()), 74) +
            FILLER2 +
            toString(branch.getFinalTurnsRatio(), 77, 82, true) +
            FILLER +
            toString(branch.getFinalAngle(), 84, 90, true) +
            toString(branch.getMinTapOrPhaseShift(), 91, 97, true) +
            toString(branch.getMaxTapOrPhaseShift(), 98, 104, true) +
            toString(branch.getStepSize(), 105, 111, true) +
            FILLER +
            toString(branch.getMinVoltageActiveOrReactivePowerLimit(), 113, 119, true) +
            toString(branch.getMaxVoltageActiveOrReactivePowerLimit(), 120, 126, true) +
            FILLER2 +
            toString(branch.getSequenceNumber(), 129, 132, false);
    }
}
