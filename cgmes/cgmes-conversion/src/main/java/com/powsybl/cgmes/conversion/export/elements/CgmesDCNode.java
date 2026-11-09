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
public final class CgmesDCNode {

    private static final String DC_NODE_DC_EQUIPMENT_CONTAINER = DC_NODE + "." + DC_EQUIPMENT_CONTAINER;

    private CgmesDCNode() {
    }

    public static void writeEq(DCNodeEq dcNode, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(DC_NODE, dcNode.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(dcNode.name), writer, context);
        if (dcNode.equipmentContainer != null) {
            CgmesExportUtil.writeResource(DC_NODE_DC_EQUIPMENT_CONTAINER, dcNode.equipmentContainer, writer, context);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    public record DCNodeEq(
        String rdfId,
        String name,
        String equipmentContainer
    ) {
    }

}
