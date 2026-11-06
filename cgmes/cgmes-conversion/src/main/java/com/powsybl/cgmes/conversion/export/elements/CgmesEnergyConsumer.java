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
public final class CgmesEnergyConsumer {

    private static final String ENERGY_CONSUMER_LOAD_RESPONSE = ENERGY_CONSUMER + ".LoadResponse";
    private static final String LOAD_GROUP = ".LoadGroup";

    private CgmesEnergyConsumer() {
    }

    public static void writeEq(EnergyConsumerEq energyConsumer, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(energyConsumer.className, energyConsumer.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(energyConsumer.name), writer, context);
        CgmesExportUtil.writeResource(EQUIPMENT_EQUIPMENT_CONTAINER, energyConsumer.equipmentContainer, writer, context);
        if (energyConsumer.loadGroup != null) {
            CgmesExportUtil.writeResource(energyConsumer.className + LOAD_GROUP, energyConsumer.loadGroup, writer, context);
        }
        if (energyConsumer.loadResponse != null) {
            CgmesExportUtil.writeResource(ENERGY_CONSUMER_LOAD_RESPONSE, energyConsumer.loadResponse, writer, context);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    public record EnergyConsumerEq(
        String className,
        String rdfId,
        String name,
        String loadGroup,
        String equipmentContainer,
        String loadResponse
    ) {
    }

}
