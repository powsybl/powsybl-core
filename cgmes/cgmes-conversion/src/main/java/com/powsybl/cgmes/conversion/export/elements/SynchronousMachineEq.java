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
public final class SynchronousMachineEq {

    public static void write(String id, String generatorName, String generatingUnit, String regulatingControlId, String reactiveCapabilityCurveId, double minQ, double maxQ, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "SynchronousMachine");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(generatorName);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "RotatingMachine.GeneratingUnit");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + generatingUnit);
        if (regulatingControlId != null) {
            writer.writeEmptyElement(cimNamespace, "RegulatingCondEq.RegulatingControl");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + regulatingControlId);
        }
        if (reactiveCapabilityCurveId != null) {
            writer.writeEmptyElement(cimNamespace, "SynchronousMachine.InitialReactiveCapabilityCurve");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + reactiveCapabilityCurveId);
        }
        writer.writeStartElement(cimNamespace, "SynchronousMachine.minQ");
        writer.writeCharacters(CgmesExportUtil.format(minQ));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "SynchronousMachine.maxQ");
        writer.writeCharacters(CgmesExportUtil.format(maxQ));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private SynchronousMachineEq() {
    }
}
