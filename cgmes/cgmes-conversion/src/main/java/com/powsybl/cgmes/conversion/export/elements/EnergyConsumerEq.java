/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class EnergyConsumerEq {

    public static void write(String className, String id, String loadName, String loadGroup, String equipmentContainer, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName(className, id, loadName, cimNamespace, writer);
        CgmesExportUtil.writeReference("Equipment.EquipmentContainer", equipmentContainer, cimNamespace, writer);
        if (loadGroup != null) {
            CgmesExportUtil.writeReference(className + ".LoadGroup", loadGroup, cimNamespace, writer);
        }
        writer.writeEndElement();
    }

    private EnergyConsumerEq() {
    }
}
