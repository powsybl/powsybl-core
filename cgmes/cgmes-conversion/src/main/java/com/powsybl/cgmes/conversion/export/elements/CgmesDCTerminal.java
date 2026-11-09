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

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class CgmesDCTerminal {

    private static final String DC_BASE_TERMINAL_DC_NODE = DC_BASE_TERMINAL + "." + DC_NODE;
    private static final String AC_DC_TERMINAL_SEQUENCE_NUMBER = AC_DC_TERMINAL + "." + SEQUENCE_NUMBER;

    private CgmesDCTerminal() {
    }

    public static void writeEq(DCTerminalEq dcTerminal, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(dcTerminal.className, dcTerminal.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(dcTerminal.name), writer, context);
        CgmesExportUtil.writeResource(dcTerminal.className + "." + DC_CONDUCTING_EQUIPMENT, dcTerminal.dcConductingEquipment, writer, context);
        if (dcTerminal.dcNode != null) {
            CgmesExportUtil.writeResource(DC_BASE_TERMINAL_DC_NODE, dcTerminal.dcNode, writer, context);
        }
        CgmesExportUtil.writeAttribute(AC_DC_TERMINAL_SEQUENCE_NUMBER, CgmesExportUtil.format(dcTerminal.sequenceNumber), writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record DCTerminalEq(
        String className,
        String rdfId,
        String name,
        int sequenceNumber,
        String dcConductingEquipment,
        String dcNode
    ) {
    }

}
