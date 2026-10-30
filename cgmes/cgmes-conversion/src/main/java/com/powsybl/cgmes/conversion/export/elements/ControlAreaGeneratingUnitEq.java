/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
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
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public final class ControlAreaGeneratingUnitEq {

    public static void write(String id, String controlAreaGeneratingUnitName, String controlArea, String generatingUnit,
                             String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("ControlAreaGeneratingUnit", id, controlAreaGeneratingUnitName, cimNamespace, writer, context);
        CgmesExportUtil.writeReference("ControlAreaGeneratingUnit.ControlArea", controlArea, cimNamespace, writer, context);
        CgmesExportUtil.writeReference("ControlAreaGeneratingUnit.GeneratingUnit", generatingUnit, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    private ControlAreaGeneratingUnitEq() {
    }
}
