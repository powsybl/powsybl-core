/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.reader;

import com.powsybl.commons.PowsyblException;
import com.powsybl.ieeecdf.model.IeeeCdfLossZone;
import com.powsybl.ieeecdf.model.schema.IeeeCdfLossZoneSchema;
import org.jsapar.parse.text.TextParseTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfLossZoneReader extends AbstractIeeeCdfReader {

    private IeeeCdfLossZoneReader() {
        // private constructor to prevent instantiation
        super();
    }

    public static List<IeeeCdfLossZone> parseLossZones(BufferedReader reader) throws IOException {
        StringReader lineReader = readLines(reader, -99);
        TextParseTask task = new TextParseTask(IeeeCdfLossZoneSchema.build(), lineReader);

        List<IeeeCdfLossZone> lossZonesList = new ArrayList<>();
        task.setLineConsumer(line -> {
            IeeeCdfLossZone bus = new IeeeCdfLossZone();
            readInteger(line, "number", bus::setNumber);
            readString(line, "name", bus::setName);
            lossZonesList.add(bus);
        });

        // Manage the exceptions
        task.setErrorConsumer(error -> {
            throw new PowsyblException("Failed to parse the file during IeeeCdfLossZone part", error);
        });

        // Execute the parsing task
        task.execute();
        return lossZonesList;
    }

}
