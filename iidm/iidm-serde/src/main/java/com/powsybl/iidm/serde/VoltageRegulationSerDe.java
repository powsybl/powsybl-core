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
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.regulation.*;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
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

    private VoltageRegulationSerDe() {
    }

    public static void writeVoltageRegulation(VoltageRegulation voltageRegulation, NetworkSerializerContext context) {
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> {
            if (voltageRegulation != null) {
                String namespace = context.getVersion().getNamespaceURI(context.isValid());
                writeVoltageRegulation(voltageRegulation, context, namespace);
            }
        });
    }

    public static void readVoltageRegulation(VoltageRegulationHolder holder, NetworkDeserializerContext context, Network network) {
        // Read attributes
        VoltageRegulationAttributes attributes = getVoltageRegulationAttributes(context);
        // Create new Voltage Regulation
        VoltageRegulationBuilder voltageRegulationBuilder = holder.newVoltageRegulation()
            .withTargetDeadband(attributes.targetDeadband())
            .withSlope(attributes.slope())
            .withMode(attributes.mode())
            .withRegulating(attributes.isRegulating());
        // Local regulation, voltageRegulation is completed
        if (Double.isNaN(attributes.targetValue())) {
            voltageRegulationBuilder.build();
        }
        // Read Sub Elements
        readSubElements(context, network, terminal -> voltageRegulationBuilder.withTerminal(terminal)
            .withTargetValue(attributes.targetValue())
            .build());
    }

    public static <A> VoltageRegulationAdder<A> readVoltageRegulation(VoltageRegulationAdder<A> adder, NetworkDeserializerContext context) {
        // Read attributes
        VoltageRegulationAttributes attributes = getVoltageRegulationAttributes(context);
        // Create new Voltage Regulation
        return adder
            .withTargetValue(attributes.targetValue())
            .withTargetDeadband(attributes.targetDeadband())
            .withSlope(attributes.slope())
            .withMode(attributes.mode())
            .withRegulating(attributes.isRegulating());
    }

    public static <T extends VoltageRegulationHolder, A> void readVoltageRegulation(List<Consumer<T>> toApply, VoltageRegulationAdder<A> adder, NetworkDeserializerContext context) {
        VoltageRegulationAdder<A> voltageRegulationAdder = VoltageRegulationSerDe.readVoltageRegulation(adder, context);
        // Read Sub Elements
        context.getReader().readChildNodes(subElementName -> {
            if (subElementName.equals(VoltageRegulationSerDe.TERMINAL)) {
                Terminal.TerminalDataMsa terminalData = TerminalRefSerDe.readTerminalDataMsa(context);
                voltageRegulationAdder.withTerminalData(terminalData);
                toApply.add(voltageRegulationHolder -> context.addEndTask(DeserializationEndTask.Step.AFTER_EXTENSIONS,
                    // The VoltageRegulation is not null here (was created before)
                    () -> voltageRegulationHolder.getVoltageRegulation().resolveTerminal()));
            } else {
                throw new PowsyblException("Unknown sub element name '" + subElementName + "' in 'voltageRegulation'");
            }
        });
        voltageRegulationAdder.add();
    }

    public static <T extends VoltageRegulationHolder & Identifiable<T>> void readRegulatingTerminal(List<Consumer<T>> toApply, NetworkDeserializerContext context) {
        TerminalRefSerDe.TerminalData terminalData = TerminalRefSerDe.readTerminalData(context);
        toApply.add(holder -> context.addEndTask(DeserializationEndTask.Step.AFTER_EXTENSIONS,
            () -> {
                Terminal terminal = TerminalRefSerDe.resolve(terminalData.id(), terminalData.side(), terminalData.number(), holder.getNetwork());
                VoltageRegulation voltageRegulation = holder.getVoltageRegulation();
                if (voltageRegulation == null) {
                    holder.newVoltageRegulation()
                        .withTargetValue(holder.getLocalTargetQ())
                        .withTerminal(terminal)
                        .withMode(RegulationMode.REACTIVE_POWER)
                        .build();
                    holder.setLocalTargetQ(Double.NaN);
                } else {
                    Optional<NetworkDeserializerContext.ExtraPropertiesData> extraProperties = context.getExtraProperties(holder);
                    double targetValue = (double) extraProperties.map(NetworkDeserializerContext.ExtraPropertiesData::value).orElse(Double.NaN);
                    voltageRegulation.setTerminal(terminal, targetValue);
                    extraProperties.ifPresent(prop -> prop.action().run());
                    context.removeExtraProperties(holder);
                }
            }));
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
        TerminalRefSerDe.writeTerminalRef(voltageRegulation.getTerminal(), context, TERMINAL);
    }

    private static @NonNull VoltageRegulationAttributes getVoltageRegulationAttributes(NetworkDeserializerContext context) {
        double targetValue = context.getReader().readDoubleAttribute(TARGET_VALUE);
        double targetDeadband = context.getReader().readDoubleAttribute(TARGET_DEADBAND);
        double slope = context.getReader().readDoubleAttribute(SLOPE);
        RegulationMode mode = context.getReader().readEnumAttribute(MODE, RegulationMode.class);
        boolean isRegulating = context.getReader().readBooleanAttribute(REGULATING);
        return new VoltageRegulationAttributes(targetValue, targetDeadband, slope, mode, isRegulating);
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

    public record VoltageRegulationAttributes(
        double targetValue,
        double targetDeadband,
        double slope,
        RegulationMode mode,
        boolean isRegulating
    ) {
    }
}
