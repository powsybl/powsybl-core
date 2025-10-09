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
public final class DCTerminalEq {

    public static void write(String element, String id, String name, String dcConductingEquipmentId, String dcNodeId, int sequenceNumber, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName(element, id, name, cimNamespace, writer, context);
        CgmesExportUtil.writeReference(element + ".DCConductingEquipment", dcConductingEquipmentId, cimNamespace, writer, context);
        CgmesExportUtil.writeReference("DCBaseTerminal.DCNode", dcNodeId, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, "ACDCTerminal.sequenceNumber");
        writer.writeCharacters(CgmesExportUtil.format(sequenceNumber));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private DCTerminalEq() {
    }
}
