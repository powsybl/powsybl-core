/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.model.CgmesNames;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

import java.util.Arrays;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class SynchronousMachineEq {

    public static void write(String id, String generatorName, String equipmentContainer, String generatingUnit, String regulatingControlId, String reactiveCapabilityCurveId,
                             double minP, double maxP, double minQ, double maxQ, double ratedS, String kind, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("SynchronousMachine", id, generatorName, cimNamespace, writer, context);
        CgmesExportUtil.writeReference("Equipment.EquipmentContainer", equipmentContainer, cimNamespace, writer, context);
        CgmesExportUtil.writeReference("RotatingMachine.GeneratingUnit", generatingUnit, cimNamespace, writer, context);
        if (regulatingControlId != null) {
            CgmesExportUtil.writeReference("RegulatingCondEq.RegulatingControl", regulatingControlId, cimNamespace, writer, context);
        }
        if (reactiveCapabilityCurveId != null) {
            CgmesExportUtil.writeReference("SynchronousMachine.InitialReactiveCapabilityCurve", reactiveCapabilityCurveId, cimNamespace, writer, context);
        }
        writer.writeStartElement(cimNamespace, "SynchronousMachine.minQ");
        writer.writeCharacters(CgmesExportUtil.format(minQ));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "SynchronousMachine.maxQ");
        writer.writeCharacters(CgmesExportUtil.format(maxQ));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "RotatingMachine.ratedS");
        writer.writeCharacters(CgmesExportUtil.format(ratedS, defaultRatedS(minP, maxP, minQ, maxQ))); //RatedS by default to 100, needed for SUV
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "SynchronousMachine.type");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, String.format("%s%s.%s", cimNamespace, "SynchronousMachineKind", kind)); // all generators are considered generators
        writer.writeEndElement();
    }

    private SynchronousMachineEq() {
    }

    private static double defaultRatedS(double minP, double maxP, double minQ, double maxQ) {
        double[] array = {Math.abs(minP), Math.abs(maxP), Math.abs(minQ), Math.abs(maxQ)};
        return Arrays.stream(array).max().orElseThrow();
    }
}
