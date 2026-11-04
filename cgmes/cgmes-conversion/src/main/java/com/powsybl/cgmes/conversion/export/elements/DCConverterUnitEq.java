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
import com.powsybl.cgmes.model.CgmesNames;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class DCConverterUnitEq {

    private static final String OPERATING_MODE_KIND = "DCConverterOperatingModeKind.";

    public static void write(String id, String dcConverterUnitName, String operationMode, String substationId,
                             XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartIdName("DCConverterUnit", id, dcConverterUnitName, writer, context);
        writer.writeEmptyElement(cimNamespace, "DCConverterUnit.operationMode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + OPERATING_MODE_KIND + operationMode);
        CgmesExportUtil.writeResource("DCConverterUnit.Substation", substationId, writer, context);
        writer.writeEndElement();
    }

    private DCConverterUnitEq() {
    }
}
