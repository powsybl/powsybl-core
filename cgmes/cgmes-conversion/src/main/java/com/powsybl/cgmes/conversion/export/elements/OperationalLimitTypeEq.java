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

    public static final String ABSOLUTEVALUE_LIMITDIRECTION = "OperationalLimitDirectionKind.absoluteValue";
    private static final String PATL = "patl";
    private static final String TATL = "tatl";

    public static void writePatl(String id, String cimNamespace, String euNamespace, String limitTypeAttributeName, String limitKindClassName, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "OperationalLimitType");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters("PATL");
        writer.writeEndElement();
        writer.writeEmptyElement(euNamespace, limitTypeAttributeName);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, String.format("%s%s.%s", euNamespace, limitKindClassName, PATL));
        writer.writeEndElement();
    }

    public static void writeTatl(String id, String name, int acceptableDuration, String cimNamespace, String euNamespace, String limitKindClassName, String limitTypeAttributeName, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "OperationalLimitType");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(name);
        writer.writeEndElement();
        writer.writeEmptyElement(euNamespace, "OperationalLimitType.direction");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + ABSOLUTEVALUE_LIMITDIRECTION);
        writer.writeEmptyElement(euNamespace, limitTypeAttributeName);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, String.format("%s%s.%s", euNamespace, limitKindClassName, TATL));
        writer.writeStartElement(cimNamespace, "OperationalLimitType.acceptableDuration");
        writer.writeCharacters(CgmesExportUtil.format(acceptableDuration));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private OperationalLimitTypeEq() {
    }
}
