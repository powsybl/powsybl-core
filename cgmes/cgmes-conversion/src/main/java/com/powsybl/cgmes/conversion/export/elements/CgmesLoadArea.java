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

import static com.powsybl.cgmes.model.CgmesNames.IDENTIFIED_OBJECT_NAME;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class CgmesLoadArea {

    private static final String LOAD_AREA = "LoadArea";
    private static final String SUB_LOAD_AREA = "SubLoadArea";
    private static final String SUB_LOAD_AREA_LOAD_AREA = "SubLoadArea.LoadArea";

    private CgmesLoadArea() {
    }

    public static void writeEq(LoadAreaEq loadArea, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(LOAD_AREA, loadArea.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(loadArea.name + " Load Area"), writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public static void writeSubLoadAreaEq(SubLoadAreaEq subLoadArea, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(SUB_LOAD_AREA, subLoadArea.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(subLoadArea.name + " SubLoad Area"), writer, context);
        CgmesExportUtil.writeResource(SUB_LOAD_AREA_LOAD_AREA, subLoadArea.loadArea, writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record LoadAreaEq(
        String rdfId,
        String name
    ) {
    }

    public record SubLoadAreaEq(
        String rdfId,
        String name,
        String loadArea
    ) {
    }

}
