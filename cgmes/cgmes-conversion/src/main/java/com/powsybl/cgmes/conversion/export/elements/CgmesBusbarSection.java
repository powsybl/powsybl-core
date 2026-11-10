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
public final class CgmesBusbarSection {

    private CgmesBusbarSection() {
    }

    public static void writeEq(BusbarSectionEq busbarSection, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(BUSBAR_SECTION, busbarSection.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(busbarSection.name), writer);
        CgmesExportUtil.writeResource(EQUIPMENT_EQUIPMENT_CONTAINER, busbarSection.equipmentContainer, writer, context);
        CgmesExportUtil.writeResource(CONDUCTING_EQUIPMENT_BASE_VOLTAGE, busbarSection.baseVoltage, writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record BusbarSectionEq(
        String rdfId,
        String name,
        String equipmentContainer,
        String baseVoltage
    ) {
    }

}
