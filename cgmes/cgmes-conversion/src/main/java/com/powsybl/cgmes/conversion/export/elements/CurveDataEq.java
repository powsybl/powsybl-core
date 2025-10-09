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
public final class CurveDataEq {

    public static void write(String id, double p, double minQ, double maxQ, String reactiveLimitsId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId("CurveData", id, false, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, "CurveData.xvalue");
        writer.writeCharacters(CgmesExportUtil.format(p));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "CurveData.y1value");
        writer.writeCharacters(CgmesExportUtil.format(minQ));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "CurveData.y2value");
        writer.writeCharacters(CgmesExportUtil.format(maxQ));
        writer.writeEndElement();
        CgmesExportUtil.writeReference("CurveData.Curve", reactiveLimitsId, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    private CurveDataEq() {
    }
}
