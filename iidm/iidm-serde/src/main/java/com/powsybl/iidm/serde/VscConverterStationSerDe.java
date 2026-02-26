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
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class VscConverterStationSerDe extends AbstractSimpleIdentifiableSerDe<VscConverterStation, VscConverterStationAdder, VoltageLevel> {

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
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () ->
            context.getWriter().writeBooleanAttribute("voltageRegulatorOn", cs.isWithMode(RegulationMode.VOLTAGE)));
        context.getWriter().writeFloatAttribute("lossFactor", cs.getLossFactor());
        writeVoltageSetpointByVersion(cs, context);
        writeReactivePowerSetpointByVersion(cs, context);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> context.getWriter().writeDoubleAttribute(TARGET_V, cs.getTargetV()));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> context.getWriter().writeDoubleAttribute(TARGET_Q, cs.getTargetQ()));

        writeNodeOrBus(null, cs.getTerminal(), context);
        writePQ(null, cs.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(VscConverterStation cs, VoltageLevel vl, NetworkSerializerContext context) {
        ReactiveLimitsSerDe.INSTANCE.write(cs, context);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () ->
            IidmSerDeUtil.assertMinimumVersionAndRunIfNotDefault(!Objects.equals(cs, cs.getRegulatingTerminal().getConnectable()),
                ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED,
                IidmVersion.V_1_6, context, () -> TerminalRefSerDe.writeTerminalRef(cs.getRegulatingTerminal(), context, REGULATING_TERMINAL))
        );
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context,
            () -> VoltageRegulationSerDe.writeVoltageRegulation(cs.getVoltageRegulation(), context, cs));
    }

    @Override
    protected VscConverterStationAdder createAdder(VoltageLevel vl) {
        return vl.newVscConverterStation();
    }

    @Override
    protected VscConverterStation readRootElementAttributes(VscConverterStationAdder adder, VoltageLevel voltageLevel, NetworkDeserializerContext context) {
        AtomicReference<Boolean> voltageRegulatorOnRef = new AtomicReference<>(null);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> voltageRegulatorOnRef.set(context.getReader().readBooleanAttribute("voltageRegulatorOn")));

        float lossFactor = context.getReader().readFloatAttribute("lossFactor");

        AtomicReference<Double> voltageSetpoint = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> voltageSetpoint.set(context.getReader().readDoubleAttribute("voltageSetpoint")));

        AtomicReference<Double> reactivePowerSetpoint = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> reactivePowerSetpoint.set(context.getReader().readDoubleAttribute("reactivePowerSetpoint")));

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> {
            adder.setTargetV(context.getReader().readDoubleAttribute(TARGET_V, Double.NaN));
            adder.setTargetQ(context.getReader().readDoubleAttribute(TARGET_Q, Double.NaN));
        });

        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> {
            Boolean voltageRegulatorOn = voltageRegulatorOnRef.get();
            RegulationMode regulationMode;
            if (voltageRegulatorOn == null) {
                if (!Double.isNaN(voltageSetpoint.get())) {
                    regulationMode = RegulationMode.VOLTAGE;
                } else if (!Double.isNaN(reactivePowerSetpoint.get())) {
                    regulationMode = RegulationMode.REACTIVE_POWER;
                } else {
                    regulationMode = RegulationMode.VOLTAGE;
                }
            } else {
                regulationMode = voltageRegulatorOn ? RegulationMode.VOLTAGE : RegulationMode.REACTIVE_POWER;
            }
            double targetValue;
            if (regulationMode == RegulationMode.REACTIVE_POWER) {
                targetValue = reactivePowerSetpoint.get();
                adder.setTargetV(voltageSetpoint.get());
            } else {
                targetValue = voltageSetpoint.get();
                adder.setTargetQ(reactivePowerSetpoint.get());
            }
            adder.newVoltageRegulation()
                .withTargetValue(targetValue)
                .withMode(regulationMode)
                .add();
        });

        readNodeOrBus(adder, context, voltageLevel.getTopologyKind());
        adder.setLossFactor(lossFactor);
        VscConverterStation cs = adder.add();
        readPQ(null, cs.getTerminal(), context.getReader());
        return cs;
    }

    @Override
    protected void readSubElements(VscConverterStation cs, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case ReactiveLimitsSerDe.ELEM_REACTIVE_CAPABILITY_CURVE -> ReactiveLimitsSerDe.INSTANCE.readReactiveCapabilityCurve(cs, context);
                case ReactiveLimitsSerDe.ELEM_MIN_MAX_REACTIVE_LIMITS -> ReactiveLimitsSerDe.INSTANCE.readMinMaxReactiveLimits(cs, context);
                case REGULATING_TERMINAL -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_6, context);
                    TerminalRefSerDe.readTerminalRef(context, cs.getNetwork(), cs.getVoltageRegulation()::setTerminal);
                }
                case VoltageRegulationSerDe.ELEMENT_NAME -> VoltageRegulationSerDe.readVoltageRegulation(cs, context, cs.getNetwork());
                default -> readSubElement(elementName, cs, context);
            }
        });
    }

    private static void writeVoltageSetpointByVersion(VscConverterStation cs, NetworkSerializerContext context) {
        double voltageSetpoint;
        if (cs.isWithMode(RegulationMode.REACTIVE_POWER)) {
            voltageSetpoint = cs.getTargetV();
        } else {
            voltageSetpoint = cs.getVoltageRegulation() != null ? cs.getVoltageRegulation().getTargetValue() : Double.NaN;
        }
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> context.getWriter().writeDoubleAttribute("voltageSetpoint", voltageSetpoint));
    }

    private static void writeReactivePowerSetpointByVersion(VscConverterStation cs, NetworkSerializerContext context) {
        double reactivePowerSetpoint;
        if (cs.isWithMode(RegulationMode.VOLTAGE)) {
            reactivePowerSetpoint = cs.getTargetQ();
        } else {
            reactivePowerSetpoint = cs.getVoltageRegulation() != null ? cs.getVoltageRegulation().getTargetValue() : Double.NaN;
        }
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> context.getWriter().writeDoubleAttribute("reactivePowerSetpoint", reactivePowerSetpoint));
    }
}
