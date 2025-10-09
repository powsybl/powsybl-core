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

    private static final String MONOPOLAR_GROUND_RETURN = "DCConverterOperatingModeKind.monopolarGroundReturn";

    public static void write(String id, String dcConverterUnitName, String substationId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("DCConverterUnit", id, dcConverterUnitName, cimNamespace, writer, context);
        writer.writeEmptyElement(cimNamespace, "DCConverterUnit.operationMode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + MONOPOLAR_GROUND_RETURN);
        CgmesExportUtil.writeReference("DCConverterUnit.Substation", substationId, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    private DCConverterUnitEq() {
    }
}
