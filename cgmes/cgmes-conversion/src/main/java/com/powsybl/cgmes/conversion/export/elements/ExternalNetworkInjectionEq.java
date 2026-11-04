/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public final class ExternalNetworkInjectionEq {

    public static void write(String id, String name, String equipmentContainer, double governorScd,
                             double maxP, double maxQ, double minP, double minQ,
                             String regulatingControlId, XMLStreamWriter writer,
                             CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartIdName("ExternalNetworkInjection", id, name, writer, context);
        CgmesExportUtil.writeReference("Equipment.EquipmentContainer", equipmentContainer, writer, context);
        writer.writeStartElement(cimNamespace, "ExternalNetworkInjection.governorSCD");
        writer.writeCharacters(CgmesExportUtil.format(governorScd));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "ExternalNetworkInjection.maxP");
        writer.writeCharacters(CgmesExportUtil.format(maxP));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "ExternalNetworkInjection.maxQ");
        writer.writeCharacters(CgmesExportUtil.format(maxQ));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "ExternalNetworkInjection.minP");
        writer.writeCharacters(CgmesExportUtil.format(minP));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "ExternalNetworkInjection.minQ");
        writer.writeCharacters(CgmesExportUtil.format(minQ));
        writer.writeEndElement();
        if (regulatingControlId != null) {
            CgmesExportUtil.writeReference("RegulatingCondEq.RegulatingControl", regulatingControlId, writer, context);
        }
        writer.writeEndElement();
    }

    private ExternalNetworkInjectionEq() {
    }
}
