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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class LoadAreaEq {

    private LoadAreaEq() {
    }

    public static void write(String id, String baseName, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("LoadArea", id, baseName + " Load Area", cimNamespace, writer, context);
        writer.writeEndElement();
    }

    public static void writeSubArea(String id, String loadAreaId, String baseName, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("SubLoadArea", id, baseName + " SubLoad Area", cimNamespace, writer, context);
        CgmesExportUtil.writeReference("SubLoadArea.LoadArea", loadAreaId, cimNamespace, writer, context);
        writer.writeEndElement();
    }
}
