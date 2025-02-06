/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class RegulatingControlEq {

    public static final String REGULATING_CONTROL_VOLTAGE = "RegulatingControlModeKind.voltage";
    public static final String REGULATING_CONTROL_REACTIVE_POWER = "RegulatingControlModeKind.reactivePower";
    public static final String REGULATING_CONTROL_ACTIVE_POWER = "RegulatingControlModeKind.activePower";
    public static final String REGULATING_CONTROL_CURRENT_FLOW = "RegulatingControlModeKind.currentFlow";

    public static String writeRegulatingControlEq(Connectable<?> c, String terminalId, Set<String> regulatingControlsWritten, String mode, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String regulatingControlId = context.getNamingStrategy().getCgmesIdFromProperty(c, Conversion.PROPERTY_REGULATING_CONTROL);
        if (regulatingControlId != null && !regulatingControlsWritten.contains(regulatingControlId)) {
            String regulatingControlName = "RC_" + c.getNameOrId();
            CgmesExportUtil.writeStartIdName("RegulatingControl", regulatingControlId, regulatingControlName, cimNamespace, writer, context);
            CgmesExportUtil.writeReference("RegulatingControl.Terminal", terminalId, cimNamespace, writer, context);
            writer.writeEmptyElement(cimNamespace, "RegulatingControl.mode");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + mode);
            writer.writeEndElement();
            regulatingControlsWritten.add(regulatingControlId);
        }
        return regulatingControlId;
    }

    private RegulatingControlEq() {
    }
}
