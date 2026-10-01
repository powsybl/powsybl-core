/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.util.VoltageRegulationUtils;
import com.powsybl.iidm.network.util.VoltageRegulationUtils.VoltageRegulationData;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;
import static com.powsybl.iidm.serde.util.VoltageRegulationSerdeUtil.writeReactivePowerSetpoint;
import static com.powsybl.iidm.serde.util.VoltageRegulationSerdeUtil.writeVoltageSetpoint;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class VscConverterStationSerDe extends AbstractComplexIdentifiableSerDe<VscConverterStation, VscConverterStationAdder, VoltageLevel> {

    static final VscConverterStationSerDe INSTANCE = new VscConverterStationSerDe();

    static final String ROOT_ELEMENT_NAME = "vscConverterStation";
    static final String ARRAY_ELEMENT_NAME = "vscConverterStations";
    private static final String LOCAL_TARGET_V = "localTargetV";
    private static final String LOCAL_TARGET_Q = "localTargetQ";

    private static final String REGULATING_TERMINAL = "regulatingTerminal";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(VscConverterStation cs, VoltageLevel vl, NetworkSerializerContext context) {
        writeEquivalent(cs, context);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_17, context, () ->
            context.getWriter().writeBooleanAttribute("voltageRegulatorOn", cs.isRegulatingWithMode(RegulationMode.VOLTAGE)));
        context.getWriter().writeFloatAttribute("lossFactor", cs.getLossFactor());
        writeVoltageSetpoint(cs, context);
        writeReactivePowerSetpoint(cs, context);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_18, context, () -> {
            context.getWriter().writeDoubleAttribute(LOCAL_TARGET_V, cs.getLocalTargetV());
            context.getWriter().writeDoubleAttribute(LOCAL_TARGET_Q, cs.getLocalTargetQ());
        });
        writeNodeOrBus(null, cs.getTerminal(), context);
        writePQ(null, cs.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(VscConverterStation cs, VoltageLevel vl, NetworkSerializerContext context) {
        ReactiveLimitsSerDe.INSTANCE.write(cs, context);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_17, context, () ->
            IidmSerDeUtil.assertMinimumVersionAndRunIfNotDefault(!Objects.equals(cs, cs.getRegulatingTerminal().getConnectable()),
                ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED,
                IidmVersion.V_1_6, context, () -> TerminalRefSerDe.writeTerminalRef(cs.getRegulatingTerminal(), context, REGULATING_TERMINAL))
        );
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_18, context,
            () -> VoltageRegulationSerDe.writeVoltageRegulation(cs.getVoltageRegulation(), context));
    }

    @Override
    protected VscConverterStationAdder createAdder(VoltageLevel vl) {
        return vl.newVscConverterStation();
    }

    @Override
    protected void readRootElementAttributes(VscConverterStationAdder adder, VoltageLevel voltageLevel, List<Consumer<VscConverterStation>> toApply, NetworkDeserializerContext context) {
        readEquivalent(adder, context);
        AtomicReference<Boolean> voltageRegulatorOnRef = new AtomicReference<>(null);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_17, context, () -> voltageRegulatorOnRef.set(context.getReader().readBooleanAttribute("voltageRegulatorOn")));

        float lossFactor = context.getReader().readFloatAttribute("lossFactor");

        AtomicReference<Double> voltageSetpoint = new AtomicReference<>(Double.NaN);
        AtomicReference<Double> reactivePowerSetpoint = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_17, context, () -> {
            voltageSetpoint.set(context.getReader().readDoubleAttribute("voltageSetpoint"));
            reactivePowerSetpoint.set(context.getReader().readDoubleAttribute("reactivePowerSetpoint"));
        });

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_18, context, () -> {
            adder.setLocalTargetV(context.getReader().readDoubleAttribute(LOCAL_TARGET_V, Double.NaN));
            adder.setLocalTargetQ(context.getReader().readDoubleAttribute(LOCAL_TARGET_Q, Double.NaN));
        });

        VoltageRegulationData voltageRegulationData = readVoltageRegulationPrevious118(adder, context,
                voltageRegulatorOnRef.get(), voltageSetpoint.get(), reactivePowerSetpoint.get());

        readNodeOrBus(adder, context, voltageLevel.getTopologyKind());
        adder.setLossFactor(lossFactor);
        double p = context.getReader().readDoubleAttribute("p");
        double q = context.getReader().readDoubleAttribute("q");
        toApply.add(vscConverterStation -> vscConverterStation.getTerminal().setP(p).setQ(q));

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
    }

    private static VoltageRegulationData readVoltageRegulationPrevious118(VscConverterStationAdder adder, NetworkDeserializerContext context,
                                                         Boolean voltageRegulatorOnRef, Double voltageSetpoint, Double reactivePowerSetpoint) {
        AtomicReference<VoltageRegulationData> voltageRegulationData = new AtomicReference<>(null);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_17, context, () -> {
            VoltageRegulationData data = VoltageRegulationUtils.buildVoltageRegulationData(voltageRegulatorOnRef,
                voltageSetpoint, reactivePowerSetpoint);
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
    protected void readSubElements(String id, VscConverterStationAdder adder, List<Consumer<VscConverterStation>> toApply, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case ReactiveLimitsSerDe.ELEM_REACTIVE_CAPABILITY_CURVE -> ReactiveLimitsSerDe.INSTANCE.readReactiveCapabilityCurve(toApply, context);
                case ReactiveLimitsSerDe.ELEM_MIN_MAX_REACTIVE_LIMITS -> ReactiveLimitsSerDe.INSTANCE.readMinMaxReactiveLimits(toApply, context);
                case REGULATING_TERMINAL -> {
                    IidmSerDeUtil.assertInBetweenTwoVersions(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED,
                            IidmVersion.V_1_6, IidmVersion.V_1_17, context);
                    VoltageRegulationSerDe.readRegulatingTerminal(toApply, context);
                }
                case VoltageRegulationSerDe.ELEMENT_NAME -> VoltageRegulationSerDe.readVoltageRegulation(toApply, adder, context);
                default -> readSubElement(elementName, id, toApply, context);
            }
        });
    }
}
