/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.reader;

import com.powsybl.ieeecdf.model.elements.IeeeCdfBranch;
import com.powsybl.ieeecdf.model.conversion.BranchSideConversion;
import com.powsybl.ieeecdf.model.conversion.BranchTypeConversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfBranchReader extends AbstractIeeeCdfReader {

    private IeeeCdfBranchReader() {
        // private constructor to prevent instantiation
        super();
    }

    public static List<IeeeCdfBranch> parseBranches(BufferedReader reader, int expectedItemsNumber) throws IOException {
        return readLines(reader, -999, IeeeCdfBranchReader::parseBranch, expectedItemsNumber);
    }

    private static IeeeCdfBranch parseBranch(String line) {
        IeeeCdfBranch branch = new IeeeCdfBranch();
        readInteger(line, 1, 4, branch::setTapBusNumber);
        readInteger(line, 6, 9, branch::setzBusNumber);
        readInteger(line, 11, 12, branch::setArea);
        readInteger(line, 14, 15, branch::setLossZone);
        readInteger(line, 17, branch::setCircuit);
        readString(line, 19, type -> branch.setType(BranchTypeConversion.fromString(type)));
        readDouble(line, 20, 29, branch::setResistance);
        readDouble(line, 30, 39, branch::setReactance);
        readDouble(line, 41, 49, branch::setChargingSusceptance);
        readInteger(line, 51, 55, branch::setRating1);
        readInteger(line, 57, 61, branch::setRating2);
        readInteger(line, 63, 67, branch::setRating3);
        readInteger(line, 69, 72, branch::setControlBusNumber);
        readString(line, 74, side -> branch.setSide(BranchSideConversion.fromString(side)));
        readDouble(line, 77, 82, branch::setFinalTurnsRatio);
        readDouble(line, 84, 90, branch::setFinalAngle);
        readDouble(line, 91, 97, branch::setMinTapOrPhaseShift);
        readDouble(line, 98, 104, branch::setMaxTapOrPhaseShift);
        readDouble(line, 105, 111, branch::setStepSize);
        try {
            readDouble(line, 113, 119, branch::setMinVoltageActiveOrReactivePowerLimit);
        } catch (NumberFormatException e) {
            // In some cases, this column is one character shorter than usual
            readDouble(line, 113, 118, branch::setMinVoltageActiveOrReactivePowerLimit);
        }
        readDouble(line, 120, 126, branch::setMaxVoltageActiveOrReactivePowerLimit);
        readInteger(line, 127, 133, branch::setUnused);
        return branch;
    }
}
