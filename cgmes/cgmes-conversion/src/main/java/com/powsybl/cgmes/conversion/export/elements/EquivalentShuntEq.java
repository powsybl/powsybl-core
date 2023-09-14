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
 */
public final class EquivalentShuntEq {

    private static final String EQ_EQUIVALENTSHUNT_B = "EquivalentShunt.b";
    private static final String EQ_EQUIVALENTSHUNT_G = "EquivalentShunt.g";

    public static void write(String id, String name, double g, double b, String equipmentContainer,
                             String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("EquivalentShunt", id, name, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, EQ_EQUIVALENTSHUNT_B);
        writer.writeCharacters(CgmesExportUtil.format(b));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_EQUIVALENTSHUNT_G);
        writer.writeCharacters(CgmesExportUtil.format(g));
        writer.writeEndElement();
        CgmesExportUtil.writeReference("Equipment.EquipmentContainer", equipmentContainer, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    private EquivalentShuntEq() {
    }
}
