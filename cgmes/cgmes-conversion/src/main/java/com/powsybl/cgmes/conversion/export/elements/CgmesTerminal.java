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
public final class CgmesTerminal {

    private static final String TERMINAL_CONDUCTING_EQUIPMENT = TERMINAL + "." + CONDUCTING_EQUIPMENT;
    private static final String TERMINAL_CONNECTIVITY_NODE = TERMINAL + "." + CONNECTIVITY_NODE;
    private static final String AC_DC_TERMINAL_SEQUENCE_NUMBER = AC_DC_TERMINAL + "." + SEQUENCE_NUMBER;

    private CgmesTerminal() {
    }

    public static void writeEq(TerminalEq terminal, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(TERMINAL, terminal.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(terminal.conductingEquipment), writer, context);
        CgmesExportUtil.writeResource(TERMINAL_CONDUCTING_EQUIPMENT, terminal.conductingEquipment, writer, context);
        if (terminal.connectivityNode != null) {
            CgmesExportUtil.writeResource(TERMINAL_CONNECTIVITY_NODE, terminal.connectivityNode, writer, context);
        }
        CgmesExportUtil.writeAttribute(AC_DC_TERMINAL_SEQUENCE_NUMBER, CgmesExportUtil.format(terminal.sequenceNumber), writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record TerminalEq(
        String rdfId,
        int sequenceNumber,
        String conductingEquipment,
        String connectivityNode
    ) {
    }

}
