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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class EquivalentInjectionEq {

    private static final String EQ_EQUIVALENTINJECTION_REGULATIONCAPABILITY = "EquivalentInjection.regulationCapability";
    private static final String EQ_EQUIVALENTINJECTION_MINP = "EquivalentInjection.minP";
    private static final String EQ_EQUIVALENTINJECTION_MAXP = "EquivalentInjection.maxP";
    private static final String EQ_EQUIVALENTINJECTION_MINQ = "EquivalentInjection.minQ";
    private static final String EQ_EQUIVALENTINJECTION_MAXQ = "EquivalentInjection.maxQ";
    private static final String EQ_EQUIVALENTINJECTION_REACTIVE_CAPABILITY_CURVE = "EquivalentInjection.ReactiveCapabilityCurve";

    public static void write(String id, String name, boolean regulationCapability, double minP, double maxP, double minQ, double maxQ,
                             String reactiveCapabilityCurveId, String baseVoltageId, String cimNamespace, XMLStreamWriter writer,
                             CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName(CgmesNames.EQUIVALENT_INJECTION, id, name, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, EQ_EQUIVALENTINJECTION_REGULATIONCAPABILITY);
        writer.writeCharacters(CgmesExportUtil.format(regulationCapability));
        writer.writeEndElement();
        // To avoid Infinite values during reimport, default values are not exported
        if (!CgmesExportUtil.isMinusOrMaxValue(minP)) {
            writer.writeStartElement(cimNamespace, EQ_EQUIVALENTINJECTION_MINP);
            writer.writeCharacters(CgmesExportUtil.format(minP));
            writer.writeEndElement();
        }
        if (!CgmesExportUtil.isMinusOrMaxValue(maxP)) {
            writer.writeStartElement(cimNamespace, EQ_EQUIVALENTINJECTION_MAXP);
            writer.writeCharacters(CgmesExportUtil.format(maxP));
            writer.writeEndElement();
        }
        if (!CgmesExportUtil.isMinusOrMaxValue(minQ)) {
            writer.writeStartElement(cimNamespace, EQ_EQUIVALENTINJECTION_MINQ);
            writer.writeCharacters(CgmesExportUtil.format(minQ));
            writer.writeEndElement();
        }
        if (!CgmesExportUtil.isMinusOrMaxValue(maxQ)) {
            writer.writeStartElement(cimNamespace, EQ_EQUIVALENTINJECTION_MAXQ);
            writer.writeCharacters(CgmesExportUtil.format(maxQ));
            writer.writeEndElement();
        }
        if (reactiveCapabilityCurveId != null) {
            CgmesExportUtil.writeReference(EQ_EQUIVALENTINJECTION_REACTIVE_CAPABILITY_CURVE, reactiveCapabilityCurveId, cimNamespace, writer, context);
        }
        CgmesExportUtil.writeReference("ConductingEquipment.BaseVoltage", baseVoltageId, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    private EquivalentInjectionEq() {
    }
}
