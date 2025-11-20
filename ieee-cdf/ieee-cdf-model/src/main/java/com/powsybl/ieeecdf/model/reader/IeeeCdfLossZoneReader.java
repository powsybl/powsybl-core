/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.reader;

import com.powsybl.ieeecdf.model.elements.IeeeCdfLossZone;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfLossZoneReader extends AbstractIeeeCdfReader {

    private IeeeCdfLossZoneReader() {
        // private constructor to prevent instantiation
        super();
    }

    public static List<IeeeCdfLossZone> parseLossZones(BufferedReader reader, int expectedItemsNumber) throws IOException {
        return readLines(reader, -99, IeeeCdfLossZoneReader::parseLossZone, expectedItemsNumber);
    }

    private static IeeeCdfLossZone parseLossZone(String line) {
        IeeeCdfLossZone lossZone = new IeeeCdfLossZone();
        readInteger(line, 1, 3, lossZone::setNumber);
        readString(line, 5, 16, lossZone::setName);
        return lossZone;
    }

}
