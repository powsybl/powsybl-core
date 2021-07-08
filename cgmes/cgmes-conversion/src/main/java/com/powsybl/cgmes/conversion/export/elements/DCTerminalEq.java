/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.model.CgmesNames;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class DCTerminalEq {

    public static void write(String id, String dcConductingEquipmentId, String dcNodeId, int sequenceNumber, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "DCTerminal");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeEmptyElement(cimNamespace, "DCTerminal.DCConductingEquipment");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + dcConductingEquipmentId);
        writer.writeEmptyElement(cimNamespace, "DCBaseTerminal.DCNode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + dcNodeId);
        writer.writeStartElement(cimNamespace, "ACDCTerminal.sequenceNumber");
        writer.writeCharacters(CgmesExportUtil.format(sequenceNumber));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private DCTerminalEq() {
    }
}
