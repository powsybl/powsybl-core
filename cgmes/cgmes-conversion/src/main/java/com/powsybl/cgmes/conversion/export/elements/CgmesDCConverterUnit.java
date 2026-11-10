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
public final class CgmesDCConverterUnit {

    private static final String OPERATING_MODE_KIND = "DCConverterOperatingModeKind.";
    private static final String DC_CONVERTER_UNIT_OPERATION_MODE = DC_CONVERTER_UNIT + ".operationMode";
    private static final String DC_CONVERTER_UNIT_SUBSTATION = DC_CONVERTER_UNIT + "." + SUBSTATION;

    private CgmesDCConverterUnit() {
    }

    public static void writeEq(DCConverterUnitEq dcConverterUnit, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(DC_CONVERTER_UNIT, dcConverterUnit.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(dcConverterUnit.name), writer);
        CgmesExportUtil.writeResource(DC_CONVERTER_UNIT_OPERATION_MODE, namespace,
            namespace + OPERATING_MODE_KIND + dcConverterUnit.operationMode, writer);
        CgmesExportUtil.writeResource(DC_CONVERTER_UNIT_SUBSTATION, dcConverterUnit.substation, writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    public record DCConverterUnitEq(
        String rdfId,
        String name,
        String operationMode,
        String substation
    ) {
    }

}
