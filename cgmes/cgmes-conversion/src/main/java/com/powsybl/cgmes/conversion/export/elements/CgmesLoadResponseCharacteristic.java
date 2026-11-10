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

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class CgmesLoadResponseCharacteristic {

    private static final String LOAD_RESPONSE_CHARACTERISTIC = "LoadResponseCharacteristic";
    private static final String LOAD_RESPONSE_CHARACTERISTIC_EXPONENT_MODEL = LOAD_RESPONSE_CHARACTERISTIC + ".exponentModel";
    private static final String LOAD_RESPONSE_CHARACTERISTIC_P_CONSTANT_CURRENT = LOAD_RESPONSE_CHARACTERISTIC + ".pConstantCurrent";
    private static final String LOAD_RESPONSE_CHARACTERISTIC_P_CONSTANT_IMPEDANCE = LOAD_RESPONSE_CHARACTERISTIC + ".pConstantImpedance";
    private static final String LOAD_RESPONSE_CHARACTERISTIC_P_CONSTANT_POWER = LOAD_RESPONSE_CHARACTERISTIC + ".pConstantPower";
    private static final String LOAD_RESPONSE_CHARACTERISTIC_P_VOLTAGE_EXPONENT = LOAD_RESPONSE_CHARACTERISTIC + ".pVoltageExponent";
    private static final String LOAD_RESPONSE_CHARACTERISTIC_Q_CONSTANT_CURRENT = LOAD_RESPONSE_CHARACTERISTIC + ".qConstantCurrent";
    private static final String LOAD_RESPONSE_CHARACTERISTIC_Q_CONSTANT_IMPEDANCE = LOAD_RESPONSE_CHARACTERISTIC + ".qConstantImpedance";
    private static final String LOAD_RESPONSE_CHARACTERISTIC_Q_CONSTANT_POWER = LOAD_RESPONSE_CHARACTERISTIC + ".qConstantPower";
    private static final String LOAD_RESPONSE_CHARACTERISTIC_Q_VOLTAGE_EXPONENT = LOAD_RESPONSE_CHARACTERISTIC + ".qVoltageExponent";

    private CgmesLoadResponseCharacteristic() {
    }

    public static void writeEq(LoadResponseCharacteristicEq loadResponseCharacteristic, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(LOAD_RESPONSE_CHARACTERISTIC, loadResponseCharacteristic.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(loadResponseCharacteristic.name), writer);
        CgmesExportUtil.writeAttribute(namespace, LOAD_RESPONSE_CHARACTERISTIC_EXPONENT_MODEL, CgmesExportUtil.format(loadResponseCharacteristic.exponentModel), writer);
        CgmesExportUtil.writeAttribute(namespace, LOAD_RESPONSE_CHARACTERISTIC_P_CONSTANT_CURRENT, CgmesExportUtil.format(loadResponseCharacteristic.pConstantCurrent), writer);
        CgmesExportUtil.writeAttribute(namespace, LOAD_RESPONSE_CHARACTERISTIC_P_CONSTANT_IMPEDANCE, CgmesExportUtil.format(loadResponseCharacteristic.pConstantImpedance), writer);
        CgmesExportUtil.writeAttribute(namespace, LOAD_RESPONSE_CHARACTERISTIC_P_CONSTANT_POWER, CgmesExportUtil.format(loadResponseCharacteristic.pConstantPower), writer);
        CgmesExportUtil.writeAttribute(namespace, LOAD_RESPONSE_CHARACTERISTIC_P_VOLTAGE_EXPONENT, CgmesExportUtil.format(loadResponseCharacteristic.pVoltageExponent), writer);
        CgmesExportUtil.writeAttribute(namespace, LOAD_RESPONSE_CHARACTERISTIC_Q_CONSTANT_CURRENT, CgmesExportUtil.format(loadResponseCharacteristic.qConstantCurrent), writer);
        CgmesExportUtil.writeAttribute(namespace, LOAD_RESPONSE_CHARACTERISTIC_Q_CONSTANT_IMPEDANCE, CgmesExportUtil.format(loadResponseCharacteristic.qConstantImpedance), writer);
        CgmesExportUtil.writeAttribute(namespace, LOAD_RESPONSE_CHARACTERISTIC_Q_CONSTANT_POWER, CgmesExportUtil.format(loadResponseCharacteristic.qConstantPower), writer);
        CgmesExportUtil.writeAttribute(namespace, LOAD_RESPONSE_CHARACTERISTIC_Q_VOLTAGE_EXPONENT, CgmesExportUtil.format(loadResponseCharacteristic.qVoltageExponent), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public record LoadResponseCharacteristicEq(
        String rdfId,
        String name,
        boolean exponentModel,
        double pVoltageExponent,
        double qVoltageExponent,
        double pConstantPower,
        double qConstantPower,
        double pConstantCurrent,
        double qConstantCurrent,
        double pConstantImpedance,
        double qConstantImpedance
    ) {
    }
}
