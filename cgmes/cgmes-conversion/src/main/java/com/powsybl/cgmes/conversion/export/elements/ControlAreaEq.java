/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.model.CgmesNames;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class ControlAreaEq {
    private static final String CONTROL_AREA_TYPE = "ControlAreaTypeKind.Interchange";

    public static void write(String id, String controlAreaName, String energyIdentificationCodeEIC, String cimNamespace, String euNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("ControlArea", id, controlAreaName, cimNamespace, writer, context);
        writer.writeStartElement(euNamespace, "IdentifiedObject.energyIdentCodeEic");
        writer.writeCharacters(energyIdentificationCodeEIC);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "ControlArea.type");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + CONTROL_AREA_TYPE);
        writer.writeEndElement();
    }

    private ControlAreaEq() {
    }
}
