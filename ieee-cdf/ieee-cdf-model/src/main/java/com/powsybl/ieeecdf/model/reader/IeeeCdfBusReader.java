/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.reader;

import com.powsybl.ieeecdf.model.elements.IeeeCdfBus;
import com.powsybl.ieeecdf.model.conversion.BusTypeConversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfBusReader extends AbstractIeeeCdfReader {

    private IeeeCdfBusReader() {
        // private constructor to prevent instantiation
        super();
    }

    public static List<IeeeCdfBus> parseBuses(BufferedReader reader, int expectedItemsNumber) throws IOException {
        return readLines(reader, -999, IeeeCdfBusReader::parseBus, expectedItemsNumber);
    }

    private static IeeeCdfBus parseBus(String line) {
        IeeeCdfBus bus = new IeeeCdfBus();
        readInteger(line, 1, 4, bus::setNumber);
        readString(line, 6, 17, bus::setName);
        readInteger(line, 19, 20, bus::setAreaNumber);
        readInteger(line, 21, 23, bus::setLossZoneNumber);
        readString(line, 25, 26, type -> bus.setType(BusTypeConversion.fromString(type)));
        readDouble(line, 28, 33, bus::setFinalVoltage);
        readDouble(line, 34, 40, bus::setFinalAngle);
        readDouble(line, 41, 49, bus::setActiveLoad);
        readDouble(line, 50, 58, bus::setReactiveLoad);
        readDouble(line, 59, 67, bus::setActiveGeneration);
        readDouble(line, 68, 75, bus::setReactiveGeneration);
        readDouble(line, 77, 83, bus::setBaseVoltage);
        readDouble(line, 85, 90, bus::setDesiredVoltage);
        readDouble(line, 91, 98, bus::setMaxReactivePowerOrVoltageLimit);
        readDouble(line, 99, 106, bus::setMinReactivePowerOrVoltageLimit);
        readDouble(line, 107, 114, bus::setShuntConductance);
        readDouble(line, 115, 122, bus::setShuntSusceptance);
        readInteger(line, 124, 127, bus::setRemoteControlledBusNumber);
        readInteger(line, 128, 132, bus::setUnused);
        return bus;
    }
}
