/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class StaticVarCompensatorSerDe extends AbstractSimpleIdentifiableSerDe<StaticVarCompensator, StaticVarCompensatorAdder, VoltageLevel> {

    static final StaticVarCompensatorSerDe INSTANCE = new StaticVarCompensatorSerDe();

    static final String ROOT_ELEMENT_NAME = "staticVarCompensator";
    static final String ARRAY_ELEMENT_NAME = "staticVarCompensators";

    private static final String REGULATING_TERMINAL = "regulatingTerminal";
    private static final String REGULATION_MODE = "regulationMode";
    private static final String REGULATING = "regulating";
    private static final String TARGET_V = "targetV";
    private static final String TARGET_Q = "targetQ";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(StaticVarCompensator svc, VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeDoubleAttribute("bMin", svc.getBmin());
        context.getWriter().writeDoubleAttribute("bMax", svc.getBmax());
        String[] voltageSetpointName = {"voltageSetpoint"};
        String[] reactivePowerSetpointName = {"reactivePowerSetpoint"};
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_2, context, () -> {
            voltageSetpointName[0] = "voltageSetPoint";
            reactivePowerSetpointName[0] = "reactivePowerSetPoint";
        });
        writeVoltageSetpointByVersion(svc, context, voltageSetpointName[0]);
        writeReactivePowerSetpointByVersion(svc, context, reactivePowerSetpointName[0]);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> context.getWriter().writeDoubleAttribute(TARGET_Q, svc.getTargetQ()));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> context.getWriter().writeDoubleAttribute(TARGET_V, svc.getTargetV()));

        // If SVC is not regulating in versions < 1.14, then its regulation mode should be exported as OFF (as it means that it has been imported with a "OFF" or null regulation mode)
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_13, context, () -> {
            if (svc.isRegulatingWithMode(RegulationMode.VOLTAGE) || svc.isRegulatingWithMode(RegulationMode.REACTIVE_POWER)) {
                context.getWriter().writeEnumAttribute(REGULATION_MODE, svc.getVoltageRegulation().getMode());
            } else {
                context.getWriter().writeEnumAttribute(REGULATION_MODE, RegulationModeSerDe.OFF);
            }
        });
        IidmSerDeUtil.runFromMinimumVersionAndUntilMaximumVersion(IidmVersion.V_1_14, IidmVersion.V_1_15, context, () -> {
            context.getWriter().writeEnumAttribute(REGULATION_MODE, svc.getRegulationMode());
            context.getWriter().writeBooleanAttribute(REGULATING, svc.isRegulating());
        });
        writeNodeOrBus(null, svc.getTerminal(), context);
        writePQ(null, svc.getTerminal(), context.getWriter());
    }

    private static void writeVoltageSetpointByVersion(StaticVarCompensator svc, NetworkSerializerContext context, String voltageSetpointName) {
        double voltageSetpoint;
        if (svc.isWithMode(RegulationMode.REACTIVE_POWER)) {
            voltageSetpoint = svc.getTargetV();
        } else {
            voltageSetpoint = svc.getVoltageRegulation() != null ? svc.getVoltageRegulation().getTargetValue() : Double.NaN;
        }
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> context.getWriter().writeDoubleAttribute(voltageSetpointName, voltageSetpoint));
    }

    private static void writeReactivePowerSetpointByVersion(StaticVarCompensator svc, NetworkSerializerContext context, String reactivePowerSetpointName) {
        double reactivePowerSetpoint;
        if (svc.isWithMode(RegulationMode.VOLTAGE)) {
            reactivePowerSetpoint = svc.getTargetQ();
        } else {
            reactivePowerSetpoint = svc.getVoltageRegulation() != null ? svc.getVoltageRegulation().getTargetValue() : Double.NaN;
        }
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> context.getWriter().writeDoubleAttribute(reactivePowerSetpointName, reactivePowerSetpoint));
    }

    @Override
    protected void writeSubElements(StaticVarCompensator svc, VoltageLevel vl, NetworkSerializerContext context) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () ->
            IidmSerDeUtil.assertMinimumVersionAndRunIfNotDefault(svc != svc.getRegulatingTerminal().getConnectable(),
                ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED,
                IidmVersion.V_1_1, context, () -> TerminalRefSerDe.writeTerminalRef(svc.getRegulatingTerminal(), context, REGULATING_TERMINAL)));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context,
            () -> VoltageRegulationSerDe.writeVoltageRegulation(svc.getVoltageRegulation(), context, svc));
    }

    @Override
    protected StaticVarCompensatorAdder createAdder(VoltageLevel vl) {
        return vl.newStaticVarCompensator();
    }

    /**
     * Create ENUM to read old regulation mode values for an SVC (versions < 1.14): OFF value is no longer present for newer versions
     * Older versions with OFF value should be imported as VOLTAGE regulation mode with a regulating boolean set to false
     */
    private enum RegulationModeSerDe {
        VOLTAGE,
        REACTIVE_POWER,
        OFF;

        static RegulationMode convertToRegulationMode(RegulationModeSerDe regulationModeSerDe) {
            switch (regulationModeSerDe) {
                case VOLTAGE, OFF -> {
                    return RegulationMode.VOLTAGE;
                }
                case REACTIVE_POWER -> {
                    return RegulationMode.REACTIVE_POWER;
                }
            }
            return RegulationMode.VOLTAGE;
        }
    }

    @Override
    protected StaticVarCompensator readRootElementAttributes(StaticVarCompensatorAdder adder, VoltageLevel voltageLevel, NetworkDeserializerContext context) {
        double bMin = context.getReader().readDoubleAttribute("bMin");
        double bMax = context.getReader().readDoubleAttribute("bMax");

        String[] voltageSetpointName = {"voltageSetpoint"};
        String[] reactivePowerSetpointName = {"reactivePowerSetpoint"};
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_2, context, () -> {
            voltageSetpointName[0] = "voltageSetPoint";
            reactivePowerSetpointName[0] = "reactivePowerSetPoint";
        });
        AtomicReference<Double> voltageSetpoint = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> voltageSetpoint.set(context.getReader().readDoubleAttribute(voltageSetpointName[0])));
        AtomicReference<Double> reactivePowerSetpoint = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> reactivePowerSetpoint.set(context.getReader().readDoubleAttribute(reactivePowerSetpointName[0])));

        adder.setBmin(bMin)
                .setBmax(bMax);

        AtomicReference<RegulationMode> regulationModeRef = new AtomicReference<>();
        AtomicBoolean regulatingRef = new AtomicBoolean(false);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_13, context, () -> {
            RegulationModeSerDe regulationModeSerDe = context.getReader().readEnumAttribute(REGULATION_MODE, RegulationModeSerDe.class);
            if (regulationModeSerDe != null) {
                regulationModeRef.set(RegulationModeSerDe.convertToRegulationMode(regulationModeSerDe));
                regulatingRef.set(!RegulationModeSerDe.OFF.equals(regulationModeSerDe));
            }
        });
        IidmSerDeUtil.runFromMinimumVersionAndUntilMaximumVersion(IidmVersion.V_1_14, IidmVersion.V_1_15, context, () -> {
            regulationModeRef.set(context.getReader().readEnumAttribute(REGULATION_MODE, RegulationMode.class));
            regulatingRef.set(context.getReader().readBooleanAttribute(REGULATING, false));
        });

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> {
            adder.setTargetV(context.getReader().readDoubleAttribute(TARGET_V, Double.NaN));
            adder.setTargetQ(context.getReader().readDoubleAttribute(TARGET_Q, Double.NaN));
        });

        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> {
            RegulationMode regulationMode = regulationModeRef.get();
            if (regulationMode == null) {
                if (!Double.isNaN(voltageSetpoint.get())) {
                    regulationMode = RegulationMode.VOLTAGE;
                } else if (!Double.isNaN(reactivePowerSetpoint.get())) {
                    regulationMode = RegulationMode.REACTIVE_POWER;
                } else {
                    regulationMode = RegulationMode.VOLTAGE;
                }
            }
            double targetValue;
            if (regulationMode == RegulationMode.REACTIVE_POWER) {
                targetValue = reactivePowerSetpoint.get();
                adder.setTargetV(voltageSetpoint.get());
            } else {
                targetValue = voltageSetpoint.get();
                adder.setTargetQ(reactivePowerSetpoint.get());
            }
            boolean regulating = regulatingRef.get();
            adder.newVoltageRegulation()
                .withTargetValue(targetValue)
                .withMode(regulationMode)
                .withRegulating(regulating)
                .add();
        });

        readNodeOrBus(adder, context, voltageLevel.getTopologyKind());
        StaticVarCompensator svc = adder.add();
        readPQ(null, svc.getTerminal(), context.getReader());
        return svc;
    }

    @Override
    protected void readSubElements(StaticVarCompensator svc, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            if (elementName.equals(REGULATING_TERMINAL)) {
                IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
                TerminalRefSerDe.readTerminalRef(context, svc.getNetwork(), svc::setRegulatingTerminal);
            } else if (elementName.equals(VoltageRegulationSerDe.ELEMENT_NAME)) {
                VoltageRegulationSerDe.readVoltageRegulation(svc, context, svc.getNetwork());
            } else {
                readSubElement(elementName, svc, context);
            }
        });
    }
}
