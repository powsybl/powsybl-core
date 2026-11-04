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
import com.powsybl.cgmes.model.CgmesNames;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class EquivalentShuntEq {

    private static final String EQ_EQUIVALENTSHUNT_B = "EquivalentShunt.b";
    private static final String EQ_EQUIVALENTSHUNT_G = "EquivalentShunt.g";

    public static void write(String id, String name, double g, double b, String equipmentContainer,
                             XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId("EquivalentShunt", id, true, writer, context);
        CgmesExportUtil.writeAttribute(CgmesNames.IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(name), writer, context);
        writer.writeStartElement(cimNamespace, EQ_EQUIVALENTSHUNT_B);
        writer.writeCharacters(CgmesExportUtil.format(b));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_EQUIVALENTSHUNT_G);
        writer.writeCharacters(CgmesExportUtil.format(g));
        writer.writeEndElement();
        CgmesExportUtil.writeResource("Equipment.EquipmentContainer", equipmentContainer, writer, context);
        writer.writeEndElement();
    }

    private EquivalentShuntEq() {
    }
}
