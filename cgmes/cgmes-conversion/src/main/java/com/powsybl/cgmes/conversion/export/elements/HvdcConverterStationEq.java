/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.HvdcConverterStation;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class HvdcConverterStationEq {

    public static void write(String id, String converterName, HvdcConverterStation.HvdcType converterType, double ratedUdc, String dcEquipmentContainerId, String capabilityCurveId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, converterClassName(converterType));
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(converterName);
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "ACDCConverter.ratedUdc");
        writer.writeCharacters(CgmesExportUtil.format(ratedUdc));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "Equipment.EquipmentContainer");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + dcEquipmentContainerId);
        if (capabilityCurveId != null) {
            writer.writeEmptyElement(cimNamespace, "VsConverter.CapabilityCurve");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + capabilityCurveId);
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
