/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.reader;

import com.powsybl.ieeecdf.model.elements.IeeeCdfInterchangeData;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfInterchangeDataReader extends AbstractIeeeCdfReader {

    private IeeeCdfInterchangeDataReader() {
        // private constructor to prevent instantiation
        super();
    }

    public static List<IeeeCdfInterchangeData> parseInterchangeData(BufferedReader reader, int expectedItemsNumber) throws IOException {
        return readLines(reader, -9, IeeeCdfInterchangeDataReader::parseInterchangeData, expectedItemsNumber);
    }

    private static IeeeCdfInterchangeData parseInterchangeData(String line) {
        IeeeCdfInterchangeData interchangeData = new IeeeCdfInterchangeData();
        readInteger(line, 1, 2, interchangeData::setAreaNumber);
        readInteger(line, 4, 7, interchangeData::setInterchangeSlackBusNumber);
        readString(line, 9, 20, interchangeData::setAlternateSwingBusName);
        readDouble(line, 21, 28, interchangeData::setAreaInterchangeExport);
        readDouble(line, 29, 35, interchangeData::setAreaInterchangeTolerance);
        readString(line, 38, 43, interchangeData::setAreaCode);
        readString(line, 46, 75, interchangeData::setAreaName);
        return interchangeData;
    }
}
