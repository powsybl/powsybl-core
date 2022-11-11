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
public final class SynchronousMachineEq {

    public static void write(String id, String generatorName, String equipmentContainer, String generatingUnit, String regulatingControlId, String reactiveCapabilityCurveId, double minQ, double maxQ, double ratedS, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("SynchronousMachine", id, generatorName, cimNamespace, writer);
        CgmesExportUtil.writeReference("Equipment.EquipmentContainer", equipmentContainer, cimNamespace, writer);
        CgmesExportUtil.writeReference("RotatingMachine.GeneratingUnit", generatingUnit, cimNamespace, writer);
        if (regulatingControlId != null) {
            CgmesExportUtil.writeReference("RegulatingCondEq.RegulatingControl", regulatingControlId, cimNamespace, writer);
        }
        if (reactiveCapabilityCurveId != null) {
            CgmesExportUtil.writeReference("SynchronousMachine.InitialReactiveCapabilityCurve", reactiveCapabilityCurveId, cimNamespace, writer);
        }
        writer.writeStartElement(cimNamespace, "SynchronousMachine.minQ");
        writer.writeCharacters(CgmesExportUtil.format(minQ));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "SynchronousMachine.maxQ");
        writer.writeCharacters(CgmesExportUtil.format(maxQ));
        writer.writeEndElement();
        if (!Double.isNaN(ratedS)) {
            writer.writeStartElement(cimNamespace, "RotatingMachine.ratedS");
            writer.writeCharacters(CgmesExportUtil.format(ratedS));
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private SynchronousMachineEq() {
    }
}
