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
public final class CgmesBaseVoltage {

    private static final String BASE_VOLTAGE_NOMINAL_VOLTAGE = BASE_VOLTAGE + ".nominalVoltage";

    private CgmesBaseVoltage() {
    }

    public static void writeEq(BaseVoltageEq baseVoltage, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        String nominalVoltage = CgmesExportUtil.format(baseVoltage.nominalVoltage);
        CgmesExportUtil.writeStartId(BASE_VOLTAGE, baseVoltage.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(nominalVoltage), writer);
        CgmesExportUtil.writeAttribute(namespace, BASE_VOLTAGE_NOMINAL_VOLTAGE, nominalVoltage, writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public record BaseVoltageEq(
        String rdfId,
        double nominalVoltage
    ) {
    }

}
