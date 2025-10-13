/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.reader;

import com.powsybl.commons.PowsyblException;
import com.powsybl.ieeecdf.model.IeeeCdfBranch;
import com.powsybl.ieeecdf.model.conversion.BranchSideConversion;
import com.powsybl.ieeecdf.model.conversion.BranchTypeConversion;
import com.powsybl.ieeecdf.model.schema.IeeeCdfBranchSchema;
import org.jsapar.parse.text.TextParseTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 1         2         3         4         5         6         7         8         9         0         1         2         3
 1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfBranchReader extends AbstractIeeeCdfReader {

    private IeeeCdfBranchReader() {
        // private constructor to prevent instantiation
        super();
    }

    public static List<IeeeCdfBranch> parseBranches(BufferedReader reader) throws IOException {
        StringReader lineReader = readLines(reader, -999);
        TextParseTask task = new TextParseTask(IeeeCdfBranchSchema.build(), lineReader);

        List<IeeeCdfBranch> branchList = new ArrayList<>();
        task.setLineConsumer(line -> {
            IeeeCdfBranch branch = new IeeeCdfBranch();
            readInteger(line, "tapBusNumber", branch::setTapBusNumber);
            readInteger(line, "zBusNumber", branch::setzBusNumber);
            readInteger(line, "area", branch::setArea);
            readInteger(line, "lossZone", branch::setLossZone);
            readInteger(line, "circuit", branch::setCircuit);
            readString(line, "type", type -> branch.setType(BranchTypeConversion.fromString(type)));
            readDouble(line, "resistance", branch::setResistance);
            readDouble(line, "reactance", branch::setReactance);
            readDouble(line, "chargingSusceptance", branch::setChargingSusceptance);
            readInteger(line, "rating1", branch::setRating1);
            readInteger(line, "rating2", branch::setRating2);
            readInteger(line, "rating3", branch::setRating3);
            readInteger(line, "controlBusNumber", branch::setControlBusNumber);
            readString(line, "side", side -> branch.setSide(BranchSideConversion.fromString(side)));
            readDouble(line, "finalTurnsRatio", branch::setFinalTurnsRatio);
            readDouble(line, "finalAngle", branch::setFinalAngle);
            readDouble(line, "minTapOrPhaseShift", branch::setMinTapOrPhaseShift);
            readDouble(line, "maxTapOrPhaseShift", branch::setMaxTapOrPhaseShift);
            readDouble(line, "stepSize", branch::setStepSize);
            readDouble(line, "minVoltageActiveOrReactivePowerLimit", branch::setMinVoltageActiveOrReactivePowerLimit);
            readDouble(line, "maxVoltageActiveOrReactivePowerLimit", branch::setMaxVoltageActiveOrReactivePowerLimit);
            branchList.add(branch);
        });

        // Manage the exceptions
        task.setErrorConsumer(error -> {
            throw new PowsyblException("Failed to parse the file during IeeeCdfBranch part", error);
        });

        // Execute the parsing task
        task.execute();
        return branchList;
    }
}
