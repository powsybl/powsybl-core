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
import com.powsybl.iidm.network.SwitchKind;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class SwitchEq {

    public static void write(String id, String switchName, String switchType, SwitchKind switchKind, String equipmentContainer, boolean open, boolean retained, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String className = switchType != null ? switchType : CgmesExportUtil.switchClassname(switchKind);
        CgmesExportUtil.writeStartIdName(className, id, switchName, cimNamespace, writer, context);
        CgmesExportUtil.writeReference("Equipment.EquipmentContainer", equipmentContainer, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, "Switch.normalOpen");
        writer.writeCharacters(CgmesExportUtil.format(open));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "Switch.retained");
        writer.writeCharacters(CgmesExportUtil.format(retained));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private SwitchEq() {
    }
}
