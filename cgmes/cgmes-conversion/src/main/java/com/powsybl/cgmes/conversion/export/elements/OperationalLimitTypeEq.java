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

import static com.powsybl.cgmes.model.CgmesNamespace.*;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class OperationalLimitTypeEq {

    public static final String ABSOLUTEVALUE_LIMITDIRECTION = "http://iec.ch/TC57/2013/CIM-schema-cim16#OperationalLimitDirectionKind.absoluteValue";
    public static final String PATL_LIMITTYPE = "http://entsoe.eu/CIM/SchemaExtension/3/1#LimitTypeKind.patl";
    public static final String TATL_LIMITTYPE = "http://entsoe.eu/CIM/SchemaExtension/3/1#LimitTypeKind.tatl";

    public static void writePatl(String id, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "OperationalLimitType");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters("PATL");
        writer.writeEndElement();
        writer.writeEmptyElement(ENTSOE_NAMESPACE, "OperationalLimitType.limitType");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, PATL_LIMITTYPE);
        writer.writeEndElement();
    }

    public static void writeTatl(String id, String name, int acceptableDuration, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "OperationalLimitType");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(name);
        writer.writeEndElement();
        writer.writeEmptyElement(ENTSOE_NAMESPACE, "OperationalLimitType.direction");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, ABSOLUTEVALUE_LIMITDIRECTION);
        writer.writeEmptyElement(ENTSOE_NAMESPACE, "OperationalLimitType.limitType");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, TATL_LIMITTYPE);
        writer.writeStartElement(cimNamespace, "OperationalLimitType.acceptableDuration");
        writer.writeCharacters(CgmesExportUtil.format(acceptableDuration));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private OperationalLimitTypeEq() {
    }
}
