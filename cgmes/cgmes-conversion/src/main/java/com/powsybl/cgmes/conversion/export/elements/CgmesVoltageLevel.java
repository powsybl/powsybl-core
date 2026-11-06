/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class CgmesVoltageLevel {

    private static final String VOLTAGE_LEVEL_LOW_VOLTAGE_LIMIT = VOLTAGE_LEVEL + "." + LOW_VOLTAGE_LIMIT;
    private static final String VOLTAGE_LEVEL_HIGH_VOLTAGE_LIMIT = VOLTAGE_LEVEL + "." + HIGH_VOLTAGE_LIMIT;
    private static final String VOLTAGE_LEVEL_SUBSTATION = VOLTAGE_LEVEL + "." + SUBSTATION;
    private static final String VOLTAGE_LEVEL_BASE_VOLTAGE = VOLTAGE_LEVEL + "." + BASE_VOLTAGE;

    private CgmesVoltageLevel() {
    }

    public static void writeEq(VoltageLevelEq voltageLevel, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(VOLTAGE_LEVEL, voltageLevel.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(voltageLevel.name), writer, context);
        if (!Double.isNaN(voltageLevel.lowVoltageLimit)) {
            CgmesExportUtil.writeAttribute(VOLTAGE_LEVEL_LOW_VOLTAGE_LIMIT, CgmesExportUtil.format(voltageLevel.lowVoltageLimit), writer, context);
        }
        if (!Double.isNaN(voltageLevel.highVoltageLimit)) {
            CgmesExportUtil.writeAttribute(VOLTAGE_LEVEL_HIGH_VOLTAGE_LIMIT, CgmesExportUtil.format(voltageLevel.highVoltageLimit), writer, context);
        }
        CgmesExportUtil.writeResource(VOLTAGE_LEVEL_SUBSTATION, voltageLevel.substation, writer, context);
        CgmesExportUtil.writeResource(VOLTAGE_LEVEL_BASE_VOLTAGE, voltageLevel.baseVoltage, writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record VoltageLevelEq(
        String rdfId,
        String name,
        double lowVoltageLimit,
        double highVoltageLimit,
        String substation,
        String baseVoltage
    ) {
    }

}
