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

import static com.powsybl.cgmes.model.CgmesNames.IDENTIFIED_OBJECT_NAME;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class CgmesSubGeographicalRegion {

    private static final String SUB_GEOGRAPHICAL_REGION = "SubGeographicalRegion";
    private static final String SUB_GEOGRAPHICAL_REGION_REGION = "SubGeographicalRegion.Region";

    private CgmesSubGeographicalRegion() {
    }

    public static void writeEq(SubGeographicalRegionEq subRegion, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(SUB_GEOGRAPHICAL_REGION, subRegion.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(subRegion.name), writer);
        CgmesExportUtil.writeResource(SUB_GEOGRAPHICAL_REGION_REGION, subRegion.region, writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record SubGeographicalRegionEq(
        String rdfId,
        String name,
        String region
    ) {
    }

}
