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

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class EnergyConsumerEq {

    public static void write(String className, String id, String loadName, String loadGroup, String equipmentContainer, String loadResponseCharacteristicId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName(className, id, loadName, cimNamespace, writer, context);
        CgmesExportUtil.writeReference("Equipment.EquipmentContainer", equipmentContainer, cimNamespace, writer, context);
        if (loadGroup != null) {
            CgmesExportUtil.writeReference(className + ".LoadGroup", loadGroup, cimNamespace, writer, context);
        }
        if (loadResponseCharacteristicId != null) {
            CgmesExportUtil.writeReference("EnergyConsumer.LoadResponse", loadResponseCharacteristicId, cimNamespace, writer, context);
        }
        writer.writeEndElement();
    }

    private EnergyConsumerEq() {
    }
}
