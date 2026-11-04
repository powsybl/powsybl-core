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
public final class TieFlowEq {

    public static void write(String id, String controlAreaId, String terminalId, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId("TieFlow", id, false, writer, context);
        CgmesExportUtil.writeResource("TieFlow.ControlArea", controlAreaId, writer, context);
        CgmesExportUtil.writeResource("TieFlow.Terminal", terminalId, writer, context);
        writer.writeStartElement(cimNamespace, "TieFlow.positiveFlowIn");
        writer.writeCharacters("true"); // always true
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private TieFlowEq() {
    }
}
