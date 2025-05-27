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
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

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
        context.getWriter().writeDoubleAttribute(voltageSetpointName[0], svc.getVoltageSetpoint());
        context.getWriter().writeDoubleAttribute(reactivePowerSetpointName[0], svc.getReactivePowerSetpoint());

        // If SVC is not regulating in versions < 1.14, then its regulation mode should be exported as OFF (as it means that it has been imported with a "OFF" or null regulation mode)
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_13, context, () -> {
            if (svc.isRegulating()) {
                context.getWriter().writeEnumAttribute(REGULATION_MODE, svc.getRegulationMode());
            } else {
                context.getWriter().writeEnumAttribute(REGULATION_MODE, RegulationModeSerDe.OFF);
            }
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_14, context, () -> {
            context.getWriter().writeEnumAttribute(REGULATION_MODE, svc.getRegulationMode());
            context.getWriter().writeBooleanAttribute(REGULATING, svc.isRegulating());
        });
        writeNodeOrBus(null, svc.getTerminal(), context);
        writePQ(null, svc.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(StaticVarCompensator svc, VoltageLevel vl, NetworkSerializerContext context) {
        IidmSerDeUtil.assertMinimumVersionAndRunIfNotDefault(svc != svc.getRegulatingTerminal().getConnectable(),
                ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED,
                IidmVersion.V_1_1, context, () -> TerminalRefSerDe.writeTerminalRef(svc.getRegulatingTerminal(), context, REGULATING_TERMINAL));
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

        static StaticVarCompensator.RegulationMode convertToRegulationMode(RegulationModeSerDe regulationModeSerDe) {
            switch (regulationModeSerDe) {
                case VOLTAGE, OFF -> {
                    return StaticVarCompensator.RegulationMode.VOLTAGE;
                }
                case REACTIVE_POWER -> {
                    return StaticVarCompensator.RegulationMode.REACTIVE_POWER;
                }
            }
            return StaticVarCompensator.RegulationMode.VOLTAGE;
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
        double voltageSetpoint = context.getReader().readDoubleAttribute(voltageSetpointName[0]);
        double reactivePowerSetpoint = context.getReader().readDoubleAttribute(reactivePowerSetpointName[0]);

        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_13, context, () -> {
            RegulationModeSerDe regulationModeSerDe = context.getReader().readEnumAttribute(REGULATION_MODE, RegulationModeSerDe.class);
            if (regulationModeSerDe != null) {
                if (!RegulationModeSerDe.OFF.equals(regulationModeSerDe)) {
                    adder.setRegulationMode(RegulationModeSerDe.convertToRegulationMode(regulationModeSerDe));
                    adder.setRegulating(true);
                } else {
                    adder.setRegulationMode(RegulationModeSerDe.convertToRegulationMode(regulationModeSerDe));
                    adder.setRegulating(false);
                }
            }
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_14, context, () -> {
            adder.setRegulationMode(context.getReader().readEnumAttribute(REGULATION_MODE, StaticVarCompensator.RegulationMode.class));
            adder.setRegulating(context.getReader().readBooleanAttribute(REGULATING));
        });

        adder.setBmin(bMin)
                .setBmax(bMax)
                .setVoltageSetpoint(voltageSetpoint)
                .setReactivePowerSetpoint(reactivePowerSetpoint);

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
            } else {
                readSubElement(elementName, svc, context);
            }
        });
    }
}
