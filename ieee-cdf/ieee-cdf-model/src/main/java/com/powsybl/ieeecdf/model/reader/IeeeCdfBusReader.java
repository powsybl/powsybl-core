/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.reader;

import com.powsybl.commons.PowsyblException;
import com.powsybl.ieeecdf.model.IeeeCdfBus;
import com.powsybl.ieeecdf.model.conversion.BusTypeConversion;
import com.powsybl.ieeecdf.model.schema.IeeeCdfBusSchema;
import org.jsapar.parse.text.TextParseTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfBusReader extends AbstractIeeeCdfReader {

    private IeeeCdfBusReader() {
        // private constructor to prevent instantiation
        super();
    }

    public static List<IeeeCdfBus> parseBuses(BufferedReader reader) throws IOException {
        StringReader lineReader = readLines(reader, -999);
        TextParseTask task = new TextParseTask(IeeeCdfBusSchema.build(), lineReader);

        List<IeeeCdfBus> buses = new ArrayList<>();
        task.setLineConsumer(line -> {
            IeeeCdfBus bus = new IeeeCdfBus();
            readInteger(line, "number", bus::setNumber);
            readString(line, "name", bus::setName);
            readInteger(line, "areaNumber", bus::setAreaNumber);
            readInteger(line, "lossZoneNumber", bus::setLossZoneNumber);
            readString(line, "type", type -> bus.setType(BusTypeConversion.fromString(type)));
            readDouble(line, "finalVoltage", bus::setFinalVoltage);
            readDouble(line, "finalAngle", bus::setFinalAngle);
            readDouble(line, "activeLoad", bus::setActiveLoad);
            readDouble(line, "reactiveLoad", bus::setReactiveLoad);
            readDouble(line, "activeGeneration", bus::setActiveGeneration);
            readDouble(line, "reactiveGeneration", bus::setReactiveGeneration);
            readDouble(line, "baseVoltage", bus::setBaseVoltage);
            readDouble(line, "desiredVoltage", bus::setDesiredVoltage);
            readDouble(line, "maxReactivePowerOrVoltageLimit", bus::setMaxReactivePowerOrVoltageLimit);
            readDouble(line, "minReactivePowerOrVoltageLimit", bus::setMinReactivePowerOrVoltageLimit);
            readDouble(line, "shuntConductance", bus::setShuntConductance);
            readDouble(line, "shuntSusceptance", bus::setShuntSusceptance);
            readInteger(line, "remoteControlledBusNumber", bus::setRemoteControlledBusNumber);
            buses.add(bus);
        });

        // Manage the exceptions
        task.setErrorConsumer(error -> {
            throw new PowsyblException("Failed to parse the file during IeeeCdfBus part", error);
        });

        // Execute the parsing task
        task.execute();
        return buses;
    }
}
