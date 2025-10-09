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
import com.powsybl.iidm.network.HvdcConverterStation;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class HvdcConverterStationEq {

    public static void write(String id, String converterName, HvdcConverterStation.HvdcType converterType, double ratedUdc, String dcEquipmentContainerId, String pccTerminal,
                             String capabilityCurveId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName(converterClassName(converterType), id, converterName, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, "ACDCConverter.ratedUdc");
        writer.writeCharacters(CgmesExportUtil.format(ratedUdc));
        writer.writeEndElement();
        CgmesExportUtil.writeReference("Equipment.EquipmentContainer", dcEquipmentContainerId, cimNamespace, writer, context);
        if (pccTerminal != null) {
            CgmesExportUtil.writeReference("ACDCConverter.PccTerminal", pccTerminal, cimNamespace, writer, context);
        }
        if (capabilityCurveId != null) {
            CgmesExportUtil.writeReference("VsConverter.CapabilityCurve", capabilityCurveId, cimNamespace, writer, context);
        }
        writer.writeEndElement();
    }

    private static String converterClassName(HvdcConverterStation.HvdcType converterType) {
        if (converterType.equals(HvdcConverterStation.HvdcType.VSC)) {
            return "VsConverter";
        } else if (converterType.equals(HvdcConverterStation.HvdcType.LCC)) {
            return "CsConverter";
        }
        return "ACDCConverter";
    }

    private HvdcConverterStationEq() {
    }
}
