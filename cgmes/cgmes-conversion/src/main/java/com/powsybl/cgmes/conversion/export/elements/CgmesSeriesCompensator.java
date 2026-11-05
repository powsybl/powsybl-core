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

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class CgmesSeriesCompensator {

    private static final String SERIES_COMPENSATOR_R = SERIES_COMPENSATOR + "." + R;
    private static final String SERIES_COMPENSATOR_X = SERIES_COMPENSATOR + "." + X;
    private static final String SERIES_COMPENSATOR_VARISTOR_PRESENT = SERIES_COMPENSATOR + ".varistorPresent";
    private static final String SERIES_COMPENSATOR_VARISTOR_RATED_CURRENT = SERIES_COMPENSATOR + ".varistorRatedCurrent";
    private static final String SERIES_COMPENSATOR_VARISTOR_VOLTAGE_THRESHOLD = SERIES_COMPENSATOR + ".varistorVoltageThreshold";

    private CgmesSeriesCompensator() {
    }

    public static void writeEq(SeriesCompensatorEq sc, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(SERIES_COMPENSATOR, sc.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(sc.name), writer, context);
        CgmesExportUtil.writeAttribute(SERIES_COMPENSATOR_R, CgmesExportUtil.format(sc.r), writer, context);
        CgmesExportUtil.writeAttribute(SERIES_COMPENSATOR_X, CgmesExportUtil.format(sc.x), writer, context);
        CgmesExportUtil.writeAttribute(SERIES_COMPENSATOR_VARISTOR_PRESENT, CgmesExportUtil.format(sc.varistorPresent), writer, context);
        CgmesExportUtil.writeAttribute(SERIES_COMPENSATOR_VARISTOR_RATED_CURRENT, CgmesExportUtil.format(sc.varistorRatedCurrent), writer, context);
        CgmesExportUtil.writeAttribute(SERIES_COMPENSATOR_VARISTOR_VOLTAGE_THRESHOLD, CgmesExportUtil.format(sc.varistorVoltageThreshold), writer, context);
        if (sc.baseVoltage != null) {
            CgmesExportUtil.writeResource(CONDUCTING_EQUIPMENT_BASE_VOLTAGE, sc.baseVoltage, writer, context);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    public record SeriesCompensatorEq(
        String rdfId,
        String name,
        double r,
        double x,
        boolean varistorPresent,
        double varistorRatedCurrent,
        double varistorVoltageThreshold,
        String baseVoltage
    ) {
    }

}
