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

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class DCLineSegmentEq {

    public static void write(String id, String lineSegmentName, double resistance, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId("DCLineSegment", id, true, writer, context);
        CgmesExportUtil.writeAttribute(CgmesNames.IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(lineSegmentName), writer, context);
        writer.writeStartElement(cimNamespace, "DCLineSegment.resistance");
        writer.writeCharacters(CgmesExportUtil.format(resistance));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private DCLineSegmentEq() {
    }
}
