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
public final class CgmesControlArea {

    private static final String CONTROL_AREA_TYPE = CONTROL_AREA + ".type";
    private static final String CONTROL_AREA_ENERGY_AREA = CONTROL_AREA + "." + ENERGY_AREA;
    private static final String IDENTIFIED_OBJECT_ENERGY_IDENT_CODE_EIC = "IdentifiedObject." + ENERGY_IDENT_CODE_EIC;

    private CgmesControlArea() {
    }

    public static void writeEq(ControlAreaEq controlArea, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        String euNamespace = context.getCim().getEuNamespace();
        CgmesExportUtil.writeStartId(CONTROL_AREA, controlArea.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(controlArea.name), writer);
        if (controlArea.energyIdentCodeEic != null) {
            CgmesExportUtil.writeAttribute(euNamespace, IDENTIFIED_OBJECT_ENERGY_IDENT_CODE_EIC, controlArea.energyIdentCodeEic, writer);
        }
        CgmesExportUtil.writeResource(CONTROL_AREA_TYPE, cimNamespace, cimNamespace + CONTROL_AREA_TYPE_KIND_INTERCHANGE, writer);
        if (!context.isCim16BusBranchExport()) {
            CgmesExportUtil.writeResource(CONTROL_AREA_ENERGY_AREA, controlArea.energyArea, writer, context);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    public record ControlAreaEq(
        String rdfId,
        String name,
        String energyIdentCodeEic,
        String energyArea
    ) {
    }

}
