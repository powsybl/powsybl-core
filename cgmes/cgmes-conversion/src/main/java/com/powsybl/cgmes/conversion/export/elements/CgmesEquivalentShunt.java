/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class CgmesEquivalentShunt {

    private static final String EQUIVALENT_SHUNT_B = EQUIVALENT_SHUNT + "." + B;
    private static final String EQUIVALENT_SHUNT_G = EQUIVALENT_SHUNT + "." + G;

    private CgmesEquivalentShunt() {
    }

    public static void writeEq(EquivalentShuntEq equivalentShunt, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(EQUIVALENT_SHUNT, equivalentShunt.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(equivalentShunt.name), writer, context);
        CgmesExportUtil.writeAttribute(EQUIVALENT_SHUNT_B, CgmesExportUtil.format(equivalentShunt.b), writer, context);
        CgmesExportUtil.writeAttribute(EQUIVALENT_SHUNT_G, CgmesExportUtil.format(equivalentShunt.g), writer, context);
        CgmesExportUtil.writeResource(EQUIPMENT_EQUIPMENT_CONTAINER, equivalentShunt.equipmentContainer, writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record EquivalentShuntEq(
        String rdfId,
        String name,
        double b,
        double g,
        String equipmentContainer
    ) {
    }
}
