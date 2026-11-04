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
import com.powsybl.cgmes.model.CgmesNames;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class CgmesACLineSegment {

    private static final String AC_LINE_SEGMENT_R = AC_LINE_SEGMENT + "." + CgmesNames.R;
    private static final String AC_LINE_SEGMENT_X = AC_LINE_SEGMENT + "." + CgmesNames.X;
    private static final String AC_LINE_SEGMENT_GCH = AC_LINE_SEGMENT + ".gch";
    private static final String AC_LINE_SEGMENT_BCH = AC_LINE_SEGMENT + ".bch";

    private CgmesACLineSegment() {
    }

    public static void writeEq(ACLineSegmentEq line, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(AC_LINE_SEGMENT, line.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(line.name), writer, context);
        CgmesExportUtil.writeAttribute(AC_LINE_SEGMENT_R, CgmesExportUtil.format(line.r), writer, context);
        CgmesExportUtil.writeAttribute(AC_LINE_SEGMENT_X, CgmesExportUtil.format(line.x), writer, context);
        if (line.g != 0.0) {
            CgmesExportUtil.writeAttribute(AC_LINE_SEGMENT_GCH, CgmesExportUtil.format(line.g), writer, context);
        }
        CgmesExportUtil.writeAttribute(AC_LINE_SEGMENT_BCH, CgmesExportUtil.format(line.b), writer, context);
        if (line.baseVoltage != null) {
            CgmesExportUtil.writeResource(CONDUCTING_EQUIPMENT_BASE_VOLTAGE, line.baseVoltage, writer, context);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    public record ACLineSegmentEq(
        String rdfId,
        String name,
        double r,
        double x,
        double g,
        double b,
        String baseVoltage
    ) {
    }

}
