/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
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

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class SeriesCompensatorEq {

    private static final String EQ_SERIES_COMPENSATOR_R = "SeriesCompensator.r";
    private static final String EQ_SERIES_COMPENSATOR_X = "SeriesCompensator.x";
    private static final String EQ_SERIES_COMPENSATOR_VARISTOR_PRESENT = "SeriesCompensator.varistorPresent";
    private static final String EQ_SERIES_COMPENSATOR_VARISTOR_RATED_CURRENT = "SeriesCompensator.varistorRatedCurrent";
    private static final String EQ_SERIES_COMPENSATOR_VARISTOR_VOLTAGE_THRESHOLD = "SeriesCompensator.varistorVoltageThreshold";

    public static void write(String id, String seriesCompensatorName, String baseVoltage, double r, double x, boolean varistorPresent,
                             double varistorRatedCurrent, double varistorVoltageThreshold, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("SeriesCompensator", id, seriesCompensatorName, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, EQ_SERIES_COMPENSATOR_R);
        writer.writeCharacters(CgmesExportUtil.format(r));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_SERIES_COMPENSATOR_X);
        writer.writeCharacters(CgmesExportUtil.format(x));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_SERIES_COMPENSATOR_VARISTOR_PRESENT);
        writer.writeCharacters(CgmesExportUtil.format(varistorPresent));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_SERIES_COMPENSATOR_VARISTOR_RATED_CURRENT);
        writer.writeCharacters(CgmesExportUtil.format(varistorRatedCurrent));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_SERIES_COMPENSATOR_VARISTOR_VOLTAGE_THRESHOLD);
        writer.writeCharacters(CgmesExportUtil.format(varistorVoltageThreshold));
        writer.writeEndElement();
        if (baseVoltage != null) {
            CgmesExportUtil.writeReference("ConductingEquipment.BaseVoltage", baseVoltage, cimNamespace, writer, context);
        }
        writer.writeEndElement();
    }

    private SeriesCompensatorEq() {
    }
}
