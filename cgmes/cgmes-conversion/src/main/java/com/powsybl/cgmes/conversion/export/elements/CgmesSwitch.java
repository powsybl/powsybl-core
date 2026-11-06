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
public final class CgmesSwitch {

    private static final String SWITCH_NORMAL_OPEN = SWITCH + "." + NORMAL_OPEN;
    private static final String SWITCH_RETAINED = SWITCH + ".retained";

    private CgmesSwitch() {
    }

    public static void writeEq(SwitchEq switchEq, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(switchEq.className, switchEq.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(switchEq.name), writer, context);
        CgmesExportUtil.writeResource(EQUIPMENT_EQUIPMENT_CONTAINER, switchEq.equipmentContainer, writer, context);
        CgmesExportUtil.writeAttribute(SWITCH_NORMAL_OPEN, CgmesExportUtil.format(switchEq.normalOpen), writer, context);
        CgmesExportUtil.writeAttribute(SWITCH_RETAINED, CgmesExportUtil.format(switchEq.retained), writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record SwitchEq(
        String className,
        String rdfId,
        String name,
        boolean normalOpen,
        boolean retained,
        String equipmentContainer
    ) {
    }

}
