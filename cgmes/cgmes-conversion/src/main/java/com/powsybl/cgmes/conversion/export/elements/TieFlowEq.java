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
public final class TieFlowEq {

    public static void write(String id, String controlAreaId, String terminalId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartId("TieFlow", id, false, cimNamespace, writer);
        CgmesExportUtil.writeReference("TieFlow.ControlArea", controlAreaId, cimNamespace, writer);
        CgmesExportUtil.writeReference("TieFlow.Terminal", terminalId, cimNamespace, writer);
        writer.writeEndElement();
    }

    private TieFlowEq() {
    }
}
