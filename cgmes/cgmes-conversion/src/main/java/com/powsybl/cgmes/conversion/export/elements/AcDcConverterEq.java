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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class AcDcConverterEq {

    public static void write(String id, String converterName, String className, double ratedUdc,
                             double idleLoss, double switchingLoss, double resistiveLoss, String dcEquipmentContainerId, String pccTerminal,
                             String capabilityCurveId, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartIdName(className, id, converterName, writer, context);
        writer.writeStartElement(cimNamespace, "ACDCConverter.ratedUdc");
        writer.writeCharacters(CgmesExportUtil.format(ratedUdc));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "ACDCConverter.idleLoss");
        writer.writeCharacters(CgmesExportUtil.format(idleLoss));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "ACDCConverter.switchingLoss");
        writer.writeCharacters(CgmesExportUtil.format(switchingLoss));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "ACDCConverter.resistiveLoss");
        writer.writeCharacters(CgmesExportUtil.format(resistiveLoss));
        writer.writeEndElement();
        CgmesExportUtil.writeReference("Equipment.EquipmentContainer", dcEquipmentContainerId, writer, context);
        if (pccTerminal != null) {
            CgmesExportUtil.writeReference("ACDCConverter.PccTerminal", pccTerminal, writer, context);
        }
        if (capabilityCurveId != null) {
            CgmesExportUtil.writeReference("VsConverter.CapabilityCurve", capabilityCurveId, writer, context);
        }
        writer.writeEndElement();
    }

    private AcDcConverterEq() {
    }
}
