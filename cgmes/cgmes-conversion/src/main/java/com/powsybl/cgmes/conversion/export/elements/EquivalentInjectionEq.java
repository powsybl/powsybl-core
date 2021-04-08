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
public final class EquivalentInjectionEq {

    private static final String EQ_EQUIVALENTINJECTION_REGULATIONCAPABILITY = "EquivalentInjection.regulationCapability";
    private static final String EQ_EQUIVALENTINJECTION_REGULATIONSTATUS = "EquivalentInjection.regulationStatus";

    public static void write(String id, String name, boolean regulationCapability, boolean regulationStatus, String baseVoltageId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "EquivalentInjection");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(name);
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_EQUIVALENTINJECTION_REGULATIONCAPABILITY);
        writer.writeCharacters(CgmesExportUtil.format(regulationCapability));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_EQUIVALENTINJECTION_REGULATIONSTATUS);
        writer.writeCharacters(CgmesExportUtil.format(regulationStatus));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "ConductingEquipment.BaseVoltage");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + baseVoltageId);
        writer.writeEndElement();
    }

    private EquivalentInjectionEq() {
    }
}
