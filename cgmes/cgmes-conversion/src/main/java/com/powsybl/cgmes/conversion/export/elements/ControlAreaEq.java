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
import com.powsybl.cgmes.model.CgmesNames;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class ControlAreaEq {
    private static final String CONTROL_AREA_TYPE = CgmesNames.CONTROL_AREA_TYPE_KIND_INTERCHANGE;

    public static void write(String id, String controlAreaName, String energyIdentCodeEIC, String energyAreaId,
                             XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId("ControlArea", id, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, CgmesNames.IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(controlAreaName), writer);
        if (energyIdentCodeEIC != null) {
            writer.writeStartElement(context.getCim().getEuNamespace(), "IdentifiedObject.energyIdentCodeEic");
            writer.writeCharacters(energyIdentCodeEIC);
            writer.writeEndElement();
        }
        writer.writeEmptyElement(namespace, "ControlArea.type");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, namespace + CONTROL_AREA_TYPE);
        if (!context.isCim16BusBranchExport()) {
            CgmesExportUtil.writeResource("ControlArea.EnergyArea", energyAreaId, writer, context);
        }
        writer.writeEndElement();
    }

    private ControlAreaEq() {
    }
}
