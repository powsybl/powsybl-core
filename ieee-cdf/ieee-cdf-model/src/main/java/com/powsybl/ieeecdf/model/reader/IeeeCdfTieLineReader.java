/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.reader;

import com.powsybl.commons.PowsyblException;
import com.powsybl.ieeecdf.model.IeeeCdfTieLine;
import com.powsybl.ieeecdf.model.schema.IeeeCdfTieLineSchema;
import org.jsapar.parse.text.TextParseTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfTieLineReader extends AbstractIeeeCdfReader {

    private IeeeCdfTieLineReader() {
        // private constructor to prevent instantiation
        super();
    }

    public static List<IeeeCdfTieLine> parseTieLine(BufferedReader reader) throws IOException {
        StringReader lineReader = readLines(reader, -999);
        TextParseTask task = new TextParseTask(IeeeCdfTieLineSchema.build(), lineReader);

        List<IeeeCdfTieLine> tieLineList = new ArrayList<>();
        task.setLineConsumer(line -> {
            IeeeCdfTieLine tieLine = new IeeeCdfTieLine();
            readInteger(line, "meteredBusNumber", tieLine::setMeteredBusNumber);
            readInteger(line, "meteredAreaNumber", tieLine::setMeteredAreaNumber);
            readInteger(line, "nonMeteredBusNumber", tieLine::setNonMeteredBusNumber);
            readInteger(line, "nonMeteredAreaNumber", tieLine::setNonMeteredAreaNumber);
            readInteger(line, "circuitNumber", tieLine::setCircuitNumber);
            tieLineList.add(tieLine);
        });

        // Manage the exceptions
        task.setErrorConsumer(error -> {
            throw new PowsyblException("Failed to parse the file during IeeeCdfTieLine part", error);
        });

        // Execute the parsing task
        task.execute();
        return tieLineList;
    }
}
