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
import com.powsybl.cgmes.model.CgmesNames;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class LoadAreaEq {

    private LoadAreaEq() {
    }

    public static void write(String id, String baseName, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId("LoadArea", id, true, writer, context);
        CgmesExportUtil.writeAttribute(CgmesNames.IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(baseName + " Load Area"), writer, context);
        writer.writeEndElement();
    }

    public static void writeSubArea(String id, String loadAreaId, String baseName, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId("SubLoadArea", id, true, writer, context);
        CgmesExportUtil.writeAttribute(CgmesNames.IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(baseName + " SubLoad Area"), writer, context);
        CgmesExportUtil.writeResource("SubLoadArea.LoadArea", loadAreaId, writer, context);
        writer.writeEndElement();
    }
}
