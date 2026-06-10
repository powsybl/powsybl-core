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
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.regulation.*;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

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
    public static final String TERMINAL = "terminalRef";

    private VoltageRegulationSerDe() {
    }

    public static void writeVoltageRegulation(VoltageRegulation voltageRegulation, NetworkSerializerContext context) {
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> {
            if (voltageRegulation != null && voltageRegulation.getMode() != null) {
                String namespace = context.getVersion().getNamespaceURI(context.isValid());
                writeVoltageRegulation(voltageRegulation, context, namespace);
            }
        });
    }

    public static void readVoltageRegulation(VoltageRegulationHolder<?> holder, NetworkDeserializerContext context, Network network) {
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

    public static <T extends VoltageRegulationHolder<?> & Identifiable<T>, A extends VoltageRegulationHolderAdder<A>> void readVoltageRegulation(
        List<Consumer<T>> toApply,
        VoltageRegulationHolderAdder<A> holderAdder,
        NetworkDeserializerContext context) {
        doReadVoltageRegulation(toApply, holderAdder, context, T::getNetwork);
    }

    public static <T extends VoltageRegulationHolder<?>, A extends VoltageRegulationHolderAdder<A>> void readVoltageRegulation(
        List<Consumer<T>> toApply,
        VoltageRegulationHolderAdder<A> holderAdder,
        NetworkDeserializerContext context,
        Network network) {
        doReadVoltageRegulation(toApply, holderAdder, context, holder -> network);
    }

    private static <T extends VoltageRegulationHolder<?>, A extends VoltageRegulationHolderAdder<A>> void doReadVoltageRegulation(
        List<Consumer<T>> toApply,
        VoltageRegulationHolderAdder<A> holderAdder,
        NetworkDeserializerContext context,
        Function<T, Network> networkProvider) {

        VoltageRegulationAdder<A> adder = holderAdder.newVoltageRegulation();
        VoltageRegulationAttributes attributes = getVoltageRegulationAttributes(context);
        AtomicBoolean isWithTerminal = new AtomicBoolean(false);

        // Read Sub Elements
        context.getReader().readChildNodes(subElementName -> {
            if (subElementName.equals(VoltageRegulationSerDe.TERMINAL)) {
                isWithTerminal.set(true);
                // Assign a temporary value to localTargetQ to allow the validation
                // without the VoltageRegulation object. This one will be created in a post-creation task.
                // The real value will be restored at the same time.
                double realLocalTargetQ = holderAdder.getLocalTargetQ();
                holderAdder.setLocalTargetQ(0.0);

                TerminalRefSerDe.TerminalData terminalData = TerminalRefSerDe.readTerminalData(context);
                toApply.add(voltageRegulationHolder -> context.addEndTask(DeserializationEndTask.Step.AFTER_EXTENSIONS,
                    () -> {
                        Terminal terminal = Terminal.getTerminal(networkProvider.apply(voltageRegulationHolder),
                            terminalData.id(), terminalData.side(), terminalData.number());
                        configureAdderOrBuilder(voltageRegulationHolder.newVoltageRegulation(), attributes)
                            .withTerminal(terminal)
                            .build();
                        // Restore the real localTargetQ value.
                        if (!(voltageRegulationHolder instanceof ShuntCompensator) && !(voltageRegulationHolder instanceof RatioTapChanger)) {
                            voltageRegulationHolder.setLocalTargetQ(realLocalTargetQ);
                        }
                    }));
            } else {
                throw new PowsyblException("Unknown sub element name '" + subElementName + "' in 'voltageRegulation'");
            }
        });
        if (!isWithTerminal.get()) {
            configureAdderOrBuilder(adder, attributes).add();
        }
    }

    private static <A extends VoltageRegulationAdderOrBuilder<A>> A configureAdderOrBuilder(A adderOrBuilder,
                                                                                               VoltageRegulationAttributes attributes) {
        return adderOrBuilder
                .withTargetValue(attributes.targetValue())
                .withTargetDeadband(attributes.targetDeadband())
                .withSlope(attributes.slope())
                .withMode(attributes.mode())
                .withRegulating(attributes.isRegulating());
    }

    public static <T extends VoltageRegulationHolder<?> & Identifiable<T>> void readRegulatingTerminal(List<Consumer<T>> toApply, NetworkDeserializerContext context) {
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

}
