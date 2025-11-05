/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.writer;

import com.powsybl.ieeecdf.model.IeeeCdfBus;
import com.powsybl.ieeecdf.model.conversion.BusTypeConversion;
import com.powsybl.ieeecdf.model.schema.IeeeCdfBusSchema;
import org.jsapar.compose.fixed.FixedWidthComposer;
import org.jsapar.model.FloatCell;
import org.jsapar.model.IntegerCell;
import org.jsapar.model.Line;
import org.jsapar.model.StringCell;

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
        FixedWidthComposer composer = new FixedWidthComposer(writer, IeeeCdfBusSchema.build());
        for (IeeeCdfBus bean : busList) {
            composer.composeLine(convertToLine(bean));
            composer.composeLineSeparator();
        }
        writeFooter(writer, -999);
    }

    private static Line convertToLine(IeeeCdfBus bus) {
        Line line = new Line("bus");
        line.addCell(new IntegerCell("number", bus.getNumber()));
        line.addCell(new StringCell("name", bus.getName()));
        line.addCell(new IntegerCell("areaNumber", bus.getAreaNumber()));
        line.addCell(new IntegerCell("lossZoneNumber", bus.getLossZoneNumber()));
        line.addCell(new StringCell("type", BusTypeConversion.revert(bus.getType())));
        line.addCell(new FloatCell("finalVoltage", bus.getFinalVoltage()));
        line.addCell(new FloatCell("finalAngle", bus.getFinalAngle()));
        line.addCell(new FloatCell("activeLoad", bus.getActiveLoad()));
        line.addCell(new FloatCell("reactiveLoad", bus.getReactiveLoad()));
        line.addCell(new FloatCell("activeGeneration", bus.getActiveGeneration()));
        line.addCell(new FloatCell("reactiveGeneration", bus.getReactiveGeneration()));
        line.addCell(new FloatCell("baseVoltage", bus.getBaseVoltage()));
        line.addCell(new FloatCell("desiredVoltage", bus.getDesiredVoltage()));
        line.addCell(new FloatCell("maxReactivePowerOrVoltageLimit", bus.getMaxReactivePowerOrVoltageLimit()));
        line.addCell(new FloatCell("minReactivePowerOrVoltageLimit", bus.getMinReactivePowerOrVoltageLimit()));
        line.addCell(new FloatCell("shuntConductance", bus.getShuntConductance()));
        line.addCell(new FloatCell("shuntSusceptance", bus.getShuntSusceptance()));
        line.addCell(new IntegerCell("remoteControlledBusNumber", bus.getRemoteControlledBusNumber()));
        if (bus.getUnused() != null) {
            line.addCell(new IntegerCell("unused", bus.getUnused()));
        }
        return line;
    }
}
