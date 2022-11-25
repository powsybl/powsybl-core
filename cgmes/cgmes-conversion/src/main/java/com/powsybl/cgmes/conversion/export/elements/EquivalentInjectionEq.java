/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class EquivalentInjectionEq {

    private static final String EQ_EQUIVALENTINJECTION_REGULATIONCAPABILITY = "EquivalentInjection.regulationCapability";
    private static final String EQ_EQUIVALENTINJECTION_REGULATIONSTATUS = "EquivalentInjection.regulationStatus";
    private static final String EQ_EQUIVALENTINJECTION_MINP = "EquivalentInjection.minP";
    private static final String EQ_EQUIVALENTINJECTION_MAXP = "EquivalentInjection.maxP";
    private static final String EQ_EQUIVALENTINJECTION_MINQ = "EquivalentInjection.minQ";
    private static final String EQ_EQUIVALENTINJECTION_MAXQ = "EquivalentInjection.maxQ";

    public static void write(String id, String name, boolean regulationCapability, boolean regulationStatus, double minP, double maxP, double minQ, double maxQ, String baseVoltageId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("EquivalentInjection", id, name, cimNamespace, writer);
        writer.writeStartElement(cimNamespace, EQ_EQUIVALENTINJECTION_REGULATIONCAPABILITY);
        writer.writeCharacters(CgmesExportUtil.format(regulationCapability));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_EQUIVALENTINJECTION_REGULATIONSTATUS);
        writer.writeCharacters(CgmesExportUtil.format(regulationStatus));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_EQUIVALENTINJECTION_MINP);
        writer.writeCharacters(CgmesExportUtil.format(minP));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_EQUIVALENTINJECTION_MAXP);
        writer.writeCharacters(CgmesExportUtil.format(maxP));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_EQUIVALENTINJECTION_MINQ);
        writer.writeCharacters(CgmesExportUtil.format(minQ));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_EQUIVALENTINJECTION_MAXQ);
        writer.writeCharacters(CgmesExportUtil.format(maxQ));
        writer.writeEndElement();
        CgmesExportUtil.writeReference("ConductingEquipment.BaseVoltage", baseVoltageId, cimNamespace, writer);
        writer.writeEndElement();
    }

    private EquivalentInjectionEq() {
    }
}
