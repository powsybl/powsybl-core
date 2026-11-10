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
public final class CgmesSubstation {

    private static final String SUBSTATION_REGION = SUBSTATION + ".Region";

    private CgmesSubstation() {
    }

    public static void writeEq(SubstationEq substation, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(SUBSTATION, substation.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(substation.name), writer);
        CgmesExportUtil.writeResource(SUBSTATION_REGION, substation.region, writer, context);
        writer.writeEndElement();
    }

    public record SubstationEq(
        String rdfId,
        String name,
        String region
    ) {
    }

}
