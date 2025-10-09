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

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class VoltageLevelEq {

    public static void write(String id, String voltageLevelName, double lowVoltageLimit, double highVoltageLimit,
                             String substationId, String baseVoltageId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("VoltageLevel", id, voltageLevelName, cimNamespace, writer, context);
        if (!Double.isNaN(lowVoltageLimit)) {
            writer.writeStartElement(cimNamespace, "VoltageLevel.lowVoltageLimit");
            writer.writeCharacters(CgmesExportUtil.format(lowVoltageLimit));
            writer.writeEndElement();
        }
        if (!Double.isNaN(highVoltageLimit)) {
            writer.writeStartElement(cimNamespace, "VoltageLevel.highVoltageLimit");
            writer.writeCharacters(CgmesExportUtil.format(highVoltageLimit));
            writer.writeEndElement();
        }
        CgmesExportUtil.writeReference("VoltageLevel.Substation", substationId, cimNamespace, writer, context);
        CgmesExportUtil.writeReference("VoltageLevel.BaseVoltage", baseVoltageId, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    private VoltageLevelEq() {
    }
}
