/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.writer;

import com.powsybl.ieeecdf.model.conversion.BusTypeConversion;
import com.powsybl.ieeecdf.model.elements.IeeeCdfBus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IeeeCdfBusWriter extends AbstractIeeeCdfWriter {

    private IeeeCdfBusWriter() {
        // private constructor to prevent instantiation
    }

    public static void writeBuses(BufferedWriter writer, List<IeeeCdfBus> busList) throws IOException {
        writeHeader(writer, "BUS DATA FOLLOWS                            %d ITEMS", busList);
        for (IeeeCdfBus bean : busList) {
            writer.write(convertBusToLins(bean));
            writer.newLine();
        }
        writeFooter(writer, -999);
    }

    private static String convertBusToLins(IeeeCdfBus bus) {
        return toString(bus.getNumber(), 1, 4, false) +
            FILLER +
            toString(bus.getName(), 6, 17, true) +
            FILLER +
            toString(bus.getAreaNumber(), 19, 20, true) +
            toString(bus.getLossZoneNumber(), 21, 23, true) +
            FILLER +
            toString(BusTypeConversion.revert(bus.getType()), 25, 26, true) +
            FILLER +
            toString(bus.getFinalVoltage(), 28, 33, true) +
            toString(bus.getFinalAngle(), 34, 40, true) +
            toString(bus.getActiveLoad(), 41, 49, true) +
            toString(bus.getReactiveLoad(), 50, 58, true) +
            toString(bus.getActiveGeneration(), 59, 67, true) +
            toString(bus.getReactiveGeneration(), 68, 75, true) +
            FILLER +
            toString(bus.getBaseVoltage(), 77, 83, true) +
            FILLER +
            toString(bus.getDesiredVoltage(), 85, 90, true) +
            toString(bus.getMaxReactivePowerOrVoltageLimit(), 91, 98, true) +
            toString(bus.getMinReactivePowerOrVoltageLimit(), 99, 106, true) +
            toString(bus.getShuntConductance(), 107, 114, true) +
            toString(bus.getShuntSusceptance(), 115, 122, true) +
            FILLER +
            toString(bus.getRemoteControlledBusNumber(), 124, 127, true) +
            (bus.getUnused() != null ? toString(bus.getUnused(), 128, 132, true) : "");
    }
}
