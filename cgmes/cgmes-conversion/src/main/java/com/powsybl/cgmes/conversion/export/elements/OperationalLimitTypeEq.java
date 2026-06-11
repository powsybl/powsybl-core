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
import com.powsybl.commons.PowsyblException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class OperationalLimitTypeEq {

    public static final String ABSOLUTEVALUE_LIMITDIRECTION = "OperationalLimitDirectionKind.absoluteValue";
    private static final String PATL = "patl";
    private static final String TATL = "tatl";

    public static void writePatl(String id, String cimNamespace, String euNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("OperationalLimitType", id, "PATL", cimNamespace, writer, context);
        writeDirection(cimNamespace, writer);
        writeKind(PATL, euNamespace, writer, context);
        if (context.getCimVersion() == 100) {
            writeInfiniteDuration(true, cimNamespace, writer);
        }
        writer.writeEndElement();
    }

    public static void writeTatl(String id, int acceptableDuration, String cimNamespace, String euNamespace,
                                 XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("OperationalLimitType", id, "TATL " + acceptableDuration, cimNamespace, writer, context);
        writeDirection(cimNamespace, writer);
        writeKind(TATL, euNamespace, writer, context);
        if (context.getCimVersion() == 100) {
            writeInfiniteDuration(false, cimNamespace, writer);
        }
        writer.writeStartElement(cimNamespace, "OperationalLimitType.acceptableDuration");
        writer.writeCharacters(CgmesExportUtil.format(acceptableDuration));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeDirection(String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEmptyElement(cimNamespace, "OperationalLimitType.direction");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + ABSOLUTEVALUE_LIMITDIRECTION);
    }

    private static void writeKind(String kind, String euNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writer.writeEmptyElement(euNamespace, getLimitTypeAttributeName(context));
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, String.format("%s%s.%s", euNamespace, getLimitKindClassName(context), kind));
    }

    private static void writeInfiniteDuration(boolean infiniteDuration, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "OperationalLimitType.isInfiniteDuration");
        writer.writeCharacters(Boolean.toString(infiniteDuration));
        writer.writeEndElement();
    }

    public static String getLimitTypeAttributeName(CgmesExportContext context) {
        if (context.getCimVersion() == 14) {
            throw new PowsyblException("Undefined limit type attribute name for version 14");
        } else if (context.getCimVersion() == 16) {
            return "OperationalLimitType.limitType";
        } else {
            return "OperationalLimitType.kind";
        }
    }

    public static String getLimitKindClassName(CgmesExportContext context) {
        if (context.getCimVersion() == 14) {
            throw new PowsyblException("Undefined limit kind class name for version 14");
        } else if (context.getCimVersion() == 16) {
            return "LimitTypeKind";
        } else {
            return "LimitKind";
        }
    }

    private OperationalLimitTypeEq() {
    }
}
