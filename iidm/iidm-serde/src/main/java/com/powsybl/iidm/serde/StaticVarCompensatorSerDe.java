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
import com.powsybl.iidm.network.extensions.removed.VoltagePerReactivePowerControl;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.serde.extensions.VoltagePerReactivePowerControlSerDe;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class StaticVarCompensatorSerDe extends AbstractComplexIdentifiableSerDe<StaticVarCompensator, StaticVarCompensatorAdder, VoltageLevel> {

    static final StaticVarCompensatorSerDe INSTANCE = new StaticVarCompensatorSerDe();

    static final String ROOT_ELEMENT_NAME = "staticVarCompensator";
    static final String ARRAY_ELEMENT_NAME = "staticVarCompensators";

    private static final String REGULATING_TERMINAL = "regulatingTerminal";
    private static final String REGULATION_MODE = "regulationMode";
    private static final String REGULATING = "regulating";
    private static final String LOCAL_TARGET_V = "localTargetV";
    private static final String LOCAL_TARGET_Q = "localTargetQ";

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
        writeVoltageSetpoint(svc, context, voltageSetpointName[0]);
        writeReactivePowerSetpoint(svc, context, reactivePowerSetpointName[0]);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> context.getWriter().writeDoubleAttribute(LOCAL_TARGET_Q, svc.getLocalTargetQ()));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> context.getWriter().writeDoubleAttribute(LOCAL_TARGET_V, svc.getLocalTargetV()));

        // If SVC is not regulating in versions < 1.14, then its regulation mode should be exported as OFF (as it means that it has been imported with a "OFF" or null regulation mode)
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_13, context, () -> {
            if (svc.isRegulatingWithMode(RegulationMode.VOLTAGE) || svc.isRegulatingWithMode(RegulationMode.REACTIVE_POWER)) {
                context.getWriter().writeEnumAttribute(REGULATION_MODE, SvcRegulationMode.from(svc.getVoltageRegulation().getMode()));
            } else {
                context.getWriter().writeEnumAttribute(REGULATION_MODE, RegulationModeSerDe.OFF);
            }
        });
        IidmSerDeUtil.runInBetweenTwoVersions(IidmVersion.V_1_14, IidmVersion.V_1_16, context, () -> {
            if (svc.getVoltageRegulation() != null) {
                context.getWriter().writeEnumAttribute(REGULATION_MODE, svc.getVoltageRegulation().getMode());
                context.getWriter().writeBooleanAttribute(REGULATING, svc.getVoltageRegulation().isRegulating());
            } else {
                context.getWriter().writeEnumAttribute(REGULATION_MODE, RegulationMode.VOLTAGE); // Previous default mode in the SVC adder
                context.getWriter().writeBooleanAttribute(REGULATING, false);
            }
        });
        writeNodeOrBus(null, svc.getTerminal(), context);
        writePQ(null, svc.getTerminal(), context.getWriter());
    }

    private static void writeVoltageSetpoint(StaticVarCompensator svc, NetworkSerializerContext context, String voltageSetpointName) {
        double voltageSetpoint;
        if (svc.isWithMode(RegulationMode.VOLTAGE) && svc.isRemoteRegulating()) {
            voltageSetpoint = svc.getVoltageRegulation() != null ? svc.getVoltageRegulation().getTargetValue() : Double.NaN;
        } else {
            voltageSetpoint = svc.getLocalTargetV();
        }
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> context.getWriter().writeDoubleAttribute(voltageSetpointName, voltageSetpoint));
    }

    private static void writeReactivePowerSetpoint(StaticVarCompensator svc, NetworkSerializerContext context, String reactivePowerSetpointName) {
        double reactivePowerSetpoint;
        if (svc.isWithMode(RegulationMode.REACTIVE_POWER) && svc.isRemoteRegulating()) {
            reactivePowerSetpoint = svc.getVoltageRegulation() != null ? svc.getVoltageRegulation().getTargetValue() : Double.NaN;
        } else {
            reactivePowerSetpoint = svc.getLocalTargetQ();
        }
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> context.getWriter().writeDoubleAttribute(reactivePowerSetpointName, reactivePowerSetpoint));
    }

    @Override
    protected void writeSubElements(StaticVarCompensator svc, VoltageLevel vl, NetworkSerializerContext context) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () ->
            IidmSerDeUtil.assertMinimumVersionAndRunIfNotDefault(svc != svc.getRegulatingTerminal().getConnectable(),
                ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED,
                IidmVersion.V_1_1, context, () -> TerminalRefSerDe.writeTerminalRef(svc.getRegulatingTerminal(), context, REGULATING_TERMINAL)));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context,
            () -> VoltageRegulationSerDe.writeVoltageRegulation(svc.getVoltageRegulation(), context));
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
    protected void readRootElementAttributes(StaticVarCompensatorAdder adder, VoltageLevel voltageLevel, List<Consumer<StaticVarCompensator>> toApply, NetworkDeserializerContext context) {
        double bMin = context.getReader().readDoubleAttribute("bMin");
        double bMax = context.getReader().readDoubleAttribute("bMax");

        String[] voltageSetpointName = {"voltageSetpoint"};
        String[] reactivePowerSetpointName = {"reactivePowerSetpoint"};
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_2, context, () -> {
            voltageSetpointName[0] = "voltageSetPoint";
            reactivePowerSetpointName[0] = "reactivePowerSetPoint";
        });
        AtomicReference<Double> voltageSetpoint = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> voltageSetpoint.set(context.getReader().readDoubleAttribute(voltageSetpointName[0])));
        AtomicReference<Double> reactivePowerSetpoint = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> reactivePowerSetpoint.set(context.getReader().readDoubleAttribute(reactivePowerSetpointName[0])));

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
        IidmSerDeUtil.runInBetweenTwoVersions(IidmVersion.V_1_14, IidmVersion.V_1_16, context, () -> {
            regulationModeRef.set(context.getReader().readEnumAttribute(REGULATION_MODE, RegulationMode.class));
            regulatingRef.set(context.getReader().readBooleanAttribute(REGULATING, false));
        });

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> {
            adder.setLocalTargetQ(context.getReader().readDoubleAttribute(LOCAL_TARGET_Q, Double.NaN));
            adder.setLocalTargetV(context.getReader().readDoubleAttribute(LOCAL_TARGET_V, Double.NaN));
        });
        AtomicReference<Double> targetValueDoubleToUseInVoltageRegulationIfRemote = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> {
            if (regulationModeRef.get() == null) {
                if (!Double.isNaN(voltageSetpoint.get())) {
                    regulationModeRef.set(RegulationMode.VOLTAGE);
                } else if (!Double.isNaN(reactivePowerSetpoint.get())) {
                    regulationModeRef.set(RegulationMode.REACTIVE_POWER);
                } else {
                    regulationModeRef.set(RegulationMode.VOLTAGE);
                }
            }
            adder.setLocalTargetV(voltageSetpoint.get());
            adder.setLocalTargetQ(reactivePowerSetpoint.get());
            if (RegulationMode.VOLTAGE.equals(regulationModeRef.get())) {
                boolean regulating = regulatingRef.get();
                adder.newVoltageRegulation()
                    .withMode(regulationModeRef.get())
                    .withRegulating(regulating)
                    .add();
                targetValueDoubleToUseInVoltageRegulationIfRemote.set(voltageSetpoint.get());
            } else {
                targetValueDoubleToUseInVoltageRegulationIfRemote.set(reactivePowerSetpoint.get());
            }
        });

        readNodeOrBus(adder, context, voltageLevel.getTopologyKind());
        double p = context.getReader().readDoubleAttribute("p");
        double q = context.getReader().readDoubleAttribute("q");
        toApply.add(svc -> svc.getTerminal().setP(p).setQ(q));
        toApply.add(svc -> {
            Runnable actionOnTerminalRemote;
            if (RegulationMode.REACTIVE_POWER.equals(regulationModeRef.get())) {
                actionOnTerminalRemote = () -> svc.setLocalTargetQ(Double.NaN);
            } else {
                actionOnTerminalRemote = () -> svc.setLocalTargetV(Double.NaN);
            }
            context.addExtraProperties(svc, new NetworkDeserializerContext.ExtraPropertiesData(targetValueDoubleToUseInVoltageRegulationIfRemote.get(), actionOnTerminalRemote));
        });
    }

    @Override
    protected void readSubElements(String id, StaticVarCompensatorAdder adder, List<Consumer<StaticVarCompensator>> toApply, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case REGULATING_TERMINAL -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
                    VoltageRegulationSerDe.readRegulatingTerminal(toApply, context);
                }
                case VoltageRegulationSerDe.ELEMENT_NAME -> VoltageRegulationSerDe.readVoltageRegulation(toApply, adder, context);
                default -> readSubElement(elementName, id, toApply, context);
            }
        });
    }

    private enum SvcRegulationMode {
        VOLTAGE(RegulationMode.VOLTAGE),
        REACTIVE_POWER(RegulationMode.REACTIVE_POWER);
        private final RegulationMode regulationMode;

        SvcRegulationMode(RegulationMode regulationMode) {
            this.regulationMode = regulationMode;
        }

        static SvcRegulationMode from(RegulationMode regulationMode) {
            if (regulationMode == null) {
                return null;
            }
            for (SvcRegulationMode value : values()) {
                if (value.regulationMode == regulationMode) {
                    return value;
                }
            }
            throw new IllegalArgumentException(
                "None SvcRegulationMode for the RegulationMode : " + regulationMode);
        }
    }

    @Override
    protected void addExtinctExtensions(StaticVarCompensator staticVarCompensator, NetworkSerializerContext context) {
        if (VoltagePerReactivePowerControlSerDe.isExtensionNeededAndExportable(staticVarCompensator, context)) {
            VoltagePerReactivePowerControl extension = new VoltagePerReactivePowerControl(staticVarCompensator,
                    staticVarCompensator.getVoltageRegulation().getSlope());
            context.addExtinctExtensionsToSerialize(staticVarCompensator.getId(), extension);
        }
    }
}
