/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.model.CgmesNames;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class DCConverterUnitEq {

    private static final String OPERATION_MODE = "http://iec.ch/TC57/2013/CIM-schema-cim16#DCConverterOperatingModeKind.monopolarGroundReturn";

    public static void write(String id, String dcConverterUnitName, String substationId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "DCConverterUnit");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(dcConverterUnitName);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "DCConverterUnit.operationMode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, OPERATION_MODE);
        writer.writeEmptyElement(cimNamespace, "DCConverterUnit.Substation");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + substationId);
        writer.writeEndElement();
    }

    private DCConverterUnitEq() {
    }
}
