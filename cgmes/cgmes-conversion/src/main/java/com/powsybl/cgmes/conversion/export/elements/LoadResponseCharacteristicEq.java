/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class LoadResponseCharacteristicEq {

    //private static final String EQ_GENERATINGUNIT_MINP = "GeneratingUnit.minOperatingP";

    public static void write(String id, String name,
                             boolean exponentModel, double pVoltageExponent, double qVoltageExponent,
                             double pConstantPower, double qConstantPower, double pConstantCurrent,
                             double qConstantCurrent, double pConstantImpedance, double qConstantImpedance,
                             String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("LoadResponseCharacteristic", id, name, cimNamespace, writer, context);

        writer.writeStartElement(cimNamespace, "LoadResponseCharacteristic.exponentModel");
        writer.writeCharacters(CgmesExportUtil.format(exponentModel));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "LoadResponseCharacteristic.pConstantCurrent");
        writer.writeCharacters(CgmesExportUtil.format(pConstantCurrent));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "LoadResponseCharacteristic.pConstantImpedance");
        writer.writeCharacters(CgmesExportUtil.format(pConstantImpedance));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "LoadResponseCharacteristic.pConstantPower");
        writer.writeCharacters(CgmesExportUtil.format(pConstantPower));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "LoadResponseCharacteristic.pVoltageExponent");
        writer.writeCharacters(CgmesExportUtil.format(pVoltageExponent));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "LoadResponseCharacteristic.qConstantCurrent");
        writer.writeCharacters(CgmesExportUtil.format(qConstantCurrent));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "LoadResponseCharacteristic.qConstantImpedance");
        writer.writeCharacters(CgmesExportUtil.format(qConstantImpedance));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "LoadResponseCharacteristic.qConstantPower");
        writer.writeCharacters(CgmesExportUtil.format(qConstantPower));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "LoadResponseCharacteristic.qVoltageExponent");
        writer.writeCharacters(CgmesExportUtil.format(qVoltageExponent));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private LoadResponseCharacteristicEq() {
    }
}
