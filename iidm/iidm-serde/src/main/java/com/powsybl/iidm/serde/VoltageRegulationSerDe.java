/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

/**
 * @author Matthieu Saur {@literal <matthieu.saur at rte-france.com>}
 */
public final class VoltageRegulationSerDe {
    // GLOBAL
    public static final String ELEMENT_NAME = "voltageRegulation";
    // ATTRIBUTES
    public static final String TARGET_VALUE = "targetValue";
    public static final String TARGET_DEADBAND = "targetDeadband";
    public static final String SLOPE = "slope";
    // TODO MSA ADD Terminal
    public static final String MODE = "mode";
    public static final String REGULATING = "regulating";

    private VoltageRegulationSerDe() { }

    public static void writeVoltageRegulation(VoltageRegulation voltageRegulation, NetworkSerializerContext context) {
        if (voltageRegulation != null) {
            String namespace = context.getVersion().getNamespaceURI(context.isValid());
            TreeDataWriter writer = context.getWriter();
            writeVoltageRegulation(voltageRegulation, writer, namespace);
        }
    }

    private static void writeVoltageRegulation(VoltageRegulation voltageRegulation, TreeDataWriter writer, String namespace) {
        writer.writeStartNode(namespace, ELEMENT_NAME);
        writeVoltageRegulationAttribute(voltageRegulation, writer);
        writer.writeEndNode();
    }

    private static void writeVoltageRegulationAttribute(VoltageRegulation voltageRegulation, TreeDataWriter writer) {
        writer.writeDoubleAttribute(TARGET_VALUE, voltageRegulation.getTargetValue());
//        writer.writeDoubleAttribute(TARGET_DEADBAND, voltageRegulation.getTargetDeadband());
//        writer.writeDoubleAttribute(SLOPE, voltageRegulation.getSlope());
        writer.writeEnumAttribute(MODE, voltageRegulation.getMode());
        writer.writeBooleanAttribute(REGULATING, voltageRegulation.isRegulating());
    }

    public static void readVoltageRegulation(VoltageRegulationHolder holder, NetworkDeserializerContext context) {
        Double targetValue = context.getReader().readDoubleAttribute(TARGET_VALUE);
        RegulationMode mode = context.getReader().readEnumAttribute(MODE, RegulationMode.class);
        boolean isRegulating = context.getReader().readBooleanAttribute(REGULATING);
        context.getReader().readEndNode();
        VoltageRegulation voltageRegulation = holder.newAndReplaceVoltageRegulation();
        voltageRegulation.setTargetValue(targetValue);
        voltageRegulation.setMode(mode);
        voltageRegulation.setRegulating(isRegulating);
    }
}
