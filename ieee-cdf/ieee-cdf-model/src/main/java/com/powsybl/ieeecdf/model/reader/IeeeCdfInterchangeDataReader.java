/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.reader;

import com.powsybl.commons.PowsyblException;
import com.powsybl.ieeecdf.model.IeeeCdfInterchangeData;
import com.powsybl.ieeecdf.model.schema.IeeeCdfInterchangeDataSchema;
import org.jsapar.parse.text.TextParseTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfInterchangeDataReader extends AbstractIeeeCdfReader {

    private IeeeCdfInterchangeDataReader() {
        // private constructor to prevent instantiation
        super();
    }

    public static List<IeeeCdfInterchangeData> parseInterchangeData(BufferedReader reader) throws IOException {
        StringReader lineReader = readLines(reader, -9);
        TextParseTask task = new TextParseTask(IeeeCdfInterchangeDataSchema.build(), lineReader);

        List<IeeeCdfInterchangeData> interchangeDataList = new ArrayList<>();
        task.setLineConsumer(line -> {
            IeeeCdfInterchangeData interchangeData = new IeeeCdfInterchangeData();
            readInteger(line, "areaNumber", interchangeData::setAreaNumber);
            readInteger(line, "interchangeSlackBusNumber", interchangeData::setInterchangeSlackBusNumber);
            readString(line, "alternateSwingBusName", interchangeData::setAlternateSwingBusName);
            readDouble(line, "areaInterchangeExport", interchangeData::setAreaInterchangeExport);
            readDouble(line, "areaInterchangeTolerance", interchangeData::setAreaInterchangeTolerance);
            readString(line, "areaCode", interchangeData::setAreaCode);
            readString(line, "areaName", interchangeData::setAreaName);
            interchangeDataList.add(interchangeData);
        });

        // Manage the exceptions
        task.setErrorConsumer(error -> {
            throw new PowsyblException("Failed to parse the file during IeeeCdfInterchangeData part", error);
        });

        // Execute the parsing task
        task.execute();
        return interchangeDataList;
    }
}
