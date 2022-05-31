/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.model.CgmesNames;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class RegulatingControlEq {

    public static final String REGULATING_CONTROL_VOLTAGE = "RegulatingControlModeKind.voltage";

    public static void write(String id, String regulatingControlName, String terminalId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        // FIXME(Luma) Obtain id through naming strategy after #2148 is merged
        CgmesExportUtil.writeStartIdName("RegulatingControl", id, regulatingControlName, cimNamespace, writer);
        CgmesExportUtil.writeReference("RegulatingControl.Terminal", terminalId, cimNamespace, writer);
        writer.writeEmptyElement(cimNamespace, "RegulatingControl.mode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + REGULATING_CONTROL_VOLTAGE);
        writer.writeEndElement();
    }

    private RegulatingControlEq() {
    }
}
