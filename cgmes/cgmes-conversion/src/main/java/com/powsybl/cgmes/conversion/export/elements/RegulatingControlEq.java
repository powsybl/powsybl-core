/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Connectable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Set;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class RegulatingControlEq {

    public static final String REGULATING_CONTROL_VOLTAGE = "RegulatingControlModeKind.voltage";

    public static String writeKindVoltage(Connectable<?> c, String terminalId, Set<String> regulatingControlsWritten, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String regulatingControlId = context.getNamingStrategy().getCgmesIdFromProperty(c, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl");
        if (regulatingControlId != null && !regulatingControlsWritten.contains(regulatingControlId)) {
            String regulatingControlName = "RC_" + c.getNameOrId();
            RegulatingControlEq.writeKindVoltage(regulatingControlId, regulatingControlName, terminalId, cimNamespace, writer, context);
            regulatingControlsWritten.add(regulatingControlId);
        }
        return regulatingControlId;
    }

    private static void writeKindVoltage(String id, String regulatingControlName, String terminalId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("RegulatingControl", id, regulatingControlName, cimNamespace, writer, context);
        CgmesExportUtil.writeReference("RegulatingControl.Terminal", terminalId, cimNamespace, writer, context);
        writer.writeEmptyElement(cimNamespace, "RegulatingControl.mode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + REGULATING_CONTROL_VOLTAGE);
        writer.writeEndElement();
    }

    private RegulatingControlEq() {
    }
}
