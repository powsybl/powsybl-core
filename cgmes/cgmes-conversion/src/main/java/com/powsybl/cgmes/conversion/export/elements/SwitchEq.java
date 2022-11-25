/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.iidm.network.SwitchKind;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class SwitchEq {

    public static void write(String id, String switchName, SwitchKind switchKind, String equipmentContainer, boolean open, boolean retained, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName(switchClassname(switchKind), id, switchName, cimNamespace, writer);
        CgmesExportUtil.writeReference("Equipment.EquipmentContainer", equipmentContainer, cimNamespace, writer);
        writer.writeStartElement(cimNamespace, "Switch.normalOpen");
        writer.writeCharacters(CgmesExportUtil.format(open));
        writer.writeEndElement();
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
