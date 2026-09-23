/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VoltageSourceConverter;
import com.powsybl.iidm.network.VoltageSourceConverterAdder;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.util.VoltageRegulationUtils;
import com.powsybl.iidm.network.util.VoltageRegulationUtils.VoltageRegulationData;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.powsybl.iidm.serde.util.VoltageRegulationSerdeUtil.writeReactivePowerSetpoint;
import static com.powsybl.iidm.serde.util.VoltageRegulationSerdeUtil.writeVoltageSetpoint;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class VoltageSourceConverterSerDe extends AbstractAcDcConverterSerDe<VoltageSourceConverter, VoltageSourceConverterAdder> {

    static final VoltageSourceConverterSerDe INSTANCE = new VoltageSourceConverterSerDe();
    static final String ROOT_ELEMENT_NAME = "voltageSourceConverter";
    static final String ARRAY_ELEMENT_NAME = "voltageSourceConverters";
    private static final String LOCAL_TARGET_V = "localTargetV";
    private static final String LOCAL_TARGET_Q = "localTargetQ";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(final VoltageSourceConverter vsc, final VoltageLevel parent, final NetworkSerializerContext context) {
        super.writeRootElementAttributes(vsc, parent, context);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_17, context,
            () -> context.getWriter().writeBooleanAttribute("voltageRegulatorOn", vsc.isRegulatingWithMode(RegulationMode.VOLTAGE)));
        writeVoltageSetpoint(vsc, context);
        writeReactivePowerSetpoint(vsc, context);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_18, context, () -> context.getWriter().writeDoubleAttribute(LOCAL_TARGET_V, vsc.getLocalTargetV()));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_18, context, () -> context.getWriter().writeDoubleAttribute(LOCAL_TARGET_Q, vsc.getLocalTargetQ()));
        super.writeRootElementPqiAttributes(vsc, context);
    }

    @Override
    protected void writeSubElements(VoltageSourceConverter vsc, VoltageLevel parent, NetworkSerializerContext context) {
        super.writeSubElements(vsc, parent, context);
        ReactiveLimitsSerDe.INSTANCE.write(vsc, context);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_18, context,
            () -> VoltageRegulationSerDe.writeVoltageRegulation(vsc.getVoltageRegulation(), context));
    }

    @Override
    protected VoltageSourceConverterAdder createAdder(final VoltageLevel voltageLevel) {
        return voltageLevel.newVoltageSourceConverter();
    }

    @Override
    protected void readRootElementAttributes(final VoltageSourceConverterAdder adder,
                                             final VoltageLevel parent,
                                             List<Consumer<VoltageSourceConverter>> toApply,
                                             final NetworkDeserializerContext context) {
        super.readRootElementCommonAttributes(adder, parent, context);

        AtomicReference<Boolean> voltageRegulatorOnRef = new AtomicReference<>(null);
        AtomicReference<Double> voltageSetpoint = new AtomicReference<>(Double.NaN);
        AtomicReference<Double> reactivePowerSetpoint = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_17, context, () -> {
            voltageRegulatorOnRef.set(context.getReader().readBooleanAttribute("voltageRegulatorOn"));
            voltageSetpoint.set(context.getReader().readDoubleAttribute("voltageSetpoint"));
            reactivePowerSetpoint.set(context.getReader().readDoubleAttribute("reactivePowerSetpoint"));
        });

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_18, context, () -> {
            adder.setLocalTargetV(context.getReader().readDoubleAttribute(LOCAL_TARGET_V, Double.NaN));
            adder.setLocalTargetQ(context.getReader().readDoubleAttribute(LOCAL_TARGET_Q, Double.NaN));
        });

        VoltageRegulationData voltageRegulationData = readVoltageRegulationPrevious118(adder, context,
                voltageRegulatorOnRef.get(), voltageSetpoint.get(), reactivePowerSetpoint.get());

        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_17, context, () -> {
            // Backward-compatibility with IIDM versions <= 1.17: store operations that will be performed if a regulating terminal is found later
            if (voltageRegulationData != null) {
                RegulationMode regulationMode = voltageRegulationData.regulationMode();
                toApply.add(vsc -> {
                    if (regulationMode == RegulationMode.VOLTAGE) {
                        VoltageRegulationSerDe.storeExtraProperties(vsc, vsc.getLocalTargetV(), holder -> holder.setLocalTargetV(Double.NaN), context);
                    } else if (regulationMode == RegulationMode.REACTIVE_POWER) {
                        VoltageRegulationSerDe.storeExtraProperties(vsc, vsc.getLocalTargetQ(), holder -> holder.setLocalTargetQ(Double.NaN), context);
                    }
                });
            }
        });
        super.readRootElementPqiAttributes(toApply, adder, context);
    }

    private static VoltageRegulationData readVoltageRegulationPrevious118(VoltageSourceConverterAdder adder,
                                                                          NetworkDeserializerContext context,
                                                                          Boolean voltageRegulatorOnRef,
                                                                          Double voltageSetpoint,
                                                                          Double reactivePowerSetpoint) {
        AtomicReference<VoltageRegulationData> voltageRegulationData = new AtomicReference<>(null);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_17, context, () -> {
            VoltageRegulationData data = VoltageRegulationUtils.buildVoltageRegulationData(voltageRegulatorOnRef, voltageSetpoint, reactivePowerSetpoint);
            adder.setLocalTargetV(data.targetV());
            adder.setLocalTargetQ(data.targetQ());
            if (data.regulationMode() != null) {
                adder.newVoltageRegulation()
                    .withMode(data.regulationMode())
                    .add();
            }
            voltageRegulationData.set(data);
        });
        return voltageRegulationData.get();
    }

    @Override
    protected void readSubElements(String id, VoltageSourceConverterAdder adder, List<Consumer<VoltageSourceConverter>> toApply, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case ReactiveLimitsSerDe.ELEM_REACTIVE_CAPABILITY_CURVE -> ReactiveLimitsSerDe.INSTANCE.readReactiveCapabilityCurve(toApply, context);
                case ReactiveLimitsSerDe.ELEM_MIN_MAX_REACTIVE_LIMITS -> ReactiveLimitsSerDe.INSTANCE.readMinMaxReactiveLimits(toApply, context);
                case VoltageRegulationSerDe.ELEMENT_NAME -> VoltageRegulationSerDe.readVoltageRegulation(toApply, adder, context);
                case PCC_TERMINAL -> {
                    TerminalRefSerDe.TerminalData terminalData = TerminalRefSerDe.readTerminalData(context);
                    IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_17, context, () -> setPccTerminalFromPrevious117(toApply, context, terminalData));
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_18, context, () -> postponeSetPccTerminal(toApply, context, terminalData));
                }
                default -> super.readSubElement(elementName, id, toApply, context);
            }
        });
    }

    private static void setPccTerminalFromPrevious117(List<Consumer<VoltageSourceConverter>> toApply, NetworkDeserializerContext context, TerminalRefSerDe.TerminalData terminalData) {
        VoltageRegulationSerDe.postponeSetTerminal(toApply, context, terminalData);
        postponeSetPccTerminal(toApply, context, terminalData);
    }
}
