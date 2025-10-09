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
public final class BaseVoltageEq {

    private static final String EQ_BASEVOLTAGE_NOMINALV = "BaseVoltage.nominalVoltage";

    public static void write(String id, double nominalV, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String name = CgmesExportUtil.format(nominalV);
        CgmesExportUtil.writeStartIdName("BaseVoltage", id, name, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, EQ_BASEVOLTAGE_NOMINALV);
        writer.writeCharacters(CgmesExportUtil.format(nominalV));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private BaseVoltageEq() {
    }
}
