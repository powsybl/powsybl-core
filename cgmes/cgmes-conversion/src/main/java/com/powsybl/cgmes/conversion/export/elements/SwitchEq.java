/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.SwitchKind;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class SwitchEq {

    public static void write(String id, String switchName, SwitchKind switchKind, String equipmentContainer, boolean retained, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, switchClassname(switchKind));
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(switchName);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "Equipment.EquipmentContainer");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + equipmentContainer);
        writer.writeStartElement(cimNamespace, "Switch.retained");
        writer.writeCharacters(CgmesExportUtil.format(retained));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static String switchClassname(SwitchKind switchKind) {
        switch (switchKind) {
            case BREAKER:
                return "Breaker";
            case DISCONNECTOR:
                return "Disconnector";
            case LOAD_BREAK_SWITCH:
                return "LoadBreakSwitch";
        }
        return "Switch";
    }

    private SwitchEq() {
    }
}
