/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.reader;

import com.powsybl.ieeecdf.model.elements.IeeeCdfTieLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfTieLineReader extends AbstractIeeeCdfReader {

    private IeeeCdfTieLineReader() {
        // private constructor to prevent instantiation
        super();
    }

    public static List<IeeeCdfTieLine> parseTieLine(BufferedReader reader, int expectedItemsNumber) throws IOException {
        return readLines(reader, -999, IeeeCdfTieLineReader::parseTieLine, expectedItemsNumber);
    }

    private static IeeeCdfTieLine parseTieLine(String line) {
        IeeeCdfTieLine tieLine = new IeeeCdfTieLine();
        readInteger(line, 1, 4, tieLine::setMeteredBusNumber);
        readInteger(line, 7, 8, tieLine::setMeteredAreaNumber);
        readInteger(line, 11, 14, tieLine::setNonMeteredBusNumber);
        readInteger(line, 17, 18, tieLine::setNonMeteredAreaNumber);
        readInteger(line, 21, tieLine::setCircuitNumber);
        return tieLine;
    }
}
