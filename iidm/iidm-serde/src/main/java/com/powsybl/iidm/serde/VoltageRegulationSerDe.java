/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.regulation.*;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.function.Consumer;

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
    public static final String MODE = "mode";
    public static final String REGULATING = "regulating";
    // SubElements
    public static final String TERMINAL = "terminal";

    private VoltageRegulationSerDe() { }

    public static void writeVoltageRegulation(VoltageRegulation voltageRegulation, NetworkSerializerContext context) {
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> {
            if (voltageRegulation != null) {
                String namespace = context.getVersion().getNamespaceURI(context.isValid());
                writeVoltageRegulation(voltageRegulation, context, namespace);
            }
        });
    }

    private static void writeVoltageRegulation(VoltageRegulation voltageRegulation, NetworkSerializerContext context, String namespace) {
        TreeDataWriter writer = context.getWriter();
        writer.writeStartNode(namespace, ELEMENT_NAME);
        writeVoltageRegulationAttribute(voltageRegulation, writer);
        writeSubElements(voltageRegulation, context);
        writer.writeEndNode();
    }

    private static void writeVoltageRegulationAttribute(VoltageRegulation voltageRegulation, TreeDataWriter writer) {
        writer.writeDoubleAttribute(TARGET_VALUE, voltageRegulation.getTargetValue());
        writer.writeDoubleAttribute(TARGET_DEADBAND, voltageRegulation.getTargetDeadband());
        writer.writeDoubleAttribute(SLOPE, voltageRegulation.getSlope());
        writer.writeEnumAttribute(MODE, voltageRegulation.getMode());
        writer.writeBooleanAttribute(REGULATING, voltageRegulation.isRegulating());
    }

    private static void writeSubElements(VoltageRegulation voltageRegulation, NetworkSerializerContext context) {
 // TODO MSA check if neeeded to test the remote terminal             !Objects.equals(voltageRegulation.getTerminal().getBusBreakerView().getConnectableBus(),
//                voltageRegulation.getExtendable().getTerminal().getBusBreakerView().getConnectableBus())
        TerminalRefSerDe.writeTerminalRef(voltageRegulation.getTerminal(), context, TERMINAL);
    }

    public static void readVoltageRegulation(VoltageRegulationHolderBuilder holder, NetworkDeserializerContext context, Network network) {
        // Read attributes
        double targetValue = context.getReader().readDoubleAttribute(TARGET_VALUE);
        double targetDeadband = context.getReader().readDoubleAttribute(TARGET_DEADBAND);
        double slope = context.getReader().readDoubleAttribute(SLOPE);
        RegulationMode mode = context.getReader().readEnumAttribute(MODE, RegulationMode.class);
        boolean isRegulating = context.getReader().readBooleanAttribute(REGULATING);
        // Create new Voltage Regulation
        VoltageRegulation voltageRegulation = holder.newVoltageRegulation()
            .withTargetValue(targetValue)
            .withTargetDeadband(targetDeadband)
            .withSlope(slope)
            .withMode(mode)
            .withRegulating(isRegulating)
            .build();
        // Read Sub Elements
        readSubElements(context, network, voltageRegulation::setTerminal);
        // THE END
    }

    private static void readSubElements(NetworkDeserializerContext context, Network network, Consumer<Terminal> setTerminal) {
        context.getReader().readChildNodes(elementName -> {
            if (elementName.equals(TERMINAL)) {
                TerminalRefSerDe.readTerminalRef(context, network, setTerminal);
            } else {
                throw new PowsyblException("Unknown sub element name '" + elementName + "' in 'voltageRegulation'");
            }
        });
    }
}
