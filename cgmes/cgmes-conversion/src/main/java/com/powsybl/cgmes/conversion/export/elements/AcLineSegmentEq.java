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

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class AcLineSegmentEq {

    private static final String EQ_ACLINESEGMENT_R = "ACLineSegment.r";
    private static final String EQ_ACLINESEGMENT_X = "ACLineSegment.x";
    private static final String EQ_ACLINESEGMENT_GCH = "ACLineSegment.gch";
    private static final String EQ_ACLINESEGMENT_BCH = "ACLineSegment.bch";

    public static void write(String id, String lineSegmentName, String baseVoltage, double r, double x, double gch, double bch, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("ACLineSegment", id, lineSegmentName, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, EQ_ACLINESEGMENT_R);
        writer.writeCharacters(CgmesExportUtil.format(r));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_ACLINESEGMENT_X);
        writer.writeCharacters(CgmesExportUtil.format(x));
        writer.writeEndElement();
        if (gch != 0.0) {
            writer.writeStartElement(cimNamespace, EQ_ACLINESEGMENT_GCH);
            writer.writeCharacters(CgmesExportUtil.format(gch));
            writer.writeEndElement();
        }
        writer.writeStartElement(cimNamespace, EQ_ACLINESEGMENT_BCH);
        writer.writeCharacters(CgmesExportUtil.format(bch));
        writer.writeEndElement();
        if (baseVoltage != null) {
            CgmesExportUtil.writeReference("ConductingEquipment.BaseVoltage", baseVoltage, cimNamespace, writer, context);
        }
        writer.writeEndElement();
    }

    private AcLineSegmentEq() {
    }
}
