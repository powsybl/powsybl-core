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
    private static final String TARGET_V = "targetV";
    private static final String TARGET_Q = "targetQ";

    private static final String REGULATING_TERMINAL = "regulatingTerminal";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(VscConverterStation cs, VoltageLevel vl, NetworkSerializerContext context) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () ->
            context.getWriter().writeBooleanAttribute("voltageRegulatorOn", cs.isRegulatingWithMode(RegulationMode.VOLTAGE)));
        context.getWriter().writeFloatAttribute("lossFactor", cs.getLossFactor());
        writeVoltageSetpoint(cs, context);
        writeReactivePowerSetpoint(cs, context);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> context.getWriter().writeDoubleAttribute(TARGET_V, cs.getLocalTargetV()));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> context.getWriter().writeDoubleAttribute(TARGET_Q, cs.getLocalTargetQ()));

        writeNodeOrBus(null, cs.getTerminal(), context);
        writePQ(null, cs.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(VscConverterStation cs, VoltageLevel vl, NetworkSerializerContext context) {
        ReactiveLimitsSerDe.INSTANCE.write(cs, context);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () ->
            IidmSerDeUtil.assertMinimumVersionAndRunIfNotDefault(!Objects.equals(cs, cs.getRegulatingTerminal().getConnectable()),
                ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED,
                IidmVersion.V_1_6, context, () -> TerminalRefSerDe.writeTerminalRef(cs.getRegulatingTerminal(), context, REGULATING_TERMINAL))
        );
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context,
            () -> VoltageRegulationSerDe.writeVoltageRegulation(cs.getVoltageRegulation(), context));
    }

    @Override
    protected VscConverterStationAdder createAdder(VoltageLevel vl) {
        return vl.newVscConverterStation();
    }

    @Override
    protected void readRootElementAttributes(VscConverterStationAdder adder, VoltageLevel voltageLevel, List<Consumer<VscConverterStation>> toApply, NetworkDeserializerContext context) {
        AtomicReference<Boolean> voltageRegulatorOnRef = new AtomicReference<>(null);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> voltageRegulatorOnRef.set(context.getReader().readBooleanAttribute("voltageRegulatorOn")));

        float lossFactor = context.getReader().readFloatAttribute("lossFactor");

        AtomicReference<Double> voltageSetpoint = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> voltageSetpoint.set(context.getReader().readDoubleAttribute("voltageSetpoint")));

        AtomicReference<Double> reactivePowerSetpoint = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> reactivePowerSetpoint.set(context.getReader().readDoubleAttribute("reactivePowerSetpoint")));

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> {
            adder.setTargetV(context.getReader().readDoubleAttribute(TARGET_V, Double.NaN));
            adder.setTargetQ(context.getReader().readDoubleAttribute(TARGET_Q, Double.NaN));
        });

        readVoltageRegulation(adder, context, voltageRegulatorOnRef, voltageSetpoint, reactivePowerSetpoint);

        readNodeOrBus(adder, context, voltageLevel.getTopologyKind());
        adder.setLossFactor(lossFactor);
        double p = context.getReader().readDoubleAttribute("p");
        double q = context.getReader().readDoubleAttribute("q");
        toApply.add(vscConverterStation -> vscConverterStation.getTerminal().setP(p).setQ(q));
        toApply.add(vscConverterStation -> {
            Runnable actionOnRemoteTerminal = () -> vscConverterStation.setLocalTargetV(Double.NaN);
            context.addExtraProperties(vscConverterStation, new NetworkDeserializerContext.ExtraPropertiesData(voltageSetpoint.get(), actionOnRemoteTerminal));
        });
    }

    private static void readVoltageRegulation(VscConverterStationAdder adder, NetworkDeserializerContext context, AtomicReference<Boolean> voltageRegulatorOnRef, AtomicReference<Double> voltageSetpoint, AtomicReference<Double> reactivePowerSetpoint) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> {
            VoltageRegulationUtils.VoltageRegulationData voltageRegulationData = VoltageRegulationUtils.buildVoltageRegulationData(voltageRegulatorOnRef.get(), voltageSetpoint.get(), reactivePowerSetpoint.get());
            adder.setTargetV(voltageRegulationData.targetV());
            adder.setTargetQ(voltageRegulationData.targetQ());
            if (voltageRegulationData.regulationMode() != null) {
                adder.newVoltageRegulation()
                    .withMode(voltageRegulationData.regulationMode())
                    .add();
            }
        });
    }

    @Override
    protected void readSubElements(String id, VscConverterStationAdder adder, List<Consumer<VscConverterStation>> toApply, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case ReactiveLimitsSerDe.ELEM_REACTIVE_CAPABILITY_CURVE -> ReactiveLimitsSerDe.INSTANCE.readReactiveCapabilityCurve(toApply, context);
                case ReactiveLimitsSerDe.ELEM_MIN_MAX_REACTIVE_LIMITS -> ReactiveLimitsSerDe.INSTANCE.readMinMaxReactiveLimits(toApply, context);
                case REGULATING_TERMINAL -> VoltageRegulationSerDe.readRegulatingTerminal(toApply, context);
                case VoltageRegulationSerDe.ELEMENT_NAME -> VoltageRegulationSerDe.readVoltageRegulation(toApply, adder.newVoltageRegulation(), context);
                default -> readSubElement(elementName, id, toApply, context);
            }
        });
    }
}
