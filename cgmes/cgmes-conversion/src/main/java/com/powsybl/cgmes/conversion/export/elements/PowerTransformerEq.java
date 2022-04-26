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
public final class PowerTransformerEq {

    private static final String EQ_TRANSFORMEREND_ENDNUMBER = "TransformerEnd.endNumber";
    private static final String EQ_TRANSFORMEREND_TERMINAL = "TransformerEnd.Terminal";

    private static final String EQ_POWERTRANSFORMEREND_POWERTRANSFORMER = "PowerTransformerEnd.PowerTransformer";
    private static final String EQ_POWERTRANSFORMEREND_R = "PowerTransformerEnd.r";
    private static final String EQ_POWERTRANSFORMEREND_X = "PowerTransformerEnd.x";
    private static final String EQ_POWERTRANSFORMEREND_G = "PowerTransformerEnd.g";
    private static final String EQ_POWERTRANSFORMEREND_B = "PowerTransformerEnd.b";
    private static final String EQ_POWERTRANSFORMEREND_RATEDU = "PowerTransformerEnd.ratedU";

    public static void write(String id, String transformerName, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("PowerTransformer", id, transformerName, cimNamespace, writer);
        writer.writeEndElement();
    }

    public static void writeEnd(String id, String transformerEndName, String transformerId, int endNumber, double r, double x, double g, double b, double ratedU, String terminalId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("PowerTransformerEnd", id, transformerEndName, cimNamespace, writer);
        writer.writeStartElement(cimNamespace, EQ_TRANSFORMEREND_ENDNUMBER);
        writer.writeCharacters(CgmesExportUtil.format(endNumber));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_POWERTRANSFORMEREND_R);
        writer.writeCharacters(CgmesExportUtil.format(r));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_POWERTRANSFORMEREND_X);
        writer.writeCharacters(CgmesExportUtil.format(x));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_POWERTRANSFORMEREND_G);
        writer.writeCharacters(CgmesExportUtil.format(g));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_POWERTRANSFORMEREND_B);
        writer.writeCharacters(CgmesExportUtil.format(b));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_POWERTRANSFORMEREND_RATEDU);
        writer.writeCharacters(CgmesExportUtil.format(ratedU));
        writer.writeEndElement();
        CgmesExportUtil.writeReference(EQ_POWERTRANSFORMEREND_POWERTRANSFORMER, transformerId, cimNamespace, writer);
        CgmesExportUtil.writeReference(EQ_TRANSFORMEREND_TERMINAL, terminalId, cimNamespace, writer);
        writer.writeEndElement();
    }

    private PowerTransformerEq() {
    }
}
